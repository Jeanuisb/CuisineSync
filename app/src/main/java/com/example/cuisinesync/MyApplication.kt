package com.example.cuisinesync

import android.app.Application
import io.realm.kotlin.mongodb.App

class MyApplication: Application(){
    val realmApp: App by lazy {
        App.create("cuisinesync-mchpg") // Replace with your actual App ID
    }

}