package com.example.keepersnotes.data.repository

import com.example.keepersnotes.data.local.dao.NpcDao
import com.example.keepersnotes.data.local.entity.NpcEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NpcRepository @Inject constructor(
    private val npcDao: NpcDao
) {
    fun getNpcsByGroupId(groupId: String): Flow<List<NpcEntity>> =
        npcDao.getNpcsByGroupId(groupId)

    fun getNpcById(npcId: String): Flow<NpcEntity?> = npcDao.getNpcById(npcId)

    suspend fun getNpcByIdOnce(npcId: String): NpcEntity? = npcDao.getNpcByIdOnce(npcId)

    fun searchNpcs(groupId: String, query: String): Flow<List<NpcEntity>> =
        npcDao.searchNpcs(groupId, query)

    suspend fun createNpc(
        groupId: String,
        name: String,
        alias: String = "",
        occupation: String = "",
        gender: String = ""
    ): String {
        val npcId = UUID.randomUUID().toString()
        npcDao.insertNpc(
            NpcEntity(
                npcId = npcId,
                groupId = groupId,
                name = name,
                alias = alias,
                occupation = occupation,
                gender = gender
            )
        )
        return npcId
    }

    suspend fun updateNpc(npc: NpcEntity) = npcDao.updateNpc(npc)

    suspend fun deleteNpc(npcId: String) = npcDao.deleteNpcById(npcId)
}
