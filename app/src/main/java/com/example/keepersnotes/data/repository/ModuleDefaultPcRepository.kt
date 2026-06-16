package com.example.keepersnotes.data.repository

import com.example.keepersnotes.data.local.dao.ModuleDefaultPcDao
import com.example.keepersnotes.data.local.entity.ModuleDefaultPcEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ModuleDefaultPcRepository @Inject constructor(
    private val dao: ModuleDefaultPcDao
) {
    fun getByModuleId(moduleId: String): Flow<List<ModuleDefaultPcEntity>> =
        dao.getByModuleId(moduleId)

    suspend fun getById(id: String): ModuleDefaultPcEntity? = dao.getById(id)

    suspend fun getCount(moduleId: String): Int = dao.getCount(moduleId)

    suspend fun insert(entity: ModuleDefaultPcEntity) = dao.insert(entity)

    suspend fun update(entity: ModuleDefaultPcEntity) = dao.update(entity)

    suspend fun deleteById(id: String) = dao.deleteById(id)

    suspend fun create(
        moduleId: String,
        name: String,
        playerName: String = "",
        system: String = "",
        description: String = ""
    ): String {
        val id = UUID.randomUUID().toString()
        dao.insert(
            ModuleDefaultPcEntity(
                id = id,
                moduleId = moduleId,
                name = name,
                playerName = playerName,
                system = system,
                description = description
            )
        )
        return id
    }
}
