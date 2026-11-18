package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.CardElevated
import com.example.ui.theme.CardSurface
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Calendar
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimePickerFlow(
    initialMillis: Long,
    onDismiss: () -> Unit,
    onDateTimeConfirmed: (Long) -> Unit
) {
    val localCal = remember(initialMillis) {
        Calendar.getInstance().apply { timeInMillis = initialMillis }
    }

    val initialUtcDateMillis = remember(initialMillis) {
        Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            clear()
            set(
                localCal.get(Calendar.YEAR),
                localCal.get(Calendar.MONTH),
                localCal.get(Calendar.DAY_OF_MONTH)
            )
        }.timeInMillis
    }

    var showTimePicker by remember { mutableStateOf(false) }
    var pickedUtcDateMillis by remember { mutableStateOf(initialUtcDateMillis) }

    if (!showTimePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = pickedUtcDateMillis
        )
        DatePickerDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                Button(
                    onClick = {
                        pickedUtcDateMillis = datePickerState.selectedDateMillis ?: initialUtcDateMillis
                        showTimePicker = true
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricBlue,
                        contentColor = TextPrimary
                    ),
                    modifier = Modifier.testTag("date_picker_next_button")
                ) {
                    Text(stringResource(R.string.picker_next))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("date_picker_cancel_button")
                ) {
                    Text(stringResource(R.string.picker_cancel), color = TextSecondary)
                }
            },
            colors = DatePickerDefaults.colors(containerColor = CardSurface)
        ) {
            DatePicker(
                state = datePickerState,
                title = {
                    Text(
                        text = stringResource(R.string.date_picker_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        modifier = Modifier.padding(start = 24.dp, top = 16.dp)
                    )
                },
                colors = DatePickerDefaults.colors(
                    containerColor = CardSurface,
                    titleContentColor = TextPrimary,
                    headlineContentColor = TextPrimary,
                    weekdayContentColor = TextSecondary,
                    subheadContentColor = TextSecondary,
                    dayContentColor = TextPrimary,
                    selectedDayContainerColor = ElectricBlue,
                    selectedDayContentColor = TextPrimary,
                    todayDateBorderColor = ElectricBlue,
                    todayContentColor = TextPrimary
                )
            )
        }
    } else {
        val timePickerState = rememberTimePickerState(
            initialHour = localCal.get(Calendar.HOUR_OF_DAY),
            initialMinute = localCal.get(Calendar.MINUTE),
            is24Hour = false
        )
        AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = CardSurface,
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = stringResource(R.string.time_picker_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TimePicker(
                        state = timePickerState,
                        colors = TimePickerDefaults.colors(
                            clockDialColor = CardElevated,
                            selectorColor = ElectricBlue,
                            containerColor = CardSurface,
                            periodSelectorSelectedContainerColor = ElectricBlue,
                            periodSelectorSelectedContentColor = TextPrimary,
                            periodSelectorUnselectedContainerColor = CardElevated,
                            periodSelectorUnselectedContentColor = TextSecondary,
                            timeSelectorSelectedContainerColor = ElectricBlue,
                            timeSelectorSelectedContentColor = TextPrimary,
                            timeSelectorUnselectedContainerColor = CardElevated,
                            timeSelectorUnselectedContentColor = TextSecondary,
                            clockDialSelectedContentColor = TextPrimary,
                            clockDialUnselectedContentColor = TextPrimary
                        )
                    )
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { showTimePicker = false }) {
                        Text("Back", color = TextSecondary)
                    }
                    Button(
                        onClick = {
                            val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                                timeInMillis = pickedUtcDateMillis
                            }
                            val resultCal = Calendar.getInstance().apply {
                                set(Calendar.YEAR, utcCal.get(Calendar.YEAR))
                                set(Calendar.MONTH, utcCal.get(Calendar.MONTH))
                                set(Calendar.DAY_OF_MONTH, utcCal.get(Calendar.DAY_OF_MONTH))
                                set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                                set(Calendar.MINUTE, timePickerState.minute)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }
                            onDateTimeConfirmed(resultCal.timeInMillis)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricBlue,
                            contentColor = TextPrimary
                        ),
                        modifier = Modifier.testTag("time_picker_confirm_button")
                    ) {
                        Text(stringResource(R.string.picker_confirm))
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("time_picker_cancel_button")
                ) {
                    Text(stringResource(R.string.picker_cancel), color = TextSecondary)
                }
            }
        )
    }
}
