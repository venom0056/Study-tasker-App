package com.example.data

import kotlinx.coroutines.flow.Flow

class StudyTaskRepository(private val dao: StudyTaskDao) {
    val allTasks: Flow<List<StudyTask>> = dao.getAllTasks()

    suspend fun insertTask(task: StudyTask): Long = dao.insertTask(task)

    suspend fun getTaskById(id: Long): StudyTask? = dao.getTaskById(id)

    suspend fun setCompleted(id: Long, isCompleted: Boolean) {
        dao.updateCompleted(id, isCompleted)
    }

    suspend fun markReminderFired(id: Long, fired: Boolean = true) {
        dao.updateReminderFired(id, fired)
    }

    suspend fun deleteTaskById(id: Long) {
        dao.deleteTaskById(id)
    }
}
