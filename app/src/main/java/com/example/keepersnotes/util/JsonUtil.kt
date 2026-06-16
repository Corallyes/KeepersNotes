package com.example.keepersnotes.util

import org.json.JSONArray
import org.json.JSONObject

object JsonUtil {

    /**
     * Parse a JSON object string into a list of key-value pairs.
     * e.g. {"射击": 60, "聆听": 70} -> [("射击", "60"), ("聆听", "70")]
     */
    fun parseKeyValueJson(json: String): List<Pair<String, String>> {
        if (json.isBlank() || json == "{}") return emptyList()
        return try {
            val obj = JSONObject(json)
            obj.keys().asSequence().map { key ->
                key to obj.get(key).toString()
            }.toList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Parse a JSON array string into a list of strings.
     * e.g. ["绳索", "手电筒"] -> ["绳索", "手电筒"]
     */
    fun parseStringArray(json: String): List<String> {
        if (json.isBlank() || json == "[]") return emptyList()
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { arr.getString(it) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Convert a list of key-value pairs to JSON object string.
     */
    fun keyValueToJson(pairs: List<Pair<String, String>>): String {
        val obj = JSONObject()
        pairs.forEach { (key, value) ->
            try {
                obj.put(key, value.toIntOrNull() ?: value)
            } catch (e: Exception) {
                obj.put(key, value)
            }
        }
        return obj.toString()
    }

    /**
     * Convert a list of strings to JSON array string.
     */
    fun stringArrayToJson(items: List<String>): String {
        return JSONArray(items).toString()
    }
}
