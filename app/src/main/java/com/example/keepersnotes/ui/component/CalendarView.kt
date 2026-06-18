package com.example.keepersnotes.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.keepersnotes.data.local.entity.CalendarEventEntity
import com.example.keepersnotes.util.LocalizedStrings
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CalendarView(
    events: List<CalendarEventEntity>,
    selectedDate: Long?,
    onDateSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentMonth by remember { mutableStateOf(Calendar.getInstance()) }
    val dateFormat = remember { SimpleDateFormat(LocalizedStrings.dateFormatYearMonth, Locale.getDefault()) }
    val dayFormat = remember { SimpleDateFormat("d", Locale.CHINA) }

    // 获取当月事件的日期集合
    val eventDates = remember(events, currentMonth) {
        val startOfMonth = (currentMonth.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val endOfMonth = (startOfMonth.clone() as Calendar).apply {
            add(Calendar.MONTH, 1)
            add(Calendar.MILLISECOND, -1)
        }
        events.filter { it.date in startOfMonth.timeInMillis..endOfMonth.timeInMillis }
            .map { event ->
                val cal = Calendar.getInstance().apply { timeInMillis = event.date }
                cal.get(Calendar.DAY_OF_MONTH)
            }
            .toSet()
    }

    Column(modifier = modifier) {
        // 月份导航
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                currentMonth = (currentMonth.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
            }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = LocalizedStrings.calendarPrevMonth)
            }
            Text(
                text = dateFormat.format(currentMonth.time),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = {
                currentMonth = (currentMonth.clone() as Calendar).apply { add(Calendar.MONTH, 1) }
            }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = LocalizedStrings.calendarNextMonth)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 星期标题
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf(
                LocalizedStrings.calendarDaySun,
                LocalizedStrings.calendarDayMon,
                LocalizedStrings.calendarDayTue,
                LocalizedStrings.calendarDayWed,
                LocalizedStrings.calendarDayThu,
                LocalizedStrings.calendarDayFri,
                LocalizedStrings.calendarDaySat
            ).forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 日期网格
        val cal = (currentMonth.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1 // 0=周日
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val today = Calendar.getInstance()

        // 填充前置空白
        val totalCells = firstDayOfWeek + daysInMonth
        val rows = (totalCells + 6) / 7

        for (row in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (col in 0..6) {
                    val index = row * 7 + col
                    if (index < firstDayOfWeek || index >= firstDayOfWeek + daysInMonth) {
                        // 空白
                        Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                    } else {
                        val day = index - firstDayOfWeek + 1
                        val dateCal = (currentMonth.clone() as Calendar).apply {
                            set(Calendar.DAY_OF_MONTH, day)
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        val dateMillis = dateCal.timeInMillis
                        val isToday = today.get(Calendar.YEAR) == dateCal.get(Calendar.YEAR) &&
                                today.get(Calendar.MONTH) == dateCal.get(Calendar.MONTH) &&
                                today.get(Calendar.DAY_OF_MONTH) == day
                        val hasEvent = day in eventDates
                        val isSelected = selectedDate == dateMillis

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isSelected -> MaterialTheme.colorScheme.primary
                                        isToday -> MaterialTheme.colorScheme.primaryContainer
                                        else -> Color.Transparent
                                    }
                                )
                                .clickable { onDateSelected(dateMillis) },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$day",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = when {
                                        isSelected -> MaterialTheme.colorScheme.onPrimary
                                        isToday -> MaterialTheme.colorScheme.onPrimaryContainer
                                        else -> MaterialTheme.colorScheme.onSurface
                                    }
                                )
                                if (hasEvent) {
                                    Box(
                                        modifier = Modifier
                                            .size(4.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.onPrimary
                                                else MaterialTheme.colorScheme.error
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarEventList(
    events: List<CalendarEventEntity>,
    onEventClick: (CalendarEventEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val timeFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.CHINA) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        events.forEach { event ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onEventClick(event) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 事件类型图标
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                when (event.type) {
                                    "session_start" -> Color(0xFF4CAF50)
                                    "session_end" -> Color(0xFFFF9800)
                                    "session" -> Color(0xFF2196F3)
                                    "memo_reminder" -> Color(0xFFE91E63)
                                    else -> Color(0xFF9C27B0)
                                }
                            )
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = event.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        if (event.time != null) {
                            Text(
                                text = event.time,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    // 提醒图标
                    if (event.isRemindEnabled) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = LocalizedStrings.calendarHasReminder,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
