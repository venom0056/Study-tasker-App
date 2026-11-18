package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlarmOff
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.StudyTask
import com.example.data.Subject
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CardElevated
import com.example.ui.theme.CardSurface
import com.example.ui.theme.CrimsonBorder
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.CrimsonRedBg
import com.example.ui.theme.EmeraldDone
import com.example.ui.theme.MidnightBg
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ReminderAlertDialog(
    task: StudyTask,
    isAlarmPlaying: Boolean,
    onStopAlarm: () -> Unit,
    onMarkDone: () -> Unit,
    onDismiss: () -> Unit
) {
    val subject = Subject.fromLabel(task.subject)
    val accent = subjectAccentColor(subject)

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        ),
        containerColor = CardSurface,
        shape = RoundedCornerShape(24.dp),
        icon = {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(CircleShape)
                    .background(if (isAlarmPlaying) CrimsonRedBg else CardElevated),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isAlarmPlaying) {
                        Icons.Filled.NotificationsActive
                    } else {
                        Icons.Filled.VolumeOff
                    },
                    contentDescription = null,
                    tint = if (isAlarmPlaying) CrimsonRed else TextSecondary,
                    modifier = Modifier.size(30.dp)
                )
            }
        },
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = stringResource(R.string.reminder_alert_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary
                )
                Text(
                    text = if (isAlarmPlaying) {
                        stringResource(R.string.reminder_alarm_ringing)
                    } else {
                        stringResource(R.string.reminder_alarm_silenced)
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isAlarmPlaying) CrimsonBorder else TextSecondary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = CardElevated,
                    border = BorderStroke(
                        1.dp,
                        if (isAlarmPlaying) CrimsonRed.copy(alpha = 0.7f) else BorderSubtle
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = accent.copy(alpha = 0.16f)
                        ) {
                            Text(
                                text = subject.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = accent,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                        Text(
                            text = task.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Text(
                            text = "Due: ${formatDueDateTime(task.dueTimeMillis)}",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary
                        )
                    }
                }

                // Prominent "Stop Alarm" button to silence the loud looping ringtone
                if (isAlarmPlaying) {
                    Button(
                        onClick = onStopAlarm,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CrimsonRed,
                            contentColor = TextPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 52.dp)
                            .testTag("stop_alarm_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AlarmOff,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.reminder_alert_stop_alarm),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onMarkDone,
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldDone,
                    contentColor = MidnightBg
                ),
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("alert_mark_done_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.reminder_alert_mark_done),
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                border = BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("alert_dismiss_button")
            ) {
                Text(
                    text = stringResource(R.string.reminder_alert_dismiss),
                    color = TextSecondary
                )
            }
        }
    )
}
