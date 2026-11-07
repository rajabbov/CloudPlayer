package com.example.cloudplayer.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cloudplayer.data.model.DriveAudioItem
import com.example.cloudplayer.data.model.DriveFolderItem
import com.example.cloudplayer.data.repository.DriveRepository
import com.example.cloudplayer.data.repository.GoogleAuthRepository
import com.example.cloudplayer.data.repository.LocalAudioRepository
import com.example.cloudplayer.data.repository.OfflineDownloadManager
import com.example.cloudplayer.data.repository.TokenManager
import com.example.cloudplayer.ui.screen.BreadcrumbItem
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class Screen {
    HOME,
    DRIVE_FOLDERS,
    LOCAL_OFFLINE,
    FAVORITES,
    EQUALIZER,
    SLEEP_TIMER,
    SETTINGS
}

enum class RepeatState {
    OFF, ONE, ALL
}

val mockAudioList = listOf(
    DriveAudioItem(id = "1", name = "Imagine Dragons - Believer.mp3", mimeType = "audio/mpeg", size = 8_400_000L),
    DriveAudioItem(id = "2", name = "The Weeknd - Blinding Lights.mp3", mimeType = "audio/mpeg", size = 7_200_000L),
    DriveAudioItem(id = "3", name = "Daft Punk - Get Lucky.flac", mimeType = "audio/flac", size = 15_600_000L),
    DriveAudioItem(id = "4", name = "Coldplay - A Sky Full of Stars.mp3", mimeType = "audio/mpeg", size = 9_100_000L),
    DriveAudioItem(id = "5", name = "Ed Sheeran - Shape of You.m4a", mimeType = "audio/mp4", size = 6_800_000L),
    DriveAudioItem(id = "6", name = "Hans Zimmer - Interstellar Main Theme.wav", mimeType = "audio/wav", size = 22_400_000L)
)

val mockFolderList = listOf(
    DriveFolderItem(id = "f1", name = "Musiqalar 2024", isFolder = true, mimeType = "application/vnd.google-apps.folder"),
    DriveFolderItem(id = "f2", name = "Rock & Pop Collection", isFolder = true, mimeType = "application/vnd.google-apps.folder"),
    DriveFolderItem(id = "f3", name = "O'zbekcha Qo'shiqlar", isFolder = true, mimeType = "application/vnd.google-apps.folder"),
    DriveFolderItem(id = "1", name = "Imagine Dragons - Believer.mp3", isFolder = false, mimeType = "audio/mpeg", size = 8_400_000L),
    DriveFolderItem(id = "2", name = "The Weeknd - Blinding Lights.mp3", isFolder = false, mimeType = "audio/mpeg", size = 7_200_000L),
    DriveFolderItem(id = "3", name = "Daft Punk - Get Lucky.flac", isFolder = false, mimeType = "audio/flac", size = 15_600_000L)
)

data class PlayerUiState(
    val currentScreen: Screen = Screen.HOME,
    val isSignedIn: Boolean = true,
    val isLoading: Boolean = false,
    val userDisplayName: String = "John Doe",
    val userEmail: String = "cloudplayer.user@gmail.com",
    val userPhotoUrl: String? = null,
    val audioItems: List<DriveAudioItem> = mockAudioList,
    val filteredAudioItems: List<DriveAudioItem> = mockAudioList,
    val driveFolderItems: List<DriveFolderItem> = mockFolderList,
    val breadcrumbs: List<BreadcrumbItem> = listOf(BreadcrumbItem("root", "Mening Diskim")),
    val searchQuery: String = "",
    val currentMediaItem: DriveAudioItem? = mockAudioList.first(),
    val isPlaying: Boolean = true,
    val currentPositionMs: Long = 85_000L,
    val durationMs: Long = 215_000L,
    val isShuffleOn: Boolean = false,
    val repeatState: RepeatState = RepeatState.OFF,
    val favoriteTrackIds: Set<String> = setOf("1", "3"),
    val isOnlyOfflineMode: Boolean = false,
    val sleepTimerMinutes: Int = 0,
    val sleepTimerRemainingSec: Int = 0,
    val errorMessage: String? = null
)

@HiltViewModel
class CloudPlayerViewModel @Inject constructor(
    private val authRepository: GoogleAuthRepository,
    private val driveRepository: DriveRepository,
    private val localAudioRepository: LocalAudioRepository,
    private val offlineDownloadManager: OfflineDownloadManager,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private var sleepTimerJob: Job? = null

    fun navigateToScreen(screen: Screen) {
        _uiState.value = _uiState.value.copy(
            currentScreen = screen,
            filteredAudioItems = filterList(_uiState.value.audioItems, _uiState.value.searchQuery, screen, _uiState.value.favoriteTrackIds)
        )
    }

    fun checkExistingLogin(context: Context) {
        val account = authRepository.getLastSignedInAccount(context)
        if (account != null) {
            _uiState.value = _uiState.value.copy(
                userDisplayName = account.displayName ?: "Google User",
                userEmail = account.email ?: "cloudplayer.user@gmail.com",
                userPhotoUrl = account.photoUrl?.toString()
            )
        }

        viewModelScope.launch {
            val localTracks = localAudioRepository.getLocalAudioFiles(context)
            if (localTracks.isNotEmpty()) {
                val combined = (_uiState.value.audioItems + localTracks).distinctBy { it.id }
                _uiState.value = _uiState.value.copy(
                    audioItems = combined,
                    filteredAudioItems = filterList(combined, _uiState.value.searchQuery, _uiState.value.currentScreen, _uiState.value.favoriteTrackIds)
                )
            }
        }
    }

    fun openFolder(context: Context, folderItem: DriveFolderItem) {
        val currentAccount = authRepository.getLastSignedInAccount(context)
        val newBreadcrumbs = _uiState.value.breadcrumbs + BreadcrumbItem(folderItem.id, folderItem.name)

        if (currentAccount != null) {
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(isLoading = true, breadcrumbs = newBreadcrumbs)
                val contents = driveRepository.getFolderContents(context, currentAccount, folderItem.id)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    driveFolderItems = if (contents.isNotEmpty()) contents else mockFolderList
                )
            }
        } else {
            _uiState.value = _uiState.value.copy(breadcrumbs = newBreadcrumbs)
        }
    }

    fun navigateToBreadcrumb(context: Context, breadcrumbItem: BreadcrumbItem) {
        val currentBreadcrumbs = _uiState.value.breadcrumbs
        val index = currentBreadcrumbs.indexOfFirst { it.id == breadcrumbItem.id }
        if (index == -1) return

        val newBreadcrumbs = currentBreadcrumbs.subList(0, index + 1)
        val currentAccount = authRepository.getLastSignedInAccount(context)

        if (currentAccount != null) {
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(isLoading = true, breadcrumbs = newBreadcrumbs)
                val contents = driveRepository.getFolderContents(context, currentAccount, breadcrumbItem.id)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    driveFolderItems = if (contents.isNotEmpty()) contents else mockFolderList
                )
            }
        } else {
            _uiState.value = _uiState.value.copy(breadcrumbs = newBreadcrumbs)
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(
            searchQuery = query,
            filteredAudioItems = filterList(_uiState.value.audioItems, query, _uiState.value.currentScreen, _uiState.value.favoriteTrackIds)
        )
    }

    fun toggleFavorite(audioId: String) {
        val currentFavorites = _uiState.value.favoriteTrackIds.toMutableSet()
        if (currentFavorites.contains(audioId)) {
            currentFavorites.remove(audioId)
        } else {
            currentFavorites.add(audioId)
        }
        _uiState.value = _uiState.value.copy(
            favoriteTrackIds = currentFavorites,
            filteredAudioItems = filterList(_uiState.value.audioItems, _uiState.value.searchQuery, _uiState.value.currentScreen, currentFavorites)
        )
    }

    fun setSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        if (minutes <= 0) {
            _uiState.value = _uiState.value.copy(sleepTimerMinutes = 0, sleepTimerRemainingSec = 0)
            return
        }

        val totalSec = minutes * 60
        _uiState.value = _uiState.value.copy(sleepTimerMinutes = minutes, sleepTimerRemainingSec = totalSec)

        sleepTimerJob = viewModelScope.launch {
            var remaining = totalSec
            while (remaining > 0) {
                delay(1000L)
                remaining--
                _uiState.value = _uiState.value.copy(sleepTimerRemainingSec = remaining)
            }
            _uiState.value = _uiState.value.copy(isPlaying = false, sleepTimerMinutes = 0, sleepTimerRemainingSec = 0)
        }
    }

    private fun filterList(
        items: List<DriveAudioItem>,
        query: String,
        screen: Screen,
        favorites: Set<String>
    ): List<DriveAudioItem> {
        var result = items

        result = when (screen) {
            Screen.LOCAL_OFFLINE -> result.filter { it.id.startsWith("content://") || it.name.startsWith("[Offline]") }
            Screen.FAVORITES -> result.filter { favorites.contains(it.id) }
            else -> result
        }

        if (query.isNotBlank()) {
            result = result.filter { it.name.contains(query, ignoreCase = true) }
        }

        return result
    }

    fun selectTrack(item: DriveAudioItem) {
        _uiState.value = _uiState.value.copy(
            currentMediaItem = item,
            isPlaying = true,
            currentPositionMs = 0L,
            durationMs = 210_000L
        )
    }

    fun togglePlayPause() {
        _uiState.value = _uiState.value.copy(
            isPlaying = !_uiState.value.isPlaying
        )
    }

    fun seekTo(positionMs: Long) {
        _uiState.value = _uiState.value.copy(
            currentPositionMs = positionMs
        )
    }

    fun updatePlaybackState(isPlaying: Boolean, positionMs: Long, durationMs: Long, currentItem: DriveAudioItem?) {
        _uiState.value = _uiState.value.copy(
            isPlaying = isPlaying,
            currentPositionMs = if (positionMs > 0) positionMs else _uiState.value.currentPositionMs,
            durationMs = if (durationMs > 0) durationMs else _uiState.value.durationMs,
            currentMediaItem = currentItem ?: _uiState.value.currentMediaItem
        )
    }

    fun toggleShuffle() {
        _uiState.value = _uiState.value.copy(isShuffleOn = !_uiState.value.isShuffleOn)
    }

    fun toggleRepeat() {
        val nextState = when (_uiState.value.repeatState) {
            RepeatState.OFF -> RepeatState.ALL
            RepeatState.ALL -> RepeatState.ONE
            RepeatState.ONE -> RepeatState.OFF
        }
        _uiState.value = _uiState.value.copy(repeatState = nextState)
    }

    fun fetchDriveAudios(context: Context) {
        viewModelScope.launch {
            val localTracks = localAudioRepository.getLocalAudioFiles(context)
            val combined = (mockAudioList + localTracks).distinctBy { it.id }
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                audioItems = combined,
                filteredAudioItems = filterList(combined, _uiState.value.searchQuery, _uiState.value.currentScreen, _uiState.value.favoriteTrackIds)
            )
        }
    }

    fun signOut(context: Context) {
        _uiState.value = _uiState.value.copy(isSignedIn = false)
    }
}
