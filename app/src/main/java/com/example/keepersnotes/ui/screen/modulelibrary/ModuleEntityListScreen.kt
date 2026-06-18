package com.example.keepersnotes.ui.screen.modulelibrary

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.keepersnotes.data.local.entity.ModuleClueEntity
import com.example.keepersnotes.data.local.entity.ModuleDefaultNpcEntity
import com.example.keepersnotes.data.local.entity.ModuleLocationEntity
import com.example.keepersnotes.data.local.entity.ModuleOrganizationEntity
import com.example.keepersnotes.ui.component.CompactTopBar
import com.example.keepersnotes.util.LocalizedStrings

enum class EntityType(val labelRes: () -> String) {
    PC({ LocalizedStrings.entityPc }),
    NPC({ LocalizedStrings.entityNpc }),
    LOCATION({ LocalizedStrings.entityLocation }),
    ORGANIZATION({ LocalizedStrings.entityOrganization }),
    CLUE({ LocalizedStrings.entityClue })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModuleEntityListScreen(
    entityType: EntityType,
    onBack: () -> Unit,
    viewModel: ModuleDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingEntity by remember { mutableStateOf<Any?>(null) }
    var colorPickerClue by remember { mutableStateOf<ModuleClueEntity?>(null) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            CompactTopBar(
                title = entityType.labelRes(),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = LocalizedStrings.back)
                    }
                }
            )
        },
        floatingActionButton = {
            if (entityType != EntityType.PC) {
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = LocalizedStrings.add)
                }
            }
        }
    ) { padding ->
        val items = when (entityType) {
            EntityType.PC -> uiState.defaultPcs
            EntityType.NPC -> uiState.defaultNpcs
            EntityType.LOCATION -> uiState.locations
            EntityType.ORGANIZATION -> uiState.organizations
            EntityType.CLUE -> uiState.clues
        }

        if (items.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding).padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        when (entityType) {
                            EntityType.PC -> Icons.Default.Person
                            EntityType.NPC -> Icons.Default.People
                            EntityType.LOCATION -> Icons.Default.LocationOn
                            EntityType.ORGANIZATION -> Icons.Default.Business
                            EntityType.CLUE -> Icons.Default.Search
                        },
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "${LocalizedStrings.entityNoItems} - ${entityType.labelRes()}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items) { entity ->
                    when (entityType) {
                        EntityType.PC -> {
                            val pc = entity as com.example.keepersnotes.data.local.entity.ModuleDefaultPcEntity
                            EntityListItem(
                                name = pc.name,
                                subtitle = pc.playerName.ifBlank { pc.system },
                                icon = Icons.Default.Person,
                                iconTint = Color(0xFF2196F3),
                                onClick = {} // 暂不开放详情
                            )
                        }
                        EntityType.NPC -> {
                            val npc = entity as ModuleDefaultNpcEntity
                            EntityListItem(
                                name = npc.name,
                                subtitle = npc.occupation.ifBlank { npc.alias },
                                icon = Icons.Default.Person,
                                iconTint = Color(0xFF4CAF50),
                                onClick = { editingEntity = npc }
                            )
                        }
                        EntityType.LOCATION -> {
                            val location = entity as ModuleLocationEntity
                            EntityListItem(
                                name = location.name,
                                subtitle = location.type,
                                icon = Icons.Default.LocationOn,
                                iconTint = Color(0xFFFF9800),
                                onClick = { editingEntity = location }
                            )
                        }
                        EntityType.ORGANIZATION -> {
                            val org = entity as ModuleOrganizationEntity
                            EntityListItem(
                                name = org.name,
                                subtitle = org.type,
                                icon = Icons.Default.Business,
                                iconTint = Color(0xFF9C27B0),
                                onClick = { editingEntity = org }
                            )
                        }
                        EntityType.CLUE -> {
                            val clue = entity as ModuleClueEntity
                            ClueListItem(
                                clue = clue,
                                onClick = { editingEntity = clue },
                                onLongClick = { colorPickerClue = clue }
                            )
                        }
                    }
                }
            }
        }
    }

    // Add dialog
    if (showAddDialog) {
        when (entityType) {
            EntityType.PC -> {} // 暂不开放
            EntityType.NPC -> {
                NpcEditDialog(
                    onDismiss = { showAddDialog = false },
                    onSave = { name, alias, occupation, description, truePurpose, gender ->
                        viewModel.createDefaultNpc(name, alias, occupation, description, truePurpose, gender)
                        showAddDialog = false
                    }
                )
            }
            EntityType.LOCATION -> {
                LocationEditDialog(
                    onDismiss = { showAddDialog = false },
                    onSave = { name, type, description, clues, inhabitants ->
                        viewModel.createLocation(name, type, description, clues, inhabitants)
                        showAddDialog = false
                    }
                )
            }
            EntityType.ORGANIZATION -> {
                OrganizationEditDialog(
                    onDismiss = { showAddDialog = false },
                    onSave = { name, type, description, members, goals ->
                        viewModel.createOrganization(name, type, description, members, goals)
                        showAddDialog = false
                    }
                )
            }
            EntityType.CLUE -> {
                ClueEditDialog(
                    onDismiss = { showAddDialog = false },
                    onSave = { name, type, description, source ->
                        viewModel.createClue(name, type, description, source, false)
                        showAddDialog = false
                    }
                )
            }
        }
    }

    // Edit dialog
    editingEntity?.let { entity ->
        when (entityType) {
            EntityType.PC -> {} // 暂不开放
            EntityType.NPC -> {
                val npc = entity as ModuleDefaultNpcEntity
                NpcEditDialog(
                    npc = npc,
                    onDismiss = { editingEntity = null },
                    onSave = { name, alias, occupation, description, truePurpose, gender ->
                        viewModel.updateDefaultNpc(npc.copy(name = name, alias = alias, occupation = occupation, description = description, truePurpose = truePurpose, gender = gender))
                        editingEntity = null
                    },
                    onDelete = {
                        viewModel.deleteDefaultNpc(npc.id)
                        editingEntity = null
                    }
                )
            }
            EntityType.LOCATION -> {
                val location = entity as ModuleLocationEntity
                LocationEditDialog(
                    location = location,
                    onDismiss = { editingEntity = null },
                    onSave = { name, type, description, clues, inhabitants ->
                        viewModel.updateLocation(location.copy(name = name, type = type, description = description, clues = clues, inhabitants = inhabitants))
                        editingEntity = null
                    },
                    onDelete = {
                        viewModel.deleteLocation(location.id)
                        editingEntity = null
                    }
                )
            }
            EntityType.ORGANIZATION -> {
                val org = entity as ModuleOrganizationEntity
                OrganizationEditDialog(
                    organization = org,
                    onDismiss = { editingEntity = null },
                    onSave = { name, type, description, members, goals ->
                        viewModel.updateOrganization(org.copy(name = name, type = type, description = description, members = members, goals = goals))
                        editingEntity = null
                    },
                    onDelete = {
                        viewModel.deleteOrganization(org.id)
                        editingEntity = null
                    }
                )
            }
            EntityType.CLUE -> {
                val clue = entity as ModuleClueEntity
                ClueEditDialog(
                    clue = clue,
                    onDismiss = { editingEntity = null },
                    onSave = { name, type, description, source ->
                        viewModel.updateClue(clue.copy(name = name, type = type, description = description, source = source))
                        editingEntity = null
                    },
                    onDelete = {
                        viewModel.deleteClue(clue.id)
                        editingEntity = null
                    }
                )
            }
        }
    }

    // Color picker dialog for clues
    colorPickerClue?.let { clue ->
        AlertDialog(
            onDismissRequest = { colorPickerClue = null },
            title = { Text(LocalizedStrings.entityMarkColor) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        clue.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    clueColors.forEach { (colorValue, colorName) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.updateClue(clue.copy(color = colorValue))
                                    colorPickerClue = null
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (colorValue != 0L) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(Color(colorValue))
                                )
                            } else {
                                Icon(
                                    Icons.Default.HighlightOff,
                                    contentDescription = null,
                                    modifier = Modifier.size(28.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(colorName, style = MaterialTheme.typography.bodyLarge)
                            if (clue.color == colorValue) {
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { colorPickerClue = null }) {
                    Text(LocalizedStrings.cancel)
                }
            }
        )
    }
}

@Composable
private fun EntityListItem(
    name: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                if (subtitle.isNotBlank()) {
                    Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private val clueColors: List<Pair<Long, String>>
    get() = listOf(
        0L to LocalizedStrings.entityNoColor,
        0xFFFFEB3B to LocalizedStrings.entityColorYellow,
        0xFF4CAF50 to LocalizedStrings.entityColorGreen,
        0xFF2196F3 to LocalizedStrings.entityColorBlue,
        0xFFFF9800 to LocalizedStrings.entityColorOrange,
        0xFFE91E63 to LocalizedStrings.entityColorPink
    )

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ClueListItem(
    clue: ModuleClueEntity,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val hasColor = clue.color != 0L
    val cardColor = if (hasColor) Color(clue.color) else MaterialTheme.colorScheme.surfaceContainerLow
    // 文字对比色：有色背景用白色，无色背景用默认
    val textColor = if (hasColor) Color.White else MaterialTheme.colorScheme.onSurface
    val subTextColor = if (hasColor) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
    val iconTint = if (hasColor) Color.White else MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Search, contentDescription = null, tint = if (hasColor) Color.White.copy(alpha = 0.8f) else Color(0xFFE91E63), modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = clue.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, color = textColor)
                val subtitle = clue.type.ifBlank { clue.source }
                if (subtitle.isNotBlank()) {
                    Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = subTextColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = iconTint)
        }
    }
}
