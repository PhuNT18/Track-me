package com.phunt.trackme.db.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.phunt.trackme.db.entity.TrackingEntity

@Dao
interface TrackingDao {
    @Query("SELECT * FROM TrackingEntity ORDER BY createDate DESC")
    fun getAllTracking(): PagingSource<Int, TrackingEntity>

    @Insert
    fun insert(trackingList: List<TrackingEntity>)

    @Insert
    fun insert(tracking: TrackingEntity)

    @Delete
    fun delete(tracking: TrackingEntity)
}