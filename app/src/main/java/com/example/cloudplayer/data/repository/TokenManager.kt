package com.example.cloudplayer.data.repository

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor() {
    // Google Drive'dan olingan musiqalarni striming qilish uchun ishlatiladigan Token
    var accessToken: String? = null
}
