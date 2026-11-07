package com.example.cloudplayer

import android.content.ComponentName
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.cloudplayer.data.model.DriveAudioItem
import com.example.cloudplayer.service.PlaybackService
import com.example.cloudplayer.ui.screen.CloudPlayerScreen
import com.example.cloudplayer.ui.theme.CloudPlayerTheme
import com.example.cloudplayer.ui.theme.DarkBackground
import com.example.cloudplayer.ui.viewmodel.CloudPlayerViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.guava.await

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: CloudPlayerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.checkExistingLogin(this)

        setContent {
            CloudPlayerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DarkBackground
                ) {
                    val uiState by viewModel.uiState.collectAsState()
                    var mediaController by remember { mutableStateOf<MediaController?>(null) }

                    LaunchedEffect(Unit) {
                        try {
                            val sessionToken = SessionToken(
                                this@MainActivity,
                                ComponentName(this@MainActivity, PlaybackService::class.java)
                            )
                            val controllerFuture = MediaController.Builder(this@MainActivity, sessionToken).buildAsync()
                            val controller = controllerFuture.await()
                            mediaController = controller

                            controller.addListener(object : Player.Listener {
                                override fun onIsPlayingChanged(isPlaying: Boolean) {
                                    viewModel.updatePlaybackState(
                                        isPlaying = isPlaying,
                                        positionMs = controller.currentPosition,
                                        durationMs = controller.duration.coerceAtLeast(0L),
                                        currentItem = null
                                    )
                                }

                                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                                    val id = mediaItem?.mediaId
                                    val item = uiState.audioItems.find { it.id == id }
                                    viewModel.updatePlaybackState(
                                        isPlaying = controller.isPlaying,
                                        positionMs = controller.currentPosition,
                                        durationMs = controller.duration.coerceAtLeast(0L),
                                        currentItem = item
                                    )
                                }
                            })
                        } catch (e: Exception) {
                            Log.e("CloudPlayerController", "MediaControllerni yaratishda xatolik", e)
                        }
                    }

                    DisposableEffect(Unit) {
                        onDispose {
                            mediaController?.release()
                        }
                    }

                    CloudPlayerScreen(
                        uiState = uiState,
                        onNavigateToScreen = { screen ->
                            viewModel.navigateToScreen(screen)
                        },
                        onSearchQueryChange = { query ->
                            viewModel.onSearchQueryChanged(query)
                        },
                        onFolderClick = { folderItem ->
                            viewModel.openFolder(this@MainActivity, folderItem)
                        },
                        onBreadcrumbClick = { breadcrumbItem ->
                            viewModel.navigateToBreadcrumb(this@MainActivity, breadcrumbItem)
                        },
                        onToggleFavorite = { audioId ->
                            viewModel.toggleFavorite(audioId)
                        },
                        onSetSleepTimer = { minutes ->
                            viewModel.setSleepTimer(minutes)
                        },
                        onRefresh = {
                            viewModel.fetchDriveAudios(this@MainActivity)
                        },
                        onSignOutClick = {
                            viewModel.signOut(this@MainActivity)
                            mediaController?.stop()
                        },
                        onPlayAudio = { audioItem ->
                            viewModel.selectTrack(audioItem)
                            playTrack(mediaController, audioItem)
                        },
                        onTogglePlayPause = {
                            viewModel.togglePlayPause()
                            mediaController?.let { controller ->
                                if (controller.isPlaying) {
                                    controller.pause()
                                } else {
                                    controller.play()
                                }
                            }
                        },
                        onSeekTo = { newPositionMs ->
                            viewModel.seekTo(newPositionMs)
                            mediaController?.seekTo(newPositionMs)
                        },
                        onSkipToNext = {
                            val currentList = uiState.filteredAudioItems
                            val currentIndex = currentList.indexOfFirst { it.id == uiState.currentMediaItem?.id }
                            if (currentIndex != -1 && currentIndex < currentList.size - 1) {
                                val nextTrack = currentList[currentIndex + 1]
                                viewModel.selectTrack(nextTrack)
                                playTrack(mediaController, nextTrack)
                            }
                        },
                        onSkipToPrevious = {
                            val currentList = uiState.filteredAudioItems
                            val currentIndex = currentList.indexOfFirst { it.id == uiState.currentMediaItem?.id }
                            if (currentIndex > 0) {
                                val prevTrack = currentList[currentIndex - 1]
                                viewModel.selectTrack(prevTrack)
                                playTrack(mediaController, prevTrack)
                            }
                        },
                        onToggleShuffle = {
                            viewModel.toggleShuffle()
                        },
                        onToggleRepeat = {
                            viewModel.toggleRepeat()
                        }
                    )
                }
            }
        }
    }

    private fun playTrack(controller: MediaController?, audioItem: DriveAudioItem) {
        if (controller == null) return
        try {
            val uri = if (audioItem.id.startsWith("content://")) audioItem.id else "https://www.googleapis.com/drive/v3/files/${audioItem.id}?alt=media"
            val mediaItem = MediaItem.Builder()
                .setMediaId(audioItem.id)
                .setUri(uri)
                .build()

            controller.setMediaItem(mediaItem)
            controller.prepare()
            controller.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
