package com.example.keepersnotes.data.repository

import com.example.keepersnotes.data.local.dao.GroupRelationshipDao
import com.example.keepersnotes.data.local.entity.GroupRelationshipEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GroupRelationshipRepository @Inject constructor(
    private val dao: GroupRelationshipDao
) {
    fun getByGroupId(groupId: String): Flow<List<GroupRelationshipEntity>> =
        dao.getByGroupId(groupId)

    suspend fun getById(id: String): GroupRelationshipEntity? = dao.getById(id)

    suspend fun create(
        groupId: String,
        sourceId: String,
        sourceType: String,
        targetId: String,
        targetType: String,
        relationType: String = "",
        description: String = ""
    ): String {
        val id = UUID.randomUUID().toString()
        dao.insert(
            GroupRelationshipEntity(
                id = id,
                groupId = groupId,
                sourceId = sourceId,
                sourceType = sourceType,
                targetId = targetId,
                targetType = targetType,
                relationType = relationType,
                description = description
            )
        )
        return id
    }

    suspend fun update(entity: GroupRelationshipEntity) = dao.update(entity)

    suspend fun deleteById(id: String) = dao.deleteById(id)

    suspend fun deleteByGroupId(groupId: String) = dao.deleteByGroupId(groupId)
}
