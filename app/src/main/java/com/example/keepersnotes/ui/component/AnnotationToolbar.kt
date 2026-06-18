package com.example.keepersnotes.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.keepersnotes.util.LocalizedStrings

@Composable
fun AnnotationToolbar(
    pureReadingMode: Boolean,
    onTogglePureReadingMode: () -> Unit,
    onShowAnnotationPanel: () -> Unit,
    onShowBookmarkPanel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        tonalElevation = 3.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 纯享阅读模式
            IconButton(
                onClick = onTogglePureReadingMode,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    if (pureReadingMode) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = if (pureReadingMode) LocalizedStrings.readerExitPureReading else LocalizedStrings.readerPureReading,
                    tint = if (pureReadingMode) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 标注管理
            Row {
                IconButton(
                    onClick = onShowBookmarkPanel,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.Bookmarks,
                        contentDescription = LocalizedStrings.readerManageBookmarks,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = onShowAnnotationPanel,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.List,
                        contentDescription = LocalizedStrings.readerManageAnnotations,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
