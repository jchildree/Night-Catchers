package com.nightcatchers.core.security

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "parent_session",
)

@Singleton
class ParentSessionManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val masterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val securePrefs by lazy {
        EncryptedSharedPreferences.create(
            context,
            SECURE_PREFS_FILE,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    val isParentSessionActive: Flow<Boolean> = context.sessionDataStore.data.map { prefs ->
        prefs[KEY_SESSION_ACTIVE] ?: false
    }

    suspend fun openParentSession() {
        context.sessionDataStore.edit { it[KEY_SESSION_ACTIVE] = true }
    }

    suspend fun closeParentSession() {
        context.sessionDataStore.edit { it[KEY_SESSION_ACTIVE] = false }
    }

    fun saveParentEmail(email: String) {
        securePrefs.edit().putString(SECURE_KEY_EMAIL, email).apply()
    }

    fun getParentEmail(): String? = securePrefs.getString(SECURE_KEY_EMAIL, null)

    fun clearParentEmail() {
        securePrefs.edit().remove(SECURE_KEY_EMAIL).apply()
    }

    companion object {
        private const val SECURE_PREFS_FILE = "nc_parent_session_prefs"
        private const val SECURE_KEY_EMAIL = "parent_email"
        private val KEY_SESSION_ACTIVE = booleanPreferencesKey("session_active")
    }
}
