package com.example.keepersnotes.ui.screen.modulelibrary

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.keepersnotes.data.local.entity.ModuleClueEntity
import com.example.keepersnotes.data.local.entity.ModuleDefaultNpcEntity
import com.example.keepersnotes.data.local.entity.ModuleLocationEntity
import com.example.keepersnotes.data.local.entity.ModuleOrganizationEntity

@Composable
fun NpcEditDialog(
    npc: ModuleDefaultNpcEntity? = null,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var name by remember { mutableStateOf(npc?.name ?: "") }
    var alias by remember { mutableStateOf(npc?.alias ?: "") }
    var occupation by remember { mutableStateOf(npc?.occupation ?: "") }
    var description by remember { mutableStateOf(npc?.description ?: "") }
    var truePurpose by remember { mutableStateOf(npc?.truePurpose ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (npc == null) "添加NPC" else "编辑NPC") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("名称 *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = alias, onValueChange = { alias = it }, label = { Text("别名") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = occupation, onValueChange = { occupation = it }, label = { Text("职业") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("描述") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                OutlinedTextField(value = truePurpose, onValueChange = { truePurpose = it }, label = { Text("真实目的") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(name, alias, occupation, description, truePurpose) }, enabled = name.isNotBlank()) { Text("保存") }
        },
        dismissButton = {
            Row {
                if (onDelete != null) {
                    TextButton(onClick = onDelete) { Text("删除", color = MaterialTheme.colorScheme.error) }
                }
                TextButton(onClick = onDismiss) { Text("取消") }
            }
        }
    )
}

@Composable
fun LocationEditDialog(
    location: ModuleLocationEntity? = null,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var name by remember { mutableStateOf(location?.name ?: "") }
    var type by remember { mutableStateOf(location?.type ?: "") }
    var description by remember { mutableStateOf(location?.description ?: "") }
    var clues by remember { mutableStateOf(location?.clues ?: "") }
    var inhabitants by remember { mutableStateOf(location?.inhabitants ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (location == null) "添加地点" else "编辑地点") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("名称 *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = type, onValueChange = { type = it }, label = { Text("类型") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("描述") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                OutlinedTextField(value = clues, onValueChange = { clues = it }, label = { Text("线索") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                OutlinedTextField(value = inhabitants, onValueChange = { inhabitants = it }, label = { Text("居民") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(name, type, description, clues, inhabitants) }, enabled = name.isNotBlank()) { Text("保存") }
        },
        dismissButton = {
            Row {
                if (onDelete != null) {
                    TextButton(onClick = onDelete) { Text("删除", color = MaterialTheme.colorScheme.error) }
                }
                TextButton(onClick = onDismiss) { Text("取消") }
            }
        }
    )
}

@Composable
fun OrganizationEditDialog(
    organization: ModuleOrganizationEntity? = null,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var name by remember { mutableStateOf(organization?.name ?: "") }
    var type by remember { mutableStateOf(organization?.type ?: "") }
    var description by remember { mutableStateOf(organization?.description ?: "") }
    var members by remember { mutableStateOf(organization?.members ?: "") }
    var goals by remember { mutableStateOf(organization?.goals ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (organization == null) "添加组织" else "编辑组织") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("名称 *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = type, onValueChange = { type = it }, label = { Text("类型") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("描述") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                OutlinedTextField(value = members, onValueChange = { members = it }, label = { Text("成员") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                OutlinedTextField(value = goals, onValueChange = { goals = it }, label = { Text("目标") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(name, type, description, members, goals) }, enabled = name.isNotBlank()) { Text("保存") }
        },
        dismissButton = {
            Row {
                if (onDelete != null) {
                    TextButton(onClick = onDelete) { Text("删除", color = MaterialTheme.colorScheme.error) }
                }
                TextButton(onClick = onDismiss) { Text("取消") }
            }
        }
    )
}

@Composable
fun ClueEditDialog(
    clue: ModuleClueEntity? = null,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, Boolean) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var name by remember { mutableStateOf(clue?.name ?: "") }
    var type by remember { mutableStateOf(clue?.type ?: "") }
    var description by remember { mutableStateOf(clue?.description ?: "") }
    var source by remember { mutableStateOf(clue?.source ?: "") }
    var isHidden by remember { mutableStateOf(clue?.isHidden ?: false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (clue == null) "添加线索" else "编辑线索") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("名称 *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = type, onValueChange = { type = it }, label = { Text("类型") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("描述") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                OutlinedTextField(value = source, onValueChange = { source = it }, label = { Text("来源") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isHidden, onCheckedChange = { isHidden = it })
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("隐藏线索（仅KP可见）", style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(name, type, description, source, isHidden) }, enabled = name.isNotBlank()) { Text("保存") }
        },
        dismissButton = {
            Row {
                if (onDelete != null) {
                    TextButton(onClick = onDelete) { Text("删除", color = MaterialTheme.colorScheme.error) }
                }
                TextButton(onClick = onDismiss) { Text("取消") }
            }
        }
    )
}
