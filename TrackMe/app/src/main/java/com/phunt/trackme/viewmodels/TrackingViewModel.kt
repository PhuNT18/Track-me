package com.phunt.trackme.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.phunt.trackme.db.AppDatabase
import com.phunt.trackme.db.entity.TrackingEntity
import com.phunt.trackme.utils.ioThread

class TrackingViewModel(app: Application) : AndroidViewModel(app) {

    private val dao = AppDatabase.getInstance(app).trackingDao()

    val allTrackings = Pager(
            PagingConfig(
                    pageSize = 10,
                    enablePlaceholders = true,
                    maxSize = 200
            )
    ){
        dao.getAllTracking()
    }.flow

    fun insert(trackingRecord: TrackingEntity) = ioThread {
        dao.insert(trackingRecord)
    }
}