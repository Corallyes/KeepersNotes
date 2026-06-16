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

enum class MemoFilter(val label: String) {
    ALL("全部"), HIDDEN("暗线笔记"), TODO("待办"), CLUE("线索"), PLOT("剧情"), GENERAL("备忘")
}

@Composable
fun KpMemoTab(
    memos: List<KpMemoEntity>,
    pendingTodos: List<KpMemoEntity>,
    onToggleCompleted: (String) -> Unit,
    onCreateMemo: () -> Unit = {},
    onMemoClick: (String) -> Unit = {}
) {
    var selectedFilter by remember { mutableStateOf(MemoFilter.ALL) }

    val filteredMemos = when (selectedFilter) {
        MemoFilter.ALL -> memos
        MemoFilter.HIDDEN -> memos.filter { it.isHidden }
        MemoFilter.TODO -> memos.filter { it.type == "todo" }
        MemoFilter.CLUE -> memos.filter { it.type == "clue" }
        MemoFilter.PLOT -> memos.filter { it.type == "plot" }
        MemoFilter.GENERAL -> memos.filter { it.type == "general" || it.type == "reminder" || it.type == "rule" }
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
                        onClick = { selectedFilter = filter },
                        text = { Text(filter.label) }
                    )
                }
            }

            if (filteredMemos.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(32.dp)) {
                    Text(
                        text = when (selectedFilter) {
                            MemoFilter.ALL -> "还没有备忘录"
                            else -> "没有${selectedFilter.label}类型的备忘"
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
            Icon(Icons.Default.Add, contentDescription = "添加备忘")
        }
    }
}
