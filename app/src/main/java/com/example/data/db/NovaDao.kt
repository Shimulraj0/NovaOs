package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface NovaDao {
    // Command Logs
    @Query("SELECT * FROM command_logs ORDER BY timestamp DESC LIMIT 100")
    fun getAllLogs(): Flow<List<CommandLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: CommandLogEntity): Long

    @Query("UPDATE command_logs SET status = :status WHERE id = :logId")
    suspend fun updateLogStatus(logId: Long, status: String)

    @Query("DELETE FROM command_logs")
    suspend fun clearAllLogs()

    // Routines
    @Query("SELECT * FROM routines ORDER BY isSystemPreset DESC, id ASC")
    fun getAllRoutines(): Flow<List<RoutineEntity>>

    @Query("SELECT * FROM routines WHERE id = :id LIMIT 1")
    suspend fun getRoutineById(id: Long): RoutineEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: RoutineEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRoutines(routines: List<RoutineEntity>)

    @Update
    suspend fun updateRoutine(routine: RoutineEntity)

    @Query("DELETE FROM routines WHERE id = :id AND isSystemPreset = 0")
    suspend fun deleteRoutineById(id: Long)
}
