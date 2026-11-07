package com.example.cloudplayer.data.repository

import android.content.Context
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GoogleAuthRepository @Inject constructor() {

    fun getSignInClient(context: Context): GoogleSignInClient {
        // Drive fayllarini faqat o'qish (readonly) huquqini so'raymiz
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_READONLY))
            .build()
        return GoogleSignIn.getClient(context, gso)
    }

    // Tizimga avval kirgan bo'lsa, akkountni olish
    fun getLastSignedInAccount(context: Context): GoogleSignInAccount? {
        return GoogleSignIn.getLastSignedInAccount(context)
    }

    // Tizimdan chiqish
    suspend fun signOut(context: Context) {
        getSignInClient(context).signOut().await()
    }

    // ExoPlayer da musiqa eshitish uchun (Striming URL ga yuborish uchun) Access Token olish
    suspend fun getAccessToken(context: Context, account: GoogleSignInAccount): String? {
        return withContext(Dispatchers.IO) {
            try {
                account.account?.let { acc ->
                    val scope = "oauth2:${DriveScopes.DRIVE_READONLY}"
                    GoogleAuthUtil.getToken(context, acc, scope)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}