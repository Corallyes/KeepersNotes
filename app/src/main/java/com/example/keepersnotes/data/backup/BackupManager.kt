package com.example.keepersnotes.data.backup

import android.content.Context
import com.example.keepersnotes.data.local.dao.*
import com.example.keepersnotes.data.local.entity.*
import com.example.keepersnotes.util.LocalizedStrings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val groupDao: GroupDao,
    private val playerCharacterDao: PlayerCharacterDao,
    private val npcDao: NpcDao,
    private val moduleDao: ModuleDao,
    private val sessionDao: SessionDao,
    private val kpMemoDao: KpMemoDao,
    private val archiveDao: ArchiveDao,
    private val imageDao: ImageDao,
    private val imageGroupDao: ImageGroupDao,
    private val highlightDao: HighlightDao,
    private val annotationDao: AnnotationDao,
    private val readingProgressDao: ReadingProgressDao,
    private val bookmarkDao: BookmarkDao,
    private val moduleDefaultPcDao: ModuleDefaultPcDao,
    private val moduleDefaultNpcDao: ModuleDefaultNpcDao,
    private val moduleLocationDao: ModuleLocationDao,
    private val moduleOrganizationDao: ModuleOrganizationDao,
    private val moduleRelationshipDao: ModuleRelationshipDao,
    private val moduleClueDao: ModuleClueDao,
    private val groupRelationshipDao: GroupRelationshipDao,
    private val calendarEventDao: CalendarEventDao,
    private val documentNodeDao: DocumentNodeDao
) {
    companion object {
        // Version history:
        // v1: basic data (groups, pcs, npcs, modules, sessions, memos, archives, images)
        // v2: + module relationships, group relationships, calendar events
        // v3: + highlights, annotations, reading progress, bookmarks, module default pcs/npcs,
        //     locations, organizations, clues, document nodes, image groups, preferences
        const val BACKUP_VERSION = 3
        private const val MIN_SUPPORTED_VERSION = 1
    }

    suspend fun exportTo(outputStream: OutputStream) {
        val json = buildBackupJson()

        // Calculate checksum of the JSON content
        val jsonBytes = json.toString(2).toByteArray(Charsets.UTF_8)
        val checksum = sha256(jsonBytes)

        // Collect all image files
        val imageFiles = collectImageFiles()
        val avatarFile = File(context.filesDir, "kp_avatar.jpg")
        val hasAvatar = avatarFile.exists()
        val totalFileCount = imageFiles.size + (if (hasAvatar) 1 else 0)

        // Build manifest
        val manifest = JSONObject().apply {
            put("backupVersion", BACKUP_VERSION)
            put("exportTime", System.currentTimeMillis())
            put("checksum", checksum)
            put("jsonSize", jsonBytes.size.toLong())
            put("fileCount", totalFileCount)
            put("appVersion", getAppVersion())
        }

        // Write ZIP
        ZipOutputStream(outputStream).use { zip ->
            // 1. manifest.json (first entry for quick validation)
            zip.putNextEntry(ZipEntry("manifest.json"))
            zip.write(manifest.toString(2).toByteArray(Charsets.UTF_8))
            zip.closeEntry()

            // 2. backup.json
            zip.putNextEntry(ZipEntry("backup.json"))
            zip.write(jsonBytes)
            zip.closeEntry()

            // 3. Image files
            for ((relativePath, file) in imageFiles) {
                zip.putNextEntry(ZipEntry(relativePath))
                file.inputStream().use { it.copyTo(zip) }
                zip.closeEntry()
            }

            // 4. Avatar
            if (hasAvatar) {
                zip.putNextEntry(ZipEntry("kp_avatar.jpg"))
                avatarFile.inputStream().use { it.copyTo(zip) }
                zip.closeEntry()
            }
        }
    }

    suspend fun importFrom(inputStream: InputStream) {
        val tempDir = File(context.cacheDir, "backup_import_${System.currentTimeMillis()}")
        try {
            tempDir.mkdirs()
            var manifestText: String? = null
            var jsonText: String? = null
            val imageFiles = mutableMapOf<String, File>()
            var actualFileCount = 0

            // Extract ZIP
            ZipInputStream(inputStream).use { zip ->
                var entry = zip.nextEntry
                while (entry != null) {
                    if (!entry.isDirectory) {
                        when {
                            entry.name == "manifest.json" -> {
                                manifestText = zip.bufferedReader().readText()
                            }
                            entry.name == "backup.json" -> {
                                jsonText = zip.bufferedReader().readText()
                            }
                            entry.name.startsWith("images/") ||
                            entry.name.startsWith("doc_images/") ||
                            entry.name == "kp_avatar.jpg" -> {
                                val outFile = File(tempDir, entry.name)
                                outFile.parentFile?.mkdirs()
                                outFile.outputStream().use { out -> zip.copyTo(out) }
                                imageFiles[entry.name] = outFile
                                actualFileCount++
                            }
                        }
                    }
                    zip.closeEntry()
                    entry = zip.nextEntry
                }
            }

            // --- Validate manifest ---
            val manifest = manifestText?.let { JSONObject(it) }
            if (manifest == null) {
                throw BackupException(LocalizedStrings.backupMissingManifest)
            }

            val backupVersion = manifest.optInt("backupVersion", 0)
            if (backupVersion < MIN_SUPPORTED_VERSION) {
                throw BackupException(LocalizedStrings.backupVersionTooOld(backupVersion, MIN_SUPPORTED_VERSION))
            }
            if (backupVersion > BACKUP_VERSION) {
                throw BackupException(LocalizedStrings.backupVersionTooNew(backupVersion, BACKUP_VERSION))
            }

            // Verify checksum
            val jsonBytes = jsonText?.toByteArray(Charsets.UTF_8)
                ?: throw BackupException(LocalizedStrings.backupMissingJson)
            val expectedChecksum = manifest.optString("checksum", "")
            if (expectedChecksum.isNotEmpty()) {
                val actualChecksum = sha256(jsonBytes)
                if (actualChecksum != expectedChecksum) {
                    throw BackupException(LocalizedStrings.backupChecksumFailed)
                }
            }

            // Verify file count
            val expectedFileCount = manifest.optInt("fileCount", -1)
            if (expectedFileCount >= 0 && actualFileCount != expectedFileCount) {
                throw BackupException(LocalizedStrings.backupIncompleteFiles(expectedFileCount, actualFileCount))
            }

            // --- Parse and migrate ---
            val json = JSONObject(jsonText)
            val jsonVersion = json.optInt("backupVersion", backupVersion)
            val migratedJson = migrateJson(json, jsonVersion)

            // --- Clear existing data ---
            clearAllData()

            // --- Import ---
            importAllData(migratedJson)

            // --- Restore files ---
            restoreImageFiles(imageFiles)

            // --- Restore preferences ---
            migratedJson.optJSONObject("preferences")?.let { importPreferences(it) }

        } finally {
            tempDir.deleteRecursively()
        }
    }

    // ==================== Migration ====================

    private suspend fun migrateJson(json: JSONObject, fromVersion: Int): JSONObject {
        var current = json
        for (v in fromVersion until BACKUP_VERSION) {
            current = when (v) {
                1 -> migrateV1ToV2(current)
                2 -> migrateV2ToV3(current)
                else -> current
            }
        }
        return current
    }

    /**
     * v1 -> v2: Add module_relationships, group_relationships, calendar_events
     * These are new tables, old backups just won't have them (optJSONArray returns null, safe)
     */
    private fun migrateV1ToV2(json: JSONObject): JSONObject {
        // v1 backups simply don't have these fields; import code uses optJSONArray so it's safe
        json.put("backupVersion", 2)
        return json
    }

    /**
     * v2 -> v3: Add highlights, annotations, reading_progress, bookmarks,
     * module_default_pcs/npcs, locations, organizations, clues, document_nodes,
     * image_groups, preferences
     */
    private fun migrateV2ToV3(json: JSONObject): JSONObject {
        // v2 backups simply don't have these fields; import code uses optJSONArray so it's safe
        json.put("backupVersion", 3)
        return json
    }

    // ==================== Export helpers ====================

    private suspend fun buildBackupJson(): JSONObject {
        return JSONObject().apply {
            put("backupVersion", BACKUP_VERSION)
            put("exportTime", System.currentTimeMillis())

            // Core entities
            put("groups", groupDao.getAllGroups().first().toJsonArray { it.toJson() })
            put("playerCharacters", playerCharacterDao.getAllPcs().first().toJsonArray { it.toJson() })
            put("npcs", npcDao.getAllNpcs().first().toJsonArray { it.toJson() })
            put("modules", moduleDao.getAllModules().first().toJsonArray { it.toJson() })
            put("sessions", sessionDao.getAllSessions().first().toJsonArray { it.toJson() })
            put("kpMemos", kpMemoDao.getAllMemos().first().toJsonArray { it.toJson() })
            put("archives", archiveDao.getAll().first().toJsonArray { it.toJson() })
            put("images", imageDao.getAll().first().toJsonArray { it.toJson() })

            // Module detail entities
            put("imageGroups", imageGroupDao.getAll().first().toJsonArray { it.toJson() })
            put("highlights", highlightDao.getAll().first().toJsonArray { it.toJson() })
            put("annotations", annotationDao.getAll().first().toJsonArray { it.toJson() })
            put("readingProgress", readingProgressDao.getAll().first().toJsonArray { it.toJson() })
            put("bookmarks", bookmarkDao.getAll().first().toJsonArray { it.toJson() })
            put("moduleDefaultPcs", moduleDefaultPcDao.getAll().first().toJsonArray { it.toJson() })
            put("moduleDefaultNpcs", moduleDefaultNpcDao.getAll().first().toJsonArray { it.toJson() })
            put("moduleLocations", moduleLocationDao.getAll().first().toJsonArray { it.toJson() })
            put("moduleOrganizations", moduleOrganizationDao.getAll().first().toJsonArray { it.toJson() })
            put("moduleRelationships", moduleRelationshipDao.getAll().first().toJsonArray { it.toJson() })
            put("moduleClues", moduleClueDao.getAll().first().toJsonArray { it.toJson() })
            put("groupRelationships", groupRelationshipDao.getAll().first().toJsonArray { it.toJson() })
            put("calendarEvents", calendarEventDao.getAll().first().toJsonArray { it.toJson() })
            put("documentNodes", documentNodeDao.getAll().first().toJsonArray { it.toJson() })

            // Preferences
            put("preferences", exportPreferences())
        }
    }

    private fun collectImageFiles(): List<Pair<String, File>> {
        val result = mutableListOf<Pair<String, File>>()

        val imagesDir = File(context.filesDir, "images")
        if (imagesDir.exists()) {
            imagesDir.walkTopDown().filter { it.isFile }.forEach { file ->
                val relativePath = "images/${file.relativeTo(imagesDir).path.replace("\\", "/")}"
                result.add(relativePath to file)
            }
        }

        val docImagesDir = File(context.filesDir, "doc_images")
        if (docImagesDir.exists()) {
            docImagesDir.walkTopDown().filter { it.isFile }.forEach { file ->
                val relativePath = "doc_images/${file.relativeTo(docImagesDir).path.replace("\\", "/")}"
                result.add(relativePath to file)
            }
        }

        return result
    }

    private fun getAppVersion(): String {
        return try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "unknown"
        } catch (_: Exception) {
            "unknown"
        }
    }

    // ==================== Import helpers ====================

    private suspend fun clearAllData() {
        // Order matters: delete children before parents (foreign key constraints)
        documentNodeDao.deleteAll()
        calendarEventDao.deleteAll()
        groupRelationshipDao.deleteAll()
        moduleClueDao.deleteAll()
        moduleRelationshipDao.deleteAll()
        moduleOrganizationDao.deleteAll()
        moduleLocationDao.deleteAll()
        moduleDefaultNpcDao.deleteAll()
        moduleDefaultPcDao.deleteAll()
        bookmarkDao.deleteAll()
        readingProgressDao.deleteAll()
        annotationDao.deleteAll()
        highlightDao.deleteAll()
        imageGroupDao.deleteAll()
        imageDao.deleteAll()
        archiveDao.deleteAll()
        kpMemoDao.deleteAll()
        sessionDao.deleteAll()
        npcDao.deleteAll()
        playerCharacterDao.deleteAll()
        moduleDao.deleteAll()
        groupDao.deleteAll()
    }

    private suspend fun importAllData(json: JSONObject) {
        // Core entities (insert one by one for entities with complex dependencies)
        json.getJSONArray("groups").let { arr ->
            (0 until arr.length()).forEach { groupDao.insertGroup(arr.getJSONObject(it).toGroupEntity()) }
        }
        json.getJSONArray("playerCharacters").let { arr ->
            (0 until arr.length()).forEach { playerCharacterDao.insertPc(arr.getJSONObject(it).toPcEntity()) }
        }
        json.getJSONArray("npcs").let { arr ->
            (0 until arr.length()).forEach { npcDao.insertNpc(arr.getJSONObject(it).toNpcEntity()) }
        }
        json.getJSONArray("modules").let { arr ->
            (0 until arr.length()).forEach { moduleDao.insertModule(arr.getJSONObject(it).toModuleEntity()) }
        }
        json.getJSONArray("sessions").let { arr ->
            (0 until arr.length()).forEach { sessionDao.insertSession(arr.getJSONObject(it).toSessionEntity()) }
        }
        json.getJSONArray("kpMemos").let { arr ->
            (0 until arr.length()).forEach { kpMemoDao.insertMemo(arr.getJSONObject(it).toKpMemoEntity()) }
        }

        // Batch insert for entities without complex dependencies
        json.optJSONArray("archives")?.toEntityList { it.toArchiveEntity() }?.let { archiveDao.insertAll(it) }
        json.optJSONArray("images")?.toEntityList { it.toImageEntity() }?.let { imageDao.insertAll(it) }
        json.optJSONArray("imageGroups")?.toEntityList { it.toImageGroupEntity() }?.let { imageGroupDao.insertAll(it) }
        json.optJSONArray("highlights")?.toEntityList { it.toHighlightEntity() }?.let { highlightDao.insertAll(it) }
        json.optJSONArray("annotations")?.toEntityList { it.toAnnotationEntity() }?.let { annotationDao.insertAll(it) }
        json.optJSONArray("readingProgress")?.toEntityList { it.toReadingProgressEntity() }?.let { readingProgressDao.insertAll(it) }
        json.optJSONArray("bookmarks")?.toEntityList { it.toBookmarkEntity() }?.let { bookmarkDao.insertAll(it) }
        json.optJSONArray("moduleDefaultPcs")?.toEntityList { it.toModuleDefaultPcEntity() }?.let { moduleDefaultPcDao.insertAll(it) }
        json.optJSONArray("moduleDefaultNpcs")?.toEntityList { it.toModuleDefaultNpcEntity() }?.let { moduleDefaultNpcDao.insertAll(it) }
        json.optJSONArray("moduleLocations")?.toEntityList { it.toModuleLocationEntity() }?.let { moduleLocationDao.insertAll(it) }
        json.optJSONArray("moduleOrganizations")?.toEntityList { it.toModuleOrganizationEntity() }?.let { moduleOrganizationDao.insertAll(it) }
        json.optJSONArray("moduleRelationships")?.toEntityList { it.toModuleRelationshipEntity() }?.let { moduleRelationshipDao.insertAll(it) }
        json.optJSONArray("moduleClues")?.toEntityList { it.toModuleClueEntity() }?.let { moduleClueDao.insertAll(it) }
        json.optJSONArray("groupRelationships")?.toEntityList { it.toGroupRelationshipEntity() }?.let { groupRelationshipDao.insertAll(it) }

        json.optJSONArray("calendarEvents")?.let { arr ->
            (0 until arr.length()).forEach { calendarEventDao.insert(arr.getJSONObject(it).toCalendarEventEntity()) }
        }
        json.optJSONArray("documentNodes")?.toEntityList { it.toDocumentNodeEntity() }?.let { documentNodeDao.insertAll(it) }
    }

    private fun restoreImageFiles(imageFiles: Map<String, File>) {
        for ((path, tempFile) in imageFiles) {
            val destFile = File(context.filesDir, path)
            destFile.parentFile?.mkdirs()
            tempFile.copyTo(destFile, overwrite = true)
        }
    }

    // ==================== Preferences ====================

    private fun exportPreferences(): JSONObject {
        val kpPrefs = context.getSharedPreferences("kp_preferences", Context.MODE_PRIVATE)
        val themePrefs = context.getSharedPreferences("theme_preferences", Context.MODE_PRIVATE)

        return JSONObject().apply {
            put("kp_nickname", kpPrefs.getString("kp_nickname", "KP") ?: "KP")
            put("theme_mode", themePrefs.getInt("theme_mode", 0))
            put("auto_save_enabled", themePrefs.getBoolean("auto_save_enabled", true))
            put("language_mode", themePrefs.getInt("language_mode", 0))
        }
    }

    private fun importPreferences(prefs: JSONObject) {
        val kpPrefs = context.getSharedPreferences("kp_preferences", Context.MODE_PRIVATE)
        val themePrefs = context.getSharedPreferences("theme_preferences", Context.MODE_PRIVATE)

        kpPrefs.edit().apply {
            putString("kp_nickname", prefs.optString("kp_nickname", "KP"))
            val avatarFile = File(context.filesDir, "kp_avatar.jpg")
            if (avatarFile.exists()) {
                putString("kp_avatar_uri", avatarFile.absolutePath)
            }
            apply()
        }

        themePrefs.edit().apply {
            putInt("theme_mode", prefs.optInt("theme_mode", 0))
            putBoolean("auto_save_enabled", prefs.optBoolean("auto_save_enabled", true))
            putInt("language_mode", prefs.optInt("language_mode", 0))
            apply()
        }
    }

    // ==================== Entity to JSON ====================

    private fun GroupEntity.toJson() = JSONObject().apply {
        put("groupId", groupId); put("groupName", groupName); put("moduleName", moduleName)
        put("system", system); put("status", status); put("currentSession", currentSession)
        put("createTime", createTime); putOpt("lastPlayTime", lastPlayTime)
        putOpt("nextPlayTime", nextPlayTime); putOpt("moduleId", moduleId)
        putOpt("coverImageUri", coverImageUri); put("notes", notes)
    }

    private fun PlayerCharacterEntity.toJson() = JSONObject().apply {
        put("pcId", pcId); put("groupId", groupId); put("playerName", playerName)
        put("characterName", characterName); put("system", system)
        put("hpCurrent", hpCurrent); put("hpMax", hpMax); put("sanCurrent", sanCurrent)
        put("sanMax", sanMax); put("luck", luck); put("attributesJson", attributesJson)
        put("skillsJson", skillsJson); put("background", background)
        put("inventoryJson", inventoryJson); put("kpNotes", kpNotes); put("status", status)
        putOpt("portraitUri", portraitUri); put("createTime", createTime)
    }

    private fun NpcEntity.toJson() = JSONObject().apply {
        put("npcId", npcId); put("groupId", groupId); put("name", name); put("alias", alias)
        put("occupation", occupation); put("description", description)
        put("truePurpose", truePurpose); put("relationshipToPc", relationshipToPc)
        put("status", status); put("firstAppearance", firstAppearance); put("kpNotes", kpNotes)
        putOpt("portraitUri", portraitUri); put("createTime", createTime)
    }

    private fun ModuleEntity.toJson() = JSONObject().apply {
        put("moduleId", moduleId); put("title", title); put("author", author); put("system", system)
        put("difficulty", difficulty); put("playerCount", playerCount); put("duration", duration)
        put("synopsis", synopsis); put("tags", tags); put("contentJson", contentJson)
        putOpt("coverImageUri", coverImageUri); put("isFavorite", isFavorite)
        put("isUserCreated", isUserCreated); put("isCollection", isCollection)
        put("createTime", createTime)
    }

    private fun ArchiveEntity.toJson() = JSONObject().apply {
        put("archiveId", archiveId); put("collectionId", collectionId); put("title", title)
        put("contentMarkdown", contentMarkdown); put("originalFileName", originalFileName)
        put("fileType", fileType); put("sortOrder", sortOrder); put("createTime", createTime)
    }

    private fun ImageEntity.toJson() = JSONObject().apply {
        put("imageId", imageId); put("collectionId", collectionId); put("title", title)
        put("filePath", filePath); put("originalFileName", originalFileName)
        putOpt("imageGroupId", imageGroupId); put("sortOrder", sortOrder); put("createTime", createTime)
    }

    private fun ImageGroupEntity.toJson() = JSONObject().apply {
        put("imageGroupId", imageGroupId); put("collectionId", collectionId); put("name", name)
        put("description", description); put("sortOrder", sortOrder); put("createTime", createTime)
    }

    private fun SessionEntity.toJson() = JSONObject().apply {
        put("sessionId", sessionId); put("groupId", groupId); put("sessionNumber", sessionNumber)
        put("date", date); put("durationMinutes", durationMinutes)
        put("participantPcIds", participantPcIds); put("summary", summary)
        put("importantEventsJson", importantEventsJson); put("cluesFoundJson", cluesFoundJson)
        put("diceRollsJson", diceRollsJson); put("nextSessionNotes", nextSessionNotes)
        put("createTime", createTime)
    }

    private fun KpMemoEntity.toJson() = JSONObject().apply {
        put("memoId", memoId); put("groupId", groupId); put("type", type); put("title", title)
        put("content", content); put("isHidden", isHidden); put("isCompleted", isCompleted)
        put("priority", priority); put("tags", tags); put("createTime", createTime)
        put("updateTime", updateTime)
    }

    private fun HighlightEntity.toJson() = JSONObject().apply {
        put("highlightId", highlightId); put("moduleId", moduleId); put("chapterId", chapterId)
        putOpt("nodeId", nodeId); put("startIndex", startIndex); put("endIndex", endIndex)
        put("selectedText", selectedText); put("color", color); put("createTime", createTime)
    }

    private fun AnnotationEntity.toJson() = JSONObject().apply {
        put("annotationId", annotationId); put("moduleId", moduleId); put("chapterId", chapterId)
        putOpt("nodeId", nodeId); put("startIndex", startIndex); put("endIndex", endIndex)
        put("selectedText", selectedText); put("note", note); put("color", color)
        put("createTime", createTime); put("updateTime", updateTime)
    }

    private fun ReadingProgressEntity.toJson() = JSONObject().apply {
        put("moduleId", moduleId); put("lastChapterId", lastChapterId)
        put("lastReadTime", lastReadTime); put("totalReadTimeMinutes", totalReadTimeMinutes)
        put("readCount", readCount); put("totalReadTimeSeconds", totalReadTimeSeconds)
    }

    private fun BookmarkEntity.toJson() = JSONObject().apply {
        put("bookmarkId", bookmarkId); put("moduleId", moduleId); put("chapterId", chapterId)
        putOpt("nodeId", nodeId); put("chapterTitle", chapterTitle)
        put("selectedText", selectedText); put("note", note); put("color", color)
        put("createTime", createTime)
    }

    private fun ModuleDefaultPcEntity.toJson() = JSONObject().apply {
        put("id", id); put("moduleId", moduleId); put("name", name); put("playerName", playerName)
        put("system", system); put("description", description); put("attributesJson", attributesJson)
        putOpt("portraitUri", portraitUri); put("sortOrder", sortOrder); put("createTime", createTime)
    }

    private fun ModuleDefaultNpcEntity.toJson() = JSONObject().apply {
        put("id", id); put("moduleId", moduleId); put("name", name); put("alias", alias)
        put("occupation", occupation); put("description", description)
        put("truePurpose", truePurpose); put("relationshipToPc", relationshipToPc)
        put("status", status); put("gender", gender); putOpt("portraitUri", portraitUri)
        put("sortOrder", sortOrder); put("createTime", createTime)
    }

    private fun ModuleLocationEntity.toJson() = JSONObject().apply {
        put("id", id); put("moduleId", moduleId); put("name", name); put("type", type)
        put("description", description); put("clues", clues); put("inhabitants", inhabitants)
        put("sortOrder", sortOrder); put("createTime", createTime)
    }

    private fun ModuleOrganizationEntity.toJson() = JSONObject().apply {
        put("id", id); put("moduleId", moduleId); put("name", name); put("type", type)
        put("description", description); put("members", members); put("goals", goals)
        put("sortOrder", sortOrder); put("createTime", createTime)
    }

    private fun ModuleRelationshipEntity.toJson() = JSONObject().apply {
        put("id", id); put("moduleId", moduleId); put("sourceId", sourceId)
        put("sourceType", sourceType); put("targetId", targetId); put("targetType", targetType)
        put("relationType", relationType); put("description", description); put("createTime", createTime)
    }

    private fun ModuleClueEntity.toJson() = JSONObject().apply {
        put("id", id); put("moduleId", moduleId); put("name", name); put("type", type)
        put("description", description); put("source", source); put("isHidden", isHidden)
        put("color", color); put("sortOrder", sortOrder); put("createTime", createTime)
    }

    private fun GroupRelationshipEntity.toJson() = JSONObject().apply {
        put("id", id); put("groupId", groupId); put("sourceId", sourceId)
        put("sourceType", sourceType); put("targetId", targetId); put("targetType", targetType)
        put("relationType", relationType); put("description", description); put("createTime", createTime)
    }

    private fun CalendarEventEntity.toJson() = JSONObject().apply {
        put("eventId", eventId); put("groupId", groupId); put("title", title); put("date", date)
        putOpt("time", time); put("type", type); putOpt("sessionId", sessionId)
        put("isRemindEnabled", isRemindEnabled); put("createTime", createTime)
    }

    private fun DocumentNodeEntity.toJson() = JSONObject().apply {
        put("nodeId", nodeId); put("moduleId", moduleId); put("type", type); put("level", level)
        put("content", content); putOpt("tableData", tableData); putOpt("imageUri", imageUri)
        put("order", order)
    }

    // ==================== JSON to Entity ====================

    private fun JSONObject.toGroupEntity() = GroupEntity(
        groupId = getString("groupId"), groupName = getString("groupName"),
        moduleName = getString("moduleName"), system = optString("system", ""),
        status = getString("status"), currentSession = optInt("currentSession", 0),
        createTime = optLong("createTime", 0), lastPlayTime = optLongOrNull("lastPlayTime"),
        nextPlayTime = optLongOrNull("nextPlayTime"), moduleId = optStringOrNull("moduleId"),
        coverImageUri = optStringOrNull("coverImageUri"), notes = optString("notes", "")
    )

    private fun JSONObject.toPcEntity() = PlayerCharacterEntity(
        pcId = getString("pcId"), groupId = getString("groupId"),
        playerName = getString("playerName"), characterName = getString("characterName"),
        system = getString("system"), hpCurrent = optInt("hpCurrent", 0),
        hpMax = optInt("hpMax", 0), sanCurrent = optInt("sanCurrent", 0),
        sanMax = optInt("sanMax", 0), luck = optInt("luck", 0),
        attributesJson = optString("attributesJson", "{}"),
        skillsJson = optString("skillsJson", "{}"), background = optString("background", ""),
        inventoryJson = optString("inventoryJson", "[]"), kpNotes = optString("kpNotes", ""),
        status = optString("status", "normal"), portraitUri = optStringOrNull("portraitUri"),
        createTime = optLong("createTime", 0)
    )

    private fun JSONObject.toNpcEntity() = NpcEntity(
        npcId = getString("npcId"), groupId = getString("groupId"), name = getString("name"),
        alias = optString("alias", ""), occupation = optString("occupation", ""),
        description = optString("description", ""), truePurpose = optString("truePurpose", ""),
        relationshipToPc = optString("relationshipToPc", ""), status = optString("status", "alive"),
        firstAppearance = optString("firstAppearance", ""), kpNotes = optString("kpNotes", ""),
        portraitUri = optStringOrNull("portraitUri"), createTime = optLong("createTime", 0)
    )

    private fun JSONObject.toModuleEntity() = ModuleEntity(
        moduleId = getString("moduleId"), title = getString("title"),
        author = optString("author", ""), system = optString("system", ""),
        difficulty = optString("difficulty", ""), playerCount = optString("playerCount", ""),
        duration = optString("duration", ""), synopsis = optString("synopsis", ""),
        tags = optString("tags", ""), contentJson = optString("contentJson", "{}"),
        coverImageUri = optStringOrNull("coverImageUri"), isFavorite = optBoolean("isFavorite", false),
        isUserCreated = optBoolean("isUserCreated", false),
        isCollection = optBoolean("isCollection", false), createTime = optLong("createTime", 0)
    )

    private fun JSONObject.toArchiveEntity() = ArchiveEntity(
        archiveId = getString("archiveId"), collectionId = getString("collectionId"),
        title = optString("title", ""), contentMarkdown = optString("contentMarkdown", ""),
        originalFileName = optString("originalFileName", ""), fileType = optString("fileType", ""),
        sortOrder = optInt("sortOrder", 0), createTime = optLong("createTime", 0)
    )

    private fun JSONObject.toImageEntity() = ImageEntity(
        imageId = getString("imageId"), collectionId = getString("collectionId"),
        title = optString("title", ""), filePath = getString("filePath"),
        originalFileName = optString("originalFileName", ""),
        imageGroupId = optStringOrNull("imageGroupId"), sortOrder = optInt("sortOrder", 0),
        createTime = optLong("createTime", 0)
    )

    private fun JSONObject.toImageGroupEntity() = ImageGroupEntity(
        imageGroupId = getString("imageGroupId"), collectionId = getString("collectionId"),
        name = optString("name", ""), description = optString("description", ""),
        sortOrder = optInt("sortOrder", 0), createTime = optLong("createTime", 0)
    )

    private fun JSONObject.toSessionEntity() = SessionEntity(
        sessionId = getString("sessionId"), groupId = getString("groupId"),
        sessionNumber = optInt("sessionNumber", 0), date = optLong("date", 0),
        durationMinutes = optInt("durationMinutes", 0),
        participantPcIds = optString("participantPcIds", ""), summary = optString("summary", ""),
        importantEventsJson = optString("importantEventsJson", "[]"),
        cluesFoundJson = optString("cluesFoundJson", "[]"),
        diceRollsJson = optString("diceRollsJson", "[]"),
        nextSessionNotes = optString("nextSessionNotes", ""), createTime = optLong("createTime", 0)
    )

    private fun JSONObject.toKpMemoEntity() = KpMemoEntity(
        memoId = getString("memoId"), groupId = getString("groupId"), type = getString("type"),
        title = optString("title", ""), content = optString("content", ""),
        isHidden = optBoolean("isHidden", false), isCompleted = optBoolean("isCompleted", false),
        priority = optInt("priority", 0), tags = optString("tags", ""),
        createTime = optLong("createTime", 0), updateTime = optLong("updateTime", 0)
    )

    private fun JSONObject.toHighlightEntity() = HighlightEntity(
        highlightId = getString("highlightId"), moduleId = getString("moduleId"),
        chapterId = optString("chapterId", ""), nodeId = optStringOrNull("nodeId"),
        startIndex = optInt("startIndex", 0), endIndex = optInt("endIndex", 0),
        selectedText = optString("selectedText", ""), color = optLong("color", 0xFFFFEB3B),
        createTime = optLong("createTime", 0)
    )

    private fun JSONObject.toAnnotationEntity() = AnnotationEntity(
        annotationId = getString("annotationId"), moduleId = getString("moduleId"),
        chapterId = optString("chapterId", ""), nodeId = optStringOrNull("nodeId"),
        startIndex = optInt("startIndex", 0), endIndex = optInt("endIndex", 0),
        selectedText = optString("selectedText", ""), note = optString("note", ""),
        color = optLong("color", 0xFF4CAF50), createTime = optLong("createTime", 0),
        updateTime = optLong("updateTime", 0)
    )

    private fun JSONObject.toReadingProgressEntity() = ReadingProgressEntity(
        moduleId = getString("moduleId"), lastChapterId = optString("lastChapterId", ""),
        lastReadTime = optLong("lastReadTime", 0),
        totalReadTimeMinutes = optInt("totalReadTimeMinutes", 0),
        readCount = optInt("readCount", 0), totalReadTimeSeconds = optLong("totalReadTimeSeconds", 0)
    )

    private fun JSONObject.toBookmarkEntity() = BookmarkEntity(
        bookmarkId = getString("bookmarkId"), moduleId = getString("moduleId"),
        chapterId = optString("chapterId", ""), nodeId = optStringOrNull("nodeId"),
        chapterTitle = optString("chapterTitle", ""),
        selectedText = optString("selectedText", ""), note = optString("note", ""),
        color = optLong("color", 0xFFFF9800), createTime = optLong("createTime", 0)
    )

    private fun JSONObject.toModuleDefaultPcEntity() = ModuleDefaultPcEntity(
        id = getString("id"), moduleId = getString("moduleId"), name = optString("name", ""),
        playerName = optString("playerName", ""), system = optString("system", ""),
        description = optString("description", ""),
        attributesJson = optString("attributesJson", "{}"),
        portraitUri = optStringOrNull("portraitUri"), sortOrder = optInt("sortOrder", 0),
        createTime = optLong("createTime", 0)
    )

    private fun JSONObject.toModuleDefaultNpcEntity() = ModuleDefaultNpcEntity(
        id = getString("id"), moduleId = getString("moduleId"), name = optString("name", ""),
        alias = optString("alias", ""), occupation = optString("occupation", ""),
        description = optString("description", ""), truePurpose = optString("truePurpose", ""),
        relationshipToPc = optString("relationshipToPc", ""),
        status = optString("status", "alive"), gender = optString("gender", ""),
        portraitUri = optStringOrNull("portraitUri"), sortOrder = optInt("sortOrder", 0),
        createTime = optLong("createTime", 0)
    )

    private fun JSONObject.toModuleLocationEntity() = ModuleLocationEntity(
        id = getString("id"), moduleId = getString("moduleId"), name = optString("name", ""),
        type = optString("type", ""), description = optString("description", ""),
        clues = optString("clues", ""), inhabitants = optString("inhabitants", ""),
        sortOrder = optInt("sortOrder", 0), createTime = optLong("createTime", 0)
    )

    private fun JSONObject.toModuleOrganizationEntity() = ModuleOrganizationEntity(
        id = getString("id"), moduleId = getString("moduleId"), name = optString("name", ""),
        type = optString("type", ""), description = optString("description", ""),
        members = optString("members", ""), goals = optString("goals", ""),
        sortOrder = optInt("sortOrder", 0), createTime = optLong("createTime", 0)
    )

    private fun JSONObject.toModuleRelationshipEntity() = ModuleRelationshipEntity(
        id = getString("id"), moduleId = getString("moduleId"),
        sourceId = optString("sourceId", ""), sourceType = optString("sourceType", ""),
        targetId = optString("targetId", ""), targetType = optString("targetType", ""),
        relationType = optString("relationType", ""), description = optString("description", ""),
        createTime = optLong("createTime", 0)
    )

    private fun JSONObject.toModuleClueEntity() = ModuleClueEntity(
        id = getString("id"), moduleId = getString("moduleId"), name = optString("name", ""),
        type = optString("type", ""), description = optString("description", ""),
        source = optString("source", ""), isHidden = optBoolean("isHidden", false),
        color = optLong("color", 0), sortOrder = optInt("sortOrder", 0),
        createTime = optLong("createTime", 0)
    )

    private fun JSONObject.toGroupRelationshipEntity() = GroupRelationshipEntity(
        id = getString("id"), groupId = getString("groupId"),
        sourceId = optString("sourceId", ""), sourceType = optString("sourceType", ""),
        targetId = optString("targetId", ""), targetType = optString("targetType", ""),
        relationType = optString("relationType", ""), description = optString("description", ""),
        createTime = optLong("createTime", 0)
    )

    private fun JSONObject.toCalendarEventEntity() = CalendarEventEntity(
        eventId = getString("eventId"), groupId = getString("groupId"),
        title = optString("title", ""), date = optLong("date", 0),
        time = optStringOrNull("time"), type = optString("type", "custom"),
        sessionId = optStringOrNull("sessionId"),
        isRemindEnabled = optBoolean("isRemindEnabled", true),
        createTime = optLong("createTime", 0)
    )

    private fun JSONObject.toDocumentNodeEntity() = DocumentNodeEntity(
        nodeId = getString("nodeId"), moduleId = getString("moduleId"),
        type = optString("type", "paragraph"), level = optInt("level", 0),
        content = optString("content", ""), tableData = optStringOrNull("tableData"),
        imageUri = optStringOrNull("imageUri"), order = optInt("order", 0)
    )

    // ==================== Helpers ====================

    private fun <T> List<T>.toJsonArray(serializer: (T) -> JSONObject): JSONArray {
        return JSONArray().apply { this@toJsonArray.forEach { put(serializer(it)) } }
    }

    private fun <T> JSONArray.toEntityList(parser: (JSONObject) -> T): List<T> {
        return (0 until length()).map { parser(getJSONObject(it)) }
    }

    private fun JSONObject.optLongOrNull(key: String): Long? {
        return if (isNull(key)) null else optLong(key)
    }

    private fun JSONObject.optStringOrNull(key: String): String? {
        return if (isNull(key)) null else optString(key)
    }

    private fun sha256(data: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(data)
        return hash.joinToString("") { "%02x".format(it) }
    }
}

class BackupException(message: String) : Exception(message)
