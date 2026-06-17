package com.example.keepersnotes.ui.screen.home

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.keepersnotes.data.local.entity.CalendarEventEntity
import com.example.keepersnotes.data.local.entity.GroupEntity
import com.example.keepersnotes.ui.component.CalendarEventList
import com.example.keepersnotes.ui.component.CalendarView
import com.example.keepersnotes.ui.component.CompactTopBar
import com.example.keepersnotes.ui.component.GroupCard
import com.example.keepersnotes.ui.component.StatsCardRow
import com.example.keepersnotes.util.Constants
import com.example.keepersnotes.util.LocalizedStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onGroupClick: (String) -> Unit,
    onCreateGroup: () -> Unit,
    onNavigateToCreatePc: (String) -> Unit,
    onNavigateToSearch: () -> Unit = {},
    onNavigateToModuleLibrary: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current

    // Update check
    var updateInfo by remember { mutableStateOf<com.example.keepersnotes.data.remote.UpdateInfo?>(null) }
    LaunchedEffect(Unit) {
        val checker = com.example.keepersnotes.data.remote.UpdateChecker()
        updateInfo = checker.checkForUpdate(context)
    }

    // Import module dialog state
    var showImportDialog by remember { mutableStateOf(false) }
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf("") }

    // Group selector dialog state
    var showGroupSelector by remember { mutableStateOf(false) }

    // Event detail dialog state
    var selectedEvent by remember { mutableStateOf<CalendarEventEntity?>(null) }

    // Snackbar
    val snackbarHostState = remember { SnackbarHostState() }

    // File picker launcher — supports txt, docx, zip, rar, 7z
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            val fileName = getFileName(context, it)
            val ext = fileName.substringAfterLast('.', "").lowercase()
            if (ext in listOf("zip", "rar", "7z")) {
                viewModel.importZipFromUri(it)
            } else {
                selectedFileUri = it
                selectedFileName = fileName.substringBeforeLast(".")
                showImportDialog = true
            }
        }
    }

    // Handle import result
    LaunchedEffect(uiState.importResult) {
        uiState.importResult?.let { result ->
            showImportDialog = false
            selectedFileUri = null
            viewModel.clearImportResult()
            when (result) {
                is ImportResult.Success -> {
                    onNavigateToModuleLibrary()
                }
                is ImportResult.Error -> {
                    snackbarHostState.showSnackbar("${LocalizedStrings.homeImportFail}: ${result.message}")
                }
            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CompactTopBar(
                title = LocalizedStrings.homeTitle,
                actions = {
                    IconButton(onClick = onNavigateToSearch) {
                        Icon(Icons.Default.Search, contentDescription = LocalizedStrings.search)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                Text(LocalizedStrings.homeQuickActions, style = MaterialTheme.typography.titleMedium)
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickActionButton(
                        icon = Icons.Default.Add,
                        label = LocalizedStrings.homeNewGroup,
                        onClick = onCreateGroup,
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionButton(
                        icon = Icons.Default.FileUpload,
                        label = LocalizedStrings.homeImportModule,
                        onClick = {
                            filePickerLauncher.launch(arrayOf("*/*"))
                        },
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionButton(
                        icon = Icons.Default.PersonAdd,
                        label = LocalizedStrings.homeAddPc,
                        onClick = {
                            if (uiState.activeGroups.size == 1) {
                                onNavigateToCreatePc(uiState.activeGroups.first().groupId)
                            } else {
                                showGroupSelector = true
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Text(LocalizedStrings.homeDataOverview, style = MaterialTheme.typography.titleMedium)
            }
            item {
                StatsCardRow(
                    completedGroupCount = uiState.completedGroupCount,
                    activeGroupCount = uiState.activeGroupCount,
                    upcomingCount = uiState.todayEvents.size,
                    weeklySessionCount = uiState.weeklySessionCount
                )
            }

            item {
                Text(LocalizedStrings.homeCalendar, style = MaterialTheme.typography.titleMedium)
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        CalendarView(
                            events = uiState.calendarEvents,
                            selectedDate = uiState.selectedDate,
                            onDateSelected = { viewModel.selectDate(it) }
                        )

                        if (uiState.selectedDate != null && uiState.selectedDateEvents.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = LocalizedStrings.homeDaySchedule,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            CalendarEventList(
                                events = uiState.selectedDateEvents,
                                onEventClick = { event ->
                                    selectedEvent = event
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Import module dialog
    if (showImportDialog) {
        ImportModuleDialog(
            initialTitle = selectedFileName,
            onDismiss = {
                showImportDialog = false
                selectedFileUri = null
            },
            onConfirm = { title, author, system ->
                selectedFileUri?.let { uri ->
                    viewModel.importModuleFromUri(uri, title, author, system)
                }
            }
        )
    }

    // Group selector dialog
    if (showGroupSelector) {
        GroupSelectorDialog(
            groups = uiState.activeGroups,
            onDismiss = { showGroupSelector = false },
            onGroupSelected = { groupId ->
                showGroupSelector = false
                onNavigateToCreatePc(groupId)
            }
        )
    }

    // Update dialog
    updateInfo?.let { info ->
        AlertDialog(
            onDismissRequest = { updateInfo = null },
            title = { Text("${LocalizedStrings.homeNewVersion} ${info.versionName}") },
            text = {
                Column {
                    Text(LocalizedStrings.homeUpdateContent)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        info.changelog,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val checker = com.example.keepersnotes.data.remote.UpdateChecker()
                    checker.openDownloadPage(context, info.downloadUrl)
                    updateInfo = null
                }) {
                    Text(LocalizedStrings.homeDownload)
                }
            },
            dismissButton = {
                TextButton(onClick = { updateInfo = null }) {
                    Text(LocalizedStrings.homeLater)
                }
            }
        )
    }

    // Import loading dialog
    if (uiState.isImporting) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text(LocalizedStrings.importing) },
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Text(LocalizedStrings.importing)
                }
            },
            confirmButton = {}
        )
    }

    // Event detail dialog
    selectedEvent?.let { event ->
        EventDetailDialog(
            event = event,
            onDismiss = { selectedEvent = null },
            onSave = { updatedEvent ->
                viewModel.updateEvent(updatedEvent)
                selectedEvent = null
            },
            onDelete = { eventId ->
                viewModel.deleteEvent(eventId)
                selectedEvent = null
            }
        )
    }
}

@Composable
private fun ImportModuleDialog(
    initialTitle: String = "",
    onDismiss: () -> Unit,
    onConfirm: (title: String, author: String, system: String) -> Unit
) {
    var title by remember { mutableStateOf(initialTitle) }
    var author by remember { mutableStateOf("") }
    var system by remember { mutableStateOf(Constants.SYSTEM_COC7) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(LocalizedStrings.homeImportDialogTitle) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(LocalizedStrings.homeModuleName) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = author,
                    onValueChange = { author = it },
                    label = { Text(LocalizedStrings.homeAuthor) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Text(LocalizedStrings.homeGameSystem, style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(Constants.SYSTEM_COC7, Constants.SYSTEM_DND5E).forEach { s ->
                        FilterChip(
                            selected = system == s,
                            onClick = { system = s },
                            label = { Text(s) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(title, author, system) }) {
                Text(LocalizedStrings.homeImport)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(LocalizedStrings.cancel)
            }
        }
    )
}

@Composable
private fun GroupSelectorDialog(
    groups: List<GroupEntity>,
    onDismiss: () -> Unit,
    onGroupSelected: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(LocalizedStrings.homeSelectGroup) },
        text = {
            if (groups.isEmpty()) {
                Text(LocalizedStrings.homeNoGroupPrompt)
            } else {
                Column {
                    groups.forEach { group ->
                        ListItem(
                            headlineContent = { Text(group.groupName) },
                            supportingContent = {
                                if (group.moduleName.isNotBlank()) {
                                    Text(group.moduleName)
                                }
                            },
                            modifier = Modifier.clickable { onGroupSelected(group.groupId) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(LocalizedStrings.cancel)
            }
        }
    )
}

@Composable
private fun QuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        onClick = onClick,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, contentDescription = null)
            Text(label, style = MaterialTheme.typography.labelSmall)
        }
    }
}

private fun getFileName(context: android.content.Context, uri: Uri): String {
    var fileName = ""
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
        if (cursor.moveToFirst() && nameIndex >= 0) {
            fileName = cursor.getString(nameIndex) ?: ""
        }
    }
    return fileName
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventDetailDialog(
    event: CalendarEventEntity,
    onDismiss: () -> Unit,
    onSave: (CalendarEventEntity) -> Unit,
    onDelete: (String) -> Unit
) {
    var title by remember { mutableStateOf(event.title) }
    var time by remember { mutableStateOf(event.time ?: "") }
    var isRemindEnabled by remember { mutableStateOf(event.isRemindEnabled) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    // Time picker state
    var showTimePicker by remember { mutableStateOf(false) }
    val timeParts = time.split(":")
    val initialHour = timeParts.getOrNull(0)?.toIntOrNull() ?: 14
    val initialMinute = timeParts.getOrNull(1)?.toIntOrNull() ?: 0
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("日程详情") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("日程名称") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                val timeInteractionSource = remember { MutableInteractionSource() }
                LaunchedEffect(timeInteractionSource) {
                    timeInteractionSource.interactions.collect { interaction ->
                        if (interaction is PressInteraction.Release) showTimePicker = true
                    }
                }
                OutlinedTextField(
                    value = time.ifBlank { "未设置" },
                    onValueChange = {},
                    label = { Text("时间") },
                    readOnly = true,
                    interactionSource = timeInteractionSource,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("提醒", style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = isRemindEnabled,
                        onCheckedChange = { isRemindEnabled = it }
                    )
                }

                // Type display
                val typeLabel = when (event.type) {
                    "session_start" -> "开团日"
                    "session_end" -> "预计结束"
                    "session" -> "开团中"
                    "memo_reminder" -> "备忘录提醒"
                    else -> "自定义日程"
                }
                Text(
                    text = "类型：$typeLabel",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val updatedTime = time.ifBlank { null }
                onSave(event.copy(title = title, time = updatedTime, isRemindEnabled = isRemindEnabled))
            }) {
                Text("保存")
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = { showDeleteConfirm = true }) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
                Spacer(modifier = Modifier.weight(1f))
                TextButton(onClick = onDismiss) {
                    Text("取消")
                }
            }
        }
    )

    // Time picker dialog
    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("选择时间") },
            text = {
                TimePicker(state = timePickerState)
            },
            confirmButton = {
                TextButton(onClick = {
                    time = "${timePickerState.hour.toString().padStart(2, '0')}:${timePickerState.minute.toString().padStart(2, '0')}"
                    showTimePicker = false
                }) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("取消")
                }
            }
        )
    }

    // Delete confirmation
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("确认删除") },
            text = { Text("确定要删除这个日程吗？") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(event.eventId)
                    showDeleteConfirm = false
                }) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("取消")
                }
            }
        )
    }
}
