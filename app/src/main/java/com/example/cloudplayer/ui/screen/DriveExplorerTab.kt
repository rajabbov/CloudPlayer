package com.example.cloudplayer.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cloudplayer.data.model.DriveAudioItem
import com.example.cloudplayer.data.model.DriveFolderItem
import com.example.cloudplayer.ui.theme.*

data class BreadcrumbItem(val id: String, val name: String)

@Composable
fun DriveExplorerTab(
    folderItems: List<DriveFolderItem>,
    breadcrumbs: List<BreadcrumbItem>,
    currentPlayingId: String?,
    isPlaying: Boolean,
    onFolderClick: (DriveFolderItem) -> Unit,
    onBreadcrumbClick: (BreadcrumbItem) -> Unit,
    onAudioFileClick: (DriveAudioItem) -> Unit
) {
    var isGridView by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Breadcrumb Navigation Bar (My Drive > Music > Pop)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LazyRow(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                itemsIndexed(breadcrumbs) { index, item ->
                    Text(
                        text = item.name,
                        color = if (index == breadcrumbs.lastIndex) ElectricCyan else TextSecondary,
                        fontWeight = if (index == breadcrumbs.lastIndex) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 14.sp,
                        modifier = Modifier.clickable { onBreadcrumbClick(item) }
                    )
                    if (index < breadcrumbs.lastIndex) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier
                                .padding(horizontal = 6.dp)
                                .size(14.dp)
                        )
                    }
                }
            }

            // Grid / List Toggle Button
            IconButton(onClick = { isGridView = !isGridView }) {
                Icon(
                    imageVector = if (isGridView) Icons.AutoMirrored.Filled.ViewList else Icons.Default.GridView,
                    contentDescription = "Toggle View",
                    tint = ElectricCyan
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        if (folderItems.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Bu papkada hechnarsa topilmadi",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else if (isGridView) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(folderItems) { item ->
                    DriveGridCard(
                        item = item,
                        isSelected = currentPlayingId == item.id,
                        isPlaying = currentPlayingId == item.id && isPlaying,
                        onClick = {
                            if (item.isFolder) {
                                onFolderClick(item)
                            } else {
                                onAudioFileClick(
                                    DriveAudioItem(
                                        id = item.id,
                                        name = item.name,
                                        mimeType = item.mimeType,
                                        size = item.size
                                    )
                                )
                            }
                        }
                    )
                }
            }
        } else {
            // Detailed List View
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                folderItems.forEach { item ->
                    DriveListCard(
                        item = item,
                        isSelected = currentPlayingId == item.id,
                        isPlaying = currentPlayingId == item.id && isPlaying,
                        onClick = {
                            if (item.isFolder) {
                                onFolderClick(item)
                            } else {
                                onAudioFileClick(
                                    DriveAudioItem(
                                        id = item.id,
                                        name = item.name,
                                        mimeType = item.mimeType,
                                        size = item.size
                                    )
                                )
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun DriveGridCard(
    item: DriveFolderItem,
    isSelected: Boolean,
    isPlaying: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isFolder) DarkSurface else if (isSelected) DarkSurfaceVariant else DarkSurface
        ),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, ElectricCyan) else null
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (item.isFolder) VioletGlow.copy(alpha = 0.2f) else ElectricCyan.copy(alpha = 0.2f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (item.isFolder) Icons.Default.Folder else if (isPlaying) Icons.Default.GraphicEq else Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = if (item.isFolder) VioletGlow else ElectricCyan,
                        modifier = Modifier.size(22.dp)
                    )
                }

                if (item.isFolder) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Text(
                text = item.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (isSelected) ElectricCyan else TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun DriveListCard(
    item: DriveFolderItem,
    isSelected: Boolean,
    isPlaying: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
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
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (item.isFolder) VioletGlow.copy(alpha = 0.2f) else ElectricCyan.copy(alpha = 0.2f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (item.isFolder) Icons.Default.Folder else if (isPlaying) Icons.Default.GraphicEq else Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = if (item.isFolder) VioletGlow else ElectricCyan,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) ElectricCyan else TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (item.isFolder) "Google Drive Papka" else "Audio fayl",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            Icon(
                imageVector = if (item.isFolder) Icons.Default.ChevronRight else Icons.Default.PlayArrow,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
