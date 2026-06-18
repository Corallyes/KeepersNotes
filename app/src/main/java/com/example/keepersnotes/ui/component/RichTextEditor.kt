package com.example.keepersnotes.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.keepersnotes.util.LocalizedStrings

@Composable
fun RichTextEditor(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = LocalizedStrings.editorPlaceholder,
    showPreview: Boolean = false
) {
    var textFieldValue by remember(value) {
        mutableStateOf(TextFieldValue(value, TextRange(value.length)))
    }
    var isPreviewMode by remember { mutableStateOf(showPreview) }

    Column(modifier = modifier) {
        // 格式工具栏
        Surface(
            tonalElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // 标题
                FormatButton(
                    icon = Icons.Default.Title,
                    tooltip = LocalizedStrings.editorHeading,
                    onClick = {
                        val newText = insertMarkdown(textFieldValue, "# ", "")
                        textFieldValue = newText
                        onValueChange(newText.text)
                    }
                )

                // 加粗
                FormatButton(
                    icon = Icons.Default.FormatBold,
                    tooltip = LocalizedStrings.editorBold,
                    onClick = {
                        val newText = insertMarkdown(textFieldValue, "**", "**")
                        textFieldValue = newText
                        onValueChange(newText.text)
                    }
                )

                // 斜体
                FormatButton(
                    icon = Icons.Default.FormatItalic,
                    tooltip = LocalizedStrings.editorItalic,
                    onClick = {
                        val newText = insertMarkdown(textFieldValue, "*", "*")
                        textFieldValue = newText
                        onValueChange(newText.text)
                    }
                )

                // 删除线
                FormatButton(
                    icon = Icons.Default.FormatStrikethrough,
                    tooltip = LocalizedStrings.editorStrikethrough,
                    onClick = {
                        val newText = insertMarkdown(textFieldValue, "~~", "~~")
                        textFieldValue = newText
                        onValueChange(newText.text)
                    }
                )

                // 无序列表
                FormatButton(
                    icon = Icons.Default.FormatListBulleted,
                    tooltip = LocalizedStrings.editorBulletList,
                    onClick = {
                        val newText = insertAtLineStart(textFieldValue, "- ")
                        textFieldValue = newText
                        onValueChange(newText.text)
                    }
                )

                // 有序列表
                FormatButton(
                    icon = Icons.Default.FormatListNumbered,
                    tooltip = LocalizedStrings.editorOrderedList,
                    onClick = {
                        val newText = insertAtLineStart(textFieldValue, "1. ")
                        textFieldValue = newText
                        onValueChange(newText.text)
                    }
                )

                // 引用
                FormatButton(
                    icon = Icons.Default.FormatQuote,
                    tooltip = LocalizedStrings.editorQuote,
                    onClick = {
                        val newText = insertAtLineStart(textFieldValue, "> ")
                        textFieldValue = newText
                        onValueChange(newText.text)
                    }
                )

                // 代码
                FormatButton(
                    icon = Icons.Default.Code,
                    tooltip = LocalizedStrings.editorCode,
                    onClick = {
                        val selection = textFieldValue.selection
                        if (selection.collapsed) {
                            val newText = insertMarkdown(textFieldValue, "`", "`")
                            textFieldValue = newText
                            onValueChange(newText.text)
                        } else {
                            val newText = insertMarkdown(textFieldValue, "`", "`")
                            textFieldValue = newText
                            onValueChange(newText.text)
                        }
                    }
                )

                // 预览切换
                IconButton(
                    onClick = { isPreviewMode = !isPreviewMode },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        if (isPreviewMode) Icons.Default.Edit else Icons.Default.Visibility,
                        contentDescription = if (isPreviewMode) LocalizedStrings.editorEdit else LocalizedStrings.editorPreview,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // 编辑器/预览区域
        if (isPreviewMode) {
            // Markdown预览
            MarkdownText(
                markdown = value,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(12.dp)
            )
        } else {
            // 文本编辑器
            OutlinedTextField(
                value = textFieldValue,
                onValueChange = { newValue ->
                    textFieldValue = newValue
                    onValueChange(newValue.text)
                },
                placeholder = { Text(placeholder) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default),
                keyboardActions = KeyboardActions()
            )
        }
    }
}

@Composable
private fun FormatButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tooltip: String,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(32.dp)
    ) {
        Icon(
            icon,
            contentDescription = tooltip,
            modifier = Modifier.size(18.dp)
        )
    }
}

private fun insertMarkdown(
    textFieldValue: TextFieldValue,
    prefix: String,
    suffix: String
): TextFieldValue {
    val selection = textFieldValue.selection
    val text = textFieldValue.text

    return if (selection.collapsed) {
        // 没有选中文本，在光标位置插入
        val newText = StringBuilder(text)
            .insert(selection.start, "$prefix$suffix")
            .toString()
        val newCursorPos = selection.start + prefix.length
        TextFieldValue(newText, TextRange(newCursorPos))
    } else {
        // 选中文本，包裹它
        val selectedText = text.substring(selection.start, selection.end)
        val newText = StringBuilder(text)
            .replace(selection.start, selection.end, "$prefix$selectedText$suffix")
            .toString()
        val newCursorPos = selection.end + prefix.length + suffix.length
        TextFieldValue(newText, TextRange(newCursorPos))
    }
}

private fun insertAtLineStart(
    textFieldValue: TextFieldValue,
    linePrefix: String
): TextFieldValue {
    val selection = textFieldValue.selection
    val text = textFieldValue.text

    // 找到当前行的开始
    val lineStart = text.lastIndexOf('\n', selection.start - 1) + 1

    val newText = StringBuilder(text)
        .insert(lineStart, linePrefix)
        .toString()
    val newCursorPos = selection.start + linePrefix.length
    return TextFieldValue(newText, TextRange(newCursorPos))
}
