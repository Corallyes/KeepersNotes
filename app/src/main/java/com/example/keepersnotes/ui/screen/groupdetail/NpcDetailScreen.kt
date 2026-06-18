package com.example.keepersnotes.ui.screen.groupdetail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.keepersnotes.data.local.entity.NpcEntity
import com.example.keepersnotes.ui.component.CompactTopBar
import com.example.keepersnotes.ui.component.NpcStatusBadge
import com.example.keepersnotes.ui.screen.modulelibrary.Gender
import com.example.keepersnotes.util.Constants
import com.example.keepersnotes.util.LocalizedStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NpcDetailScreen(
    npcId: String,
    onBack: () -> Unit,
    viewModel: NpcDetailViewModel = hiltViewModel()
) {
    val npc by viewModel.npc.collectAsStateWithLifecycle()
    var showEditSheet by remember { mutableStateOf(false) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            CompactTopBar(
                title = npc?.name ?: LocalizedStrings.npcDetail,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = LocalizedStrings.back)
                    }
                },
                actions = {
                    IconButton(onClick = { showEditSheet = true }) {
                        Icon(Icons.Default.Edit, contentDescription = LocalizedStrings.npcEdit)
                    }
                }
            )
        }
    ) { padding ->
        val currentNpc = npc
        if (currentNpc == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                Text(LocalizedStrings.groupLoading, modifier = Modifier.padding(16.dp))
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Basic info
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = currentNpc.name,
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.weight(1f)
                        )
                        NpcStatusBadge(status = currentNpc.status)
                    }
                    if (currentNpc.alias.isNotBlank()) {
                        Text("${LocalizedStrings.npcAlias}: ${currentNpc.alias}", style = MaterialTheme.typography.bodyMedium)
                    }
                    if (currentNpc.occupation.isNotBlank()) {
                        Text("${LocalizedStrings.npcOccupation}: ${currentNpc.occupation}", style = MaterialTheme.typography.bodyMedium)
                    }
                    if (currentNpc.gender.isNotBlank()) {
                        Text("${LocalizedStrings.pcGender}: ${Gender.fromKey(currentNpc.gender)?.labelRes() ?: currentNpc.gender}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            // Description
            if (currentNpc.description.isNotBlank()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(LocalizedStrings.npcDescription, style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(currentNpc.description, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            // Hidden info (KP only)
            if (currentNpc.truePurpose.isNotBlank()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(LocalizedStrings.npcTruePurpose, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(currentNpc.truePurpose, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            // Relationship
            if (currentNpc.relationshipToPc.isNotBlank()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(LocalizedStrings.npcPcRelation, style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(currentNpc.relationshipToPc, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            // KP Notes
            if (currentNpc.kpNotes.isNotBlank()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(LocalizedStrings.groupRemarks, style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(currentNpc.kpNotes, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }

    // Edit NPC bottom sheet
    val editNpc = npc
    if (showEditSheet && editNpc != null) {
        EditNpcSheet(
            npc = editNpc,
            onDismiss = { showEditSheet = false },
            onSave = { updatedNpc ->
                viewModel.updateNpc(updatedNpc)
                showEditSheet = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditNpcSheet(
    npc: NpcEntity,
    onDismiss: () -> Unit,
    onSave: (NpcEntity) -> Unit
) {
    var name by remember { mutableStateOf(npc.name) }
    var alias by remember { mutableStateOf(npc.alias) }
    var occupation by remember { mutableStateOf(npc.occupation) }
    var gender by remember { mutableStateOf(npc.gender) }
    var genderExpanded by remember { mutableStateOf(false) }
    var description by remember { mutableStateOf(npc.description) }
    var truePurpose by remember { mutableStateOf(npc.truePurpose) }
    var relationshipToPc by remember { mutableStateOf(npc.relationshipToPc) }
    var status by remember { mutableStateOf(npc.status) }
    var kpNotes by remember { mutableStateOf(npc.kpNotes) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item {
                Text(LocalizedStrings.npcEdit, style = MaterialTheme.typography.titleLarge)
            }
            item {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(LocalizedStrings.npcName) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            item {
                OutlinedTextField(
                    value = alias,
                    onValueChange = { alias = it },
                    label = { Text(LocalizedStrings.npcAlias) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            item {
                OutlinedTextField(
                    value = occupation,
                    onValueChange = { occupation = it },
                    label = { Text(LocalizedStrings.npcOccupation) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            item {
                ExposedDropdownMenuBox(
                    expanded = genderExpanded,
                    onExpandedChange = { genderExpanded = it }
                ) {
                    OutlinedTextField(
                        value = Gender.fromKey(gender)?.labelRes() ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(LocalizedStrings.pcGender) },
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
            }
            item {
                Text(LocalizedStrings.npcStatus, style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        Constants.NPC_STATUS_ALIVE to LocalizedStrings.npcStatusAlive,
                        Constants.NPC_STATUS_DEAD to LocalizedStrings.npcStatusDead,
                        Constants.NPC_STATUS_MISSING to LocalizedStrings.npcStatusMissing,
                        Constants.NPC_STATUS_UNKNOWN to LocalizedStrings.npcStatusUnknown
                    ).forEach { (value, label) ->
                        FilterChip(
                            selected = status == value,
                            onClick = { status = value },
                            label = { Text(label) }
                        )
                    }
                }
            }
            item {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(LocalizedStrings.npcDescription) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
            item {
                OutlinedTextField(
                    value = truePurpose,
                    onValueChange = { truePurpose = it },
                    label = { Text(LocalizedStrings.npcTruePurpose) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
            item {
                OutlinedTextField(
                    value = relationshipToPc,
                    onValueChange = { relationshipToPc = it },
                    label = { Text(LocalizedStrings.npcPcRelation) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
            item {
                OutlinedTextField(
                    value = kpNotes,
                    onValueChange = { kpNotes = it },
                    label = { Text(LocalizedStrings.groupRemarks) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
            item {
                Button(
                    onClick = {
                        onSave(
                            npc.copy(
                                name = name.trim(),
                                alias = alias.trim(),
                                occupation = occupation.trim(),
                                gender = gender,
                                description = description.trim(),
                                truePurpose = truePurpose.trim(),
                                relationshipToPc = relationshipToPc.trim(),
                                status = status,
                                kpNotes = kpNotes.trim()
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(LocalizedStrings.save)
                }
            }
        }
    }
}
