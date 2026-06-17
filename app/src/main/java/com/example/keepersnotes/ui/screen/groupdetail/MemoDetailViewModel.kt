package com.example.keepersnotes.ui.screen.groupdetail

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.keepersnotes.data.local.entity.CalendarEventEntity
import com.example.keepersnotes.data.local.entity.KpMemoEntity
import com.example.keepersnotes.data.repository.CalendarEventRepository
import com.example.keepersnotes.data.repository.KpMemoRepository
import com.example.keepersnotes.util.NotificationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MemoDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val kpMemoRepository: KpMemoRepository,
    private val calendarEventRepository: CalendarEventRepository
) : ViewModel() {

    private val memoId: String = savedStateHandle.get<String>("memoId") ?: ""

    val memo: StateFlow<KpMemoEntity?> = kpMemoRepository.getMemoById(memoId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun updateMemo(updatedMemo: KpMemoEntity, context: Context? = null) {
        viewModelScope.launch {
            val oldMemo = memo.value
            kpMemoRepository.updateMemo(updatedMemo)

            // 处理通知调度
            if (context != null) {
                if (updatedMemo.isNotificationEnabled && updatedMemo.notificationTime != null && updatedMemo.notificationTime > System.currentTimeMillis()) {
                    NotificationHelper.scheduleNotification(
                        context = context,
                        notificationId = updatedMemo.notificationId.toLong(),
                        title = updatedMemo.title.ifBlank { "备忘录提醒" },
                        content = updatedMemo.content.take(100),
                        triggerTime = updatedMemo.notificationTime
                    )
                } else {
                    NotificationHelper.cancelNotification(context, updatedMemo.notificationId.toLong())
                }
            }

            // 同步日历日程：先删除旧的提醒日程，再创建新的
            val reminderChanged = oldMemo != null && (
                oldMemo.notificationTime != updatedMemo.notificationTime ||
                oldMemo.isNotificationEnabled != updatedMemo.isNotificationEnabled ||
                oldMemo.title != updatedMemo.title
            )
            if (reminderChanged || (oldMemo == null && updatedMemo.isNotificationEnabled)) {
                // 删除该备忘录对应的旧日历日程
                calendarEventRepository.getEventsByGroupId(updatedMemo.groupId).firstOrNull()?.let { events ->
                    events.filter { it.type == "memo_reminder" && it.title.contains(oldMemo?.title ?: "") }
                        .forEach { calendarEventRepository.deleteById(it.eventId) }
                }
                // 创建新日历日程
                if (updatedMemo.isNotificationEnabled && updatedMemo.notificationTime != null) {
                    val cal = java.util.Calendar.getInstance().apply { timeInMillis = updatedMemo.notificationTime }
                    val dateOnly = java.util.Calendar.getInstance().apply {
                        set(cal.get(java.util.Calendar.YEAR), cal.get(java.util.Calendar.MONTH), cal.get(java.util.Calendar.DAY_OF_MONTH), 0, 0, 0)
                        set(java.util.Calendar.MILLISECOND, 0)
                    }.timeInMillis
                    val timeStr = "${cal.get(java.util.Calendar.HOUR_OF_DAY).toString().padStart(2, '0')}:${cal.get(java.util.Calendar.MINUTE).toString().padStart(2, '0')}"
                    calendarEventRepository.create(
                        groupId = updatedMemo.groupId,
                        title = "⏰ ${updatedMemo.title.ifBlank { "备忘录提醒" }}",
                        date = dateOnly,
                        time = timeStr,
                        type = "memo_reminder"
                    )
                }
            }
        }
    }

    fun toggleCompleted() {
        viewModelScope.launch { kpMemoRepository.toggleCompleted(memoId) }
    }

    fun deleteMemo(context: Context? = null, onDeleted: () -> Unit) {
        viewModelScope.launch {
            val currentMemo = memo.value
            // 取消通知
            if (context != null && currentMemo != null && currentMemo.isNotificationEnabled) {
                NotificationHelper.cancelNotification(context, currentMemo.notificationId.toLong())
            }
            // 删除对应的日历日程
            if (currentMemo != null && currentMemo.isNotificationEnabled) {
                calendarEventRepository.getEventsByGroupId(currentMemo.groupId).firstOrNull()?.let { events ->
                    events.filter { it.type == "memo_reminder" && it.title.contains(currentMemo.title) }
                        .forEach { calendarEventRepository.deleteById(it.eventId) }
                }
            }
            kpMemoRepository.deleteMemo(memoId)
            onDeleted()
        }
    }
}
