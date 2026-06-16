package com.example.keepersnotes.ui.screen.collection.tab

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.keepersnotes.data.local.entity.ImageEntity
import com.example.keepersnotes.data.repository.GroupWithCount
import java.io.File

@Composable
fun ClueTab(
    images: List<ImageEntity>,
    groups: List<GroupWithCount> = emptyList(),
    selectedGroupId: String? = null,
    onImageClick: (Int) -> Unit = {},
    onGroupSelect: (String?) -> Unit = {},
    onCreateGroup: (String, String) -> Unit = { _, _ -> }
) {
    var showCreateDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Group filter chips
        if (groups.isNotEmpty() || images.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LazyRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedGroupId == null,
                            onClick = { onGroupSelect(null) },
                            label = { Text("全部(${images.size})") }
                        )
                    }
                    items(groups) { gwc ->
                        FilterChip(
                            selected = selectedGroupId == gwc.group.imageGroupId,
                            onClick = { onGroupSelect(gwc.group.imageGroupId) },
                            label = { Text("${gwc.group.name}(${gwc.imageCount})") }
                        )
                    }
                }
                IconButton(onClick = { showCreateDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "创建图片组")
                }
            }
            HorizontalDivider()
        }

        // Image grid
        if (images.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (selectedGroupId != null) "该组暂无图片" else "暂无线索图片",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(images.size) { index ->
                    ClueImageCard(
                        image = images[index],
                        onClick = { onImageClick(index) }
                    )
                }
            }
        }
    }

    // Create group dialog
    if (showCreateDialog) {
        CreateImageGroupDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { name, desc ->
                onCreateGroup(name, desc)
                showCreateDialog = false
            }
        )
    }
}

@Composable
private fun ClueImageCard(
    image: ImageEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.aspectRatio(1f).clickable(onClick = onClick)
    ) {
        Box {
            val file = File(image.filePath)
            if (file.exists()) {
                Image(
                    painter = rememberAsyncImagePainter(model = file),
                    contentDescription = image.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "图片丢失",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            if (image.title.isNotBlank()) {
                Surface(
                    modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                ) {
                    Text(
                        text = image.title,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CreateImageGroupDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, description: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("创建图片组") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("组名 *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("描述（可选）") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name.trim(), description.trim()) },
                enabled = name.isNotBlank()
            ) {
                Text("创建")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
