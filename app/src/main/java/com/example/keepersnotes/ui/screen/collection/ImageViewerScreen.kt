package com.example.keepersnotes.ui.screen.collection

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.example.keepersnotes.data.local.entity.ImageEntity
import com.example.keepersnotes.data.repository.GroupWithCount
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageViewerScreen(
    collectionId: String,
    initialIndex: Int,
    onBack: () -> Unit,
    viewModel: ImageViewerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val allImages = uiState.images
    val groups = uiState.groups

    if (allImages.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
            Text("无图片", color = Color.White)
        }
        return
    }

    val safeIndex = initialIndex.coerceIn(0, allImages.size - 1)
    val pagerState = rememberPagerState(initialPage = safeIndex, pageCount = { allImages.size })
    var showControls by remember { mutableStateOf(true) }
    var showGroupPicker by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // HorizontalPager for swipe between images
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            ZoomableImage(
                image = allImages[page],
                onTap = { showControls = !showControls }
            )
        }

        // Top bar (toggleable)
        if (showControls) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(horizontal = 8.dp, vertical = 12.dp)
                    .align(Alignment.TopCenter),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = Color.White)
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "${pagerState.currentPage + 1} / ${allImages.size}",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = { showGroupPicker = true }) {
                    Icon(Icons.Default.AccountTree, contentDescription = "加入图片组", tint = Color.White)
                }
            }
        }

        // Bottom bar with image title and group info
        if (showControls) {
            val currentImage = allImages[pagerState.currentPage]
            val currentGroupName = groups.find { it.group.imageGroupId == currentImage.imageGroupId }?.group?.name

            Surface(
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
                color = Color.Black.copy(alpha = 0.5f)
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    if (currentImage.title.isNotBlank()) {
                        Text(text = currentImage.title, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                    }
                    if (currentGroupName != null) {
                        Text(
                            text = "所属组: $currentGroupName",
                            color = Color.White.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

    }

    // Group picker dialog
    if (showGroupPicker) {
        val currentImage = allImages[pagerState.currentPage]
        GroupPickerDialog(
            groups = groups,
            currentGroupId = currentImage.imageGroupId,
            onDismiss = { showGroupPicker = false },
            onSelectGroup = { groupId ->
                if (groupId != null) {
                    viewModel.assignImageToGroup(currentImage.imageId, groupId)
                } else {
                    viewModel.removeImageFromGroup(currentImage.imageId)
                }
                showGroupPicker = false
            },
            onCreateGroup = { name, desc ->
                viewModel.createGroup(name, desc)
            }
        )
    }
}

@Composable
private fun GroupPickerDialog(
    groups: List<GroupWithCount>,
    currentGroupId: String?,
    onDismiss: () -> Unit,
    onSelectGroup: (String?) -> Unit,
    onCreateGroup: (String, String) -> Unit
) {
    var showCreateNew by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择图片组") },
        text = {
            Column {
                // "No group" option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectGroup(null) }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = currentGroupId == null, onClick = { onSelectGroup(null) })
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("未分组")
                }
                // Existing groups
                groups.forEach { gwc ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectGroup(gwc.group.imageGroupId) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentGroupId == gwc.group.imageGroupId,
                            onClick = { onSelectGroup(gwc.group.imageGroupId) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("${gwc.group.name}(${gwc.imageCount})")
                    }
                }
                // Create new group
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showCreateNew = true }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("创建新组", color = MaterialTheme.colorScheme.primary)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("关闭") }
        }
    )

    // Create new group sub-dialog
    if (showCreateNew) {
        var newName by remember { mutableStateOf("") }
        var newDesc by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCreateNew = false },
            title = { Text("创建图片组") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("组名 *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newDesc,
                        onValueChange = { newDesc = it },
                        label = { Text("描述") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onCreateGroup(newName.trim(), newDesc.trim())
                        showCreateNew = false
                    },
                    enabled = newName.isNotBlank()
                ) { Text("创建") }
            },
            dismissButton = {
                TextButton(onClick = { showCreateNew = false }) { Text("取消") }
            }
        )
    }
}

@Composable
private fun ZoomableImage(
    image: ImageEntity,
    onTap: () -> Unit = {}
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    val file = File(image.filePath)
    if (!file.exists()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("图片文件不存在", color = Color.White)
        }
        return
    }

    Image(
        painter = rememberAsyncImagePainter(model = file),
        contentDescription = image.title,
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                translationX = offsetX,
                translationY = offsetY
            )
            .pointerInput(image.imageId) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    down.consume()

                    // Wait for up or second pointer
                    val upOrCancel = waitForUpOrCancellation()

                    if (upOrCancel != null) {
                        // Single pointer went up — check if it was a tap
                        onTap()
                        upOrCancel.consume()
                    } else {
                        // Cancelled (likely second pointer down) — handle transform
                        var prevScale = scale
                        var prevOffsetX = offsetX
                        var prevOffsetY = offsetY

                        do {
                            val event = awaitPointerEvent()
                            val pointers = event.changes

                            if (pointers.size >= 2) {
                                val p1 = pointers[0]
                                val p2 = pointers[1]

                                val prevCentroid = Offset(
                                    (p1.previousPosition.x + p2.previousPosition.x) / 2,
                                    (p1.previousPosition.y + p2.previousPosition.y) / 2
                                )
                                val currCentroid = Offset(
                                    (p1.position.x + p2.position.x) / 2,
                                    (p1.position.y + p2.position.y) / 2
                                )

                                val prevDist = (p1.previousPosition - p2.previousPosition).getDistance()
                                val currDist = (p1.position - p2.position).getDistance()
                                val zoomFactor = if (prevDist > 0f) currDist / prevDist else 1f

                                scale = (prevScale * zoomFactor).coerceIn(0.5f, 5f)
                                if (scale > 1f) {
                                    offsetX = prevOffsetX + (currCentroid.x - prevCentroid.x)
                                    offsetY = prevOffsetY + (currCentroid.y - prevCentroid.y)
                                } else {
                                    offsetX = 0f
                                    offsetY = 0f
                                }

                                pointers.forEach { it.consume() }
                            }

                            prevScale = scale
                            prevOffsetX = offsetX
                            prevOffsetY = offsetY
                        } while (event.changes.any { it.pressed })
                    }
                }
            },
        contentScale = ContentScale.Fit
    )

    LaunchedEffect(image.imageId) {
        scale = 1f
        offsetX = 0f
        offsetY = 0f
    }
}
