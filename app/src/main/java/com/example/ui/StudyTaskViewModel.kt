package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.StudyTask
import com.example.data.StudyTaskRepository
import com.example.data.Subject
import com.example.reminder.ReminderNotificationHelper
import com.example.reminder.ReminderScheduler
import com.example.reminder.SoundAlertPlayer
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Calendar

data class StudyTaskUiState(
    val allTasks: List<StudyTask> = emptyList(),
    val filteredTasks: List<StudyTask> = emptyList(),
    val taskInput: String = "",
    val selectedSubject: Subject = Subject.PHYSICS,
    val selectedDueTimeMillis: Long = defaultDueTimeMillis(),
    val selectedFilter: Subject? = null,
    val inputError: String? = null,
    val currentTimeMillis: Long = System.currentTimeMillis(),
    val activeAlertTask: StudyTask? = null,
    val isAlarmPlaying: Boolean = false,
    val pendingCount: Int = 0,
    val overdueCount: Int = 0,
    val completedCount: Int = 0
)

private fun defaultDueTimeMillis(): Long {
    val cal = Calendar.getInstance().apply {
        add(Calendar.MINUTE, 15)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return cal.timeInMillis
}

class StudyTaskViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: StudyTaskRepository by lazy {
        val dao = AppDatabase.getInstance(application).studyTaskDao()
        StudyTaskRepository(dao)
    }

    private val soundAlertPlayer = SoundAlertPlayer(application)

    private val _taskInput = MutableStateFlow("")
    private val _selectedSubject = MutableStateFlow(Subject.PHYSICS)
    private val _selectedDueTimeMillis = MutableStateFlow(defaultDueTimeMillis())
    private val _selectedFilter = MutableStateFlow<Subject?>(null)
    private val _inputError = MutableStateFlow<String?>(null)
    private val _currentTimeMillis = MutableStateFlow(System.currentTimeMillis())
    private val _activeAlertTask = MutableStateFlow<StudyTask?>(null)
    private val _isAlarmPlaying = MutableStateFlow(false)

    private val pendingAlertQueue = ArrayDeque<StudyTask>()
    private val triggeredIdsInSession = mutableSetOf<Long>()

    private data class FormState(
        val taskInput: String,
        val selectedSubject: Subject,
        val selectedDueTimeMillis: Long,
        val selectedFilter: Subject?,
        val inputError: String?
    )

    private data class AlertState(
        val activeAlertTask: StudyTask?,
        val isAlarmPlaying: Boolean
    )

    private val formStateFlow = combine(
        _taskInput,
        _selectedSubject,
        _selectedDueTimeMillis,
        _selectedFilter,
        _inputError
    ) { input, subject, dueMillis, filter, error ->
        FormState(
            taskInput = input,
            selectedSubject = subject,
            selectedDueTimeMillis = dueMillis,
            selectedFilter = filter,
            inputError = error
        )
    }

    private val alertStateFlow = combine(
        _activeAlertTask,
        _isAlarmPlaying
    ) { task, isPlaying ->
        AlertState(activeAlertTask = task, isAlarmPlaying = isPlaying)
    }

    val uiState: StateFlow<StudyTaskUiState> = combine(
        repository.allTasks,
        formStateFlow,
        _currentTimeMillis,
        alertStateFlow
    ) { tasks, form, now, alertState ->
        val filtered = if (form.selectedFilter == null) {
            tasks
        } else {
            tasks.filter { it.subject.equals(form.selectedFilter.label, ignoreCase = true) }
        }
        val overdue = tasks.count { !it.isCompleted && it.dueTimeMillis <= now }
        val completed = tasks.count { it.isCompleted }
        val pending = tasks.count { !it.isCompleted }

        StudyTaskUiState(
            allTasks = tasks,
            filteredTasks = filtered,
            taskInput = form.taskInput,
            selectedSubject = form.selectedSubject,
            selectedDueTimeMillis = form.selectedDueTimeMillis,
            selectedFilter = form.selectedFilter,
            inputError = form.inputError,
            currentTimeMillis = now,
            activeAlertTask = alertState.activeAlertTask,
            isAlarmPlaying = alertState.isAlarmPlaying,
            pendingCount = pending,
            overdueCount = overdue,
            completedCount = completed
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = StudyTaskUiState()
    )

    init {
        ReminderNotificationHelper.ensureChannel(getApplication())
        startClockAndReminderWatcher()
    }

    private fun startClockAndReminderWatcher() {
        viewModelScope.launch {
            while (isActive) {
                val now = System.currentTimeMillis()
                _currentTimeMillis.value = now
                checkDueReminders(now)
                delay(1000L)
            }
        }
    }

    private suspend fun checkDueReminders(now: Long) {
        val currentTasks = uiState.value.allTasks
        val newlyDue = currentTasks.filter { task ->
            !task.isCompleted &&
                !task.reminderFired &&
                task.dueTimeMillis <= now &&
                !triggeredIdsInSession.contains(task.id)
        }

        if (newlyDue.isEmpty()) return

        for (task in newlyDue) {
            triggeredIdsInSession.add(task.id)
            ReminderScheduler.cancelReminder(getApplication(), task.id)
            repository.markReminderFired(task.id, true)

            if (_activeAlertTask.value == null) {
                _activeAlertTask.value = task
            } else {
                pendingAlertQueue.addLast(task)
            }

            ReminderNotificationHelper.showReminderNotification(
                context = getApplication(),
                taskId = task.id,
                title = task.title,
                subject = task.subject
            )
        }

        // Start looping default alarm ringtone via RingtoneManager + MediaPlayer
        _isAlarmPlaying.value = true
        viewModelScope.launch {
            soundAlertPlayer.startAlarmLoop()
        }
    }

    fun onTaskInputChange(value: String) {
        _taskInput.value = value
        if (_inputError.value != null && value.isNotBlank()) {
            _inputError.value = null
        }
    }

    fun onSubjectSelected(subject: Subject) {
        _selectedSubject.value = subject
    }

    fun onDueTimeSelected(dueMillis: Long) {
        _selectedDueTimeMillis.value = dueMillis
    }

    fun onFilterSelected(subject: Subject?) {
        _selectedFilter.value = subject
    }

    fun addTask(): Boolean {
        val trimmed = _taskInput.value.trim()
        if (trimmed.isEmpty()) {
            _inputError.value = "Please enter a study task first."
            return false
        }

        val subject = _selectedSubject.value
        val dueMillis = _selectedDueTimeMillis.value
        _taskInput.value = ""
        _inputError.value = null

        viewModelScope.launch {
            val newTask = StudyTask(
                title = trimmed,
                subject = subject.label,
                dueTimeMillis = dueMillis,
                isCompleted = false,
                reminderFired = false
            )
            val id = repository.insertTask(newTask)
            if (dueMillis > System.currentTimeMillis()) {
                ReminderScheduler.scheduleReminder(
                    context = getApplication(),
                    taskId = id,
                    title = trimmed,
                    subject = subject.label,
                    dueTimeMillis = dueMillis
                )
            }
            if (_selectedDueTimeMillis.value <= System.currentTimeMillis()) {
                _selectedDueTimeMillis.value = defaultDueTimeMillis()
            }
        }
        return true
    }

    fun toggleTaskDone(task: StudyTask) {
        val newCompleted = !task.isCompleted
        viewModelScope.launch {
            repository.setCompleted(task.id, newCompleted)
            if (newCompleted) {
                ReminderScheduler.cancelReminder(getApplication(), task.id)
                if (_activeAlertTask.value?.id == task.id) {
                    dismissActiveAlert()
                }
            } else if (!task.reminderFired && task.dueTimeMillis > System.currentTimeMillis()) {
                ReminderScheduler.scheduleReminder(
                    context = getApplication(),
                    taskId = task.id,
                    title = task.title,
                    subject = task.subject,
                    dueTimeMillis = task.dueTimeMillis
                )
            }
        }
    }

    fun deleteTask(task: StudyTask) {
        viewModelScope.launch {
            ReminderScheduler.cancelReminder(getApplication(), task.id)
            repository.deleteTaskById(task.id)
            if (_activeAlertTask.value?.id == task.id) {
                dismissActiveAlert()
            }
        }
    }

    fun stopAlarm() {
        soundAlertPlayer.stopAlarm()
        _isAlarmPlaying.value = false
    }

    fun dismissActiveAlert() {
        stopAlarm()
        if (pendingAlertQueue.isNotEmpty()) {
            _activeAlertTask.value = pendingAlertQueue.removeFirst()
            _isAlarmPlaying.value = true
            viewModelScope.launch {
                soundAlertPlayer.startAlarmLoop()
            }
        } else {
            _activeAlertTask.value = null
        }
    }

    fun completeAlertTask(task: StudyTask) {
        viewModelScope.launch {
            repository.setCompleted(task.id, true)
            ReminderScheduler.cancelReminder(getApplication(), task.id)
            dismissActiveAlert()
        }
    }

    override fun onCleared() {
        soundAlertPlayer.stopAlarm()
        super.onCleared()
    }
}
