package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "routines")
data class RoutineEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val triggerPhrase: String,
    val description: String,
    val icon: String,
    val taskTypesJson: String, // Comma-separated or serialized task descriptors
    val isSystemPreset: Boolean = false,
    val executionCount: Int = 0
)
