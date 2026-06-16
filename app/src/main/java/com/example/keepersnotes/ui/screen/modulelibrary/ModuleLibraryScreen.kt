package com.example.keepersnotes.ui.screen.modulelibrary

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.keepersnotes.data.local.entity.ModuleEntity
import com.example.keepersnotes.ui.component.CompactTopBar
import com.example.keepersnotes.ui.component.EditModuleDialog
import com.example.keepersnotes.util.Constants
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModuleLibraryScreen(
    onModuleClick: (moduleId: String, isCollection: Boolean) -> Unit,
    viewModel: ModuleLibraryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Import dialog state
    var showImportDialog by remember { mutableStateOf(false) }
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Edit/Delete module state
    var moduleToEdit by remember { mutableStateOf<ModuleEntity?>(null) }
    var moduleToDelete by remember { mutableStateOf<ModuleEntity?>(null) }

    // File picker launcher for ZIP
    val zipPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { viewModel.importZipFromUri(it) }
    }

    // File picker launcher for TXT/DOCX
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
                    snackbarHostState.showSnackbar("「${result.title}」导入成功")
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
        topBar = {
            CompactTopBar(title = "卷宗库")
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            Box {
                var showFabMenu by remember { mutableStateOf(false) }
                FloatingActionButton(onClick = { showFabMenu = true }) {
                    Icon(Icons.Default.Add, contentDescription = "导入")
                }
                DropdownMenu(
                    expanded = showFabMenu,
                    onDismissRequest = { showFabMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("导入 ZIP 卷宗") },
                        onClick = {
                            showFabMenu = false
                            zipPickerLauncher.launch(arrayOf("application/zip", "application/x-zip-compressed"))
                        },
                        leadingIcon = { Icon(Icons.Default.Folder, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("导入单个文档") },
                        onClick = {
                            showFabMenu = false
                            filePickerLauncher.launch(arrayOf("text/plain", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                        },
                        leadingIcon = { Icon(Icons.Default.FileUpload, contentDescription = null) }
                    )
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Search bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::setSearchQuery,
                placeholder = {
                    Text(
                        "搜索卷宗",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Serif
                    )
                },
                textStyle = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Serif,
                    fontSize = 12.sp
                ),
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                trailingIcon = {
                    if (uiState.searchQuery.isNotBlank()) {
                        IconButton(
                            onClick = { viewModel.setSearchQuery("") },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = "清除",
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp)
                    .heightIn(min = 40.dp),
                singleLine = true
            )

            // Tab row
            TabRow(selectedTabIndex = ModuleTab.entries.indexOf(uiState.selectedTab)) {
                ModuleTab.entries.forEach { tab ->
                    Tab(
                        selected = uiState.selectedTab == tab,
                        onClick = { viewModel.setTab(tab) },
                        text = { Text(tab.label) }
                    )
                }
            }

            // System filter chips + Sort
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LazyRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                item {
                    FilterChip(
                        selected = uiState.selectedSystem == null,
                        onClick = { viewModel.setSystemFilter(null) },
                        label = { Text("全部") }
                    )
                }
                item {
                    FilterChip(
                        selected = uiState.selectedSystem == Constants.SYSTEM_COC7,
                        onClick = { viewModel.setSystemFilter(Constants.SYSTEM_COC7) },
                        label = { Text("COC7") }
                    )
                }
                item {
                    FilterChip(
                        selected = uiState.selectedSystem == Constants.SYSTEM_DND5E,
                        onClick = { viewModel.setSystemFilter(Constants.SYSTEM_DND5E) },
                        label = { Text("DND5e") }
                    )
                }
                }

                // Sort button
                var showSortMenu by remember { mutableStateOf(false) }
                Box {
                    IconButton(onClick = { showSortMenu = true }) {
                        Icon(Icons.Default.Sort, contentDescription = "排序")
                    }
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        ModuleSort.entries.forEach { sort ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        sort.label,
                                        fontWeight = if (uiState.sortOption == sort) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    viewModel.setSortOption(sort)
                                    showSortMenu = false
                                },
                                leadingIcon = {
                                    if (uiState.sortOption == sort) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // Module list
            if (uiState.modules.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(32.dp)) {
                    Text(
                        text = "暂无卷宗",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.modules, key = { it.moduleId }) { module ->
                        ModuleCard(
                            module = module,
                            onClick = { onModuleClick(module.moduleId, module.isCollection) },
                            onToggleFavorite = { viewModel.toggleFavorite(module.moduleId) },
                            onEdit = { moduleToEdit = module },
                            onDelete = { moduleToDelete = module }
                        )
                    }
                }
            }
        }
    }

    // Import module dialog
    if (showImportDialog && selectedFileUri != null) {
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

    // Edit module dialog
    moduleToEdit?.let { module ->
        EditModuleDialog(
            module = module,
            onDismiss = { moduleToEdit = null },
            onSave = { updatedModule ->
                viewModel.updateModule(updatedModule)
                moduleToEdit = null
            }
        )
    }

    // Delete module confirmation dialog
    moduleToDelete?.let { module ->
        AlertDialog(
            onDismissRequest = { moduleToDelete = null },
            title = { Text("删除模组") },
            text = { Text("确定要删除「${module.title}」吗？此操作将同时删除该模组的所有高亮、批注和书签，且不可撤销。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteModule(module.moduleId)
                        moduleToDelete = null
                    }
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { moduleToDelete = null }) {
                    Text("取消")
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
    var system by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("导入卷宗") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("卷宗名称 *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = author,
                    onValueChange = { author = it },
                    label = { Text("作者") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("游戏系统", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        Constants.SYSTEM_COC7 to "COC7",
                        Constants.SYSTEM_DND5E to "DND5e",
                        Constants.SYSTEM_CUSTOM to "自定义"
                    ).forEach { (value, label) ->
                        FilterChip(
                            selected = system == value,
                            onClick = { system = if (system == value) "" else value },
                            label = { Text(label) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(title.trim(), author.trim(), system) },
                enabled = title.isNotBlank()
            ) {
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun ModuleCard(
    module: ModuleEntity,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showContextMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = { showContextMenu = true }
            )
    ) {
        Box {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = module.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (module.author.isNotBlank()) {
                            Text(
                                text = "作者: ${module.author}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = onToggleFavorite) {
                        Icon(
                            if (module.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (module.isFavorite) "取消收藏" else "收藏",
                            tint = if (module.isFavorite) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (module.synopsis.isNotBlank()) {
                    Text(
                        text = module.synopsis,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // Tags row
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (module.system.isNotBlank()) {
                        SuggestionChip(
                            onClick = {},
                            label = { Text(module.system, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                    if (module.difficulty.isNotBlank()) {
                        SuggestionChip(
                            onClick = {},
                            label = { Text(module.difficulty, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                    if (module.playerCount.isNotBlank()) {
                        SuggestionChip(
                            onClick = {},
                            label = { Text("${module.playerCount}人", style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }

            // Context menu
            DropdownMenu(
                expanded = showContextMenu,
                onDismissRequest = { showContextMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("编辑") },
                    onClick = {
                        showContextMenu = false
                        onEdit()
                    },
                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                )
                HorizontalDivider()
                DropdownMenuItem(
                    text = { Text("删除", color = MaterialTheme.colorScheme.error) },
                    onClick = {
                        showContextMenu = false
                        onDelete()
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                )
            }
        }
    }
}
