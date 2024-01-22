package com.example.workoutapp

import android.app.Application

class WorkoutApp: Application() {
    // lazy means that it loads the variable when it is needed, not directly
    val db by lazy {
        HistoryDB.getInstance(this)
    }
}