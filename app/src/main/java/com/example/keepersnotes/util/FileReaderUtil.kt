package com.example.keepersnotes.util

import android.content.Context
import android.net.Uri
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.apache.poi.xwpf.usermodel.XWPFParagraph
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

object FileReaderUtil {

    /**
     * Read text content from a file URI.
     * Supports .txt and .docx files.
     */
    fun readFileContent(context: Context, uri: Uri): Result<String> {
        val mimeType = context.contentResolver.getType(uri) ?: ""
        val fileName = getFileName(context, uri)

        return try {
            when {
                mimeType == "text/plain" || fileName.endsWith(".txt") -> readTxtFile(context, uri)
                mimeType.contains("wordprocessingml") || fileName.endsWith(".docx") -> readDocxFile(context, uri)
                fileName.endsWith(".doc") -> Result.failure(Exception("不支持 .doc 格式，请转换为 .docx 后重试"))
                else -> Result.failure(Exception("不支持的文件格式，仅支持 .txt 和 .docx"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("读取文件失败: ${e.message}"))
        }
    }

    private fun readTxtFile(context: Context, uri: Uri): Result<String> {
        val content = context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                reader.readText()
            }
        } ?: return Result.failure(Exception("无法打开文件"))
        return Result.success(content)
    }

    private fun readDocxFile(context: Context, uri: Uri): Result<String> {
        val content = context.contentResolver.openInputStream(uri)?.use { inputStream ->
            val document = XWPFDocument(inputStream)
            val text = StringBuilder()
            document.paragraphs.forEach { paragraph ->
                val paragraphText = paragraph.text
                if (paragraphText.isNotBlank()) {
                    // Detect heading styles from Word and convert to markdown headings
                    val styleName = paragraph.style ?: ""
                    val headingLevel = detectDocxHeadingLevel(styleName)
                    if (headingLevel > 0) {
                        text.appendLine("${"#".repeat(headingLevel)} $paragraphText")
                    } else {
                        text.appendLine(paragraphText)
                    }
                }
            }
            document.close()
            text.toString()
        } ?: return Result.failure(Exception("无法打开文件"))
        return Result.success(content)
    }

    /**
     * Read DOCX file from File object and convert to Markdown with formatting.
     * Used by ZipImportManager for ZIP extraction.
     */
    fun readDocxToMarkdown(file: File): String {
        val document = XWPFDocument(file.inputStream())
        val text = StringBuilder()
        document.paragraphs.forEach { paragraph ->
            val markdown = convertParagraphToMarkdown(paragraph)
            if (markdown.isNotBlank()) {
                text.appendLine(markdown)
            }
        }
        document.close()
        return text.toString().trim()
    }

    private fun convertParagraphToMarkdown(paragraph: XWPFParagraph): String {
        val styleName = paragraph.style ?: ""
        val headingLevel = detectDocxHeadingLevel(styleName)

        // Build markdown text with inline formatting
        val content = StringBuilder()
        paragraph.runs.forEach { run ->
            val runText = run.text() ?: ""
            if (runText.isNotEmpty()) {
                val formatted = when {
                    run.isBold && run.isItalic -> "***$runText***"
                    run.isBold -> "**$runText**"
                    run.isItalic -> "*$runText*"
                    run.isStrikeThrough -> "~~$runText~~"
                    else -> runText
                }
                content.append(formatted)
            }
        }

        val text = content.toString().trim()
        if (text.isBlank()) return ""

        // Check for list items
        val numFmt = paragraph.numFmt ?: ""
        val ilvl = paragraph.numLevelText ?: ""
        if (numFmt.isNotEmpty() || ilvl.isNotEmpty()) {
            val indent = "  ".repeat(ilvl.length.coerceAtMost(3))
            return when {
                numFmt.contains("bullet") -> "$indent- $text"
                numFmt.matches(Regex("\\d+")) -> "${indent}1. $text"
                else -> "$indent- $text"
            }
        }

        return when {
            headingLevel > 0 -> "${"#".repeat(headingLevel)} $text"
            else -> text
        }
    }

    /**
     * Detect heading level from Word paragraph style name.
     * Common styles: "Heading1", "Heading2", "标题 1", "标题 2", etc.
     */
    private fun detectDocxHeadingLevel(styleName: String): Int {
        val normalized = styleName.lowercase().trim()
        // English styles
        if (normalized.startsWith("heading")) {
            val num = normalized.removePrefix("heading").trim().toIntOrNull()
            if (num != null && num in 1..6) return num
        }
        // Chinese styles: 标题 1, 标题1, heading 1
        if (normalized.startsWith("标题")) {
            val num = normalized.removePrefix("标题").trim().toIntOrNull()
            if (num != null && num in 1..6) return num
        }
        // TOC headings
        if (normalized.startsWith("toc ") || normalized.startsWith("tocheading")) {
            return 1
        }
        return 0
    }

    private fun getFileName(context: Context, uri: Uri): String {
        var fileName = ""
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex >= 0) {
                fileName = cursor.getString(nameIndex) ?: ""
            }
        }
        return fileName
    }
}
