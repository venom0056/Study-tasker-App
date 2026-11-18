package com.example.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(ReminderScheduler.EXTRA_TASK_ID, -1L)
        if (taskId <= 0L) return

        val pendingResult = goAsync()
        val appContext = context.applicationContext
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dao = AppDatabase.getInstance(appContext).studyTaskDao()
                val task = dao.getTaskById(taskId)
                if (task != null && !task.isCompleted && !task.reminderFired) {
                    dao.updateReminderFired(taskId, true)
                    ReminderNotificationHelper.showReminderNotification(
                        context = appContext,
                        taskId = task.id,
                        title = task.title,
                        subject = task.subject
                    )
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
