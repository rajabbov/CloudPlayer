package com.example.cloudplayer.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.cloudplayer.ui.theme.*
import com.example.cloudplayer.ui.viewmodel.PlayerUiState
import com.example.cloudplayer.ui.viewmodel.Screen

@Composable
fun AppNavigationDrawer(
    drawerState: DrawerState,
    uiState: PlayerUiState,
    onNavigateToScreen: (Screen) -> Unit,
    onSignOutClick: () -> Unit,
    content: @Composable () -> Unit
) {
    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = DarkSurface,
                drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp),
                modifier = Modifier.width(320.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(DarkSurface)
                ) {
                    // Telegram-Style Header Profile
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    listOf(DarkSurfaceVariant, DarkSurface)
                                )
                            )
                            .padding(24.dp)
                    ) {
                        Column {
                            // User Avatar
                            if (!uiState.userPhotoUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = uiState.userPhotoUrl,
                                    contentDescription = "User Avatar",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .border(2.dp, ElectricCyan, CircleShape)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(Brush.linearGradient(listOf(ElectricCyan, VioletGlow)))
                                        .border(2.dp, ElectricCyan, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = uiState.userDisplayName.take(1).uppercase(),
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = DarkBackground
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = uiState.userDisplayName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )

                            Text(
                                text = uiState.userEmail,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Google Drive Badge
                            Surface(
                                shape = CircleShape,
                                color = ElectricCyan.copy(alpha = 0.15f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Cloud,
                                        contentDescription = null,
                                        tint = ElectricCyan,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Google Drive Connected",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ElectricCyan,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = CardBorder)

                    Spacer(modifier = Modifier.height(8.dp))

                    // Menu Items - Separate Screens Navigation
                    DrawerMenuItem(
                        icon = Icons.Default.MusicNote,
                        title = "Music Player Home",
                        isSelected = uiState.currentScreen == Screen.HOME,
                        onClick = { onNavigateToScreen(Screen.HOME) }
                    )

                    DrawerMenuItem(
                        icon = Icons.Default.Folder,
                        title = "Drive Folders",
                        isSelected = uiState.currentScreen == Screen.DRIVE_FOLDERS,
                        onClick = { onNavigateToScreen(Screen.DRIVE_FOLDERS) }
                    )

                    DrawerMenuItem(
                        icon = Icons.Default.Smartphone,
                        title = "Local & Offline",
                        isSelected = uiState.currentScreen == Screen.LOCAL_OFFLINE,
                        onClick = { onNavigateToScreen(Screen.LOCAL_OFFLINE) }
                    )

                    DrawerMenuItem(
                        icon = Icons.Default.Favorite,
                        title = "Favorites",
                        isSelected = uiState.currentScreen == Screen.FAVORITES,
                        onClick = { onNavigateToScreen(Screen.FAVORITES) }
                    )

                    HorizontalDivider(
                        color = CardBorder,
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp)
                    )

                    DrawerMenuItem(
                        icon = Icons.Default.Equalizer,
                        title = "Audio Equalizer",
                        isSelected = uiState.currentScreen == Screen.EQUALIZER,
                        onClick = { onNavigateToScreen(Screen.EQUALIZER) }
                    )

                    DrawerMenuItem(
                        icon = Icons.Default.Timer,
                        title = "Sleep Timer",
                        isSelected = uiState.currentScreen == Screen.SLEEP_TIMER,
                        onClick = { onNavigateToScreen(Screen.SLEEP_TIMER) }
                    )

                    DrawerMenuItem(
                        icon = Icons.Default.Settings,
                        title = "Settings & Cache",
                        isSelected = uiState.currentScreen == Screen.SETTINGS,
                        onClick = { onNavigateToScreen(Screen.SETTINGS) }
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    HorizontalDivider(color = CardBorder)

                    DrawerMenuItem(
                        icon = Icons.AutoMirrored.Filled.ExitToApp,
                        title = "Sign Out",
                        isSelected = false,
                        iconTint = Color.Red,
                        onClick = onSignOutClick
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        },
        content = content
    )
}

@Composable
fun DrawerMenuItem(
    icon: ImageVector,
    title: String,
    isSelected: Boolean,
    iconTint: Color? = null,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        color = if (isSelected) ElectricCyan.copy(alpha = 0.15f) else Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconTint ?: if (isSelected) ElectricCyan else TextSecondary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) ElectricCyan else TextPrimary
            )
        }
    }
}
