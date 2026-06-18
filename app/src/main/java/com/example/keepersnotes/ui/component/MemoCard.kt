package com.example.keepersnotes.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.keepersnotes.data.local.entity.KpMemoEntity
import com.example.keepersnotes.util.Constants
import com.example.keepersnotes.util.LocalizedStrings

@Composable
fun MemoCard(
    memo: KpMemoEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onToggleCompleted: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Left indicator
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MemoTypeBadge(type = memo.type)
                    if (memo.priority >= 1) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (memo.priority == 2) LocalizedStrings.memoPriorityUrgent else LocalizedStrings.memoPriorityImportant,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                if (memo.title.isNotBlank()) {
                    Text(
                        text = memo.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        textDecoration = if (memo.isCompleted) TextDecoration.LineThrough else null,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (memo.content.isNotBlank()) {
                    Text(
                        text = memo.content,
                        style = MaterialTheme.typography.bodySmall,
                        textDecoration = if (memo.isCompleted) TextDecoration.LineThrough else null,
                        maxLines = 6,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Completion toggle for todo type
            if (memo.type == Constants.MEMO_TYPE_TODO && onToggleCompleted != null) {
                IconButton(onClick = onToggleCompleted) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = if (memo.isCompleted) LocalizedStrings.memoUncomplete else LocalizedStrings.memoMarkComplete,
                        tint = if (memo.isCompleted) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

@Composable
fun MemoTypeBadge(type: String) {
    val (label, color) = when (type) {
        Constants.MEMO_TYPE_CLUE -> LocalizedStrings.memoType(Constants.MEMO_TYPE_CLUE) to MaterialTheme.colorScheme.primary
        Constants.MEMO_TYPE_PLOT -> LocalizedStrings.memoType(Constants.MEMO_TYPE_PLOT) to MaterialTheme.colorScheme.tertiary
        Constants.MEMO_TYPE_TODO -> LocalizedStrings.memoType(Constants.MEMO_TYPE_TODO) to MaterialTheme.colorScheme.secondary
        Constants.MEMO_TYPE_REMINDER -> LocalizedStrings.memoType(Constants.MEMO_TYPE_REMINDER) to MaterialTheme.colorScheme.error
        Constants.MEMO_TYPE_RULE -> LocalizedStrings.memoType(Constants.MEMO_TYPE_RULE) to MaterialTheme.colorScheme.outline
        else -> LocalizedStrings.memoType("memo") to MaterialTheme.colorScheme.outline
    }
    SuggestionChip(
        onClick = {},
        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
        colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = color.copy(alpha = 0.12f),
            labelColor = color
        )
    )
}
