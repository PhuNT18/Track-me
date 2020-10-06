package com.phunt.trackme.tracker

import android.content.Context
import com.google.android.gms.maps.model.LatLng
import com.phunt.trackme.db.SingletonHolder

class DataManager private constructor(context: Context) {
    var distance: Float = 0f // Km
    var points: MutableList<LatLng>? = mutableListOf()
    var isPauseTracking: Boolean = false
    var time: Long = 0 // second

    companion object : SingletonHolder<DataManager, Context>(:: DataManager)

    init {

    }
}