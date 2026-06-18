package com.example.keepersnotes.data.local.dao

import androidx.room.*
import com.example.keepersnotes.data.local.entity.ModuleOrganizationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ModuleOrganizationDao {
    @Query("SELECT * FROM module_organizations")
    fun getAll(): Flow<List<ModuleOrganizationEntity>>

    @Query("DELETE FROM module_organizations")
    suspend fun deleteAll()

    @Query("SELECT * FROM module_organizations WHERE moduleId = :moduleId ORDER BY sortOrder, createTime")
    fun getByModuleId(moduleId: String): Flow<List<ModuleOrganizationEntity>>

    @Query("SELECT * FROM module_organizations WHERE id = :id")
    suspend fun getById(id: String): ModuleOrganizationEntity?

    @Query("SELECT COUNT(*) FROM module_organizations WHERE moduleId = :moduleId")
    suspend fun getCount(moduleId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ModuleOrganizationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<ModuleOrganizationEntity>)

    @Update
    suspend fun update(entity: ModuleOrganizationEntity)

    @Query("DELETE FROM module_organizations WHERE id = :id")
    suspend fun deleteById(id: String)
}
