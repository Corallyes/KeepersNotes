package com.example.keepersnotes.data.repository

import com.example.keepersnotes.data.local.dao.ModuleDao
import com.example.keepersnotes.data.local.entity.ModuleEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ModuleRepository @Inject constructor(
    private val moduleDao: ModuleDao
) {
    fun getAllModules(): Flow<List<ModuleEntity>> = moduleDao.getAllModules()

    fun getFavoriteModules(): Flow<List<ModuleEntity>> = moduleDao.getFavoriteModules()

    fun getUserModules(): Flow<List<ModuleEntity>> = moduleDao.getUserModules()

    fun getModuleById(moduleId: String): Flow<ModuleEntity?> = moduleDao.getModuleById(moduleId)

    fun getModulesBySystem(system: String): Flow<List<ModuleEntity>> =
        moduleDao.getModulesBySystem(system)

    fun searchModules(query: String): Flow<List<ModuleEntity>> = moduleDao.searchModules(query)

    suspend fun importModule(
        title: String,
        author: String = "",
        system: String = "",
        content: String = "",
        isCollection: Boolean = false,
        moduleId: String = UUID.randomUUID().toString()
    ): String {
        moduleDao.insertModule(
            ModuleEntity(
                moduleId = moduleId,
                title = title,
                author = author,
                system = system,
                contentJson = content,
                isUserCreated = true,
                isCollection = isCollection
            )
        )
        return moduleId
    }

    suspend fun updateModule(module: ModuleEntity) = moduleDao.updateModule(module)

    suspend fun toggleFavorite(moduleId: String) {
        moduleDao.getModuleByIdOnce(moduleId)?.let { module ->
            moduleDao.updateModule(module.copy(isFavorite = !module.isFavorite))
        }
    }

    suspend fun deleteModule(moduleId: String) = moduleDao.deleteModuleById(moduleId)
}
