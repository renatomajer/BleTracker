package com.renatomajer.bletracker.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("credentials")

@Singleton
class CredentialsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        val USERNAME_KEY = stringPreferencesKey("username")
        val PASSWORD_KEY = stringPreferencesKey("password")
        val TOKEN_KEY = stringPreferencesKey("token")
        val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
        val DEVICE_ID_KEY = stringPreferencesKey("device_id")
    }

    val username: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[USERNAME_KEY]
        }

    val password: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[PASSWORD_KEY]
        }

    val token: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[TOKEN_KEY]
        }

    val refreshToken: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[REFRESH_TOKEN_KEY]
        }

    val deviceId: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[DEVICE_ID_KEY]
        }

    suspend fun setUsername(username: String) {
        context.dataStore.edit { credentials ->
            credentials[USERNAME_KEY] = username
        }
    }

    suspend fun setPassword(password: String) {
        context.dataStore.edit { credentials ->
            credentials[PASSWORD_KEY] = password
        }
    }

    suspend fun setToken(token: String) {
        context.dataStore.edit { credentials ->
            credentials[TOKEN_KEY] = token
        }
    }

    suspend fun setRefreshToken(refreshToken: String) {
        context.dataStore.edit { credentials ->
            credentials[REFRESH_TOKEN_KEY] = refreshToken
        }
    }

    suspend fun setDeviceId(deviceId: String) {
        context.dataStore.edit { credentials ->
            credentials[DEVICE_ID_KEY] = deviceId
        }
    }
}