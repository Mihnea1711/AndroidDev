package com.example.roomdemo

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [EmployeeNTT::class], version = 1)
abstract class EmployeeDB: RoomDatabase() {
    abstract fun employeeDAO(): IEmployeeDAO

    companion object {
        // singleton pattern for db
        // keep a reference to the currently initialized db so we don't repeatedly instantiate the db (expensive in time)
        @Volatile
        private var INSTANCE: EmployeeDB? = null

        fun getInstance(context: Context): EmployeeDB {
            // multiple threads can ask for data at once. only one thread may enter a synchronized block at a time
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        EmployeeDB::class.java,
                        "employee_db"
                    ).fallbackToDestructiveMigration().build()

                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}