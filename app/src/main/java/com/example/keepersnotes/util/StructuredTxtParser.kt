package com.example.keepersnotes.util

import android.content.Context
import android.net.Uri
import com.chaquo.python.Python
import org.json.JSONArray
import java.io.File

/**
 * Structured TXT parser — calls Python txt_parser via Chaquopy.
 * Returns a list of DocumentNode with typed content (heading, paragraph, quote, list_item).
 */
object StructuredTxtParser {

    data class DocumentNode(
        val type: String,               // heading / paragraph / quote / list_item
        val level: Int = 0,             // heading level (1-6) or list nesting depth
        val content: String = "",       // text content
        val order: Int = 0              // document order
    )

    /**
     * Parse a TXT file using Python txt_parser via Chaquopy.
     *
     * @param context Android context (needed for Chaquopy initialization)
     * @param file The TXT file to parse
     * @return List of DocumentNode in document order
     */
    fun parse(context: Context, file: File): List<DocumentNode> {
        android.util.Log.d("TxtParser", "parse() file=${file.absolutePath}, exists=${file.exists()}, size=${file.length()}")

        val python = Python.getInstance()
        val module = python.getModule("txt_parser")
        val jsonResult = module.callAttr(
            "parse_txt",
            file.absolutePath
        ).toString()

        android.util.Log.d("TxtParser", "Python returned ${jsonResult.length} chars, preview=${jsonResult.take(200)}")
        return parseJsonToNodes(jsonResult)
    }

    /**
     * Parse a TXT file from URI using Python txt_parser via Chaquopy.
     *
     * @param context Android context
     * @param uri The URI of the TXT file
     * @return List of DocumentNode in document order
     */
    fun parseFromUri(context: Context, uri: Uri): List<DocumentNode> {
        // Copy URI content to temp file
        val tempFile = File.createTempFile("txt_import_", ".txt", context.cacheDir)
        try {
            val bytesWritten = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                tempFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            } ?: return emptyList()

            android.util.Log.d("TxtParser", "Copied $bytesWritten bytes to temp file: ${tempFile.absolutePath}")

            return parse(context, tempFile)
        } finally {
            tempFile.delete()
        }
    }

    /**
     * Parse the JSON string returned by Python into DocumentNode list.
     */
    private fun parseJsonToNodes(json: String): List<DocumentNode> {
        if (json.isBlank() || json == "[]") return emptyList()

        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                DocumentNode(
                    type = obj.getString("type"),
                    level = obj.optInt("level", 0),
                    content = obj.optString("content", ""),
                    order = obj.optInt("order", 0)
                )
            }
        } catch (e: Exception) {
            android.util.Log.e("TxtParser", "JSON parse error: ${e.message}", e)
            emptyList()
        }
    }
}
