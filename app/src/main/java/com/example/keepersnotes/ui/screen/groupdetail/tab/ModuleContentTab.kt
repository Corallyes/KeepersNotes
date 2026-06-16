package com.example.keepersnotes.ui.screen.groupdetail.tab

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.keepersnotes.data.local.entity.ModuleEntity
import com.example.keepersnotes.util.Chapter
import com.example.keepersnotes.util.ModuleContentParser

@Composable
fun ModuleContentTab(
    module: ModuleEntity?,
    modifier: Modifier = Modifier
) {
    if (module == null) {
        Box(modifier = modifier.fillMaxSize().padding(32.dp)) {
            Text(
                "该团未关联模组",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val chapters = remember(module.contentJson) {
        ModuleContentParser.jsonToChapters(module.contentJson)
    }

    if (chapters.isEmpty()) {
        Box(modifier = modifier.fillMaxSize().padding(32.dp)) {
            Text(
                "模组内容为空",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    var selectedChapter by remember { mutableStateOf<Chapter?>(null) }

    // Auto-select first chapter
    LaunchedEffect(chapters) {
        if (selectedChapter == null) {
            selectedChapter = findFirstLeaf(chapters)
        }
    }

    Row(modifier = modifier.fillMaxSize()) {
        // Left: Chapter tree
        Column(
            modifier = Modifier
                .weight(0.35f)
                .fillMaxHeight()
                .padding(8.dp)
        ) {
            Text(
                "目录",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                chapters.forEach { chapter ->
                    chapterTreeItems(
                        chapter = chapter,
                        depth = 0,
                        selectedChapter = selectedChapter,
                        onChapterClick = { selectedChapter = it }
                    )
                }
            }
        }

        VerticalDivider(modifier = Modifier.fillMaxHeight())

        // Right: Content reader
        Column(
            modifier = Modifier
                .weight(0.65f)
                .fillMaxHeight()
                .padding(8.dp)
        ) {
            if (selectedChapter != null) {
                val chapter = selectedChapter!!
                Text(
                    chapter.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    if (chapter.content.isNotBlank()) {
                        Text(
                            chapter.content,
                            style = MaterialTheme.typography.bodyLarge,
                            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
                        )
                    } else {
                        Text(
                            "本节无内容",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize()) {
                    Text(
                        "选择一个章节开始阅读",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun LazyListScope.chapterTreeItems(
    chapter: Chapter,
    depth: Int,
    selectedChapter: Chapter?,
    onChapterClick: (Chapter) -> Unit
) {
    item(key = chapter.id) {
        ChapterTreeItem(
            chapter = chapter,
            depth = depth,
            isSelected = selectedChapter?.id == chapter.id,
            onChapterClick = onChapterClick
        )
    }
    // Children are handled inside ChapterTreeItem with expand/collapse
}

@Composable
private fun ChapterTreeItem(
    chapter: Chapter,
    depth: Int,
    isSelected: Boolean,
    onChapterClick: (Chapter) -> Unit
) {
    val hasChildren = chapter.children.isNotEmpty()
    var expanded by remember { mutableStateOf(true) }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    if (hasChildren) {
                        expanded = !expanded
                    } else {
                        onChapterClick(chapter)
                    }
                }
                .padding(
                    start = (depth * 16).dp,
                    top = 4.dp,
                    bottom = 4.dp,
                    end = 4.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (hasChildren) {
                IconButton(
                    onClick = { expanded = !expanded },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "折叠" else "展开",
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(24.dp))
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = chapter.title,
                style = if (depth == 0) MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium
                ) else MaterialTheme.typography.bodySmall,
                color = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onChapterClick(chapter) }
            )
        }

        // Children
        if (hasChildren) {
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column {
                    chapter.children.forEach { child ->
                        ChapterTreeItem(
                            chapter = child,
                            depth = depth + 1,
                            isSelected = isSelected,
                            onChapterClick = onChapterClick
                        )
                    }
                }
            }
        }
    }
}

private fun findFirstLeaf(chapters: List<Chapter>): Chapter? {
    for (chapter in chapters) {
        if (chapter.children.isEmpty()) return chapter
        findFirstLeaf(chapter.children)?.let { return it }
    }
    return chapters.firstOrNull()
}
