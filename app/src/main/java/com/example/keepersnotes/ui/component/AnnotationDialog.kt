package com.example.keepersnotes.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.keepersnotes.util.LocalizedStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnnotationDialog(
    selectedText: String,
    onDismiss: () -> Unit,
    onSave: (note: String) -> Unit
) {
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(LocalizedStrings.annotationDialogTitle) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // 显示选中的文本
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = selectedText,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(12.dp),
                        maxLines = 3
                    )
                }

                // 批注输入
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(LocalizedStrings.annotationContent) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(note.trim()) },
                enabled = note.isNotBlank()
            ) {
                Text(LocalizedStrings.save)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(LocalizedStrings.cancel)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnnotationViewDialog(
    selectedText: String,
    note: String,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(LocalizedStrings.readerAnnotations) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // 原文
                Text(
                    text = LocalizedStrings.readerOriginalText,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = selectedText,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                // 批注内容
                Text(
                    text = LocalizedStrings.annotationContent,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = note,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onEdit) {
                Text(LocalizedStrings.edit)
            }
        },
        dismissButton = {
            Row {
                TextButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(LocalizedStrings.delete)
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = onDismiss) {
                    Text(LocalizedStrings.close)
                }
            }
        }
    )
}
