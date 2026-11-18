package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.StudyTask
import com.example.data.Subject
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CardSurface
import com.example.ui.theme.CrimsonBorder
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.CrimsonRedBg
import com.example.ui.theme.EmeraldDone
import com.example.ui.theme.EmeraldDoneBg
import com.example.ui.theme.MidnightBg
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun TaskCard(
    task: StudyTask,
    currentTimeMillis: Long,
    onToggleDone: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isOverdue = !task.isCompleted && task.dueTimeMillis <= currentTimeMillis
    val subject = Subject.fromLabel(task.subject)
    val subjectAccent = subjectAccentColor(subject)

    val borderStroke = when {
        isOverdue -> BorderStroke(2.dp, CrimsonRed)
        task.isCompleted -> BorderStroke(1.dp, EmeraldDone.copy(alpha = 0.35f))
        else -> BorderStroke(1.dp, BorderSubtle)
    }

    val containerColor = when {
        isOverdue -> ColorBlendCrimson
        else -> CardSurface
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("task_card_${task.id}"),
        shape = RoundedCornerShape(18.dp),
        color = containerColor,
        border = borderStroke,
        tonalElevation = if (isOverdue) 6.dp else 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .alpha(if (task.isCompleted) 0.65f else 1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Subject & Status Badges
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = subjectAccent.copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, subjectAccent.copy(alpha = 0.45f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(subjectAccent)
                            )
                            Text(
                                text = subject.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = subjectAccent
                            )
                        }
                    }

                    if (isOverdue) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = CrimsonRedBg,
                            border = BorderStroke(1.dp, CrimsonRed)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.WarningAmber,
                                    contentDescription = null,
                                    tint = CrimsonBorder,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = stringResource(R.string.badge_overdue),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = CrimsonBorder
                                )
                            }
                        }
                    } else if (task.isCompleted) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = EmeraldDoneBg,
                            border = BorderStroke(1.dp, EmeraldDone.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = stringResource(R.string.badge_completed),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldDone,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Task Title (crossed out when completed)
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        textDecoration = if (task.isCompleted) {
                            TextDecoration.LineThrough
                        } else {
                            TextDecoration.None
                        }
                    ),
                    color = if (task.isCompleted) TextMuted else TextPrimary,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                // Due Date/Time & Relative Countdown
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = if (isOverdue) CrimsonBorder else TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = formatDueDateTime(task.dueTimeMillis),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isOverdue) CrimsonBorder else TextSecondary
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                    Text(
                        text = formatRelativeDue(
                            dueMillis = task.dueTimeMillis,
                            nowMillis = currentTimeMillis,
                            isCompleted = task.isCompleted
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = when {
                            isOverdue -> CrimsonBorder
                            task.isCompleted -> EmeraldDone
                            else -> TextSecondary
                        }
                    )
                }
            }

            // Action Buttons: Green Done Checkmark & Red Delete
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledIconButton(
                    onClick = onToggleDone,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = if (task.isCompleted) EmeraldDone else EmeraldDoneBg,
                        contentColor = if (task.isCompleted) MidnightBg else EmeraldDone
                    ),
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("done_button_${task.id}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = if (task.isCompleted) {
                            stringResource(R.string.action_undo)
                        } else {
                            stringResource(R.string.action_done)
                        }
                    )
                }

                FilledIconButton(
                    onClick = onDelete,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = CrimsonRedBg,
                        contentColor = CrimsonRed
                    ),
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("delete_button_${task.id}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = stringResource(R.string.action_delete)
                    )
                }
            }
        }
    }
}

private val ColorBlendCrimson = androidx.compose.ui.graphics.Color(0xFF1D1320)
