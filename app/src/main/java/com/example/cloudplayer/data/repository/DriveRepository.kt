package com.example.cloudplayer.data.repository

import android.content.Context
import com.example.cloudplayer.data.model.DriveAudioItem
import com.example.cloudplayer.data.model.DriveFolderItem
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DriveRepository @Inject constructor() {

    private fun getDriveService(context: Context, account: GoogleSignInAccount): Drive {
        val credential = GoogleAccountCredential.usingOAuth2(
            context, listOf(DriveScopes.DRIVE_READONLY)
        ).apply {
            selectedAccount = account.account
        }

        return Drive.Builder(
            NetHttpTransport(),
            GsonFactory(),
            credential
        )
            .setApplicationName("CloudPlayer")
            .build()
    }

    suspend fun getAudioFiles(context: Context, account: GoogleSignInAccount): List<DriveAudioItem> {
        return withContext(Dispatchers.IO) {
            val driveService = getDriveService(context, account)
            val audioList = mutableListOf<DriveAudioItem>()

            try {
                val result = driveService.files().list()
                    .setQ("mimeType contains 'audio/' and trashed = false")
                    .setSpaces("drive")
                    .setFields("nextPageToken, files(id, name, mimeType, size)")
                    .execute()

                val files = result.files
                if (files != null) {
                    for (file in files) {
                        audioList.add(
                            DriveAudioItem(
                                id = file.id,
                                name = file.name,
                                mimeType = file.mimeType ?: "audio/mpeg",
                                size = file.getSize()
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            audioList
        }
    }

    // Google Drive bitta papkasi ichidagi sub-papkalar va audio fayllarni o'qish
    suspend fun getFolderContents(
        context: Context,
        account: GoogleSignInAccount,
        folderId: String = "root"
    ): List<DriveFolderItem> {
        return withContext(Dispatchers.IO) {
            val driveService = getDriveService(context, account)
            val itemList = mutableListOf<DriveFolderItem>()

            try {
                val query = "'$folderId' in parents and (mimeType = 'application/vnd.google-apps.folder' or mimeType contains 'audio/') and trashed = false"
                val result = driveService.files().list()
                    .setQ(query)
                    .setSpaces("drive")
                    .setFields("files(id, name, mimeType, size)")
                    .setOrderBy("mimeType desc, name asc")
                    .execute()

                val files = result.files
                if (files != null) {
                    for (file in files) {
                        val isFolder = file.mimeType == "application/vnd.google-apps.folder"
                        itemList.add(
                            DriveFolderItem(
                                id = file.id,
                                name = file.name,
                                isFolder = isFolder,
                                mimeType = file.mimeType ?: "",
                                size = file.getSize(),
                                parentFolderId = folderId
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            itemList
        }
    }
}
