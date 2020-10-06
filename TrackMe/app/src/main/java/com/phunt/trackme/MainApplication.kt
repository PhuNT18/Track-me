package com.phunt.trackme

import android.app.Application
import com.phunt.trackme.db.AppDatabase

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        AppDatabase.getInstance(instance)
    }

    companion object {
        lateinit var instance: Application
            private set
    }
}