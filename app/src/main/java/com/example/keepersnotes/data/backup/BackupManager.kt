package com.example.keepersnotes.data.backup

import com.example.keepersnotes.data.local.dao.*
import com.example.keepersnotes.data.local.entity.*
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupManager @Inject constructor(
    private val groupDao: GroupDao,
    private val playerCharacterDao: PlayerCharacterDao,
    private val npcDao: NpcDao,
    private val moduleDao: ModuleDao,
    private val sessionDao: SessionDao,
    private val kpMemoDao: KpMemoDao,
    private val archiveDao: ArchiveDao,
    private val imageDao: ImageDao,
    private val imageGroupDao: ImageGroupDao
) {
    companion object {
        const val BACKUP_VERSION = 2
    }

    suspend fun exportTo(outputStream: OutputStream) {
        val json = JSONObject().apply {
            put("backupVersion", BACKUP_VERSION)
            put("exportTime", System.currentTimeMillis())
            put("groups", groupDao.getAllGroups().first().toJsonArray { it.toJson() })
            put("playerCharacters", playerCharacterDao.getAllPcs().first().toJsonArray { it.toJson() })
            put("npcs", npcDao.getAllNpcs().first().toJsonArray { it.toJson() })
            put("modules", moduleDao.getAllModules().first().toJsonArray { it.toJson() })
            put("sessions", sessionDao.getAllSessions().first().toJsonArray { it.toJson() })
            put("kpMemos", kpMemoDao.getAllMemos().first().toJsonArray { it.toJson() })
            put("archives", archiveDao.getAll().first().toJsonArray { it.toJson() })
            put("images", imageDao.getAll().first().toJsonArray { it.toJson() })
            put("imageGroups", imageGroupDao.getAll().first().toJsonArray { it.toJson() })
        }
        outputStream.write(json.toString(2).toByteArray(Charsets.UTF_8))
    }

    suspend fun importFrom(inputStream: InputStream) {
        val text = inputStream.bufferedReader().readText()
        val json = JSONObject(text)

        // Clear existing data (order matters for foreign keys)
        imageDao.deleteAll()
        imageGroupDao.deleteAll()
        archiveDao.deleteAll()
        kpMemoDao.deleteAll()
        sessionDao.deleteAll()
        npcDao.deleteAll()
        playerCharacterDao.deleteAll()
        moduleDao.deleteAll()
        groupDao.deleteAll()

        // Import data
        json.getJSONArray("groups").let { arr ->
            for (i in 0 until arr.length()) groupDao.insertGroup(arr.getJSONObject(i).toGroupEntity())
        }
        json.getJSONArray("playerCharacters").let { arr ->
            for (i in 0 until arr.length()) playerCharacterDao.insertPc(arr.getJSONObject(i).toPcEntity())
        }
        json.getJSONArray("npcs").let { arr ->
            for (i in 0 until arr.length()) npcDao.insertNpc(arr.getJSONObject(i).toNpcEntity())
        }
        json.getJSONArray("modules").let { arr ->
            for (i in 0 until arr.length()) moduleDao.insertModule(arr.getJSONObject(i).toModuleEntity())
        }
        json.getJSONArray("sessions").let { arr ->
            for (i in 0 until arr.length()) sessionDao.insertSession(arr.getJSONObject(i).toSessionEntity())
        }
        json.getJSONArray("kpMemos").let { arr ->
            for (i in 0 until arr.length()) kpMemoDao.insertMemo(arr.getJSONObject(i).toKpMemoEntity())
        }
        json.optJSONArray("imageGroups")?.let { arr ->
            for (i in 0 until arr.length()) imageGroupDao.insert(arr.getJSONObject(i).toImageGroupEntity())
        }
        json.optJSONArray("archives")?.let { arr ->
            for (i in 0 until arr.length()) archiveDao.insert(arr.getJSONObject(i).toArchiveEntity())
        }
        json.optJSONArray("images")?.let { arr ->
            for (i in 0 until arr.length()) imageDao.insert(arr.getJSONObject(i).toImageEntity())
        }
    }

    // --- Entity to JSON ---

    private fun GroupEntity.toJson() = JSONObject().apply {
        put("groupId", groupId)
        put("groupName", groupName)
        put("moduleName", moduleName)
        put("system", system)
        put("status", status)
        put("currentSession", currentSession)
        put("createTime", createTime)
        putOpt("lastPlayTime", lastPlayTime)
        putOpt("nextPlayTime", nextPlayTime)
        putOpt("moduleId", moduleId)
        putOpt("coverImageUri", coverImageUri)
        put("notes", notes)
    }

    private fun PlayerCharacterEntity.toJson() = JSONObject().apply {
        put("pcId", pcId)
        put("groupId", groupId)
        put("playerName", playerName)
        put("characterName", characterName)
        put("system", system)
        put("hpCurrent", hpCurrent)
        put("hpMax", hpMax)
        put("sanCurrent", sanCurrent)
        put("sanMax", sanMax)
        put("luck", luck)
        put("attributesJson", attributesJson)
        put("skillsJson", skillsJson)
        put("background", background)
        put("inventoryJson", inventoryJson)
        put("kpNotes", kpNotes)
        put("status", status)
        putOpt("portraitUri", portraitUri)
        put("createTime", createTime)
    }

    private fun NpcEntity.toJson() = JSONObject().apply {
        put("npcId", npcId)
        put("groupId", groupId)
        put("name", name)
        put("alias", alias)
        put("occupation", occupation)
        put("description", description)
        put("truePurpose", truePurpose)
        put("relationshipToPc", relationshipToPc)
        put("status", status)
        put("firstAppearance", firstAppearance)
        put("kpNotes", kpNotes)
        putOpt("portraitUri", portraitUri)
        put("createTime", createTime)
    }

    private fun ModuleEntity.toJson() = JSONObject().apply {
        put("moduleId", moduleId)
        put("title", title)
        put("author", author)
        put("system", system)
        put("difficulty", difficulty)
        put("playerCount", playerCount)
        put("duration", duration)
        put("synopsis", synopsis)
        put("tags", tags)
        put("contentJson", contentJson)
        putOpt("coverImageUri", coverImageUri)
        put("isFavorite", isFavorite)
        put("isUserCreated", isUserCreated)
        put("isCollection", isCollection)
        put("createTime", createTime)
    }

    private fun ArchiveEntity.toJson() = JSONObject().apply {
        put("archiveId", archiveId)
        put("collectionId", collectionId)
        put("title", title)
        put("contentMarkdown", contentMarkdown)
        put("originalFileName", originalFileName)
        put("fileType", fileType)
        put("sortOrder", sortOrder)
        put("createTime", createTime)
    }

    private fun ImageEntity.toJson() = JSONObject().apply {
        put("imageId", imageId)
        put("collectionId", collectionId)
        put("title", title)
        put("filePath", filePath)
        put("originalFileName", originalFileName)
        putOpt("imageGroupId", imageGroupId)
        put("sortOrder", sortOrder)
        put("createTime", createTime)
    }

    private fun ImageGroupEntity.toJson() = JSONObject().apply {
        put("imageGroupId", imageGroupId)
        put("collectionId", collectionId)
        put("name", name)
        put("description", description)
        put("sortOrder", sortOrder)
        put("createTime", createTime)
    }

    private fun SessionEntity.toJson() = JSONObject().apply {
        put("sessionId", sessionId)
        put("groupId", groupId)
        put("sessionNumber", sessionNumber)
        put("date", date)
        put("durationMinutes", durationMinutes)
        put("participantPcIds", participantPcIds)
        put("summary", summary)
        put("importantEventsJson", importantEventsJson)
        put("cluesFoundJson", cluesFoundJson)
        put("diceRollsJson", diceRollsJson)
        put("nextSessionNotes", nextSessionNotes)
        put("createTime", createTime)
    }

    private fun KpMemoEntity.toJson() = JSONObject().apply {
        put("memoId", memoId)
        put("groupId", groupId)
        put("type", type)
        put("title", title)
        put("content", content)
        put("isHidden", isHidden)
        put("isCompleted", isCompleted)
        put("priority", priority)
        put("tags", tags)
        put("createTime", createTime)
        put("updateTime", updateTime)
    }

    // --- JSON to Entity ---

    private fun JSONObject.toGroupEntity() = GroupEntity(
        groupId = getString("groupId"),
        groupName = getString("groupName"),
        moduleName = getString("moduleName"),
        system = optString("system", ""),
        status = getString("status"),
        currentSession = optInt("currentSession", 0),
        createTime = optLong("createTime", 0),
        lastPlayTime = optLongOrNull("lastPlayTime"),
        nextPlayTime = optLongOrNull("nextPlayTime"),
        moduleId = optStringOrNull("moduleId"),
        coverImageUri = optStringOrNull("coverImageUri"),
        notes = optString("notes", "")
    )

    private fun JSONObject.toPcEntity() = PlayerCharacterEntity(
        pcId = getString("pcId"),
        groupId = getString("groupId"),
        playerName = getString("playerName"),
        characterName = getString("characterName"),
        system = getString("system"),
        hpCurrent = optInt("hpCurrent", 0),
        hpMax = optInt("hpMax", 0),
        sanCurrent = optInt("sanCurrent", 0),
        sanMax = optInt("sanMax", 0),
        luck = optInt("luck", 0),
        attributesJson = optString("attributesJson", "{}"),
        skillsJson = optString("skillsJson", "{}"),
        background = optString("background", ""),
        inventoryJson = optString("inventoryJson", "[]"),
        kpNotes = optString("kpNotes", ""),
        status = optString("status", "normal"),
        portraitUri = optStringOrNull("portraitUri"),
        createTime = optLong("createTime", 0)
    )

    private fun JSONObject.toNpcEntity() = NpcEntity(
        npcId = getString("npcId"),
        groupId = getString("groupId"),
        name = getString("name"),
        alias = optString("alias", ""),
        occupation = optString("occupation", ""),
        description = optString("description", ""),
        truePurpose = optString("truePurpose", ""),
        relationshipToPc = optString("relationshipToPc", ""),
        status = optString("status", "alive"),
        firstAppearance = optString("firstAppearance", ""),
        kpNotes = optString("kpNotes", ""),
        portraitUri = optStringOrNull("portraitUri"),
        createTime = optLong("createTime", 0)
    )

    private fun JSONObject.toModuleEntity() = ModuleEntity(
        moduleId = getString("moduleId"),
        title = getString("title"),
        author = optString("author", ""),
        system = optString("system", ""),
        difficulty = optString("difficulty", ""),
        playerCount = optString("playerCount", ""),
        duration = optString("duration", ""),
        synopsis = optString("synopsis", ""),
        tags = optString("tags", ""),
        contentJson = optString("contentJson", "{}"),
        coverImageUri = optStringOrNull("coverImageUri"),
        isFavorite = optBoolean("isFavorite", false),
        isUserCreated = optBoolean("isUserCreated", false),
        isCollection = optBoolean("isCollection", false),
        createTime = optLong("createTime", 0)
    )

    private fun JSONObject.toArchiveEntity() = ArchiveEntity(
        archiveId = getString("archiveId"),
        collectionId = getString("collectionId"),
        title = optString("title", ""),
        contentMarkdown = optString("contentMarkdown", ""),
        originalFileName = optString("originalFileName", ""),
        fileType = optString("fileType", ""),
        sortOrder = optInt("sortOrder", 0),
        createTime = optLong("createTime", 0)
    )

    private fun JSONObject.toImageEntity() = ImageEntity(
        imageId = getString("imageId"),
        collectionId = getString("collectionId"),
        title = optString("title", ""),
        filePath = getString("filePath"),
        originalFileName = optString("originalFileName", ""),
        imageGroupId = optStringOrNull("imageGroupId"),
        sortOrder = optInt("sortOrder", 0),
        createTime = optLong("createTime", 0)
    )

    private fun JSONObject.toImageGroupEntity() = ImageGroupEntity(
        imageGroupId = getString("imageGroupId"),
        collectionId = getString("collectionId"),
        name = optString("name", ""),
        description = optString("description", ""),
        sortOrder = optInt("sortOrder", 0),
        createTime = optLong("createTime", 0)
    )

    private fun JSONObject.toSessionEntity() = SessionEntity(
        sessionId = getString("sessionId"),
        groupId = getString("groupId"),
        sessionNumber = optInt("sessionNumber", 0),
        date = optLong("date", 0),
        durationMinutes = optInt("durationMinutes", 0),
        participantPcIds = optString("participantPcIds", ""),
        summary = optString("summary", ""),
        importantEventsJson = optString("importantEventsJson", "[]"),
        cluesFoundJson = optString("cluesFoundJson", "[]"),
        diceRollsJson = optString("diceRollsJson", "[]"),
        nextSessionNotes = optString("nextSessionNotes", ""),
        createTime = optLong("createTime", 0)
    )

    private fun JSONObject.toKpMemoEntity() = KpMemoEntity(
        memoId = getString("memoId"),
        groupId = getString("groupId"),
        type = getString("type"),
        title = optString("title", ""),
        content = optString("content", ""),
        isHidden = optBoolean("isHidden", false),
        isCompleted = optBoolean("isCompleted", false),
        priority = optInt("priority", 0),
        tags = optString("tags", ""),
        createTime = optLong("createTime", 0),
        updateTime = optLong("updateTime", 0)
    )

    // --- Helpers ---

    private fun <T> List<T>.toJsonArray(serializer: (T) -> JSONObject): JSONArray {
        return JSONArray().apply { this@toJsonArray.forEach { put(serializer(it)) } }
    }

    private fun JSONArray.forEachObj(action: (JSONObject) -> Unit) {
        for (i in 0 until length()) action(getJSONObject(i))
    }

    private fun JSONObject.optLongOrNull(key: String): Long? {
        return if (isNull(key)) null else optLong(key)
    }

    private fun JSONObject.optStringOrNull(key: String): String? {
        return if (isNull(key)) null else optString(key)
    }
}
