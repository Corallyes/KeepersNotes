package com.example.keepersnotes.ui.screen.groupdetail.tab

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.keepersnotes.data.local.entity.KpMemoEntity
import com.example.keepersnotes.ui.component.MemoCard
import com.example.keepersnotes.util.Constants
import com.example.keepersnotes.util.LocalizedStrings

enum class MemoFilter(val labelRes: () -> String) {
    ALL({ LocalizedStrings.memoFilterAll }),
    TODO({ LocalizedStrings.memoType(Constants.MEMO_TYPE_TODO) }),
    REMINDER({ LocalizedStrings.memoType(Constants.MEMO_TYPE_REMINDER) }),
    RULE({ LocalizedStrings.memoType(Constants.MEMO_TYPE_RULE) }),
    PLOT({ LocalizedStrings.memoType(Constants.MEMO_TYPE_PLOT) }),
    CLUE({ LocalizedStrings.memoType(Constants.MEMO_TYPE_CLUE) })
}

@Composable
fun KpMemoTab(
    memos: List<KpMemoEntity>,
    pendingTodos: List<KpMemoEntity>,
    onToggleCompleted: (String) -> Unit,
    onCreateMemo: () -> Unit = {},
    onMemoClick: (String) -> Unit = {},
    filterIndex: Int = 0,
    onFilterChanged: (Int) -> Unit = {}
) {
    var selectedFilter by remember { mutableStateOf(MemoFilter.entries[filterIndex]) }

    LaunchedEffect(filterIndex) {
        selectedFilter = MemoFilter.entries[filterIndex]
    }

    val filteredMemos = when (selectedFilter) {
        MemoFilter.ALL -> memos
        MemoFilter.TODO -> memos.filter { it.type == "todo" }
        MemoFilter.REMINDER -> memos.filter { it.type == "reminder" }
        MemoFilter.RULE -> memos.filter { it.type == "rule" }
        MemoFilter.PLOT -> memos.filter { it.type == "plot" }
        MemoFilter.CLUE -> memos.filter { it.type == "clue" }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Filter chips
            ScrollableTabRow(
                selectedTabIndex = MemoFilter.entries.indexOf(selectedFilter),
                edgePadding = 8.dp
            ) {
                MemoFilter.entries.forEach { filter ->
                    Tab(
                        selected = selectedFilter == filter,
                        onClick = {
                            selectedFilter = filter
                            onFilterChanged(MemoFilter.entries.indexOf(filter))
                        },
                        text = { Text(filter.labelRes()) }
                    )
                }
            }

            if (filteredMemos.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(32.dp)) {
                    Text(
                        text = when (selectedFilter) {
                            MemoFilter.ALL -> LocalizedStrings.memoNoMemos
                            else -> LocalizedStrings.memoNoMemosType(selectedFilter.labelRes())
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredMemos, key = { it.memoId }) { memo ->
                        MemoCard(
                            memo = memo,
                            onClick = { onMemoClick(memo.memoId) },
                            onToggleCompleted = if (memo.type == "todo") {
                                { onToggleCompleted(memo.memoId) }
                            } else null
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onCreateMemo,
            modifier = Modifier
                .align(androidx.compose.ui.Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = LocalizedStrings.memoAdd)
        }
    }
}
