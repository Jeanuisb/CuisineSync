package com.example.cuisinesync

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import io.realm.kotlin.mongodb.App

class MyApplication: Application(){
    val realmApp: App by lazy {
        App.create("cuisinesync-mtzod")
    }

    val dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_loggedin_datastore")

}