package com.example.keepersnotes.ui.screen.grouplist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.keepersnotes.ui.component.CompactTopBar
import com.example.keepersnotes.ui.component.GroupCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupListScreen(
    onGroupClick: (String) -> Unit,
    onCreateGroup: () -> Unit,
    viewModel: GroupListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            CompactTopBar(title = "我的团")
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateGroup) {
                Icon(Icons.Default.Add, contentDescription = "新建团")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Filter tabs
            TabRow(selectedTabIndex = GroupFilter.entries.indexOf(uiState.selectedFilter)) {
                GroupFilter.entries.forEach { filter ->
                    Tab(
                        selected = uiState.selectedFilter == filter,
                        onClick = { viewModel.setFilter(filter) },
                        text = { Text(filter.label) }
                    )
                }
            }

            // Group list
            if (uiState.groups.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp)
                ) {
                    Text(
                        text = when (uiState.selectedFilter) {
                            GroupFilter.ALL -> "还没有创建任何团"
                            GroupFilter.ACTIVE -> "没有进行中的团"
                            GroupFilter.PAUSED -> "没有暂停的团"
                            GroupFilter.COMPLETED -> "没有已完结的团"
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.groups, key = { it.groupId }) { group ->
                        GroupCard(
                            group = group,
                            onClick = { onGroupClick(group.groupId) }
                        )
                    }
                }
            }
        }
    }
}
