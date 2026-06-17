package com.example.keepersnotes.data.repository

import com.example.keepersnotes.data.local.dao.PlayerCharacterDao
import com.example.keepersnotes.data.local.entity.PlayerCharacterEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerCharacterRepository @Inject constructor(
    private val pcDao: PlayerCharacterDao
) {
    fun getPcsByGroupId(groupId: String): Flow<List<PlayerCharacterEntity>> =
        pcDao.getPcsByGroupId(groupId)

    fun getPcById(pcId: String): Flow<PlayerCharacterEntity?> = pcDao.getPcById(pcId)

    suspend fun getPcByIdOnce(pcId: String): PlayerCharacterEntity? = pcDao.getPcByIdOnce(pcId)

    fun getPcCountByGroupId(groupId: String): Flow<Int> = pcDao.getPcCountByGroupId(groupId)

    fun getTotalPcCount(): Flow<Int> = pcDao.getTotalPcCount()

    fun searchPcs(query: String): Flow<List<PlayerCharacterEntity>> = pcDao.searchPcs(query)

    suspend fun createPc(
        groupId: String,
        playerName: String,
        characterName: String,
        system: String = "COC7",
        gender: String = ""
    ): String {
        val pcId = UUID.randomUUID().toString()
        pcDao.insertPc(
            PlayerCharacterEntity(
                pcId = pcId,
                groupId = groupId,
                playerName = playerName,
                characterName = characterName,
                system = system,
                gender = gender
            )
        )
        return pcId
    }

    suspend fun updatePc(pc: PlayerCharacterEntity) = pcDao.updatePc(pc)

    suspend fun deletePc(pcId: String) = pcDao.deletePcById(pcId)

    suspend fun updateHp(pcId: String, current: Int, max: Int) {
        pcDao.getPcByIdOnce(pcId)?.let { pc ->
            pcDao.updatePc(pc.copy(hpCurrent = current, hpMax = max))
        }
    }

    suspend fun updateSan(pcId: String, current: Int, max: Int) {
        pcDao.getPcByIdOnce(pcId)?.let { pc ->
            pcDao.updatePc(pc.copy(sanCurrent = current, sanMax = max))
        }
    }

    suspend fun updateStatus(pcId: String, status: String) {
        pcDao.getPcByIdOnce(pcId)?.let { pc ->
            pcDao.updatePc(pc.copy(status = status))
        }
    }
}
