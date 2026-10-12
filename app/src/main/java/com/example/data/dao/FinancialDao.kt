package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.FinancialEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface FinancialDao {
    @Query("SELECT * FROM financial_entries ORDER BY date DESC")
    fun getAllEntries(): Flow<List<FinancialEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: FinancialEntry): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<FinancialEntry>): List<Long>

    @Delete
    suspend fun deleteEntry(entry: FinancialEntry)

    @Query("DELETE FROM financial_entries WHERE orderId = :orderId")
    suspend fun deleteEntryByOrderId(orderId: Long)
}
