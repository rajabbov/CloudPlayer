package com.example.cloudplayer.data.repository

import android.content.Context
import com.example.cloudplayer.data.model.DriveAudioItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineDownloadManager @Inject constructor(
    private val tokenManager: TokenManager
) {

    private fun getDownloadDir(context: Context): File {
        val dir = File(context.getExternalFilesDir(null), "CloudPlayerDownloads")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun isDownloaded(context: Context, audioId: String): Boolean {
        val file = File(getDownloadDir(context), "$audioId.mp3")
        return file.exists() && file.length() > 0
    }

    fun getDownloadedFileUri(context: Context, audioId: String): String? {
        val file = File(getDownloadDir(context), "$audioId.mp3")
        return if (file.exists()) file.absolutePath else null
    }

    suspend fun downloadTrack(context: Context, item: DriveAudioItem, onProgress: (Int) -> Unit): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val token = tokenManager.accessToken ?: return@withContext false
                val downloadUrl = "https://www.googleapis.com/drive/v3/files/${item.id}?alt=media"
                
                val url = URL(downloadUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.setRequestProperty("Authorization", "Bearer $token")
                connection.connect()

                if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                    return@withContext false
                }

                val fileLength = connection.contentLength
                val inputStream = connection.inputStream
                val outputFile = File(getDownloadDir(context), "${item.id}.mp3")
                val outputStream = FileOutputStream(outputFile)

                val data = ByteArray(4096)
                var total: Long = 0
                var count: Int

                while (inputStream.read(data).also { count = it } != -1) {
                    total += count.toLong()
                    if (fileLength > 0) {
                        onProgress((total * 100 / fileLength).toInt())
                    }
                    outputStream.write(data, 0, count)
                }

                outputStream.flush()
                outputStream.close()
                inputStream.close()
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    fun getDownloadedTracks(context: Context): List<DriveAudioItem> {
        val dir = getDownloadDir(context)
        val files = dir.listFiles() ?: return emptyList()
        return files.map { file ->
            DriveAudioItem(
                id = file.nameWithoutExtension,
                name = "[Offline] ${file.nameWithoutExtension}",
                mimeType = "audio/mpeg",
                size = file.length()
            )
        }
    }
}
