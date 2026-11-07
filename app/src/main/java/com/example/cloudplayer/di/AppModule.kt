package com.example.cloudplayer.di

import com.example.cloudplayer.data.repository.DriveRepository
import com.example.cloudplayer.data.repository.GoogleAuthRepository
import com.example.cloudplayer.data.repository.TokenManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideGoogleAuthRepository(): GoogleAuthRepository {
        return GoogleAuthRepository()
    }

    @Provides
    @Singleton
    fun provideDriveRepository(): DriveRepository {
        return DriveRepository()
    }

    @Provides
    @Singleton
    fun provideTokenManager(): TokenManager {
        return TokenManager()
    }
}