package com.example.keepersnotes.data.repository

import com.example.keepersnotes.data.local.dao.ModuleRelationshipDao
import com.example.keepersnotes.data.local.entity.ModuleRelationshipEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ModuleRelationshipRepository @Inject constructor(
    private val dao: ModuleRelationshipDao
) {
    fun getByModuleId(moduleId: String): Flow<List<ModuleRelationshipEntity>> =
        dao.getByModuleId(moduleId)

    suspend fun getById(id: String): ModuleRelationshipEntity? = dao.getById(id)

    suspend fun getCount(moduleId: String): Int = dao.getCount(moduleId)

    suspend fun insert(entity: ModuleRelationshipEntity) = dao.insert(entity)

    suspend fun update(entity: ModuleRelationshipEntity) = dao.update(entity)

    suspend fun deleteById(id: String) = dao.deleteById(id)

    suspend fun create(
        moduleId: String,
        sourceId: String,
        sourceType: String,
        targetId: String,
        targetType: String,
        relationType: String = "",
        description: String = ""
    ): String {
        val id = UUID.randomUUID().toString()
        dao.insert(
            ModuleRelationshipEntity(
                id = id,
                moduleId = moduleId,
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
}
