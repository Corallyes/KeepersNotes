package com.example.keepersnotes.ui.screen.groupdetail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.keepersnotes.data.local.entity.PlayerCharacterEntity
import com.example.keepersnotes.ui.component.CompactTopBar
import com.example.keepersnotes.ui.component.PcStatusBadge
import com.example.keepersnotes.ui.screen.modulelibrary.Gender
import com.example.keepersnotes.util.Constants
import com.example.keepersnotes.util.JsonUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PcDetailScreen(
    pcId: String,
    onBack: () -> Unit,
    viewModel: PcDetailViewModel = hiltViewModel()
) {
    val pc by viewModel.pc.collectAsStateWithLifecycle()
    var showEditSheet by remember { mutableStateOf(false) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            CompactTopBar(
                title = pc?.characterName ?: "PC详情",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { showEditSheet = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "编辑")
                    }
                }
            )
        }
    ) { padding ->
        val currentPc = pc
        if (currentPc == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                Text("加载中...", modifier = Modifier.padding(16.dp))
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Basic info
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = currentPc.characterName,
                                style = MaterialTheme.typography.headlineSmall,
                                modifier = Modifier.weight(1f)
                            )
                            PcStatusBadge(status = currentPc.status)
                        }
                        Text("玩家: ${currentPc.playerName}", style = MaterialTheme.typography.bodyMedium)
                        Text("系统: ${currentPc.system}", style = MaterialTheme.typography.bodySmall)
                        if (currentPc.gender.isNotBlank()) {
                            Text("性别: ${Gender.fromKey(currentPc.gender)?.label ?: currentPc.gender}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            // Stats panel (HP/SAN/LUCK)
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("属性面板", style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatItem("HP", "${currentPc.hpCurrent}/${currentPc.hpMax}")
                            StatItem("SAN", "${currentPc.sanCurrent}/${currentPc.sanMax}")
                            StatItem("幸运", "${currentPc.luck}")
                        }
                    }
                }
            }

            // Skills list
            item {
                val skills = JsonUtil.parseKeyValueJson(currentPc.skillsJson)
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("技能列表", style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        if (skills.isEmpty()) {
                            Text(
                                "暂无技能数据",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            skills.forEach { (name, value) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(name, style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        value,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Inventory list
            item {
                val inventory = JsonUtil.parseStringArray(currentPc.inventoryJson)
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("物品清单", style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        if (inventory.isEmpty()) {
                            Text(
                                "暂无物品",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            inventory.forEach { item ->
                                Text("· $item", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }

            // Background
            if (currentPc.background.isNotBlank()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("背景故事", style = MaterialTheme.typography.titleSmall)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(currentPc.background, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            // KP Notes
            if (currentPc.kpNotes.isNotBlank()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("KP备注", style = MaterialTheme.typography.titleSmall)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(currentPc.kpNotes, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

        }
    }

    // Edit PC bottom sheet
    val editPc = pc
    if (showEditSheet && editPc != null) {
        EditPcSheet(
            pc = editPc,
            onDismiss = { showEditSheet = false },
            onSave = { updatedPc ->
                viewModel.updatePc(updatedPc)
                showEditSheet = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditPcSheet(
    pc: PlayerCharacterEntity,
    onDismiss: () -> Unit,
    onSave: (PlayerCharacterEntity) -> Unit
) {
    var playerName by remember { mutableStateOf(pc.playerName) }
    var characterName by remember { mutableStateOf(pc.characterName) }
    var hpCurrent by remember { mutableStateOf(pc.hpCurrent.toString()) }
    var hpMax by remember { mutableStateOf(pc.hpMax.toString()) }
    var sanCurrent by remember { mutableStateOf(pc.sanCurrent.toString()) }
    var sanMax by remember { mutableStateOf(pc.sanMax.toString()) }
    var luck by remember { mutableStateOf(pc.luck.toString()) }
    var status by remember { mutableStateOf(pc.status) }
    var gender by remember { mutableStateOf(pc.gender) }
    var genderExpanded by remember { mutableStateOf(false) }
    var background by remember { mutableStateOf(pc.background) }
    var kpNotes by remember { mutableStateOf(pc.kpNotes) }
    var skillsJson by remember { mutableStateOf(pc.skillsJson) }
    var inventoryJson by remember { mutableStateOf(pc.inventoryJson) }

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
                Text("编辑PC", style = MaterialTheme.typography.titleLarge)
            }
            item {
                OutlinedTextField(
                    value = playerName,
                    onValueChange = { playerName = it },
                    label = { Text("玩家昵称") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            item {
                OutlinedTextField(
                    value = characterName,
                    onValueChange = { characterName = it },
                    label = { Text("角色名称") },
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
                        value = Gender.fromKey(gender)?.label ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("性别") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = genderExpanded,
                        onDismissRequest = { genderExpanded = false }
                    ) {
                        Gender.entries.forEach { g ->
                            DropdownMenuItem(
                                text = { Text(g.label) },
                                onClick = { gender = g.key; genderExpanded = false }
                            )
                        }
                    }
                }
            }
            item {
                Text("状态", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        Constants.PC_STATUS_NORMAL to "正常",
                        Constants.PC_STATUS_WOUNDED to "重伤",
                        Constants.PC_STATUS_INSANE to "疯狂",
                        Constants.PC_STATUS_DEAD to "死亡"
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
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = hpCurrent,
                        onValueChange = { hpCurrent = it },
                        label = { Text("HP当前") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = hpMax,
                        onValueChange = { hpMax = it },
                        label = { Text("HP上限") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = sanCurrent,
                        onValueChange = { sanCurrent = it },
                        label = { Text("SAN当前") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = sanMax,
                        onValueChange = { sanMax = it },
                        label = { Text("SAN上限") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
            item {
                OutlinedTextField(
                    value = luck,
                    onValueChange = { luck = it },
                    label = { Text("幸运值") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            item {
                OutlinedTextField(
                    value = background,
                    onValueChange = { background = it },
                    label = { Text("背景故事") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
            item {
                OutlinedTextField(
                    value = skillsJson,
                    onValueChange = { skillsJson = it },
                    label = { Text("技能JSON (如 {\"射击\":60,\"聆听\":70})") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
            item {
                OutlinedTextField(
                    value = inventoryJson,
                    onValueChange = { inventoryJson = it },
                    label = { Text("物品JSON (如 [\"绳索\",\"手电筒\"])") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
            item {
                OutlinedTextField(
                    value = kpNotes,
                    onValueChange = { kpNotes = it },
                    label = { Text("KP备注") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
            item {
                Button(
                    onClick = {
                        onSave(
                            pc.copy(
                                playerName = playerName.trim(),
                                characterName = characterName.trim(),
                                hpCurrent = hpCurrent.toIntOrNull() ?: pc.hpCurrent,
                                hpMax = hpMax.toIntOrNull() ?: pc.hpMax,
                                sanCurrent = sanCurrent.toIntOrNull() ?: pc.sanCurrent,
                                sanMax = sanMax.toIntOrNull() ?: pc.sanMax,
                                luck = luck.toIntOrNull() ?: pc.luck,
                                status = status,
                                gender = gender,
                                background = background.trim(),
                                skillsJson = skillsJson.trim(),
                                inventoryJson = inventoryJson.trim(),
                                kpNotes = kpNotes.trim()
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("保存")
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge)
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
