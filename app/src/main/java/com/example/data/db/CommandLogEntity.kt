package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "command_logs")
data class CommandLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val prompt: String,
    val taskType: String,
    val summary: String,
    val status: String, // "SUCCESS", "CONFIRMATION_REQUIRED", "CANCELLED", "FAILED"
    val timestamp: Long = System.currentTimeMillis(),
    val latencyMs: Long = 0,
    val reasoning: String = ""
)
