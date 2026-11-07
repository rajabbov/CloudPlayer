package com.example.cloudplayer.service

import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.ResolvingDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.example.cloudplayer.data.repository.TokenManager
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class PlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    private var player: ExoPlayer? = null

    @Inject
    lateinit var tokenManager: TokenManager

    companion object {
        private var downloadCache: SimpleCache? = null

        @Synchronized
        private fun getCache(service: MediaSessionService): SimpleCache {
            if (downloadCache == null) {
                val cacheDir = File(service.cacheDir, "media_cache")
                val evictor = LeastRecentlyUsedCacheEvictor(100 * 1024 * 1024) // 100 MB cache limit
                val databaseProvider = StandaloneDatabaseProvider(service)
                downloadCache = SimpleCache(cacheDir, evictor, databaseProvider)
            }
            return downloadCache!!
        }
    }

    override fun onCreate() {
        super.onCreate()

        // 1. HttpDataSource - Google Drive uchun Authorization header qo'shamiz
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
        
        // ResolvingDataSource - Har bir so'rovga (url) dynamic tarzda Token qo'shamiz
        val resolvingDataSourceFactory = ResolvingDataSource.Factory(httpDataSourceFactory) { dataSpec ->
            tokenManager.accessToken?.let { token ->
                dataSpec.withAdditionalHeaders(mapOf("Authorization" to "Bearer $token"))
            } ?: dataSpec
        }

        // 2. CacheDataSource - Barcha eshitilgan qo'shiqlarni xotirada keshlab boradi (Internet tejash)
        val cacheDataSourceFactory = CacheDataSource.Factory()
            .setCache(getCache(this))
            .setUpstreamDataSourceFactory(resolvingDataSourceFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

        // 3. ExoPlayer ni keshli DataSource bilan yaratish
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

        player = ExoPlayer.Builder(this)
            .setMediaSourceFactory(DefaultMediaSourceFactory(cacheDataSourceFactory))
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .build()

        // 4. MediaSession yaratish
        mediaSession = MediaSession.Builder(this, player!!)
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}
