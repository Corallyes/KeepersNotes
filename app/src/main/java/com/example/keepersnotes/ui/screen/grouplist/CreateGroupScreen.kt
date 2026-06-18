package com.example.keepersnotes.ui.screen.grouplist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.keepersnotes.ui.component.CompactTopBar
import com.example.keepersnotes.util.Constants
import com.example.keepersnotes.util.LocalizedStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupScreen(
    onBack: () -> Unit,
    onGroupCreated: (String) -> Unit,
    viewModel: CreateGroupViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showModuleSelector by remember { mutableStateOf(false) }
    var pendingModule by remember { mutableStateOf<com.example.keepersnotes.data.local.entity.ModuleEntity?>(null) }

    LaunchedEffect(uiState.createdGroupId) {
        uiState.createdGroupId?.let { onGroupCreated(it) }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            CompactTopBar(
                title = LocalizedStrings.groupCreateTitle,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = LocalizedStrings.back)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = uiState.groupName,
                onValueChange = viewModel::updateGroupName,
                label = { Text(LocalizedStrings.groupCreateName) },
                isError = uiState.groupNameError != null,
                supportingText = uiState.groupNameError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Module name + pick from library
            OutlinedTextField(
                value = uiState.moduleName,
                onValueChange = viewModel::updateModuleName,
                label = { Text(LocalizedStrings.groupCreateModuleName) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                trailingIcon = {
                    TextButton(onClick = { showModuleSelector = true }) {
                        Text(LocalizedStrings.moduleSelectFromLibrary)
                    }
                }
            )

            // System selector
            Text(LocalizedStrings.groupCreateSystem, style = MaterialTheme.typography.labelMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(Constants.SYSTEM_COC7, Constants.SYSTEM_DND5E).forEach { s ->
                    FilterChip(
                        selected = uiState.system == s,
                        onClick = { viewModel.updateSystem(s) },
                        label = { Text(s) }
                    )
                }
            }

            // Game format
            OutlinedTextField(
                value = uiState.gameFormat,
                onValueChange = viewModel::updateGameFormat,
                label = { Text(LocalizedStrings.groupCreateFormat) },
                placeholder = { Text(LocalizedStrings.groupCreateFormatPlaceholder) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Scale
            OutlinedTextField(
                value = uiState.scale,
                onValueChange = viewModel::updateScale,
                label = { Text(LocalizedStrings.groupCreateScale) },
                placeholder = { Text(LocalizedStrings.groupCreateScalePlaceholder) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Start time & Expected end time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                var showStartDatePicker by remember { mutableStateOf(false) }
                var showEndDatePicker by remember { mutableStateOf(false) }

                val startInteractionSource = remember { MutableInteractionSource() }
                val endInteractionSource = remember { MutableInteractionSource() }

                LaunchedEffect(startInteractionSource) {
                    startInteractionSource.interactions.collect { interaction ->
                        if (interaction is PressInteraction.Release) showStartDatePicker = true
                    }
                }
                LaunchedEffect(endInteractionSource) {
                    endInteractionSource.interactions.collect { interaction ->
                        if (interaction is PressInteraction.Release) showEndDatePicker = true
                    }
                }

                OutlinedTextField(
                    value = uiState.startTime?.let { dateFormat.format(java.util.Date(it)) } ?: "",
                    onValueChange = {},
                    label = { Text(LocalizedStrings.groupCreateStartTime) },
                    placeholder = { Text(LocalizedStrings.groupCreateClickSelect) },
                    readOnly = true,
                    interactionSource = startInteractionSource,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = uiState.expectedEndTime?.let { dateFormat.format(java.util.Date(it)) } ?: "",
                    onValueChange = {},
                    label = { Text(LocalizedStrings.groupCreateEndTime) },
                    placeholder = { Text(LocalizedStrings.groupCreateClickSelect) },
                    readOnly = true,
                    interactionSource = endInteractionSource,
                    modifier = Modifier.weight(1f)
                )

                if (showStartDatePicker) {
                    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = uiState.startTime)
                    DatePickerDialog(
                        onDismissRequest = { showStartDatePicker = false },
                        confirmButton = {
                            TextButton(onClick = {
                                viewModel.updateStartTime(datePickerState.selectedDateMillis)
                                showStartDatePicker = false
                            }) { Text(LocalizedStrings.confirm) }
                        },
                        dismissButton = {
                            TextButton(onClick = { showStartDatePicker = false }) { Text(LocalizedStrings.cancel) }
                        }
                    ) {
                        DatePicker(state = datePickerState)
                    }
                }
                if (showEndDatePicker) {
                    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = uiState.expectedEndTime)
                    DatePickerDialog(
                        onDismissRequest = { showEndDatePicker = false },
                        confirmButton = {
                            TextButton(onClick = {
                                viewModel.updateExpectedEndTime(datePickerState.selectedDateMillis)
                                showEndDatePicker = false
                            }) { Text(LocalizedStrings.confirm) }
                        },
                        dismissButton = {
                            TextButton(onClick = { showEndDatePicker = false }) { Text(LocalizedStrings.cancel) }
                        }
                    ) {
                        DatePicker(state = datePickerState)
                    }
                }
            }

            // Default session time
            var showTimePicker by remember { mutableStateOf(false) }
            val timeParts = uiState.defaultSessionTime.split(":")
            val initHour = timeParts.getOrNull(0)?.toIntOrNull() ?: 14
            val initMinute = timeParts.getOrNull(1)?.toIntOrNull() ?: 0
            val timePickerState = rememberTimePickerState(
                initialHour = initHour,
                initialMinute = initMinute,
                is24Hour = true
            )

            val timeInteractionSource = remember { MutableInteractionSource() }
            LaunchedEffect(timeInteractionSource) {
                timeInteractionSource.interactions.collect { interaction ->
                    if (interaction is PressInteraction.Release) showTimePicker = true
                }
            }

            OutlinedTextField(
                value = uiState.defaultSessionTime,
                onValueChange = {},
                label = { Text(LocalizedStrings.groupCreateTime) },
                placeholder = { Text(LocalizedStrings.groupCreateTimePlaceholder) },
                readOnly = true,
                interactionSource = timeInteractionSource,
                modifier = Modifier.fillMaxWidth()
            )

            if (showTimePicker) {
                AlertDialog(
                    onDismissRequest = { showTimePicker = false },
                    title = { Text(LocalizedStrings.groupCreateTimeTitle) },
                    text = { TimePicker(state = timePickerState) },
                    confirmButton = {
                        TextButton(onClick = {
                            val h = timePickerState.hour.toString().padStart(2, '0')
                            val m = timePickerState.minute.toString().padStart(2, '0')
                            viewModel.updateDefaultSessionTime("$h:$m")
                            showTimePicker = false
                        }) { Text(LocalizedStrings.confirm) }
                    },
                    dismissButton = {
                        TextButton(onClick = { showTimePicker = false }) { Text(LocalizedStrings.cancel) }
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = viewModel::submit,
                enabled = !uiState.isSubmitting,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                } else {
                    Text(LocalizedStrings.groupCreateSubmit)
                }
            }
        }
    }

    // Module selector dialog
    if (showModuleSelector) {
        ModuleSelectorDialog(
            modules = uiState.modules,
            onDismiss = { showModuleSelector = false },
            onModuleSelected = { module ->
                pendingModule = module
                showModuleSelector = false
            }
        )
    }

    // Module confirmation dialog
    val confirmModule = pendingModule
    if (confirmModule != null) {
        AlertDialog(
            onDismissRequest = { pendingModule = null },
            title = { Text(LocalizedStrings.moduleConfirmSelect) },
            text = {
                Text(LocalizedStrings.moduleConfirmSelectDesc(confirmModule.title))
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.selectModule(confirmModule)
                    pendingModule = null
                }) {
                    Text(LocalizedStrings.confirm)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingModule = null }) {
                    Text(LocalizedStrings.cancel)
                }
            }
        )
    }
}

@Composable
private fun ModuleSelectorDialog(
    modules: List<com.example.keepersnotes.data.local.entity.ModuleEntity>,
    onDismiss: () -> Unit,
    onModuleSelected: (com.example.keepersnotes.data.local.entity.ModuleEntity) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filtered = if (searchQuery.isBlank()) modules
    else modules.filter {
        it.title.contains(searchQuery, ignoreCase = true) ||
                it.author.contains(searchQuery, ignoreCase = true)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(LocalizedStrings.moduleSelectTitle) },
        text = {
            Column {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(LocalizedStrings.moduleSearchName) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (filtered.isEmpty()) {
                    Text(
                        LocalizedStrings.moduleNoModulesImport,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                        items(filtered, key = { it.moduleId }) { module ->
                            ListItem(
                                headlineContent = { Text(module.title) },
                                supportingContent = {
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        if (module.author.isNotBlank()) {
                                            Text(module.author, style = MaterialTheme.typography.bodySmall)
                                        }
                                        if (module.system.isNotBlank()) {
                                            Text("· ${module.system}", style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                },
                                modifier = Modifier.clickable { onModuleSelected(module) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(LocalizedStrings.cancel) }
        }
    )
}
