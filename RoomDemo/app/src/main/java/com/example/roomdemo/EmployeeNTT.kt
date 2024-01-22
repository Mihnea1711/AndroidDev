package com.example.roomdemo

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "employee")
data class EmployeeNTT(
    @PrimaryKey(autoGenerate = true)
    val uid: Int = 0,
    val name: String = "",
//    @ColumnInfo(name = "email-id")  <- sets the column name internally inside the db
//    (email will be the field in the EmployeeNTT, but email-id will be the column name)
    val email: String = ""
)
