package com.example.roomdemo

import android.app.Application

class EmployeeApp: Application() {
    // lazy means that it loads the variable when it is needed, not directly
    val db by lazy {
        EmployeeDB.getInstance(this)
    }
}