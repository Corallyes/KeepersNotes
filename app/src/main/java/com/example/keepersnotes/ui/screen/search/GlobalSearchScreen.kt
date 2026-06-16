package com.example.keepersnotes.ui.screen.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.keepersnotes.data.local.dao.SearchResult
import com.example.keepersnotes.ui.component.CompactTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalSearchScreen(
    onBack: () -> Unit,
    onNavigateToModule: (String) -> Unit,
    onNavigateToMemo: (String) -> Unit,
    viewModel: GlobalSearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf<String?>(null) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            CompactTopBar(
                title = "全局搜索",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 搜索框
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    viewModel.search(it, selectedFilter)
                },
                placeholder = { Text("搜索模组、笔记、高亮、批注...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = {
                            searchQuery = ""
                            viewModel.clearSearch()
                        }) {
                            Icon(Icons.Default.Clear, "清除")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                singleLine = true
            )

            // 筛选标签
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == null,
                    onClick = {
                        selectedFilter = null
                        viewModel.search(searchQuery, null)
                    },
                    label = { Text("全部") }
                )
                FilterChip(
                    selected = selectedFilter == "module",
                    onClick = {
                        selectedFilter = "module"
                        viewModel.search(searchQuery, "module")
                    },
                    label = { Text("模组") }
                )
                FilterChip(
                    selected = selectedFilter == "memo",
                    onClick = {
                        selectedFilter = "memo"
                        viewModel.search(searchQuery, "memo")
                    },
                    label = { Text("笔记") }
                )
                FilterChip(
                    selected = selectedFilter == "highlight",
                    onClick = {
                        selectedFilter = "highlight"
                        viewModel.search(searchQuery, "highlight")
                    },
                    label = { Text("高亮") }
                )
                FilterChip(
                    selected = selectedFilter == "annotation",
                    onClick = {
                        selectedFilter = "annotation"
                        viewModel.search(searchQuery, "annotation")
                    },
                    label = { Text("批注") }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 搜索结果
            if (uiState.isSearching) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (searchQuery.isBlank()) {
                // 显示搜索历史
                if (uiState.searchHistory.isNotEmpty()) {
                    Text(
                        "搜索历史",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    LazyColumn {
                        items(uiState.searchHistory) { history ->
                            ListItem(
                                headlineContent = { Text(history) },
                                leadingContent = {
                                    Icon(Icons.Default.History, null)
                                },
                                modifier = Modifier.clickable {
                                    searchQuery = history
                                    viewModel.search(history, selectedFilter)
                                }
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "输入关键词搜索",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else if (uiState.results.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.SearchOff,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "未找到匹配内容",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                Text(
                    "找到 ${uiState.results.size} 个结果",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
                LazyColumn {
                    items(uiState.results) { result ->
                        SearchResultItem(
                            result = result,
                            onClick = {
                                when (result.type) {
                                    "module" -> result.moduleId?.let { onNavigateToModule(it) }
                                    "memo" -> onNavigateToMemo(result.id)
                                    else -> result.moduleId?.let { onNavigateToModule(it) }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultItem(
    result: SearchResult,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // 类型图标
            Icon(
                imageVector = when (result.type) {
                    "module" -> Icons.Default.Book
                    "memo" -> Icons.Default.Note
                    "highlight" -> Icons.Default.Highlight
                    "annotation" -> Icons.Default.Comment
                    "bookmark" -> Icons.Default.Bookmark
                    else -> Icons.Default.Description
                },
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = when (result.type) {
                    "module" -> MaterialTheme.colorScheme.primary
                    "memo" -> MaterialTheme.colorScheme.secondary
                    "highlight" -> MaterialTheme.colorScheme.tertiary
                    "annotation" -> MaterialTheme.colorScheme.error
                    "bookmark" -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                // 类型标签
                Text(
                    text = when (result.type) {
                        "module" -> "模组"
                        "memo" -> "笔记"
                        "highlight" -> "高亮"
                        "annotation" -> "批注"
                        "bookmark" -> "书签"
                        else -> "其他"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // 标题
                Text(
                    text = result.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // 内容预览
                if (result.content.isNotBlank() && result.content != result.title) {
                    Text(
                        text = result.content,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
