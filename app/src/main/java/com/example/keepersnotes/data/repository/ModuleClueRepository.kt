package com.example.keepersnotes.data.repository

import com.example.keepersnotes.data.local.dao.ModuleClueDao
import com.example.keepersnotes.data.local.entity.ModuleClueEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ModuleClueRepository @Inject constructor(
    private val dao: ModuleClueDao
) {
    fun getByModuleId(moduleId: String): Flow<List<ModuleClueEntity>> =
        dao.getByModuleId(moduleId)

    suspend fun getById(id: String): ModuleClueEntity? = dao.getById(id)

    suspend fun getCount(moduleId: String): Int = dao.getCount(moduleId)

    suspend fun insert(entity: ModuleClueEntity) = dao.insert(entity)

    suspend fun update(entity: ModuleClueEntity) = dao.update(entity)

    suspend fun deleteById(id: String) = dao.deleteById(id)

    suspend fun create(
        moduleId: String,
        name: String,
        type: String = "",
        description: String = "",
        source: String = "",
        isHidden: Boolean = false
    ): String {
        val id = UUID.randomUUID().toString()
        dao.insert(
            ModuleClueEntity(
                id = id,
                moduleId = moduleId,
                name = name,
                type = type,
                description = description,
                source = source,
                isHidden = isHidden
            )
        )
        return id
    }
}
