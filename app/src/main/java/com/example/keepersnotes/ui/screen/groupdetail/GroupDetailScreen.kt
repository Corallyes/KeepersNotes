package com.example.keepersnotes.ui.screen.groupdetail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.keepersnotes.data.local.entity.GroupEntity
import com.example.keepersnotes.ui.component.CompactTopBar
import com.example.keepersnotes.ui.screen.groupdetail.tab.*
import com.example.keepersnotes.util.Constants
import com.example.keepersnotes.util.LocalizedStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailScreen(
    groupId: String,
    onBack: () -> Unit,
    onPcClick: (String) -> Unit,
    onNpcClick: (String) -> Unit,
    onSessionClick: (String) -> Unit,
    onMemoClick: (String) -> Unit = {},
    onCreatePc: () -> Unit = {},
    onCreateNpc: () -> Unit = {},
    onCreateSession: () -> Unit = {},
    onCreateMemo: () -> Unit = {},
    onNavigateToRelationship: () -> Unit = {},
    viewModel: GroupDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val tabs = listOf(
        LocalizedStrings.groupOverview,
        LocalizedStrings.groupCharacters,
        LocalizedStrings.groupKpMemo,
        LocalizedStrings.groupSessionRecord
    )
    var selectedTab by remember { mutableIntStateOf(viewModel.selectedTab) }
    var characterSubTab by remember { mutableIntStateOf(viewModel.characterSubTab) }
    var showMenu by remember { mutableStateOf(false) }
    var showEditSheet by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            CompactTopBar(
                title = uiState.group?.groupName ?: LocalizedStrings.groupDetailTitle,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = LocalizedStrings.back)
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = null)
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(LocalizedStrings.edit) },
                                onClick = { showMenu = false; showEditSheet = true }
                            )
                            DropdownMenuItem(
                                text = { Text(LocalizedStrings.delete, color = MaterialTheme.colorScheme.error) },
                                onClick = { showMenu = false; showDeleteDialog = true }
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 0.dp
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index; viewModel.selectedTab = index },
                        text = { Text(title, maxLines = 1) }
                    )
                }
            }

            when (selectedTab) {
                0 -> GroupOverviewTab(
                    uiState = uiState,
                    onStatusChange = viewModel::updateGroupStatus
                )
                1 -> {
                    val subTabs = listOf(
                        LocalizedStrings.groupPcLibrary,
                        LocalizedStrings.groupNpcArchive,
                        LocalizedStrings.groupRelationships
                    )
                    Column {
                        TabRow(selectedTabIndex = characterSubTab) {
                            subTabs.forEachIndexed { index, title ->
                                Tab(
                                    selected = characterSubTab == index,
                                    onClick = { characterSubTab = index; viewModel.characterSubTab = index },
                                    text = { Text(title, maxLines = 1) }
                                )
                            }
                        }
                        when (characterSubTab) {
                            0 -> PcLibraryTab(
                                pcs = uiState.pcs,
                                onPcClick = onPcClick,
                                onCreatePc = onCreatePc
                            )
                            1 -> NpcArchiveTab(
                                npcs = uiState.npcs,
                                onNpcClick = onNpcClick,
                                onCreateNpc = onCreateNpc
                            )
                            2 -> GroupRelationshipTab(
                                relationshipCount = uiState.relationships.size,
                                onNavigateToRelationship = onNavigateToRelationship
                            )
                        }
                    }
                }
                2 -> KpMemoTab(
                    memos = uiState.memos,
                    pendingTodos = uiState.pendingTodos,
                    onToggleCompleted = viewModel::toggleMemoCompleted,
                    onCreateMemo = onCreateMemo,
                    onMemoClick = onMemoClick,
                    filterIndex = viewModel.memoFilterIndex,
                    onFilterChanged = { viewModel.memoFilterIndex = it }
                )
                3 -> SessionRecordTab(
                    sessions = uiState.sessions,
                    onSessionClick = onSessionClick,
                    onCreateSession = onCreateSession
                )
            }
        }
    }

    // Edit group bottom sheet
    if (showEditSheet && uiState.group != null) {
        EditGroupSheet(
            group = uiState.group!!,
            onDismiss = { showEditSheet = false },
            onSave = { updatedGroup ->
                viewModel.updateGroup(updatedGroup)
                showEditSheet = false
            }
        )
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(LocalizedStrings.groupDeleteTitle) },
            text = { Text(LocalizedStrings.groupDeleteConfirmAll) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteGroup()
                    showDeleteDialog = false
                    onBack()
                }) {
                    Text(LocalizedStrings.delete, color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text(LocalizedStrings.cancel) }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditGroupSheet(
    group: GroupEntity,
    onDismiss: () -> Unit,
    onSave: (GroupEntity) -> Unit
) {
    var groupName by remember { mutableStateOf(group.groupName) }
    var moduleName by remember { mutableStateOf(group.moduleName) }
    var system by remember { mutableStateOf(group.system) }
    var gameFormat by remember { mutableStateOf(group.gameFormat) }
    var scale by remember { mutableStateOf(group.scale) }
    var notes by remember { mutableStateOf(group.notes) }
    var status by remember { mutableStateOf(group.status) }
    var startTime by remember { mutableStateOf(group.startTime) }
    var expectedEndTime by remember { mutableStateOf(group.expectedEndTime) }
    var defaultSessionTime by remember { mutableStateOf(group.defaultSessionTime) }

    val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // DatePicker states
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showTimeChangeConfirm by remember { mutableStateOf(false) }
    var pendingSaveGroup by remember { mutableStateOf<GroupEntity?>(null) }

    val timeChanged = group.startTime != startTime ||
            group.expectedEndTime != expectedEndTime ||
            group.defaultSessionTime != defaultSessionTime.trim()

    val startInteractionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val endInteractionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val timeInteractionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }

    LaunchedEffect(startInteractionSource) {
        startInteractionSource.interactions.collect { interaction ->
            if (interaction is androidx.compose.foundation.interaction.PressInteraction.Release) showStartDatePicker = true
        }
    }
    LaunchedEffect(endInteractionSource) {
        endInteractionSource.interactions.collect { interaction ->
            if (interaction is androidx.compose.foundation.interaction.PressInteraction.Release) showEndDatePicker = true
        }
    }
    LaunchedEffect(timeInteractionSource) {
        timeInteractionSource.interactions.collect { interaction ->
            if (interaction is androidx.compose.foundation.interaction.PressInteraction.Release) showTimePicker = true
        }
    }

    val timeParts = defaultSessionTime.split(":")
    val initHour = timeParts.getOrNull(0)?.toIntOrNull() ?: 14
    val initMinute = timeParts.getOrNull(1)?.toIntOrNull() ?: 0
    val timePickerState = rememberTimePickerState(
        initialHour = initHour,
        initialMinute = initMinute,
        is24Hour = true
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item { Text(LocalizedStrings.groupEditTitle, style = MaterialTheme.typography.titleLarge) }
            item {
                OutlinedTextField(
                    value = groupName,
                    onValueChange = { groupName = it },
                    label = { Text(LocalizedStrings.groupCreateName) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            item {
                OutlinedTextField(
                    value = moduleName,
                    onValueChange = { moduleName = it },
                    label = { Text(LocalizedStrings.groupCreateModuleName) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            item {
                Text(LocalizedStrings.groupCreateSystem, style = MaterialTheme.typography.labelMedium)
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
            item {
                OutlinedTextField(
                    value = gameFormat,
                    onValueChange = { gameFormat = it },
                    label = { Text(LocalizedStrings.groupCreateFormat) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            item {
                OutlinedTextField(
                    value = scale,
                    onValueChange = { scale = it },
                    label = { Text(LocalizedStrings.groupCreateScale) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            // 开团时间 & 预计结束
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = startTime?.let { dateFormat.format(java.util.Date(it)) } ?: "",
                        onValueChange = {},
                        label = { Text(LocalizedStrings.groupCreateStartTime) },
                        placeholder = { Text(LocalizedStrings.groupCreateClickSelect) },
                        readOnly = true,
                        interactionSource = startInteractionSource,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = expectedEndTime?.let { dateFormat.format(java.util.Date(it)) } ?: "",
                        onValueChange = {},
                        label = { Text(LocalizedStrings.groupCreateEndTime) },
                        placeholder = { Text(LocalizedStrings.groupCreateClickSelect) },
                        readOnly = true,
                        interactionSource = endInteractionSource,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            // 默认开团时间
            item {
                OutlinedTextField(
                    value = defaultSessionTime,
                    onValueChange = {},
                    label = { Text(LocalizedStrings.groupCreateTime) },
                    placeholder = { Text(LocalizedStrings.groupCreateTimePlaceholder) },
                    readOnly = true,
                    interactionSource = timeInteractionSource,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                Text(LocalizedStrings.groupStatus, style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        Constants.GROUP_STATUS_ACTIVE to LocalizedStrings.groupStatusActive,
                        Constants.GROUP_STATUS_PAUSED to LocalizedStrings.groupStatusPaused,
                        Constants.GROUP_STATUS_COMPLETED to LocalizedStrings.groupStatusCompleted
                    ).forEach { (value, label) ->
                        FilterChip(
                            selected = status == value,
                            onClick = { status = value },
                            label = { Text(label) }
                        )
                    }
                }
            }
            item {
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(LocalizedStrings.groupRemarks) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
            item {
                Button(
                    onClick = {
                        val updatedGroup = group.copy(
                            groupName = groupName.trim(),
                            moduleName = moduleName.trim(),
                            system = system,
                            gameFormat = gameFormat.trim(),
                            scale = scale.trim(),
                            status = status,
                            notes = notes.trim(),
                            startTime = startTime,
                            expectedEndTime = expectedEndTime,
                            defaultSessionTime = defaultSessionTime.trim()
                        )
                        if (timeChanged) {
                            pendingSaveGroup = updatedGroup
                            showTimeChangeConfirm = true
                        } else {
                            onSave(updatedGroup)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text(LocalizedStrings.save) }
            }
        }
    }

    // Date/Time pickers
    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = startTime)
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    startTime = datePickerState.selectedDateMillis
                    showStartDatePicker = false
                }) { Text(LocalizedStrings.confirm) }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) { Text(LocalizedStrings.cancel) }
            }
        ) { DatePicker(state = datePickerState) }
    }
    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = expectedEndTime)
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    expectedEndTime = datePickerState.selectedDateMillis
                    showEndDatePicker = false
                }) { Text(LocalizedStrings.confirm) }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) { Text(LocalizedStrings.cancel) }
            }
        ) { DatePicker(state = datePickerState) }
    }
    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text(LocalizedStrings.groupSelectTime) },
            text = { TimePicker(state = timePickerState) },
            confirmButton = {
                TextButton(onClick = {
                    val h = timePickerState.hour.toString().padStart(2, '0')
                    val m = timePickerState.minute.toString().padStart(2, '0')
                    defaultSessionTime = "$h:$m"
                    showTimePicker = false
                }) { Text(LocalizedStrings.confirm) }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text(LocalizedStrings.cancel) }
            }
        )
    }

    // 时间变更确认弹窗
    if (showTimeChangeConfirm) {
        AlertDialog(
            onDismissRequest = { showTimeChangeConfirm = false },
            title = { Text(LocalizedStrings.groupScheduleOverwrite) },
            text = { Text(LocalizedStrings.groupScheduleOverwriteDesc) },
            confirmButton = {
                TextButton(onClick = {
                    pendingSaveGroup?.let { onSave(it) }
                    showTimeChangeConfirm = false
                    pendingSaveGroup = null
                }) { Text(LocalizedStrings.confirm) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showTimeChangeConfirm = false
                    pendingSaveGroup = null
                }) { Text(LocalizedStrings.cancel) }
            }
        )
    }
}

@Composable
private fun GroupRelationshipTab(
    relationshipCount: Int,
    onNavigateToRelationship: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.AccountTree,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                LocalizedStrings.groupRelationshipTitle,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "${LocalizedStrings.groupRelationshipPrefix}$relationshipCount${LocalizedStrings.groupRelationshipCount}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onNavigateToRelationship) {
                Text(LocalizedStrings.groupRelationshipView)
            }
        }
    }
}
