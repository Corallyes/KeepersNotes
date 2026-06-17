package com.example.keepersnotes.util

import android.content.Context
import android.net.Uri
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStreamReader
import java.nio.charset.Charset

object FileReaderUtil {

    /**
     * 读取文件内容，支持 .txt 和 .docx。
     * - TXT：自动检测编码（UTF-8/GBK/GB2312），返回原文
     * - DOCX：使用 DocxParser 三层识别 + 评分系统转 Markdown
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

    /**
     * 读取 TXT 文件 — 自动检测编码，返回原文。
     * 检测顺序：BOM → UTF-8 → GBK → GB18030
     */
    private fun readTxtFile(context: Context, uri: Uri): Result<String> {
        val bytes = context.contentResolver.openInputStream(uri)?.use { inputStream ->
            inputStream.readBytes()
        } ?: return Result.failure(Exception("无法打开文件"))

        if (bytes.isEmpty()) return Result.success("")

        val content = decodeBytes(bytes)
        return Result.success(content)
    }

    /**
     * 智能解码字节数组，自动检测编码。
     */
    private fun decodeBytes(bytes: ByteArray): String {
        // 1. 检测 BOM
        if (bytes.size >= 3 && bytes[0] == 0xEF.toByte() && bytes[1] == 0xBB.toByte() && bytes[2] == 0xBF.toByte()) {
            return String(bytes, 3, bytes.size - 3, Charsets.UTF_8)
        }
        if (bytes.size >= 2 && bytes[0] == 0xFF.toByte() && bytes[1] == 0xFE.toByte()) {
            return String(bytes, 2, bytes.size - 2, Charsets.UTF_16LE)
        }
        if (bytes.size >= 2 && bytes[0] == 0xFE.toByte() && bytes[1] == 0xFF.toByte()) {
            return String(bytes, 2, bytes.size - 2, Charsets.UTF_16BE)
        }

        // 2. 尝试 UTF-8
        val utf8Result = tryDecode(bytes, Charsets.UTF_8)
        if (isValidText(utf8Result)) {
            return utf8Result
        }

        // 3. 尝试 GBK
        if (Charset.isSupported("GBK")) {
            val gbkResult = tryDecode(bytes, Charset.forName("GBK"))
            if (isValidText(gbkResult)) {
                return gbkResult
            }
        }

        // 4. 尝试 GB18030
        if (Charset.isSupported("GB18030")) {
            val gb18030Result = tryDecode(bytes, Charset.forName("GB18030"))
            if (isValidText(gb18030Result)) {
                return gb18030Result
            }
        }

        // 5. 兜底：UTF-8（含替换字符）
        return String(bytes, Charsets.UTF_8)
    }

    private fun tryDecode(bytes: ByteArray, charset: Charset): String {
        return try {
            String(bytes, charset)
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * 判断解码结果是否为有效文本。
     * 检查：无乱码符号、中文字符比例合理、内容长度合理。
     */
    private fun isValidText(text: String): Boolean {
        if (text.isBlank()) return false
        if (text.contains('�')) return false // 含替换字符 → 编码错误

        val totalChars = text.length
        if (totalChars == 0) return false

        // 统计中文字符
        val chineseCount = text.count { it.code in 0x4E00..0x9FFF || it.code in 0x3400..0x4DBF }
        // 统计常见乱码符号（GBK误读UTF-8的典型产物）
        val garbledCount = text.count { it.code in 0xE000..0xF8FF } // 私用区通常为乱码

        // 如果乱码字符占比过高，判定为无效
        if (totalChars > 10 && garbledCount.toFloat() / totalChars > 0.3f) return false

        // 如果有中文字符，判定为有效
        if (chineseCount > 0) return true

        // 纯ASCII文本也有效
        if (text.all { it.code < 128 }) return true

        return true
    }

    /**
     * 读取 DOCX 文件 — 使用 DocxParser 三层识别转 Markdown。
     */
    private fun readDocxFile(context: Context, uri: Uri): Result<String> {
        val content = context.contentResolver.openInputStream(uri)?.use { inputStream ->
            val tempFile = File.createTempFile("docx_import_", ".docx", context.cacheDir)
            try {
                tempFile.outputStream().use { out -> inputStream.copyTo(out) }
                DocxParser.parse(tempFile)
            } finally {
                tempFile.delete()
            }
        } ?: return Result.failure(Exception("无法打开文件"))
        return Result.success(content)
    }

    /**
     * 读取 DOCX 文件对象并转 Markdown（供 ZipImportManager 使用）。
     */
    fun readDocxToMarkdown(file: File): String {
        return DocxParser.parse(file)
    }

    /**
     * 智能读取文本文件（供 ZipImportManager 使用），自动检测编码。
     */
    fun readTextFileSmart(file: File): String {
        val bytes = file.readBytes()
        if (bytes.isEmpty()) return ""
        return decodeBytes(bytes)
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
