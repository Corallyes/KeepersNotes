package com.example.keepersnotes.data.local.dao

import androidx.room.*
import com.example.keepersnotes.data.local.entity.ModuleLocationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ModuleLocationDao {
    @Query("SELECT * FROM module_locations WHERE moduleId = :moduleId ORDER BY sortOrder, createTime")
    fun getByModuleId(moduleId: String): Flow<List<ModuleLocationEntity>>

    @Query("SELECT * FROM module_locations WHERE id = :id")
    suspend fun getById(id: String): ModuleLocationEntity?

    @Query("SELECT COUNT(*) FROM module_locations WHERE moduleId = :moduleId")
    suspend fun getCount(moduleId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ModuleLocationEntity)

    @Update
    suspend fun update(entity: ModuleLocationEntity)

    @Query("DELETE FROM module_locations WHERE id = :id")
    suspend fun deleteById(id: String)
}
