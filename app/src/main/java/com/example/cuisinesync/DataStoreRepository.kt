package com.example.cuisinesync

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.cuisinesync.DataStoreRepository.PreferencesKeys.IS_LOGGED_IN
import com.example.cuisinesync.DataStoreRepository.PreferencesKeys.USERNAME
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

class DataStoreRepository (private val dataStore: DataStore<Preferences>) {


    private object PreferencesKeys {
        val USERNAME = stringPreferencesKey("username")
        val IS_LOGGED_IN = booleanPreferencesKey("isLoggedIn") // will check if user wants to be remembered

    }

    suspend fun saveToDataStore(user: String, isLoggedIn: Boolean) {
        dataStore.edit { preference ->
            preference[USERNAME] = user
            preference[IS_LOGGED_IN] = isLoggedIn
        }
    }

    val readFromDataStore : Flow<UserPreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.d("DataStoreRepository", exception.message.toString())
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preference ->
            val username = preference[USERNAME] ?: ""
            val isloggedin = preference[IS_LOGGED_IN] ?: false
            UserPreferences(username, isloggedin)
        }

    suspend fun clearDataStore() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    suspend fun removeUsername() {
        dataStore.edit { preference ->
            preference.remove(USERNAME)
        }
    }

    suspend fun saveLoginStatus(isLoggedIn: Boolean) {
        dataStore.edit { preference ->
            preference[IS_LOGGED_IN] = isLoggedIn
        }
    }

    suspend fun getLoginStatus(): Boolean {
        val preferences = dataStore.data.first()
        return preferences[IS_LOGGED_IN] ?: false
    }

    val readLoginStatus: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.d("DataStoreRepository", exception.message.toString())
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preference ->
            preference[IS_LOGGED_IN] ?: false
        }

    data class UserPreferences(
        val username: String,
        val isLoggedIn: Boolean
    )
}