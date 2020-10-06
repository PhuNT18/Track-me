package com.phunt.trackme

import android.app.Application
import com.phunt.trackme.db.AppDatabase
import com.phunt.trackme.tracker.DataManager
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        AppDatabase.getInstance(instance)
        GlobalScope.launch {
            DataManager.getInstance(instance)
        }
    }

    companion object {
        lateinit var instance: Application
            private set
    }
}