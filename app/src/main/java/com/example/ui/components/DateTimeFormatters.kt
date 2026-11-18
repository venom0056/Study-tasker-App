package com.example.ui.components

import androidx.compose.ui.graphics.Color
import com.example.data.Subject
import com.example.ui.theme.ChemistryViolet
import com.example.ui.theme.MathsAmber
import com.example.ui.theme.OtherTeal
import com.example.ui.theme.PhysicsCyan
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

fun formatDueDateTime(millis: Long): String {
    val formatter = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
    return formatter.format(Date(millis))
}

fun formatRelativeDue(dueMillis: Long, nowMillis: Long, isCompleted: Boolean): String {
    if (isCompleted) return "Completed"
    val diff = dueMillis - nowMillis
    val absSec = abs(diff) / 1000L
    val mins = absSec / 60L
    val hours = mins / 60L
    val days = hours / 24L

    val durationText = when {
        absSec < 60L -> "${absSec}s"
        mins < 60L -> "${mins}m"
        hours < 24L -> {
            val remMins = mins % 60L
            if (remMins == 0L) "${hours}h" else "${hours}h ${remMins}m"
        }
        else -> {
            val remHours = hours % 24L
            if (remHours == 0L) "${days}d" else "${days}d ${remHours}h"
        }
    }

    return if (diff <= 0L) {
        if (absSec < 5L) "Due now" else "Overdue by $durationText"
    } else {
        "Due in $durationText"
    }
}

fun subjectAccentColor(subject: Subject): Color {
    return when (subject) {
        Subject.PHYSICS -> PhysicsCyan
        Subject.CHEMISTRY -> ChemistryViolet
        Subject.MATHS -> MathsAmber
        Subject.OTHER -> OtherTeal
    }
}
