package com.nightcatchers.core.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Derives and persists the SQLCipher database passphrase using Android KeyStore AES-256-GCM.
 * The passphrase is generated once with SecureRandom and stored in EncryptedSharedPreferences,
 * which is itself protected by a MasterKey backed by the hardware KeyStore.
 */
@Singleton
class DatabaseKeyManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val masterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val prefs by lazy {
        EncryptedSharedPreferences.create(
            context,
            PREFS_FILE,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    fun getOrCreatePassphrase(): CharArray {
        val stored = prefs.getString(KEY_PASSPHRASE, null)
        if (stored != null) return stored.toCharArray()
        val generated = generatePassphrase()
        prefs.edit().putString(KEY_PASSPHRASE, String(generated)).apply()
        return generated
    }

    private fun generatePassphrase(): CharArray {
        val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#\$%^&*()-_=+"
        val rng = SecureRandom()
        return CharArray(48) { alphabet[rng.nextInt(alphabet.length)] }
    }

    companion object {
        private const val PREFS_FILE = "nc_db_prefs"
        private const val KEY_PASSPHRASE = "db_passphrase"
    }
}
