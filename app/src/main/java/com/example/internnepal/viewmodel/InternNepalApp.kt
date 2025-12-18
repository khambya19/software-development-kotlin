package com.example.internnepal.viewmodel

import android.app.Application
import com.google.firebase.FirebaseApp

class InternNepalApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}