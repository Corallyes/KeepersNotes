package com.example.keepersnotes.data.remote

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

data class UpdateInfo(
    val versionName: String,
    val versionCode: Int,
    val downloadUrl: String,
    val changelog: String
)

@Singleton
class UpdateChecker @Inject constructor() {

    companion object {
        // TODO: Replace with your actual GitHub repo
        private const val GITHUB_API_URL =
            "https://api.github.com/repos/OWNER/REPO/releases/latest"
        private const val CONNECT_TIMEOUT = 5000
        private const val READ_TIMEOUT = 5000
    }

    suspend fun checkForUpdate(context: Context): UpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val currentVersionCode = getCurrentVersionCode(context)
            val json = fetchLatestRelease() ?: return@withContext null

            val tagName = json.getString("tag_name") // e.g. "v2.0"
            val latestVersionCode = parseVersionCode(tagName)
            val changelog = json.getString("body")
            val downloadUrl = parseApkDownloadUrl(json)

            if (latestVersionCode > currentVersionCode && downloadUrl != null) {
                UpdateInfo(
                    versionName = tagName.removePrefix("v"),
                    versionCode = latestVersionCode,
                    downloadUrl = downloadUrl,
                    changelog = changelog
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun getCurrentVersionCode(context: Context): Int {
        return try {
            context.packageManager.getPackageInfo(context.packageName, 0).longVersionCode.toInt()
        } catch (e: PackageManager.NameNotFoundException) {
            0
        }
    }

    private fun fetchLatestRelease(): JSONObject? {
        val url = URL(GITHUB_API_URL)
        val conn = url.openConnection() as HttpURLConnection
        return try {
            conn.connectTimeout = CONNECT_TIMEOUT
            conn.readTimeout = READ_TIMEOUT
            conn.setRequestProperty("Accept", "application/vnd.github.v3+json")
            if (conn.responseCode == 200) {
                JSONObject(conn.inputStream.bufferedReader().readText())
            } else null
        } finally {
            conn.disconnect()
        }
    }

    private fun parseVersionCode(tagName: String): Int {
        // "v2.0" -> 200, "v1.5" -> 150, "v1.0.1" -> 101
        val version = tagName.removePrefix("v")
        val parts = version.split(".")
        var code = 0
        for ((i, part) in parts.withIndex()) {
            code += part.toIntOrNull()?.let { it * power(100, parts.size - 1 - i) } ?: 0
        }
        return code
    }

    private fun power(base: Int, exp: Int): Int {
        var result = 1
        repeat(exp) { result *= base }
        return result
    }

    private fun parseApkDownloadUrl(json: JSONObject): String? {
        val assets = json.optJSONArray("assets") ?: return null
        for (i in 0 until assets.length()) {
            val asset = assets.getJSONObject(i)
            val name = asset.getString("name")
            if (name.endsWith(".apk")) {
                return asset.getString("browser_download_url")
            }
        }
        return null
    }

    fun openDownloadPage(context: Context, url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}
