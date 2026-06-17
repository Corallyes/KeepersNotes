package com.example.keepersnotes.ui.screen.groupdetail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.keepersnotes.ui.component.CompactTopBar

data class GroupGraphNode(
    val id: String,
    val name: String,
    val type: String,
    val gender: String = "",
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
    var selectedEdge by remember { mutableStateOf<GroupGraphEdge?>(null) }
    var showPanel by remember { mutableStateOf(false) }
    var panelTab by remember { mutableIntStateOf(0) }

    val nodes = remember(uiState) {
        val nodeList = mutableListOf<GroupGraphNode>()
        uiState.pcs.forEach { nodeList.add(GroupGraphNode(it.pcId, it.characterName, "pc", it.gender)) }
        uiState.npcs.forEach { nodeList.add(GroupGraphNode(it.npcId, it.name, "npc", it.gender)) }
        val centerX = 500f; val centerY = 500f; val radius = 200f
        nodeList.forEachIndexed { index, node ->
            val angle = (2 * Math.PI * index / nodeList.size).toFloat()
            node.position = Offset(centerX + radius * kotlin.math.cos(angle), centerY + radius * kotlin.math.sin(angle))
        }
        nodeList
    }

    val edges = remember(uiState) {
        uiState.relationships.map { GroupGraphEdge(it.sourceId, it.targetId, it.relationType, it.description) }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            CompactTopBar(
                title = "人物关系图谱",
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回") }
                },
                actions = {
                    IconButton(onClick = { showPanel = !showPanel }) {
                        Icon(if (showPanel) Icons.Default.ChevronRight else Icons.Default.List, contentDescription = "列表")
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
        Row(modifier = Modifier.fillMaxSize().padding(padding)) {
            Box(modifier = Modifier.weight(1f).fillMaxHeight().background(MaterialTheme.colorScheme.surface)) {
                if (nodes.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AccountTree, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("暂无关系数据", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("请先在团详情中添加PC或NPC角色", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    GroupRelationshipGraph(nodes = nodes, edges = edges)
                }
            }

            AnimatedVisibility(visible = showPanel) {
                Surface(modifier = Modifier.width(280.dp).fillMaxHeight(), tonalElevation = 2.dp) {
                    Column {
                        TabRow(selectedTabIndex = panelTab, modifier = Modifier.height(40.dp)) {
                            Tab(selected = panelTab == 0, onClick = { panelTab = 0 }, text = { Text("关系", style = MaterialTheme.typography.labelSmall) })
                            Tab(selected = panelTab == 1, onClick = { panelTab = 1 }, text = { Text("人物", style = MaterialTheme.typography.labelSmall) })
                        }
                        when (panelTab) {
                            0 -> {
                                if (edges.isEmpty()) {
                                    Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                                        Text("暂无关系", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                } else {
                                    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        items(edges) { edge ->
                                            val src = nodes.find { it.id == edge.sourceId }
                                            val tgt = nodes.find { it.id == edge.targetId }
                                            Card(modifier = Modifier.fillMaxWidth().clickable { selectedEdge = edge }) {
                                                Column(modifier = Modifier.padding(10.dp)) {
                                                    Text("${src?.name ?: "?"} → ${tgt?.name ?: "?"}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                    Text(edge.relationType, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                                    if (edge.description.isNotBlank()) {
                                                        Text(edge.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            1 -> {
                                LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    items(nodes) { node ->
                                        Card(modifier = Modifier.fillMaxWidth()) {
                                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(groupGenderColor(node.gender)))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column {
                                                    Text(node.name, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                                                    Text(if (node.type == "pc") "PC" else "NPC", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

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

    selectedEdge?.let { edge ->
        var editMode by remember { mutableStateOf(false) }
        var editRelationType by remember { mutableStateOf(edge.relationType) }
        var editDescription by remember { mutableStateOf(edge.description) }
        val sourceNode = nodes.find { it.id == edge.sourceId }
        val targetNode = nodes.find { it.id == edge.targetId }
        val relationshipEntity = uiState.relationships.find {
            it.sourceId == edge.sourceId && it.targetId == edge.targetId && it.relationType == edge.relationType
        }

        if (editMode) {
            AlertDialog(
                onDismissRequest = { editMode = false; selectedEdge = null },
                title = { Text("编辑关系") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("${sourceNode?.name ?: "?"} → ${targetNode?.name ?: "?"}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        OutlinedTextField(value = editRelationType, onValueChange = { editRelationType = it }, label = { Text("关系类型") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = editDescription, onValueChange = { editDescription = it }, label = { Text("描述") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        relationshipEntity?.let { viewModel.updateRelationship(it.copy(relationType = editRelationType, description = editDescription)) }
                        editMode = false; selectedEdge = null
                    }) { Text("保存") }
                },
                dismissButton = {
                    Row {
                        TextButton(onClick = { relationshipEntity?.let { viewModel.deleteRelationship(it.id) }; editMode = false; selectedEdge = null }) { Text("删除", color = MaterialTheme.colorScheme.error) }
                        TextButton(onClick = { editMode = false; selectedEdge = null }) { Text("取消") }
                    }
                }
            )
        } else {
            AlertDialog(
                onDismissRequest = { selectedEdge = null },
                title = { Text(edge.relationType) },
                text = {
                    Column {
                        Text("${sourceNode?.name ?: "?"} → ${targetNode?.name ?: "?"}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        if (edge.description.isNotBlank()) Text(edge.description, style = MaterialTheme.typography.bodySmall)
                        else Text("暂无描述", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                confirmButton = { TextButton(onClick = { editMode = true }) { Text("编辑") } },
                dismissButton = { TextButton(onClick = { selectedEdge = null }) { Text("关闭") } }
            )
        }
    }
}

@Composable
private fun GroupRelationshipGraph(
    nodes: List<GroupGraphNode>,
    edges: List<GroupGraphEdge>
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { centroid, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(0.5f, 3f)
                    offsetX = (offsetX + centroid.x) * zoom - centroid.x + pan.x
                    offsetY = (offsetY + centroid.y) * zoom - centroid.y + pan.y
                }
            }
    ) {
        drawContext.canvas.nativeCanvas.save()
        drawContext.canvas.nativeCanvas.translate(offsetX, offsetY)
        drawContext.canvas.nativeCanvas.scale(scale, scale)

        edges.forEach { edge ->
            val source = nodes.find { it.id == edge.sourceId }
            val target = nodes.find { it.id == edge.targetId }
            if (source != null && target != null) {
                val dir = Offset(target.position.x - source.position.x, target.position.y - source.position.y)
                val len = kotlin.math.sqrt(dir.x * dir.x + dir.y * dir.y)
                val nodeRadius = 30f; val arrowLen = 16f; val arrowAngle = 0.45f
                if (len > 0f) {
                    val ux = dir.x / len; val uy = dir.y / len
                    val start = Offset(source.position.x + ux * nodeRadius, source.position.y + uy * nodeRadius)
                    val end = Offset(target.position.x - ux * nodeRadius, target.position.y - uy * nodeRadius)
                    drawLine(color = Color.Gray, start = start, end = end, strokeWidth = 2f)
                    val leftX = end.x - arrowLen * (ux * kotlin.math.cos(arrowAngle) - uy * kotlin.math.sin(arrowAngle))
                    val leftY = end.y - arrowLen * (ux * kotlin.math.sin(arrowAngle) + uy * kotlin.math.cos(arrowAngle))
                    val rightX = end.x - arrowLen * (ux * kotlin.math.cos(-arrowAngle) - uy * kotlin.math.sin(-arrowAngle))
                    val rightY = end.y - arrowLen * (ux * kotlin.math.sin(-arrowAngle) + uy * kotlin.math.cos(-arrowAngle))
                    drawLine(color = Color.Gray, start = end, end = Offset(leftX, leftY), strokeWidth = 3f)
                    drawLine(color = Color.Gray, start = end, end = Offset(rightX, rightY), strokeWidth = 3f)
                }
                val midPoint = Offset((source.position.x + target.position.x) / 2, (source.position.y + target.position.y) / 2)
                drawContext.canvas.nativeCanvas.apply {
                    val bgPaint = android.graphics.Paint().apply { color = android.graphics.Color.argb(180, 245, 245, 245); textSize = 24f; textAlign = android.graphics.Paint.Align.CENTER }
                    val textPaint = android.graphics.Paint().apply { color = android.graphics.Color.DKGRAY; textSize = 24f; textAlign = android.graphics.Paint.Align.CENTER; isFakeBoldText = true }
                    val tw = textPaint.measureText(edge.relationType)
                    drawRoundRect(android.graphics.RectF(midPoint.x - tw / 2 - 8f, midPoint.y - 18f, midPoint.x + tw / 2 + 8f, midPoint.y + 8f), 6f, 6f, bgPaint)
                    drawText(edge.relationType, midPoint.x, midPoint.y, textPaint)
                }
            }
        }

        nodes.forEach { node ->
            drawCircle(color = groupGenderColor(node.gender), radius = 30f, center = node.position)
            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply { this.color = android.graphics.Color.WHITE; textSize = 20f; textAlign = android.graphics.Paint.Align.CENTER; isFakeBoldText = true }
                drawText(node.name, node.position.x, node.position.y + 8f, paint)
            }
        }

        drawContext.canvas.nativeCanvas.restore()
    }
}

private fun groupGenderColor(gender: String): Color {
    return when (gender) {
        "male" -> Color(0xFF2196F3)
        "female" -> Color(0xFFE91E63)
        "alien" -> Color(0xFF4CAF50)
        "other" -> Color(0xFFFF9800)
        else -> Color(0xFF9E9E9E)
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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加关系") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ExposedDropdownMenuBox(expanded = sourceExpanded, onExpandedChange = { sourceExpanded = it }) {
                    OutlinedTextField(
                        value = nodes.find { it.id == sourceId }?.name ?: "",
                        onValueChange = {}, readOnly = true,
                        label = { Text("源角色") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sourceExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = sourceExpanded, onDismissRequest = { sourceExpanded = false }) {
                        nodes.filter { it.id != targetId }.forEach { node ->
                            DropdownMenuItem(text = { Text("${node.name} (${if (node.type == "pc") "PC" else "NPC"})") }, onClick = { sourceId = node.id; sourceExpanded = false })
                        }
                    }
                }
                ExposedDropdownMenuBox(expanded = targetExpanded, onExpandedChange = { targetExpanded = it }) {
                    OutlinedTextField(
                        value = nodes.find { it.id == targetId }?.name ?: "",
                        onValueChange = {}, readOnly = true,
                        label = { Text("目标角色") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = targetExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = targetExpanded, onDismissRequest = { targetExpanded = false }) {
                        nodes.filter { it.id != sourceId }.forEach { node ->
                            DropdownMenuItem(text = { Text("${node.name} (${if (node.type == "pc") "PC" else "NPC"})") }, onClick = { targetId = node.id; targetExpanded = false })
                        }
                    }
                }
                OutlinedTextField(value = relationType, onValueChange = { relationType = it }, label = { Text("关系类型") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("描述") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val source = nodes.find { it.id == sourceId }
                    val target = nodes.find { it.id == targetId }
                    if (source != null && target != null) onAdd(sourceId, source.type, targetId, target.type, relationType, description)
                },
                enabled = sourceId.isNotBlank() && targetId.isNotBlank() && relationType.isNotBlank()
            ) { Text("添加") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}

private fun findGroupEntity(uiState: GroupDetailUiState, id: String, type: String): String? {
    return when (type) {
        "pc" -> uiState.pcs.find { it.pcId == id }?.let { "玩家: ${it.playerName}\n${it.background}" }
        "npc" -> uiState.npcs.find { it.npcId == id }?.description
        else -> null
    }
}
