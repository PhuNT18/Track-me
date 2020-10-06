package com.phunt.trackme.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.phunt.trackme.db.dao.TrackingDao
import com.phunt.trackme.db.entity.TrackingEntity

@Database(entities = [TrackingEntity::class], version = 1, exportSchema = false)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun trackingDao(): TrackingDao

    companion object : SingletonHolder<AppDatabase, Context>({
        Room.databaseBuilder(
                it.applicationContext,
                AppDatabase::class.java, "TrackingMeDatabase"
        ).build()
    })
   /* companion object {
        private var instance: AppDatabase? = null

        @Synchronized
        fun get(context: Context): AppDatabase {
            if (instance == null) {
                instance = Room.databaseBuilder(context.applicationContext,
                        AppDatabase::class.java, "TrackingMeDatabase")
                        .build()
            }
            return instance!!
        }
    }*/
}