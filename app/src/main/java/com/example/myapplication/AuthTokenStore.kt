package com.example.myapplication

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey




    class AuthTokenStore(context: Context) {

        // EncryptedSharedPreferences — tokens encrypted via Android Keystore
        // Never use regular SharedPreferences for tokens
        private val prefs = EncryptedSharedPreferences.create(
            context,
            "auth_prefs",                                   // filename
            MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        fun saveToken(token: String) {
            prefs.edit().putString(KEY_TOKEN, token).apply()
        }

        fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

        fun clearToken() {
            prefs.edit().remove(KEY_TOKEN).apply()
        }

        fun isLoggedIn(): Boolean = getToken() != null

        companion object {
            private const val KEY_TOKEN = "jwt_token"
        }
    }
