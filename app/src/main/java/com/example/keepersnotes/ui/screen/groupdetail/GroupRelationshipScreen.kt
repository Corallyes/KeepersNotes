package com.example.keepersnotes.ui.screen.groupdetail

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.keepersnotes.data.local.entity.GroupRelationshipEntity
import com.example.keepersnotes.ui.component.CompactTopBar

data class GroupGraphNode(
    val id: String,
    val name: String,
    val type: String, // pc, npc
    var position: Offset = Offset.Zero
)

data class GroupGraphEdge(
    val sourceId: String,
    val targetId: String,
    val relationType: String,
    val description: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupRelationshipScreen(
    groupId: String,
    onBack: () -> Unit,
    viewModel: GroupDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedNode by remember { mutableStateOf<GroupGraphNode?>(null) }

    // Build graph nodes from PC and NPC
    val nodes = remember(uiState) {
        val nodeList = mutableListOf<GroupGraphNode>()
        uiState.pcs.forEach { nodeList.add(GroupGraphNode(it.pcId, it.characterName, "pc")) }
        uiState.npcs.forEach { nodeList.add(GroupGraphNode(it.npcId, it.name, "npc")) }
        // Assign initial positions in a circle
        val centerX = 500f
        val centerY = 500f
        val radius = 200f
        nodeList.forEachIndexed { index, node ->
            val angle = (2 * Math.PI * index / nodeList.size).toFloat()
            node.position = Offset(
                centerX + radius * kotlin.math.cos(angle),
                centerY + radius * kotlin.math.sin(angle)
            )
        }
        nodeList
    }

    // Build edges
    val edges = remember(uiState) {
        uiState.relationships.map { rel ->
            GroupGraphEdge(rel.sourceId, rel.targetId, rel.relationType, rel.description)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            CompactTopBar(
                title = "团关系图谱",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        floatingActionButton = {
            if (nodes.size >= 2) {
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "添加关系")
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            if (nodes.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.AccountTree,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "暂无关系数据",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "请先在团详情中添加PC或NPC角色",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                GroupRelationshipGraph(
                    nodes = nodes,
                    edges = edges,
                    onNodeClick = { selectedNode = it }
                )
            }
        }
    }

    // Add relationship dialog
    if (showAddDialog) {
        AddGroupRelationshipDialog(
            nodes = nodes,
            onDismiss = { showAddDialog = false },
            onAdd = { sourceId, sourceType, targetId, targetType, relationType, description ->
                viewModel.createRelationship(sourceId, sourceType, targetId, targetType, relationType, description)
                showAddDialog = false
            }
        )
    }

    // Node detail dialog
    selectedNode?.let { node ->
        val entity = findGroupEntity(uiState, node.id, node.type)
        AlertDialog(
            onDismissRequest = { selectedNode = null },
            title = { Text(node.name) },
            text = {
                Column {
                    Text("类型: ${if (node.type == "pc") "PC" else "NPC"}", style = MaterialTheme.typography.bodyMedium)
                    if (entity != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(entity, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedNode = null }) { Text("关闭") }
            }
        )
    }
}

@Composable
private fun GroupRelationshipGraph(
    nodes: List<GroupGraphNode>,
    edges: List<GroupGraphEdge>,
    onNodeClick: (GroupGraphNode) -> Unit
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                translationX = offsetX,
                translationY = offsetY
            )
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(0.5f, 3f)
                    offsetX += pan.x
                    offsetY += pan.y
                }
            }
    ) {
        // Draw edges
        edges.forEach { edge ->
            val source = nodes.find { it.id == edge.sourceId }
            val target = nodes.find { it.id == edge.targetId }
            if (source != null && target != null) {
                drawLine(
                    color = Color.Gray,
                    start = source.position,
                    end = target.position,
                    strokeWidth = 2f
                )
                // Draw relation type label
                val midPoint = Offset(
                    (source.position.x + target.position.x) / 2,
                    (source.position.y + target.position.y) / 2
                )
                drawContext.canvas.nativeCanvas.apply {
                    val paint = android.graphics.Paint().apply {
                        color = android.graphics.Color.GRAY
                        textSize = 24f
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                    drawText(edge.relationType, midPoint.x, midPoint.y, paint)
                }
            }
        }

        // Draw nodes
        nodes.forEach { node ->
            val color = when (node.type) {
                "pc" -> Color(0xFF2196F3)
                "npc" -> Color(0xFF4CAF50)
                else -> Color.Gray
            }
            drawCircle(
                color = color,
                radius = 30f,
                center = node.position
            )
            // Draw node name
            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    this.color = android.graphics.Color.WHITE
                    textSize = 20f
                    textAlign = android.graphics.Paint.Align.CENTER
                    isFakeBoldText = true
                }
                drawText(node.name, node.position.x, node.position.y + 8f, paint)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddGroupRelationshipDialog(
    nodes: List<GroupGraphNode>,
    onDismiss: () -> Unit,
    onAdd: (String, String, String, String, String, String) -> Unit
) {
    var sourceId by remember { mutableStateOf("") }
    var targetId by remember { mutableStateOf("") }
    var relationType by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var sourceExpanded by remember { mutableStateOf(false) }
    var targetExpanded by remember { mutableStateOf(false) }

    val typeLabel = { type: String ->
        when (type) {
            "pc" -> "PC"
            "npc" -> "NPC"
            else -> type
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加关系") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Source selector
                ExposedDropdownMenuBox(
                    expanded = sourceExpanded,
                    onExpandedChange = { sourceExpanded = it }
                ) {
                    OutlinedTextField(
                        value = nodes.find { it.id == sourceId }?.let { "${it.name} (${typeLabel(it.type)})" } ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("源角色") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sourceExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = sourceExpanded,
                        onDismissRequest = { sourceExpanded = false }
                    ) {
                        nodes.filter { it.id != targetId }.forEach { node ->
                            DropdownMenuItem(
                                text = { Text("${node.name} (${typeLabel(node.type)})") },
                                onClick = {
                                    sourceId = node.id
                                    sourceExpanded = false
                                }
                            )
                        }
                    }
                }
                // Target selector
                ExposedDropdownMenuBox(
                    expanded = targetExpanded,
                    onExpandedChange = { targetExpanded = it }
                ) {
                    OutlinedTextField(
                        value = nodes.find { it.id == targetId }?.let { "${it.name} (${typeLabel(it.type)})" } ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("目标角色") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = targetExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = targetExpanded,
                        onDismissRequest = { targetExpanded = false }
                    ) {
                        nodes.filter { it.id != sourceId }.forEach { node ->
                            DropdownMenuItem(
                                text = { Text("${node.name} (${typeLabel(node.type)})") },
                                onClick = {
                                    targetId = node.id
                                    targetExpanded = false
                                }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = relationType,
                    onValueChange = { relationType = it },
                    label = { Text("关系类型（如：盟友/敌人/亲属/同事）") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("描述") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val source = nodes.find { it.id == sourceId }
                    val target = nodes.find { it.id == targetId }
                    if (source != null && target != null) {
                        onAdd(sourceId, source.type, targetId, target.type, relationType, description)
                    }
                },
                enabled = sourceId.isNotBlank() && targetId.isNotBlank() && relationType.isNotBlank()
            ) { Text("添加") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

private fun findGroupEntity(uiState: GroupDetailUiState, id: String, type: String): String? {
    return when (type) {
        "pc" -> uiState.pcs.find { it.pcId == id }?.let { "玩家: ${it.playerName}\n${it.background}" }
        "npc" -> uiState.npcs.find { it.npcId == id }?.description
        else -> null
    }
}
