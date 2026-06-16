package com.example.keepersnotes.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.keepersnotes.ui.screen.modulelibrary.AnnotationTool
import com.example.keepersnotes.ui.screen.modulelibrary.EraserMode

@Composable
fun AnnotationToolbar(
    activeTool: AnnotationTool,
    selectedColor: Long,
    eraserMode: EraserMode,
    onToolSelected: (AnnotationTool) -> Unit,
    onColorSelected: (Long) -> Unit,
    onEraserModeChanged: (EraserMode) -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showColorPicker by remember { mutableStateOf(false) }
    var showEraserOptions by remember { mutableStateOf(false) }

    val highlightColors = listOf(
        0xFFFFEB3B to "黄色",
        0xFF4CAF50 to "绿色",
        0xFF2196F3 to "蓝色",
        0xFFFF9800 to "橙色",
        0xFFE91E63 to "粉色",
        0xFF9C27B0 to "紫色"
    )

    Surface(
        tonalElevation = 3.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column {
            // 主工具栏
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 荧光笔按钮
                ToolButton(
                    icon = Icons.Default.Highlight,
                    label = "荧光笔",
                    isActive = activeTool == AnnotationTool.HIGHLIGHT,
                    onClick = { onToolSelected(AnnotationTool.HIGHLIGHT) }
                )

                // 颜色选择器
                Box {
                    IconButton(
                        onClick = { showColorPicker = !showColorPicker },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(selectedColor))
                                .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
                        )
                    }

                    DropdownMenu(
                        expanded = showColorPicker,
                        onDismissRequest = { showColorPicker = false }
                    ) {
                        highlightColors.forEach { (color, name) ->
                            DropdownMenuItem(
                                text = { Text(name) },
                                onClick = {
                                    onColorSelected(color)
                                    showColorPicker = false
                                },
                                leadingIcon = {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(Color(color))
                                            .then(
                                                if (selectedColor == color) {
                                                    Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                                } else {
                                                    Modifier
                                                }
                                            )
                                    )
                                }
                            )
                        }
                    }
                }

                // 批注按钮
                ToolButton(
                    icon = Icons.Default.Comment,
                    label = "批注",
                    isActive = activeTool == AnnotationTool.ANNOTATE,
                    onClick = { onToolSelected(AnnotationTool.ANNOTATE) }
                )

                // 书签按钮
                ToolButton(
                    icon = Icons.Default.Bookmark,
                    label = "书签",
                    isActive = activeTool == AnnotationTool.BOOKMARK,
                    onClick = { onToolSelected(AnnotationTool.BOOKMARK) }
                )

                // 橡皮擦按钮
                Box {
                    ToolButton(
                        icon = Icons.Default.AutoFixHigh,
                        label = "橡皮擦",
                        isActive = activeTool == AnnotationTool.ERASER,
                        onClick = { onToolSelected(AnnotationTool.ERASER) },
                        onLongClick = { showEraserOptions = true }
                    )

                    DropdownMenu(
                        expanded = showEraserOptions,
                        onDismissRequest = { showEraserOptions = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("清除全部") },
                            onClick = {
                                onEraserModeChanged(EraserMode.ALL)
                                showEraserOptions = false
                            },
                            leadingIcon = { Icon(Icons.Default.Delete, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("仅清除高亮") },
                            onClick = {
                                onEraserModeChanged(EraserMode.HIGHLIGHTS_ONLY)
                                showEraserOptions = false
                            },
                            leadingIcon = { Icon(Icons.Default.Highlight, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("仅清除批注") },
                            onClick = {
                                onEraserModeChanged(EraserMode.ANNOTATIONS_ONLY)
                                showEraserOptions = false
                            },
                            leadingIcon = { Icon(Icons.Default.Comment, null) }
                        )
                    }
                }

                // 清除全部按钮
                IconButton(
                    onClick = onClearAll,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.DeleteSweep,
                        contentDescription = "清除全部",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            // 橡皮擦模式提示
            AnimatedVisibility(
                visible = activeTool == AnnotationTool.ERASER,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when (eraserMode) {
                                EraserMode.ALL -> "橡皮擦模式：点击清除高亮和批注"
                                EraserMode.HIGHLIGHTS_ONLY -> "橡皮擦模式：仅清除高亮"
                                EraserMode.ANNOTATIONS_ONLY -> "橡皮擦模式：仅清除批注"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ToolButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = if (isActive) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isActive) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
