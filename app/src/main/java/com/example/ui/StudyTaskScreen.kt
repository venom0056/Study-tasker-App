package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.Subject
import com.example.ui.components.DateTimePickerFlow
import com.example.ui.components.ReminderAlertDialog
import com.example.ui.components.TaskCard
import com.example.ui.components.TaskFormCard
import com.example.ui.components.subjectAccentColor
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CardElevated
import com.example.ui.theme.CardSurface
import com.example.ui.theme.CrimsonBorder
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.CrimsonRedBg
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricBlueBright
import com.example.ui.theme.ElectricBlueGlow
import com.example.ui.theme.EmeraldDone
import com.example.ui.theme.EmeraldDoneBg
import com.example.ui.theme.MidnightBg
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun StudyTaskScreen(
    viewModel: StudyTaskViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDateTimePicker by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { /* Graceful: in-app beep + dialog always work regardless */ }
    )

    val handleAddTask = {
        val added = viewModel.addTask()
        if (added) {
            focusManager.clearFocus()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val granted = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
                if (!granted) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(DeepNavy, MidnightBg, MidnightBg)
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundBrush)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets.safeDrawing
        ) { innerPadding ->
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                val isExpanded = maxWidth >= 840.dp

                if (isExpanded) {
                    // Expanded Tablet / Wide Layout: Two-Pane
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(0.45f)
                                .fillMaxHeight()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            HeaderSection(
                                pendingCount = uiState.pendingCount,
                                overdueCount = uiState.overdueCount,
                                completedCount = uiState.completedCount
                            )
                            TaskFormCard(
                                taskInput = uiState.taskInput,
                                selectedSubject = uiState.selectedSubject,
                                selectedDueTimeMillis = uiState.selectedDueTimeMillis,
                                inputError = uiState.inputError,
                                onTaskInputChange = viewModel::onTaskInputChange,
                                onSubjectSelected = viewModel::onSubjectSelected,
                                onOpenDateTimePicker = { showDateTimePicker = true },
                                onAddTask = handleAddTask
                            )
                        }

                        Column(
                            modifier = Modifier
                                .weight(0.55f)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            SubjectFilterRow(
                                selectedFilter = uiState.selectedFilter,
                                onFilterSelected = viewModel::onFilterSelected
                            )
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .testTag("task_list"),
                                contentPadding = PaddingValues(bottom = 24.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                if (uiState.filteredTasks.isEmpty()) {
                                    item(key = "empty_state") {
                                        EmptyTasksCard(hasFilter = uiState.selectedFilter != null)
                                    }
                                } else {
                                    items(
                                        items = uiState.filteredTasks,
                                        key = { it.id }
                                    ) { task ->
                                        TaskCard(
                                            task = task,
                                            currentTimeMillis = uiState.currentTimeMillis,
                                            onToggleDone = { viewModel.toggleTaskDone(task) },
                                            onDelete = { viewModel.deleteTask(task) },
                                            modifier = Modifier.animateItem()
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Compact / Mobile Portrait Layout: Centered Single Scrollable Viewport
                    LazyColumn(
                        modifier = Modifier
                            .widthIn(max = 640.dp)
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                            .fillMaxHeight()
                            .testTag("task_list"),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 12.dp,
                            bottom = 28.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item(key = "header") {
                            HeaderSection(
                                pendingCount = uiState.pendingCount,
                                overdueCount = uiState.overdueCount,
                                completedCount = uiState.completedCount
                            )
                        }

                        item(key = "task_form") {
                            TaskFormCard(
                                taskInput = uiState.taskInput,
                                selectedSubject = uiState.selectedSubject,
                                selectedDueTimeMillis = uiState.selectedDueTimeMillis,
                                inputError = uiState.inputError,
                                onTaskInputChange = viewModel::onTaskInputChange,
                                onSubjectSelected = viewModel::onSubjectSelected,
                                onOpenDateTimePicker = { showDateTimePicker = true },
                                onAddTask = handleAddTask
                            )
                        }

                        item(key = "subject_filters") {
                            SubjectFilterRow(
                                selectedFilter = uiState.selectedFilter,
                                onFilterSelected = viewModel::onFilterSelected
                            )
                        }

                        if (uiState.filteredTasks.isEmpty()) {
                            item(key = "empty_state") {
                                EmptyTasksCard(hasFilter = uiState.selectedFilter != null)
                            }
                        } else {
                            items(
                                items = uiState.filteredTasks,
                                key = { it.id }
                            ) { task ->
                                TaskCard(
                                    task = task,
                                    currentTimeMillis = uiState.currentTimeMillis,
                                    onToggleDone = { viewModel.toggleTaskDone(task) },
                                    onDelete = { viewModel.deleteTask(task) },
                                    modifier = Modifier.animateItem()
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDateTimePicker) {
        DateTimePickerFlow(
            initialMillis = uiState.selectedDueTimeMillis,
            onDismiss = { showDateTimePicker = false },
            onDateTimeConfirmed = { chosenMillis ->
                viewModel.onDueTimeSelected(chosenMillis)
                showDateTimePicker = false
            }
        )
    }

    val alertTask = uiState.activeAlertTask
    if (alertTask != null) {
        ReminderAlertDialog(
            task = alertTask,
            isAlarmPlaying = uiState.isAlarmPlaying,
            onStopAlarm = viewModel::stopAlarm,
            onMarkDone = { viewModel.completeAlertTask(alertTask) },
            onDismiss = { viewModel.dismissActiveAlert() }
        )
    }
}

@Composable
private fun HeaderSection(
    pendingCount: Int,
    overdueCount: Int,
    completedCount: Int
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(ElectricBlueGlow),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.MenuBook,
                    contentDescription = null,
                    tint = ElectricBlueBright,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.main_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary,
                    modifier = Modifier.testTag("main_title")
                )
                Text(
                    text = stringResource(R.string.subtitle_tagline),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }

        // Live Task Summary Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatBadge(
                label = stringResource(R.string.stat_pending),
                count = pendingCount,
                accentColor = ElectricBlueBright,
                bgColor = ElectricBlueGlow,
                modifier = Modifier.weight(1f)
            )
            StatBadge(
                label = stringResource(R.string.stat_overdue),
                count = overdueCount,
                accentColor = if (overdueCount > 0) CrimsonBorder else TextSecondary,
                bgColor = if (overdueCount > 0) CrimsonRedBg else CardSurface,
                modifier = Modifier.weight(1f)
            )
            StatBadge(
                label = stringResource(R.string.stat_done),
                count = completedCount,
                accentColor = EmeraldDone,
                bgColor = EmeraldDoneBg,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatBadge(
    label: String,
    count: Int,
    accentColor: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = bgColor,
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
        }
    }
}

@Composable
private fun SubjectFilterRow(
    selectedFilter: Subject?,
    onFilterSelected: (Subject?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilterChip(
            selected = selectedFilter == null,
            onClick = { onFilterSelected(null) },
            label = { Text(stringResource(R.string.filter_all)) },
            colors = FilterChipDefaults.filterChipColors(
                containerColor = CardSurface,
                labelColor = TextSecondary,
                selectedContainerColor = ElectricBlue,
                selectedLabelColor = TextPrimary
            ),
            border = FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = selectedFilter == null,
                borderColor = BorderSubtle,
                selectedBorderColor = ElectricBlueBright
            ),
            modifier = Modifier.testTag("filter_all")
        )

        Subject.entries.forEach { subject ->
            val accent = subjectAccentColor(subject)
            val isSelected = selectedFilter == subject
            FilterChip(
                selected = isSelected,
                onClick = { onFilterSelected(if (isSelected) null else subject) },
                leadingIcon = {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(accent)
                    )
                },
                label = { Text(subject.label) },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = CardSurface,
                    labelColor = TextSecondary,
                    selectedContainerColor = accent.copy(alpha = 0.22f),
                    selectedLabelColor = TextPrimary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = BorderSubtle,
                    selectedBorderColor = accent
                ),
                modifier = Modifier.testTag("filter_${subject.label.lowercase()}")
            )
        }
    }
}

@Composable
private fun EmptyTasksCard(hasFilter: Boolean) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("empty_tasks_card"),
        shape = RoundedCornerShape(20.dp),
        color = CardSurface,
        border = BorderStroke(1.dp, BorderSubtle)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(CardElevated),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.TaskAlt,
                    contentDescription = null,
                    tint = ElectricBlueBright,
                    modifier = Modifier.size(30.dp)
                )
            }
            Text(
                text = if (hasFilter) "No tasks in this subject" else stringResource(R.string.empty_title),
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
            Text(
                text = if (hasFilter) {
                    "Switch back to All or add a new task for this subject above."
                } else {
                    stringResource(R.string.empty_subtitle)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted,
                textAlign = TextAlign.Center
            )
        }
    }
}
