package com.example.cloudplayer.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cloudplayer.data.model.DriveAudioItem
import com.example.cloudplayer.data.model.DriveFolderItem
import com.example.cloudplayer.ui.theme.*
import com.example.cloudplayer.ui.viewmodel.PlayerUiState
import com.example.cloudplayer.ui.viewmodel.RepeatState
import com.example.cloudplayer.ui.viewmodel.Screen
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloudPlayerScreen(
    uiState: PlayerUiState,
    onNavigateToScreen: (Screen) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onFolderClick: (DriveFolderItem) -> Unit,
    onBreadcrumbClick: (BreadcrumbItem) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onSetSleepTimer: (Int) -> Unit,
    onRefresh: () -> Unit,
    onSignOutClick: () -> Unit,
    onPlayAudio: (DriveAudioItem) -> Unit,
    onTogglePlayPause: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onSkipToNext: () -> Unit,
    onSkipToPrevious: () -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    val bottomSheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.PartiallyExpanded,
        skipHiddenState = true
    )
    val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState)

    AppNavigationDrawer(
        drawerState = drawerState,
        uiState = uiState,
        onNavigateToScreen = { screen ->
            onNavigateToScreen(screen)
            coroutineScope.launch { drawerState.close() }
        },
        onSignOutClick = {
            onSignOutClick()
            coroutineScope.launch { drawerState.close() }
        }
    ) {
        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetPeekHeight = if (uiState.currentMediaItem != null) 76.dp else 0.dp,
            sheetContainerColor = DarkSurfaceVariant,
            sheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            sheetContent = {
                uiState.currentMediaItem?.let { currentItem ->
                    PlayerSheetContent(
                        currentItem = currentItem,
                        isPlaying = uiState.isPlaying,
                        currentPositionMs = uiState.currentPositionMs,
                        durationMs = uiState.durationMs,
                        isShuffleOn = uiState.isShuffleOn,
                        repeatState = uiState.repeatState,
                        isFavorite = uiState.favoriteTrackIds.contains(currentItem.id),
                        isExpanded = bottomSheetState.targetValue == SheetValue.Expanded,
                        onToggleFavorite = { onToggleFavorite(currentItem.id) },
                        onTogglePlayPause = onTogglePlayPause,
                        onSeekTo = onSeekTo,
                        onSkipToNext = onSkipToNext,
                        onSkipToPrevious = onSkipToPrevious,
                        onToggleShuffle = onToggleShuffle,
                        onToggleRepeat = onToggleRepeat
                    )
                }
            },
            topBar = {
                Column(
                    modifier = Modifier
                        .background(DarkBackground)
                        .statusBarsPadding()
                ) {
                    TopAppBar(
                        navigationIcon = {
                            if (uiState.currentScreen == Screen.HOME) {
                                IconButton(onClick = {
                                    coroutineScope.launch { drawerState.open() }
                                }) {
                                    Icon(Icons.Default.Menu, contentDescription = "Menu", tint = ElectricCyan)
                                }
                            } else {
                                IconButton(onClick = {
                                    onNavigateToScreen(Screen.HOME)
                                }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to Home", tint = ElectricCyan)
                                }
                            }
                        },
                        title = {
                            Text(
                                text = when (uiState.currentScreen) {
                                    Screen.HOME -> "CloudPlayer"
                                    Screen.DRIVE_FOLDERS -> "Drive Folders"
                                    Screen.LOCAL_OFFLINE -> "Local & Offline"
                                    Screen.FAVORITES -> "Favorites"
                                    Screen.EQUALIZER -> "Audio Equalizer"
                                    Screen.SLEEP_TIMER -> "Sleep Timer"
                                    Screen.SETTINGS -> "Settings & Storage"
                                },
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp,
                                color = TextPrimary
                            )
                        },
                        actions = {
                            if (uiState.currentScreen == Screen.HOME) {
                                IconButton(onClick = onRefresh) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = TextSecondary)
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
                    )

                    if (uiState.currentScreen == Screen.HOME) {
                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = onSearchQueryChange,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            placeholder = { Text("Search track by name...", color = TextSecondary) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = ElectricCyan) },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = DarkSurface,
                                unfocusedContainerColor = DarkSurface,
                                focusedBorderColor = ElectricCyan,
                                unfocusedBorderColor = Color.Transparent,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DarkBackground)
                    .padding(innerPadding)
            ) {
                when (uiState.currentScreen) {
                    Screen.HOME -> {
                        // Clean Home Screen: Pure Music Player List
                        if (uiState.filteredAudioItems.isEmpty()) {
                            EmptyState("No music tracks found")
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp)
                            ) {
                                items(uiState.filteredAudioItems) { audioItem ->
                                    AudioCardItem(
                                        audioItem = audioItem,
                                        isSelected = uiState.currentMediaItem?.id == audioItem.id,
                                        isPlaying = uiState.currentMediaItem?.id == audioItem.id && uiState.isPlaying,
                                        isFavorite = uiState.favoriteTrackIds.contains(audioItem.id),
                                        onClick = { onPlayAudio(audioItem) },
                                        onToggleFavorite = { onToggleFavorite(audioItem.id) }
                                    )
                                }
                            }
                        }
                    }

                    Screen.DRIVE_FOLDERS -> {
                        DriveExplorerTab(
                            folderItems = uiState.driveFolderItems,
                            breadcrumbs = uiState.breadcrumbs,
                            currentPlayingId = uiState.currentMediaItem?.id,
                            isPlaying = uiState.isPlaying,
                            onFolderClick = onFolderClick,
                            onBreadcrumbClick = onBreadcrumbClick,
                            onAudioFileClick = onPlayAudio
                        )
                    }

                    Screen.LOCAL_OFFLINE -> {
                        val localList = uiState.audioItems.filter { it.id.startsWith("content://") || it.name.startsWith("[Offline]") }
                        if (localList.isEmpty()) {
                            EmptyState("No local songs found on device")
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp)
                            ) {
                                items(localList) { audioItem ->
                                    AudioCardItem(
                                        audioItem = audioItem,
                                        isSelected = uiState.currentMediaItem?.id == audioItem.id,
                                        isPlaying = uiState.currentMediaItem?.id == audioItem.id && uiState.isPlaying,
                                        isFavorite = uiState.favoriteTrackIds.contains(audioItem.id),
                                        onClick = { onPlayAudio(audioItem) },
                                        onToggleFavorite = { onToggleFavorite(audioItem.id) }
                                    )
                                }
                            }
                        }
                    }

                    Screen.FAVORITES -> {
                        val favList = uiState.audioItems.filter { uiState.favoriteTrackIds.contains(it.id) }
                        if (favList.isEmpty()) {
                            EmptyState("No favorite songs added yet")
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp)
                            ) {
                                items(favList) { audioItem ->
                                    AudioCardItem(
                                        audioItem = audioItem,
                                        isSelected = uiState.currentMediaItem?.id == audioItem.id,
                                        isPlaying = uiState.currentMediaItem?.id == audioItem.id && uiState.isPlaying,
                                        isFavorite = true,
                                        onClick = { onPlayAudio(audioItem) },
                                        onToggleFavorite = { onToggleFavorite(audioItem.id) }
                                    )
                                }
                            }
                        }
                    }

                    Screen.EQUALIZER -> {
                        EqualizerScreenView()
                    }

                    Screen.SLEEP_TIMER -> {
                        SleepTimerScreenView(
                            remainingSec = uiState.sleepTimerRemainingSec,
                            onSetTimer = onSetSleepTimer
                        )
                    }

                    Screen.SETTINGS -> {
                        SettingsScreenView(uiState = uiState)
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyState(message: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.MusicNote, contentDescription = null, modifier = Modifier.size(64.dp), tint = TextSecondary)
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = message, style = MaterialTheme.typography.bodyLarge, color = TextSecondary, textAlign = TextAlign.Center)
    }
}

@Composable
fun EqualizerScreenView() {
    var bassLevel by remember { mutableFloatStateOf(500f) }
    var band1 by remember { mutableFloatStateOf(0f) }
    var band2 by remember { mutableFloatStateOf(0f) }
    var band3 by remember { mutableFloatStateOf(0f) }
    var band4 by remember { mutableFloatStateOf(0f) }
    var band5 by remember { mutableFloatStateOf(0f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("5-Band Equalizer & Bass Boost", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(modifier = Modifier.height(24.dp))

        Text("Bass Boost (${bassLevel.toInt()})", color = ElectricCyan, modifier = Modifier.align(Alignment.Start))
        Slider(
            value = bassLevel,
            onValueChange = { bassLevel = it },
            valueRange = 0f..1000f,
            colors = SliderDefaults.colors(thumbColor = ElectricCyan, activeTrackColor = ElectricCyan)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            BandSliderColumn("60Hz", band1) { band1 = it }
            BandSliderColumn("230Hz", band2) { band2 = it }
            BandSliderColumn("910Hz", band3) { band3 = it }
            BandSliderColumn("3.6kHz", band4) { band4 = it }
            BandSliderColumn("14kHz", band5) { band5 = it }
        }
    }
}

@Composable
fun SleepTimerScreenView(remainingSec: Int, onSetTimer: (Int) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Sleep Timer", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Set duration after which playback will automatically stop", color = TextSecondary, textAlign = TextAlign.Center)

        if (remainingSec > 0) {
            Spacer(modifier = Modifier.height(32.dp))
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = ElectricCyan.copy(alpha = 0.15f)
            ) {
                Text(
                    text = "Timer Active: ${remainingSec / 60}:${String.format(Locale.getDefault(), "%02d", remainingSec % 60)}",
                    modifier = Modifier.padding(16.dp),
                    color = ElectricCyan,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            TimerOptionButton("15 Min") { onSetTimer(15) }
            TimerOptionButton("30 Min") { onSetTimer(30) }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            TimerOptionButton("45 Min") { onSetTimer(45) }
            TimerOptionButton("60 Min") { onSetTimer(60) }
        }
        Spacer(modifier = Modifier.height(32.dp))
        TextButton(onClick = { onSetTimer(0) }) {
            Text("Turn Off Timer", color = Color.Red, fontSize = 16.sp)
        }
    }
}

@Composable
fun SettingsScreenView(uiState: PlayerUiState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text("Account & Storage Settings", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(modifier = Modifier.height(24.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Google Account", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                Text(text = uiState.userDisplayName, style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                Text(text = uiState.userEmail, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Media Cache", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                Text(text = "100 MB Limit (ExoPlayer SimpleCache)", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = {}, colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan, contentColor = DarkBackground)) {
                    Text("Clear Stream Cache")
                }
            }
        }
    }
}

@Composable
fun AudioCardItem(
    audioItem: DriveAudioItem,
    isSelected: Boolean,
    isPlaying: Boolean,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) DarkSurfaceVariant else DarkSurface
        ),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, ElectricCyan) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                if (isSelected) ElectricCyan else DarkSurfaceVariant,
                                if (isSelected) VioletGlow else DarkBackground
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.GraphicEq else Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = if (isSelected) DarkBackground else ElectricCyan,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = audioItem.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) ElectricCyan else TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${if (audioItem.id.startsWith("content://")) "Local" else "Cloud"} • ${formatFileSize(audioItem.size)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) Color.Red else TextSecondary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
fun PlayerSheetContent(
    currentItem: DriveAudioItem,
    isPlaying: Boolean,
    currentPositionMs: Long,
    durationMs: Long,
    isShuffleOn: Boolean,
    repeatState: RepeatState,
    isFavorite: Boolean,
    isExpanded: Boolean,
    onToggleFavorite: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onSkipToNext: () -> Unit,
    onSkipToPrevious: () -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit
) {
    if (!isExpanded) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            val progress = if (durationMs > 0) currentPositionMs.toFloat() / durationMs.toFloat() else 0f
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp),
                color = ElectricCyan,
                trackColor = Color.Transparent
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Brush.linearGradient(listOf(ElectricCyan, VioletGlow))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.MusicNote, contentDescription = null, tint = DarkBackground)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currentItem.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "CloudPlayer Stream",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                IconButton(
                    onClick = onTogglePlayPause,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(ElectricCyan)
                        .size(40.dp)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = DarkBackground
                    )
                }
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .size(280.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.radialGradient(
                            listOf(VioletGlow, DarkSurfaceVariant, DarkBackground)
                        )
                    )
                    .border(2.dp, ElectricCyan, RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Album,
                    contentDescription = null,
                    modifier = Modifier.size(120.dp),
                    tint = ElectricCyan
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currentItem.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Cloud Music Player",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) Color.Red else TextSecondary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            var sliderPosition by remember(currentPositionMs) { mutableFloatStateOf(currentPositionMs.toFloat()) }
            var isUserSeeking by remember { mutableStateOf(false) }

            Slider(
                value = if (isUserSeeking) sliderPosition else currentPositionMs.toFloat(),
                onValueChange = {
                    isUserSeeking = true
                    sliderPosition = it
                },
                onValueChangeFinished = {
                    isUserSeeking = false
                    onSeekTo(sliderPosition.toLong())
                },
                valueRange = 0f..(if (durationMs > 0) durationMs.toFloat() else 1f),
                colors = SliderDefaults.colors(
                    thumbColor = ElectricCyan,
                    activeTrackColor = ElectricCyan,
                    inactiveTrackColor = DarkSurface
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatTimeMs(if (isUserSeeking) sliderPosition.toLong() else currentPositionMs),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Text(
                    text = formatTimeMs(durationMs),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onToggleShuffle) {
                    Icon(
                        Icons.Default.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (isShuffleOn) ElectricCyan else TextSecondary
                    )
                }

                IconButton(onClick = onSkipToPrevious) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Previous",
                        modifier = Modifier.size(36.dp),
                        tint = TextPrimary
                    )
                }

                FloatingActionButton(
                    onClick = onTogglePlayPause,
                    shape = CircleShape,
                    containerColor = ElectricCyan,
                    contentColor = DarkBackground,
                    modifier = Modifier.size(68.dp)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        modifier = Modifier.size(36.dp)
                    )
                }

                IconButton(onClick = onSkipToNext) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next",
                        modifier = Modifier.size(36.dp),
                        tint = TextPrimary
                    )
                }

                IconButton(onClick = onToggleRepeat) {
                    Icon(
                        imageVector = when (repeatState) {
                            RepeatState.ONE -> Icons.Default.RepeatOne
                            else -> Icons.Default.Repeat
                        },
                        contentDescription = "Repeat",
                        tint = if (repeatState != RepeatState.OFF) ElectricCyan else TextSecondary
                    )
                }
            }
        }
    }
}

private fun formatTimeMs(timeMs: Long): String {
    if (timeMs <= 0) return "00:00"
    val totalSeconds = timeMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
}

private fun formatFileSize(bytes: Long?): String {
    if (bytes == null || bytes <= 0) return "Unknown size"
    val mb = bytes.toDouble() / (1024 * 1024)
    return String.format(Locale.getDefault(), "%.1f MB", mb)
}
