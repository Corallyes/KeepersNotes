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
import com.example.keepersnotes.util.LocalizedStrings

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
                    snackbarHostState.showSnackbar("${result.moduleTitle} ${LocalizedStrings.homeImportSuccess}")
                }
                is ImportResult.Error -> {
                    snackbarHostState.showSnackbar("${LocalizedStrings.homeImportFail}: ${result.message}")
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
                title = LocalizedStrings.homeTitle,
                actions = {
                    IconButton(onClick = onNavigateToSearch) {
                        Icon(Icons.Default.Search, contentDescription = LocalizedStrings.search)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateGroup) {
                Icon(Icons.Default.Add, contentDescription = LocalizedStrings.homeNewGroup)
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
            item {
                Text(LocalizedStrings.homeDataOverview, style = MaterialTheme.typography.titleMedium)
            }
            item {
                StatsCardRow(
                    activeGroupCount = uiState.activeGroupCount,
                    totalPcCount = uiState.totalPcCount,
                    upcomingCount = uiState.upcomingGroups.size,
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
                                    onGroupClick(event.groupId)
                                }
                            )
                        }
                    }
                }
            }

            item {
                Text(LocalizedStrings.homeUpcoming, style = MaterialTheme.typography.titleMedium)
            }
            if (uiState.upcomingGroups.isEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = LocalizedStrings.homeNoUpcoming,
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
                Text(LocalizedStrings.homeActiveGroups, style = MaterialTheme.typography.titleMedium)
            }
            if (uiState.activeGroups.isEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = LocalizedStrings.homeNoGroups,
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
