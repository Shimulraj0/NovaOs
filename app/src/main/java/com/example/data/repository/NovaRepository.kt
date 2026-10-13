package com.example.data.repository

import com.example.data.db.CommandLogEntity
import com.example.data.db.NovaDao
import com.example.data.db.RoutineEntity
import kotlinx.coroutines.flow.Flow

class NovaRepository(private val dao: NovaDao) {
    val allLogs: Flow<List<CommandLogEntity>> = dao.getAllLogs()
    val allRoutines: Flow<List<RoutineEntity>> = dao.getAllRoutines()

    suspend fun logCommand(
        prompt: String,
        taskType: String,
        summary: String,
        status: String,
        latencyMs: Long,
        reasoning: String
    ): Long {
        val entity = CommandLogEntity(
            prompt = prompt,
            taskType = taskType,
            summary = summary,
            status = status,
            latencyMs = latencyMs,
            reasoning = reasoning
        )
        return dao.insertLog(entity)
    }

    suspend fun updateLogStatus(logId: Long, status: String) {
        dao.updateLogStatus(logId, status)
    }

    suspend fun clearHistory() {
        dao.clearAllLogs()
    }

    suspend fun addRoutine(routine: RoutineEntity): Long {
        return dao.insertRoutine(routine)
    }

    suspend fun deleteRoutine(id: Long) {
        dao.deleteRoutineById(id)
    }

    suspend fun getRoutine(id: Long): RoutineEntity? {
        return dao.getRoutineById(id)
    }

    suspend fun incrementRoutineRun(routine: RoutineEntity) {
        dao.updateRoutine(routine.copy(executionCount = routine.executionCount + 1))
    }
}
