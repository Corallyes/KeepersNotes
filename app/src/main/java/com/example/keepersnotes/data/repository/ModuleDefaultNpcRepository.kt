package com.example.keepersnotes.data.repository

import com.example.keepersnotes.data.local.dao.ModuleDefaultNpcDao
import com.example.keepersnotes.data.local.entity.ModuleDefaultNpcEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ModuleDefaultNpcRepository @Inject constructor(
    private val dao: ModuleDefaultNpcDao
) {
    fun getByModuleId(moduleId: String): Flow<List<ModuleDefaultNpcEntity>> =
        dao.getByModuleId(moduleId)

    suspend fun getById(id: String): ModuleDefaultNpcEntity? = dao.getById(id)

    suspend fun getCount(moduleId: String): Int = dao.getCount(moduleId)

    suspend fun insert(entity: ModuleDefaultNpcEntity) = dao.insert(entity)

    suspend fun update(entity: ModuleDefaultNpcEntity) = dao.update(entity)

    suspend fun deleteById(id: String) = dao.deleteById(id)

    suspend fun create(
        moduleId: String,
        name: String,
        alias: String = "",
        occupation: String = "",
        description: String = "",
        truePurpose: String = "",
        gender: String = ""
    ): String {
        val id = UUID.randomUUID().toString()
        dao.insert(
            ModuleDefaultNpcEntity(
                id = id,
                moduleId = moduleId,
                name = name,
                alias = alias,
                occupation = occupation,
                description = description,
                truePurpose = truePurpose,
                gender = gender
            )
        )
        return id
    }
}
