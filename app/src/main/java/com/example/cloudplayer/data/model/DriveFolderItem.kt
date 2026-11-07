package com.example.cloudplayer.data.model

data class DriveFolderItem(
    val id: String,
    val name: String,
    val isFolder: Boolean,
    val mimeType: String,
    val size: Long? = null,
    val parentFolderId: String? = null
)
