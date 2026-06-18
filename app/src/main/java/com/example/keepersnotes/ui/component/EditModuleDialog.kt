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
import com.example.keepersnotes.util.LocalizedStrings

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
        title = { Text(LocalizedStrings.editModuleTitle) },
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
                    label = { Text(LocalizedStrings.editModuleName) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = author,
                    onValueChange = { author = it },
                    label = { Text(LocalizedStrings.editModuleAuthor) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // 游戏系统
                Text(LocalizedStrings.editModuleSystem, style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        "" to LocalizedStrings.editModuleNone,
                        Constants.SYSTEM_COC7 to "COC7",
                        Constants.SYSTEM_DND5E to "DND5e",
                        Constants.SYSTEM_CUSTOM to LocalizedStrings.editModuleCustom
                    ).forEach { (value, label) ->
                        FilterChip(
                            selected = system == value,
                            onClick = { system = if (system == value) "" else value },
                            label = { Text(label) }
                        )
                    }
                }

                // 难度
                Text(LocalizedStrings.editModuleDifficulty, style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        "" to LocalizedStrings.editModuleNone,
                        "新手" to LocalizedStrings.editModuleBeginner,
                        "进阶" to LocalizedStrings.editModuleIntermediate,
                        "高难" to LocalizedStrings.editModuleAdvanced
                    ).forEach { (value, label) ->
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
                    label = { Text(LocalizedStrings.editModulePlayerCount) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = duration,
                    onValueChange = { duration = it },
                    label = { Text(LocalizedStrings.editModuleDuration) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = synopsis,
                    onValueChange = { synopsis = it },
                    label = { Text(LocalizedStrings.editModuleSummary) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = { Text(LocalizedStrings.editModuleTags) },
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
