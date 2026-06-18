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
import com.example.keepersnotes.util.LocalizedStrings

enum class Gender(val labelRes: () -> String, val key: String) {
    MALE({ LocalizedStrings.entityMale }, "male"),
    FEMALE({ LocalizedStrings.entityFemale }, "female"),
    ALIEN({ LocalizedStrings.entityAlien }, "alien"),
    OTHER({ LocalizedStrings.entityOther }, "other");

    companion object {
        fun fromKey(key: String): Gender? = entries.find { it.key == key }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NpcEditDialog(
    npc: ModuleDefaultNpcEntity? = null,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String, String) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var name by remember { mutableStateOf(npc?.name ?: "") }
    var alias by remember { mutableStateOf(npc?.alias ?: "") }
    var occupation by remember { mutableStateOf(npc?.occupation ?: "") }
    var description by remember { mutableStateOf(npc?.description ?: "") }
    var truePurpose by remember { mutableStateOf(npc?.truePurpose ?: "") }
    var gender by remember { mutableStateOf(npc?.gender ?: "") }
    var genderExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (npc == null) LocalizedStrings.entityAddNpc else LocalizedStrings.entityEditNpc) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(LocalizedStrings.entityName) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = alias, onValueChange = { alias = it }, label = { Text(LocalizedStrings.entityAlias) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                // Gender selector
                ExposedDropdownMenuBox(
                    expanded = genderExpanded,
                    onExpandedChange = { genderExpanded = it }
                ) {
                    OutlinedTextField(
                        value = Gender.fromKey(gender)?.labelRes() ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(LocalizedStrings.entityGender) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = genderExpanded,
                        onDismissRequest = { genderExpanded = false }
                    ) {
                        Gender.entries.forEach { g ->
                            DropdownMenuItem(
                                text = { Text(g.labelRes()) },
                                onClick = { gender = g.key; genderExpanded = false }
                            )
                        }
                    }
                }
                OutlinedTextField(value = occupation, onValueChange = { occupation = it }, label = { Text(LocalizedStrings.entityOccupation) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text(LocalizedStrings.entityDescription) }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                OutlinedTextField(value = truePurpose, onValueChange = { truePurpose = it }, label = { Text(LocalizedStrings.entityTruePurpose) }, modifier = Modifier.fillMaxWidth(), minLines = 2)
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(name, alias, occupation, description, truePurpose, gender) }, enabled = name.isNotBlank()) { Text(LocalizedStrings.save) }
        },
        dismissButton = {
            Row {
                if (onDelete != null) {
                    TextButton(onClick = onDelete) { Text(LocalizedStrings.delete, color = MaterialTheme.colorScheme.error) }
                }
                TextButton(onClick = onDismiss) { Text(LocalizedStrings.cancel) }
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
        title = { Text(if (location == null) LocalizedStrings.entityAddLocation else LocalizedStrings.entityEditLocation) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(LocalizedStrings.entityName) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = type, onValueChange = { type = it }, label = { Text(LocalizedStrings.entityType) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text(LocalizedStrings.entityDescription) }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                OutlinedTextField(value = clues, onValueChange = { clues = it }, label = { Text(LocalizedStrings.entityClues) }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                OutlinedTextField(value = inhabitants, onValueChange = { inhabitants = it }, label = { Text(LocalizedStrings.entityInhabitants) }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(name, type, description, clues, inhabitants) }, enabled = name.isNotBlank()) { Text(LocalizedStrings.save) }
        },
        dismissButton = {
            Row {
                if (onDelete != null) {
                    TextButton(onClick = onDelete) { Text(LocalizedStrings.delete, color = MaterialTheme.colorScheme.error) }
                }
                TextButton(onClick = onDismiss) { Text(LocalizedStrings.cancel) }
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
        title = { Text(if (organization == null) LocalizedStrings.entityAddOrganization else LocalizedStrings.entityEditOrganization) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(LocalizedStrings.entityName) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = type, onValueChange = { type = it }, label = { Text(LocalizedStrings.entityType) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text(LocalizedStrings.entityDescription) }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                OutlinedTextField(value = members, onValueChange = { members = it }, label = { Text(LocalizedStrings.entityMembers) }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                OutlinedTextField(value = goals, onValueChange = { goals = it }, label = { Text(LocalizedStrings.entityGoals) }, modifier = Modifier.fillMaxWidth(), minLines = 2)
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(name, type, description, members, goals) }, enabled = name.isNotBlank()) { Text(LocalizedStrings.save) }
        },
        dismissButton = {
            Row {
                if (onDelete != null) {
                    TextButton(onClick = onDelete) { Text(LocalizedStrings.delete, color = MaterialTheme.colorScheme.error) }
                }
                TextButton(onClick = onDismiss) { Text(LocalizedStrings.cancel) }
            }
        }
    )
}

@Composable
fun ClueEditDialog(
    clue: ModuleClueEntity? = null,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var name by remember { mutableStateOf(clue?.name ?: "") }
    var type by remember { mutableStateOf(clue?.type ?: "") }
    var description by remember { mutableStateOf(clue?.description ?: "") }
    var source by remember { mutableStateOf(clue?.source ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (clue == null) LocalizedStrings.entityAddClue else LocalizedStrings.entityEditClue) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(LocalizedStrings.entityName) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = type, onValueChange = { type = it }, label = { Text(LocalizedStrings.entityType) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text(LocalizedStrings.entityDescription) }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                OutlinedTextField(value = source, onValueChange = { source = it }, label = { Text(LocalizedStrings.entitySource) }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(name, type, description, source) }, enabled = name.isNotBlank()) { Text(LocalizedStrings.save) }
        },
        dismissButton = {
            Row {
                if (onDelete != null) {
                    TextButton(onClick = onDelete) { Text(LocalizedStrings.delete, color = MaterialTheme.colorScheme.error) }
                }
                TextButton(onClick = onDismiss) { Text(LocalizedStrings.cancel) }
            }
        }
    )
}
