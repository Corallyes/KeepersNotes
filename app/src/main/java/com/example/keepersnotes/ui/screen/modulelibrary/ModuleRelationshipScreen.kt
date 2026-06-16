package com.example.keepersnotes.ui.screen.modulelibrary

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.keepersnotes.data.local.entity.ModuleRelationshipEntity
import com.example.keepersnotes.ui.component.CompactTopBar

data class GraphNode(
    val id: String,
    val name: String,
    val type: String,
    var position: Offset = Offset.Zero
)

data class GraphEdge(
    val sourceId: String,
    val targetId: String,
    val relationType: String,
    val description: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModuleRelationshipScreen(
    moduleId: String,
    onBack: () -> Unit,
    viewModel: ModuleDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedNode by remember { mutableStateOf<GraphNode?>(null) }

    // Build graph nodes from entities
    val nodes = remember(uiState) {
        val nodeList = mutableListOf<GraphNode>()
        uiState.defaultPcs.forEach { nodeList.add(GraphNode(it.id, it.name, "pc")) }
        uiState.defaultNpcs.forEach { nodeList.add(GraphNode(it.id, it.name, "npc")) }
        uiState.locations.forEach { nodeList.add(GraphNode(it.id, it.name, "location")) }
        uiState.organizations.forEach { nodeList.add(GraphNode(it.id, it.name, "organization")) }
        uiState.clues.forEach { nodeList.add(GraphNode(it.id, it.name, "clue")) }
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
            GraphEdge(rel.sourceId, rel.targetId, rel.relationType, rel.description)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            CompactTopBar(
                title = "关系图谱",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "添加关系")
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
                            "请先在模组设置中添加PC、NPC、地点、组织或线索",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                RelationshipGraph(
                    nodes = nodes,
                    edges = edges,
                    onNodeClick = { selectedNode = it }
                )
            }
        }
    }

    // Add relationship dialog
    if (showAddDialog) {
        AddRelationshipDialog(
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
        val entity = findEntity(uiState, node.id, node.type)
        AlertDialog(
            onDismissRequest = { selectedNode = null },
            title = { Text(node.name) },
            text = {
                Column {
                    Text("类型: ${node.type}", style = MaterialTheme.typography.bodyMedium)
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
private fun RelationshipGraph(
    nodes: List<GraphNode>,
    edges: List<GraphEdge>,
    onNodeClick: (GraphNode) -> Unit
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var draggedNode by remember { mutableStateOf<GraphNode?>(null) }

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
                "location" -> Color(0xFFFF9800)
                "organization" -> Color(0xFF9C27B0)
                "clue" -> Color(0xFFE91E63)
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
private fun AddRelationshipDialog(
    nodes: List<GraphNode>,
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
            "location" -> "地点"
            "organization" -> "组织"
            "clue" -> "线索"
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
                        label = { Text("源实体") },
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
                        label = { Text("目标实体") },
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

private fun findEntity(uiState: ModuleDetailUiState, id: String, type: String): String? {
    return when (type) {
        "pc" -> uiState.defaultPcs.find { it.id == id }?.description
        "npc" -> uiState.defaultNpcs.find { it.id == id }?.description
        "location" -> uiState.locations.find { it.id == id }?.description
        "organization" -> uiState.organizations.find { it.id == id }?.description
        "clue" -> uiState.clues.find { it.id == id }?.description
        else -> null
    }
}
