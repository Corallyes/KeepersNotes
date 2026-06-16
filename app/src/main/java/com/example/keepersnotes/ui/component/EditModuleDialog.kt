package com.example.keepersnotes.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.keepersnotes.data.local.entity.ModuleEntity
import com.example.keepersnotes.util.Constants

@Composable
fun EditModuleDialog(
    module: ModuleEntity,
    onDismiss: () -> Unit,
    onSave: (ModuleEntity) -> Unit
) {
    var title by remember { mutableStateOf(module.title) }
    var author by remember { mutableStateOf(module.author) }
    var system by remember { mutableStateOf(module.system) }
    var difficulty by remember { mutableStateOf(module.difficulty) }
    var playerCount by remember { mutableStateOf(module.playerCount) }
    var duration by remember { mutableStateOf(module.duration) }
    var synopsis by remember { mutableStateOf(module.synopsis) }
    var tags by remember { mutableStateOf(module.tags) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("编辑模组信息") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("模组名称 *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = author,
                    onValueChange = { author = it },
                    label = { Text("作者") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // 游戏系统
                Text("游戏系统", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        "" to "无",
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

                // 难度
                Text("难度", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("" to "无", "新手" to "新手", "进阶" to "进阶", "高难" to "高难").forEach { (value, label) ->
                        FilterChip(
                            selected = difficulty == value,
                            onClick = { difficulty = if (difficulty == value) "" else value },
                            label = { Text(label) }
                        )
                    }
                }

                OutlinedTextField(
                    value = playerCount,
                    onValueChange = { playerCount = it },
                    label = { Text("玩家人数（如 3-5）") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = duration,
                    onValueChange = { duration = it },
                    label = { Text("时长（如 4-6h）") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = synopsis,
                    onValueChange = { synopsis = it },
                    label = { Text("简介") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = { Text("标签（逗号分隔）") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(
                        module.copy(
                            title = title.trim(),
                            author = author.trim(),
                            system = system,
                            difficulty = difficulty,
                            playerCount = playerCount.trim(),
                            duration = duration.trim(),
                            synopsis = synopsis.trim(),
                            tags = tags.trim()
                        )
                    )
                },
                enabled = title.isNotBlank()
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
