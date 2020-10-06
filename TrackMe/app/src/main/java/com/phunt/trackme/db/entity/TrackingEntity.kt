package com.phunt.trackme.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class TrackingEntity(val polyLine: String,
                          val distance: Float,
                          val avgSpeed: Float,
                          val time: Long,
                          val createDate: Date ){
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Long = 0
}