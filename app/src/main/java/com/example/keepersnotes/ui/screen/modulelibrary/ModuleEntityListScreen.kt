package com.example.keepersnotes.ui.screen.modulelibrary

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

enum class EntityType(val label: String) {
    PC("推荐PC"),
    NPC("默认NPC"),
    LOCATION("地点"),
    ORGANIZATION("组织"),
    CLUE("线索")
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

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            CompactTopBar(
                title = entityType.label,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        floatingActionButton = {
            if (entityType != EntityType.PC) {
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "添加")
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
                        "暂无${entityType.label}",
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
                            EntityListItem(
                                name = clue.name,
                                subtitle = clue.type.ifBlank { clue.source },
                                icon = Icons.Default.Search,
                                iconTint = Color(0xFFE91E63),
                                onClick = { editingEntity = clue }
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
                    onSave = { name, alias, occupation, description, truePurpose ->
                        viewModel.createDefaultNpc(name, alias, occupation, description, truePurpose)
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
                    onSave = { name, type, description, source, isHidden ->
                        viewModel.createClue(name, type, description, source, isHidden)
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
                    onSave = { name, alias, occupation, description, truePurpose ->
                        viewModel.updateDefaultNpc(npc.copy(name = name, alias = alias, occupation = occupation, description = description, truePurpose = truePurpose))
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
                    onSave = { name, type, description, source, isHidden ->
                        viewModel.updateClue(clue.copy(name = name, type = type, description = description, source = source, isHidden = isHidden))
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
