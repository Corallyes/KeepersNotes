package com.example.keepersnotes.data.repository

import com.example.keepersnotes.data.local.dao.GroupDao
import com.example.keepersnotes.data.local.entity.GroupEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GroupRepository @Inject constructor(
    private val groupDao: GroupDao
) {
    fun getAllGroups(): Flow<List<GroupEntity>> = groupDao.getAllGroups()

    fun getActiveGroups(): Flow<List<GroupEntity>> = groupDao.getActiveGroups()

    fun getActiveGroupCount(): Flow<Int> = groupDao.getActiveGroupCount()

    fun getCompletedGroupCount(): Flow<Int> = groupDao.getCompletedGroupCount()

    fun getGroupById(groupId: String): Flow<GroupEntity?> = groupDao.getGroupById(groupId)

    suspend fun getGroupByIdOnce(groupId: String): GroupEntity? = groupDao.getGroupByIdOnce(groupId)

    suspend fun createGroup(
        groupName: String,
        moduleName: String,
        system: String = "",
        moduleId: String? = null,
        coverImageUri: String? = null,
        gameFormat: String = "",
        scale: String = "",
        startTime: Long? = null,
        expectedEndTime: Long? = null,
        defaultSessionTime: String = ""
    ): String {
        val groupId = UUID.randomUUID().toString()
        groupDao.insertGroup(
            GroupEntity(
                groupId = groupId,
                groupName = groupName,
                moduleName = moduleName,
                system = system,
                status = "active",
                moduleId = moduleId,
                coverImageUri = coverImageUri,
                gameFormat = gameFormat,
                scale = scale,
                startTime = startTime,
                expectedEndTime = expectedEndTime,
                defaultSessionTime = defaultSessionTime
            )
        )
        return groupId
    }

    suspend fun updateGroup(group: GroupEntity) = groupDao.updateGroup(group)

    suspend fun deleteGroup(groupId: String) = groupDao.deleteGroupById(groupId)

    suspend fun updateGroupStatus(groupId: String, status: String) {
        groupDao.getGroupByIdOnce(groupId)?.let { group ->
            groupDao.updateGroup(group.copy(status = status))
        }
    }

    suspend fun updateLastPlayTime(groupId: String) {
        groupDao.getGroupByIdOnce(groupId)?.let { group ->
            groupDao.updateGroup(group.copy(lastPlayTime = System.currentTimeMillis()))
        }
    }
}
