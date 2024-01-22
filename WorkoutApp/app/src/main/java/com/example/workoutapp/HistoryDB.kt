package com.example.workoutapp

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [HistoryNTT::class], version = 1)
abstract class HistoryDB: RoomDatabase() {
    abstract fun historyDAO(): IHistoryDAO

    companion object {
        // singleton pattern for db
        // keep a reference to the currently initialized db so we don't repeatedly instantiate the db (expensive in time)
        @Volatile
        private var INSTANCE: HistoryDB? = null

        fun getInstance(context: Context): HistoryDB {
            // multiple threads can ask for data at once. only one thread may enter a synchronized block at a time
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        HistoryDB::class.java,
                        "history_db"
                    ).fallbackToDestructiveMigration().build()

                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}