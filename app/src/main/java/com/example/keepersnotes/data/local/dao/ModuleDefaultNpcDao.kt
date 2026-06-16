package com.example.keepersnotes.data.local.dao

import androidx.room.*
import com.example.keepersnotes.data.local.entity.ModuleDefaultNpcEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ModuleDefaultNpcDao {
    @Query("SELECT * FROM module_default_npcs WHERE moduleId = :moduleId ORDER BY sortOrder, createTime")
    fun getByModuleId(moduleId: String): Flow<List<ModuleDefaultNpcEntity>>

    @Query("SELECT * FROM module_default_npcs WHERE id = :id")
    suspend fun getById(id: String): ModuleDefaultNpcEntity?

    @Query("SELECT COUNT(*) FROM module_default_npcs WHERE moduleId = :moduleId")
    suspend fun getCount(moduleId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ModuleDefaultNpcEntity)

    @Update
    suspend fun update(entity: ModuleDefaultNpcEntity)

    @Query("DELETE FROM module_default_npcs WHERE id = :id")
    suspend fun deleteById(id: String)
}
