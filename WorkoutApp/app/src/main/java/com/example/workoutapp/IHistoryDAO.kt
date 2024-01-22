package com.example.workoutapp

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface IHistoryDAO {
    @Insert
    suspend fun insert(historyNTT: HistoryNTT)

    @Query("SELECT * FROM `history`")
    fun fetchHistory(): Flow<List<HistoryNTT>>
}