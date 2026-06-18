package com.example.keepersnotes.util

import android.content.Context
import com.chaquo.python.Python
import org.json.JSONArray
import java.io.File

/**
 * Structured DOCX parser — calls Python docx_parser via Chaquopy.
 * Returns a list of DocumentNode with typed content (heading, paragraph, table, image, quote, list_item).
 */
object StructuredDocxParser {

    data class DocumentNode(
        val type: String,               // heading / paragraph / table / image / quote / list_item
        val level: Int = 0,             // heading level (1-6) or list nesting depth
        val content: String = "",       // text content
        val tableData: List<List<String>>? = null, // 2D array for tables
        val imageUri: String? = null,   // image file path
        val order: Int = 0              // document order
    )

    /**
     * Parse a DOCX file using python-docx via Chaquopy.
     *
     * @param context Android context (needed for Chaquopy initialization)
     * @param file The DOCX file to parse
     * @return List of DocumentNode in document order
     */
    fun parse(context: Context, file: File): List<DocumentNode> {
        val imageDir = File(context.filesDir, "doc_images/${file.nameWithoutExtension}")

        val python = Python.getInstance()
        val module = python.getModule("docx_parser")
        val jsonResult = module.callAttr(
            "parse_docx",
            file.absolutePath,
            imageDir.absolutePath
        ).toString()

        return parseJsonToNodes(jsonResult)
    }

    /**
     * Parse the JSON string returned by Python into DocumentNode list.
     */
    private fun parseJsonToNodes(json: String): List<DocumentNode> {
        if (json.isBlank() || json == "[]") return emptyList()

        val arr = JSONArray(json)
        return (0 until arr.length()).map { i ->
            val obj = arr.getJSONObject(i)
            DocumentNode(
                type = obj.getString("type"),
                level = obj.optInt("level", 0),
                content = obj.optString("content", ""),
                tableData = obj.optJSONArray("tableData")?.let { tableArr ->
                    (0 until tableArr.length()).map { r ->
                        val row = tableArr.getJSONArray(r)
                        (0 until row.length()).map { row.getString(it) }
                    }
                },
                imageUri = obj.opt("imageUri")?.toString(),
                order = obj.optInt("order", 0)
            )
        }
    }
}
