package com.example.roomdemo

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface IEmployeeDAO {
    @Insert
    suspend fun insert(employeeEntity: EmployeeNTT)

    @Update
    suspend fun update(employeeEntity: EmployeeNTT)

    @Delete
    suspend fun delete(employeeEntity: EmployeeNTT)

    @Query("SELECT * FROM `employee`")
    fun fetchAll(): Flow<List<EmployeeNTT>>

    @Query("SELECT * FROM `employee` WHERE  uid=:uid")
    fun fetchEmployeeByID(uid: Int): Flow<EmployeeNTT>
}