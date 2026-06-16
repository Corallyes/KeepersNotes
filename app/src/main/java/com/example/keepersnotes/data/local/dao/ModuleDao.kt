package com.example.keepersnotes.data.local.dao

import androidx.room.*
import com.example.keepersnotes.data.local.entity.ModuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ModuleDao {

    @Query("SELECT * FROM modules ORDER BY createTime DESC")
    fun getAllModules(): Flow<List<ModuleEntity>>

    @Query("SELECT * FROM modules WHERE isFavorite = 1 ORDER BY title ASC")
    fun getFavoriteModules(): Flow<List<ModuleEntity>>

    @Query("SELECT * FROM modules WHERE isUserCreated = 1 ORDER BY createTime DESC")
    fun getUserModules(): Flow<List<ModuleEntity>>

    @Query("SELECT * FROM modules WHERE moduleId = :moduleId")
    fun getModuleById(moduleId: String): Flow<ModuleEntity?>

    @Query("SELECT * FROM modules WHERE moduleId = :moduleId")
    suspend fun getModuleByIdOnce(moduleId: String): ModuleEntity?

    @Query("SELECT * FROM modules WHERE system = :system ORDER BY title ASC")
    fun getModulesBySystem(system: String): Flow<List<ModuleEntity>>

    @Query("SELECT * FROM modules WHERE title LIKE '%' || :query || '%' OR author LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%'")
    fun searchModules(query: String): Flow<List<ModuleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModule(module: ModuleEntity)

    @Update
    suspend fun updateModule(module: ModuleEntity)

    @Delete
    suspend fun deleteModule(module: ModuleEntity)

    @Query("DELETE FROM modules WHERE moduleId = :moduleId")
    suspend fun deleteModuleById(moduleId: String)

    @Query("DELETE FROM modules")
    suspend fun deleteAll()
}
