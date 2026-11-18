package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "study_tasks")
data class StudyTask(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val subject: String,
    val dueTimeMillis: Long,
    val isCompleted: Boolean = false,
    val reminderFired: Boolean = false,
    val createdAtMillis: Long = System.currentTimeMillis()
)
