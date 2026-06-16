package com.example.keepersnotes.ui.screen.home

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.example.keepersnotes.data.local.entity.GroupEntity
import com.example.keepersnotes.ui.component.CalendarEventList
import com.example.keepersnotes.ui.component.CalendarView
import com.example.keepersnotes.ui.component.CompactTopBar
import com.example.keepersnotes.ui.component.GroupCard
import com.example.keepersnotes.ui.component.StatsCardRow
import com.example.keepersnotes.util.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onGroupClick: (String) -> Unit,
    onCreateGroup: () -> Unit,
    onNavigateToCreatePc: (String) -> Unit,
    onNavigateToSearch: () -> Unit = {},
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

    // Group selector dialog state
    var showGroupSelector by remember { mutableStateOf(false) }

    // Snackbar
    val snackbarHostState = remember { SnackbarHostState() }

    // File picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            selectedFileUri = it
            showImportDialog = true
        }
    }

    // Handle import result
    LaunchedEffect(uiState.importResult) {
        uiState.importResult?.let { result ->
            showImportDialog = false
            selectedFileUri = null
            when (result) {
                is ImportResult.Success -> {
                    snackbarHostState.showSnackbar("模组「${result.moduleTitle}」导入成功")
                }
                is ImportResult.Error -> {
                    snackbarHostState.showSnackbar("导入失败: ${result.message}")
                }
            }
            viewModel.clearImportResult()
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CompactTopBar(
                title = "守密人笔记",
                actions = {
                    IconButton(onClick = onNavigateToSearch) {
                        Icon(Icons.Default.Search, contentDescription = "搜索")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateGroup) {
                Icon(Icons.Default.Add, contentDescription = "新建团")
            }
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
            // Stats overview cards
            item {
                Text("数据概览", style = MaterialTheme.typography.titleMedium)
            }
            item {
                StatsCardRow(
                    activeGroupCount = uiState.activeGroupCount,
                    totalPcCount = uiState.totalPcCount,
                    upcomingCount = uiState.upcomingGroups.size,
                    weeklySessionCount = uiState.weeklySessionCount
                )
            }

            // Calendar
            item {
                Text("日程日历", style = MaterialTheme.typography.titleMedium)
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        CalendarView(
                            events = uiState.calendarEvents,
                            selectedDate = uiState.selectedDate,
                            onDateSelected = { viewModel.selectDate(it) }
                        )

                        // 选中日期的事件列表
                        if (uiState.selectedDate != null && uiState.selectedDateEvents.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "当天日程",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            CalendarEventList(
                                events = uiState.selectedDateEvents,
                                onEventClick = { event ->
                                    onGroupClick(event.groupId)
                                }
                            )
                        }
                    }
                }
            }

            // Upcoming session plan
            item {
                Text("近期开团计划", style = MaterialTheme.typography.titleMedium)
            }
            if (uiState.upcomingGroups.isEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "暂无近期开团计划",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(uiState.upcomingGroups, key = { it.groupId }) { group ->
                    GroupCard(
                        group = group,
                        onClick = { onGroupClick(group.groupId) }
                    )
                }
            }

            // Quick actions
            item {
                Text("快速操作", style = MaterialTheme.typography.titleMedium)
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickActionButton(
                        icon = Icons.Default.Add,
                        label = "新建团",
                        onClick = onCreateGroup,
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionButton(
                        icon = Icons.Default.FileUpload,
                        label = "导入模组",
                        onClick = {
                            filePickerLauncher.launch(
                                arrayOf(
                                    "text/plain",
                                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                                )
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionButton(
                        icon = Icons.Default.PersonAdd,
                        label = "添加PC",
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

            // Active groups
            item {
                Text("进行中的团", style = MaterialTheme.typography.titleMedium)
            }
            if (uiState.activeGroups.isEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "还没有创建任何团，点击右下角 + 开始吧",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(uiState.activeGroups, key = { it.groupId }) { group ->
                    GroupCard(
                        group = group,
                        onClick = { onGroupClick(group.groupId) }
                    )
                }
            }
        }
    }

    // Import module dialog
    if (showImportDialog) {
        ImportModuleDialog(
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
            title = { Text("发现新版本 ${info.versionName}") },
            text = {
                Column {
                    Text("更新内容：")
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
                    Text("去下载")
                }
            },
            dismissButton = {
                TextButton(onClick = { updateInfo = null }) {
                    Text("稍后")
                }
            }
        )
    }
}

@Composable
private fun ImportModuleDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, author: String, system: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var system by remember { mutableStateOf(Constants.SYSTEM_COC7) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("导入模组") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("模组名称") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = author,
                    onValueChange = { author = it },
                    label = { Text("作者") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Text("游戏系统", style = MaterialTheme.typography.labelMedium)
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
                Text("导入")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
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
        title = { Text("选择团") },
        text = {
            if (groups.isEmpty()) {
                Text("还没有创建任何团，请先创建一个团")
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
                Text("取消")
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
