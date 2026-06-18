package com.example.keepersnotes.ui.screen.modulelibrary

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.RectF
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.window.Dialog
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
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.keepersnotes.ui.component.CompactTopBar
import com.example.keepersnotes.util.ForceLayoutEngine
import com.example.keepersnotes.util.LocalizedStrings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class GraphNode(
    val id: String,
    val name: String,
    val type: String,
    val gender: String = "",
    val pinned: Boolean = false,
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
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedEdge by remember { mutableStateOf<GraphEdge?>(null) }
    var showPanel by remember { mutableStateOf(false) }
    var panelTab by remember { mutableIntStateOf(0) } // 0=关系, 1=人物
    val snackbarHostState = remember { SnackbarHostState() }
    var isExporting by remember { mutableStateOf(false) }
    var exportTrigger by remember { mutableIntStateOf(0) }

    val edges = remember(uiState) {
        uiState.relationships.map { GraphEdge(it.sourceId, it.targetId, it.relationType, it.description) }
    }

    val baseNodes = remember(uiState) {
        val nodeList = mutableListOf<GraphNode>()
        uiState.defaultNpcs.forEach { nodeList.add(GraphNode(it.id, it.name, "npc", it.gender)) }
        nodeList.toList()
    }

    var graphNodes by remember { mutableStateOf<List<GraphNode>>(baseNodes) }
    var canvasSize by remember { mutableStateOf(Offset.Zero) }
    val engine = remember { ForceLayoutEngine() }

    // Merge new base data with existing positions; reset only when nodes are added/removed
    LaunchedEffect(baseNodes.map { it.id }, edges.map { it.sourceId + it.targetId }, canvasSize) {
        if (baseNodes.isNotEmpty() && canvasSize.x > 0f) {
            val posMap = graphNodes.associateBy { it.id }
            val inputNodes = baseNodes.map { node ->
                val existing = posMap[node.id]
                ForceLayoutEngine.InputNode(
                    id = node.id,
                    position = existing?.position ?: node.position,
                    pinned = existing?.pinned ?: node.pinned
                )
            }
            val inputEdges = edges.map { ForceLayoutEngine.InputEdge(it.sourceId, it.targetId) }
            val positions = withContext(Dispatchers.Default) {
                engine.simulate(inputNodes, inputEdges, canvasSize.x, canvasSize.y)
            }
            graphNodes = baseNodes.map { node ->
                val existing = posMap[node.id]
                node.copy(
                    pinned = existing?.pinned ?: node.pinned
                ).apply {
                    position = positions[node.id] ?: node.position
                }
            }
        }
    }

    // 导出图片
    LaunchedEffect(exportTrigger) {
        if (exportTrigger == 0) return@LaunchedEffect
        isExporting = true
        snackbarHostState.showSnackbar(LocalizedStrings.groupRelationshipExporting)
        val result = withContext(Dispatchers.IO) {
            try {
                val bitmap = renderModuleGraphToBitmap(graphNodes, edges, canvasSize)
                val uri = saveModuleBitmapToGallery(context, bitmap)
                bitmap.recycle()
                if (uri != null) Result.success(uri) else Result.failure(Exception("save failed"))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
        isExporting = false
        if (result.isSuccess) {
            snackbarHostState.showSnackbar(LocalizedStrings.groupRelationshipExportSuccess)
        } else {
            snackbarHostState.showSnackbar("${LocalizedStrings.groupRelationshipExportFail}: ${result.exceptionOrNull()?.message ?: ""}")
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CompactTopBar(
                title = LocalizedStrings.groupRelationshipTitle,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = LocalizedStrings.back)
                    }
                },
                actions = {
                    if (graphNodes.isNotEmpty()) {
                        IconButton(onClick = { exportTrigger++ }, enabled = !isExporting) {
                            if (isExporting) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.Image, contentDescription = LocalizedStrings.groupRelationshipExport)
                            }
                        }
                    }
                    IconButton(onClick = { showPanel = true }) {
                        Icon(Icons.Default.List, contentDescription = LocalizedStrings.back)
                    }
                }
            )
        },
        floatingActionButton = {
            if (graphNodes.size >= 2) {
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = null)
                }
            }
        }
    ) { padding ->
        // Graph area — full width
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.surface)
                .onGloballyPositioned { coordinates ->
                    canvasSize = Offset(
                        coordinates.size.width.toFloat(),
                        coordinates.size.height.toFloat()
                    )
                }
        ) {
            if (graphNodes.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.AccountTree, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(LocalizedStrings.relationshipNoData, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(LocalizedStrings.relationshipAddNpcHint, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                RelationshipGraph(
                    nodes = graphNodes,
                    edges = edges,
                    onNodeDragged = { nodeId, newPos ->
                        graphNodes = graphNodes.map {
                            if (it.id == nodeId) it.copy(pinned = true).apply { position = newPos } else it
                        }
                    },
                    onNodePinned = { nodeId, pinned ->
                        graphNodes = graphNodes.map {
                            if (it.id == nodeId) it.copy(pinned = pinned) else it
                        }
                    }
                )
            }
        }
    }

    // Centered panel dialog
    if (showPanel) {
        Dialog(onDismissRequest = { showPanel = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                tonalElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .fillMaxHeight(0.7f)
            ) {
                Column {
                    // Tab row
                    TabRow(selectedTabIndex = panelTab, modifier = Modifier.height(40.dp)) {
                        Tab(selected = panelTab == 0, onClick = { panelTab = 0 }, text = { Text(LocalizedStrings.relationshipTab, style = MaterialTheme.typography.labelSmall) })
                        Tab(selected = panelTab == 1, onClick = { panelTab = 1 }, text = { Text(LocalizedStrings.relationshipCharacters, style = MaterialTheme.typography.labelSmall) })
                    }
                    when (panelTab) {
                        0 -> {
                            if (edges.isEmpty()) {
                                Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                                    Text(LocalizedStrings.relationshipNoRelations, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    items(edges) { edge ->
                                        val src = graphNodes.find { it.id == edge.sourceId }
                                        val tgt = graphNodes.find { it.id == edge.targetId }
                                        Card(
                                            modifier = Modifier.fillMaxWidth().clickable { selectedEdge = edge; showPanel = false }
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Text(
                                                    "${src?.name ?: "?"} → ${tgt?.name ?: "?"}",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Medium,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    edge.relationType,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                if (edge.description.isNotBlank()) {
                                                    Text(
                                                        edge.description,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        maxLines = 2,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        1 -> {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items(graphNodes) { node ->
                                    Card(modifier = Modifier.fillMaxWidth()) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier.size(12.dp).clip(CircleShape).background(genderColor(node.gender))
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(node.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                                val desc = findEntity(uiState, node.id, node.type)
                                                if (desc != null) {
                                                    Text(desc, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                                }
                                            }
                                            IconButton(
                                                onClick = {
                                                    graphNodes = graphNodes.map {
                                                        if (it.id == node.id) it.copy(pinned = !it.pinned) else it
                                                    }
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.PushPin,
                                                    contentDescription = if (node.pinned) LocalizedStrings.relationshipUnpin else LocalizedStrings.relationshipPin,
                                                    modifier = Modifier.size(16.dp),
                                                    tint = if (node.pinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
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

    // Add relationship dialog
    if (showAddDialog) {
        AddRelationshipDialog(
            nodes = graphNodes,
            onDismiss = { showAddDialog = false },
            onAdd = { sourceId, sourceType, targetId, targetType, relationType, description ->
                viewModel.createRelationship(sourceId, sourceType, targetId, targetType, relationType, description)
                showAddDialog = false
            }
        )
    }

    // Edge detail/edit dialog
    selectedEdge?.let { edge ->
        var editMode by remember { mutableStateOf(false) }
        var editRelationType by remember { mutableStateOf(edge.relationType) }
        var editDescription by remember { mutableStateOf(edge.description) }
        val sourceNode = graphNodes.find { it.id == edge.sourceId }
        val targetNode = graphNodes.find { it.id == edge.targetId }
        val relationshipEntity = uiState.relationships.find {
            it.sourceId == edge.sourceId && it.targetId == edge.targetId && it.relationType == edge.relationType
        }

        if (editMode) {
            AlertDialog(
                onDismissRequest = { editMode = false; selectedEdge = null },
                title = { Text(LocalizedStrings.relationshipEditTitle) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("${sourceNode?.name ?: "?"} → ${targetNode?.name ?: "?"}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        OutlinedTextField(value = editRelationType, onValueChange = { editRelationType = it }, label = { Text(LocalizedStrings.relationshipType) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = editDescription, onValueChange = { editDescription = it }, label = { Text(LocalizedStrings.entityDescription) }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        relationshipEntity?.let { viewModel.updateRelationship(it.copy(relationType = editRelationType, description = editDescription)) }
                        editMode = false; selectedEdge = null
                    }) { Text(LocalizedStrings.save) }
                },
                dismissButton = {
                    Row {
                        TextButton(onClick = {
                            relationshipEntity?.let { viewModel.deleteRelationship(it.id) }
                            editMode = false; selectedEdge = null
                        }) { Text(LocalizedStrings.delete, color = MaterialTheme.colorScheme.error) }
                        TextButton(onClick = { editMode = false; selectedEdge = null }) { Text(LocalizedStrings.cancel) }
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
                        else Text(LocalizedStrings.relationshipNoDescription, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                confirmButton = { TextButton(onClick = { editMode = true }) { Text(LocalizedStrings.edit) } },
                dismissButton = { TextButton(onClick = { selectedEdge = null }) { Text(LocalizedStrings.close) } }
            )
        }
    }
}

@Composable
private fun RelationshipGraph(
    nodes: List<GraphNode>,
    edges: List<GraphEdge>,
    onNodeDragged: (nodeId: String, newPos: Offset) -> Unit = { _, _ -> },
    onNodePinned: (nodeId: String, pinned: Boolean) -> Unit = { _, _ -> }
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    val isDark = isSystemInDarkTheme()

    // Pre-build node lookup for hit testing
    val nodeIndex = remember(nodes) { nodes.associateBy { it.id } }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            // 1. Node drag (priority) — consumes event only when hitting a node
            .pointerInput(nodes) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val down = event.changes.firstOrNull { it.pressed } ?: continue
                        val graphX = (down.position.x - offsetX) / scale
                        val graphY = (down.position.y - offsetY) / scale
                        val hitNode = nodeIndex.values.find { node ->
                            val dx = node.position.x - graphX
                            val dy = node.position.y - graphY
                            dx * dx + dy * dy < 35f * 35f
                        }
                        if (hitNode != null) {
                            if (!hitNode.pinned) onNodePinned(hitNode.id, true)
                            down.consume()

                            var drag: PointerInputChange?
                            do {
                                drag = awaitPointerEvent().changes.firstOrNull { it.id == down.id }
                                if (drag != null && drag.pressed) {
                                    val gx = (drag.position.x - offsetX) / scale
                                    val gy = (drag.position.y - offsetY) / scale
                                    onNodeDragged(hitNode.id, Offset(gx, gy))
                                    drag.consume()
                                }
                            } while (drag != null && drag.pressed)
                        }
                    }
                }
            }
            // 2. Pan & zoom — only receives events not consumed by drag handler
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

        // Pre-build edge node lookup
        val edgeNodeMap = mutableMapOf<String, GraphNode>()
        edges.forEach { edge ->
            edgeNodeMap[edge.sourceId] = nodeIndex[edge.sourceId] ?: return@forEach
            edgeNodeMap[edge.targetId] = nodeIndex[edge.targetId] ?: return@forEach
        }

        // Draw edges with arrows
        edges.forEach { edge ->
            val source = edgeNodeMap[edge.sourceId]
            val target = edgeNodeMap[edge.targetId]
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
                    val bgColor = if (isDark) android.graphics.Color.argb(200, 50, 50, 50) else android.graphics.Color.argb(180, 245, 245, 245)
                    val textColor = if (isDark) android.graphics.Color.LTGRAY else android.graphics.Color.DKGRAY
                    val bgPaint = android.graphics.Paint().apply { color = bgColor; textSize = 24f; textAlign = android.graphics.Paint.Align.CENTER }
                    val textPaint = android.graphics.Paint().apply { color = textColor; textSize = 24f; textAlign = android.graphics.Paint.Align.CENTER; isFakeBoldText = true }
                    val tw = textPaint.measureText(edge.relationType)
                    drawRoundRect(android.graphics.RectF(midPoint.x - tw / 2 - 8f, midPoint.y - 18f, midPoint.x + tw / 2 + 8f, midPoint.y + 8f), 6f, 6f, bgPaint)
                    drawText(edge.relationType, midPoint.x, midPoint.y, textPaint)
                }
            }
        }

        // Draw nodes
        nodes.forEach { node ->
            drawCircle(color = genderColor(node.gender), radius = 30f, center = node.position)
            // Pinned indicator: outer ring
            if (node.pinned) {
                drawCircle(color = Color.White, radius = 33f, center = node.position, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f))
            }
            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply { this.color = android.graphics.Color.WHITE; textSize = 20f; textAlign = android.graphics.Paint.Align.CENTER; isFakeBoldText = true }
                drawText(node.name, node.position.x, node.position.y + 8f, paint)
            }
        }

        drawContext.canvas.nativeCanvas.restore()
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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(LocalizedStrings.relationshipAddTitle) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ExposedDropdownMenuBox(expanded = sourceExpanded, onExpandedChange = { sourceExpanded = it }) {
                    OutlinedTextField(
                        value = nodes.find { it.id == sourceId }?.name ?: "",
                        onValueChange = {}, readOnly = true,
                        label = { Text(LocalizedStrings.relationshipSourceEntity) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sourceExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = sourceExpanded, onDismissRequest = { sourceExpanded = false }) {
                        nodes.filter { it.id != targetId }.forEach { node ->
                            DropdownMenuItem(text = { Text(node.name) }, onClick = { sourceId = node.id; sourceExpanded = false })
                        }
                    }
                }
                ExposedDropdownMenuBox(expanded = targetExpanded, onExpandedChange = { targetExpanded = it }) {
                    OutlinedTextField(
                        value = nodes.find { it.id == targetId }?.name ?: "",
                        onValueChange = {}, readOnly = true,
                        label = { Text(LocalizedStrings.relationshipTargetEntity) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = targetExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = targetExpanded, onDismissRequest = { targetExpanded = false }) {
                        nodes.filter { it.id != sourceId }.forEach { node ->
                            DropdownMenuItem(text = { Text(node.name) }, onClick = { targetId = node.id; targetExpanded = false })
                        }
                    }
                }
                OutlinedTextField(value = relationType, onValueChange = { relationType = it }, label = { Text(LocalizedStrings.relationshipType) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text(LocalizedStrings.entityDescription) }, modifier = Modifier.fillMaxWidth(), minLines = 2)
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
            ) { Text(LocalizedStrings.add) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(LocalizedStrings.cancel) } }
    )
}

private fun genderColor(gender: String): Color {
    return when (gender) {
        "male" -> Color(0xFF2196F3)
        "female" -> Color(0xFFE91E63)
        "alien" -> Color(0xFF4CAF50)
        "other" -> Color(0xFFFF9800)
        else -> Color(0xFF9E9E9E)
    }
}

private fun genderColorInt(gender: String): Int {
    return when (gender) {
        "male" -> 0xFF2196F3.toInt()
        "female" -> 0xFFE91E63.toInt()
        "alien" -> 0xFF4CAF50.toInt()
        "other" -> 0xFFFF9800.toInt()
        else -> 0xFF9E9E9E.toInt()
    }
}

private fun renderModuleGraphToBitmap(
    nodes: List<GraphNode>,
    edges: List<GraphEdge>,
    canvasSize: Offset
): Bitmap {
    val w = canvasSize.x.toInt().coerceAtLeast(800)
    val h = canvasSize.y.toInt().coerceAtLeast(600)
    val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    val canvas = AndroidCanvas(bitmap)
    canvas.drawColor(AndroidColor.WHITE)

    val nodeRadius = 30f
    val arrowLen = 16f
    val arrowAngle = 0.45f
    val nodeIndex = nodes.associateBy { it.id }

    edges.forEach { edge ->
        val source = nodeIndex[edge.sourceId]
        val target = nodeIndex[edge.targetId]
        if (source != null && target != null) {
            val dirX = target.position.x - source.position.x
            val dirY = target.position.y - source.position.y
            val len = kotlin.math.sqrt(dirX * dirX + dirY * dirY)
            if (len > 0f) {
                val ux = dirX / len; val uy = dirY / len
                val startX = source.position.x + ux * nodeRadius
                val startY = source.position.y + uy * nodeRadius
                val endX = target.position.x - ux * nodeRadius
                val endY = target.position.y - uy * nodeRadius
                val linePaint = Paint().apply { color = AndroidColor.GRAY; strokeWidth = 2f; isAntiAlias = true }
                canvas.drawLine(startX, startY, endX, endY, linePaint)
                val leftX = endX - arrowLen * (ux * kotlin.math.cos(arrowAngle) - uy * kotlin.math.sin(arrowAngle)).toFloat()
                val leftY = endY - arrowLen * (ux * kotlin.math.sin(arrowAngle) + uy * kotlin.math.cos(arrowAngle)).toFloat()
                val rightX = endX - arrowLen * (ux * kotlin.math.cos(-arrowAngle) - uy * kotlin.math.sin(-arrowAngle)).toFloat()
                val rightY = endY - arrowLen * (ux * kotlin.math.sin(-arrowAngle) + uy * kotlin.math.cos(-arrowAngle)).toFloat()
                val arrowPaint = Paint().apply { color = AndroidColor.GRAY; strokeWidth = 3f; isAntiAlias = true }
                canvas.drawLine(endX, endY, leftX, leftY, arrowPaint)
                canvas.drawLine(endX, endY, rightX, rightY, arrowPaint)
                val midX = (source.position.x + target.position.x) / 2
                val midY = (source.position.y + target.position.y) / 2
                val textPaint = Paint().apply { color = AndroidColor.DKGRAY; textSize = 24f; textAlign = Paint.Align.CENTER; isFakeBoldText = true; isAntiAlias = true }
                val tw = textPaint.measureText(edge.relationType)
                val bgPaint = Paint().apply { color = AndroidColor.argb(180, 245, 245, 245); isAntiAlias = true }
                canvas.drawRoundRect(RectF(midX - tw / 2 - 8f, midY - 18f, midX + tw / 2 + 8f, midY + 8f), 6f, 6f, bgPaint)
                canvas.drawText(edge.relationType, midX, midY, textPaint)
            }
        }
    }

    nodes.forEach { node ->
        val circlePaint = Paint().apply { color = genderColorInt(node.gender); isAntiAlias = true }
        canvas.drawCircle(node.position.x, node.position.y, nodeRadius, circlePaint)
        if (node.pinned) {
            val strokePaint = Paint().apply { color = AndroidColor.WHITE; style = Paint.Style.STROKE; strokeWidth = 3f; isAntiAlias = true }
            canvas.drawCircle(node.position.x, node.position.y, 33f, strokePaint)
        }
        val namePaint = Paint().apply { color = AndroidColor.WHITE; textSize = 20f; textAlign = Paint.Align.CENTER; isFakeBoldText = true; isAntiAlias = true }
        canvas.drawText(node.name, node.position.x, node.position.y + 8f, namePaint)
    }

    return bitmap
}

private fun saveModuleBitmapToGallery(context: android.content.Context, bitmap: Bitmap): android.net.Uri? {
    val filename = "module_relationship_${System.currentTimeMillis()}.png"
    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, filename)
        put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/KeepersNotes")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
    }
    val resolver = context.contentResolver
    val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues) ?: return null
    resolver.openOutputStream(uri)?.use { stream ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        contentValues.clear()
        contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
        resolver.update(uri, contentValues, null, null)
    }
    return uri
}

private fun findEntity(uiState: ModuleDetailUiState, id: String, type: String): String? {
    return when (type) {
        "npc" -> uiState.defaultNpcs.find { it.id == id }?.description
        else -> null
    }
}
