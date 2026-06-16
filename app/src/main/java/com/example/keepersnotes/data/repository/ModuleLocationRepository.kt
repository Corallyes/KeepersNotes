package com.example.keepersnotes.data.repository

import com.example.keepersnotes.data.local.dao.ModuleLocationDao
import com.example.keepersnotes.data.local.entity.ModuleLocationEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ModuleLocationRepository @Inject constructor(
    private val dao: ModuleLocationDao
) {
    fun getByModuleId(moduleId: String): Flow<List<ModuleLocationEntity>> =
        dao.getByModuleId(moduleId)

    suspend fun getById(id: String): ModuleLocationEntity? = dao.getById(id)

    suspend fun getCount(moduleId: String): Int = dao.getCount(moduleId)

    suspend fun insert(entity: ModuleLocationEntity) = dao.insert(entity)

    suspend fun update(entity: ModuleLocationEntity) = dao.update(entity)

    suspend fun deleteById(id: String) = dao.deleteById(id)

    suspend fun create(
        moduleId: String,
        name: String,
        type: String = "",
        description: String = "",
        clues: String = "",
        inhabitants: String = ""
    ): String {
        val id = UUID.randomUUID().toString()
        dao.insert(
            ModuleLocationEntity(
                id = id,
                moduleId = moduleId,
                name = name,
                type = type,
                description = description,
                clues = clues,
                inhabitants = inhabitants
            )
        )
        return id
    }
}
