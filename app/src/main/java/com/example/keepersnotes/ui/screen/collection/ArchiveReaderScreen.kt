package com.example.keepersnotes.ui.screen.collection

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.keepersnotes.ui.component.MarkdownText
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchiveReaderScreen(
    archiveId: String,
    onBack: () -> Unit,
    viewModel: ArchiveReaderViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    var showSearch by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    // Scroll to a specific line (approximate by scrolling proportionally)
    fun scrollToLine(lineIndex: Int) {
        val totalLines = uiState.archive?.contentMarkdown?.lines()?.size ?: 1
        val proportion = lineIndex.toFloat() / totalLines.coerceAtLeast(1)
        coroutineScope.launch {
            val targetScroll = (scrollState.maxValue * proportion).toInt()
            scrollState.scrollTo(targetScroll.coerceIn(0, scrollState.maxValue))
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.width(280.dp)) {
                Text(
                    "目录",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )
                HorizontalDivider()
                if (uiState.toc.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("无目录", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(uiState.toc) { entry ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        scrollToLine(entry.lineIndex)
                                        coroutineScope.launch { drawerState.close() }
                                    }
                                    .padding(
                                        start = (12 + entry.level * 8).dp,
                                        top = 8.dp,
                                        bottom = 8.dp,
                                        end = 16.dp
                                    ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = entry.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = when (entry.level) {
                                            1 -> 16.sp
                                            2 -> 14.sp
                                            else -> 13.sp
                                        },
                                        fontWeight = if (entry.level <= 2) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    ) {
        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                Column {
                    TopAppBar(
                        title = {
                            if (showSearch) {
                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = viewModel::setSearchQuery,
                                    placeholder = { Text("搜索内容", fontSize = 14.sp) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = MaterialTheme.typography.bodyMedium
                                )
                            } else {
                                Text(
                                    uiState.archive?.title ?: "阅读档案",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        },
                        navigationIcon = {
                            if (showSearch) {
                                IconButton(onClick = {
                                    showSearch = false
                                    viewModel.setSearchQuery("")
                                }) {
                                    Icon(Icons.Default.Close, contentDescription = "关闭搜索")
                                }
                            } else {
                                IconButton(onClick = onBack) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                                }
                            }
                        },
                        actions = {
                            if (!showSearch) {
                                IconButton(onClick = { showSearch = true }) {
                                    Icon(Icons.Default.Search, contentDescription = "搜索")
                                }
                                IconButton(onClick = {
                                    coroutineScope.launch { drawerState.open() }
                                }) {
                                    Icon(Icons.Default.List, contentDescription = "目录")
                                }
                            }
                        }
                    )
                    // Search results bar
                    if (showSearch && searchResults.isNotEmpty()) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                                items(searchResults) { match ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { scrollToLine(match.lineIndex) }
                                            .padding(horizontal = 16.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "...${match.lineText}...",
                                            style = MaterialTheme.typography.bodySmall,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    } else if (showSearch && searchQuery.isNotBlank() && searchResults.isEmpty()) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                "无匹配结果",
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        ) { padding ->
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .padding(16.dp)
                        .verticalScroll(scrollState)
                ) {
                    MarkdownText(
                        markdown = uiState.archive?.contentMarkdown ?: "无内容",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
