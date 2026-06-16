package com.example.keepersnotes.data.local.dao

import androidx.room.*
import com.example.keepersnotes.data.local.entity.ModuleDefaultPcEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ModuleDefaultPcDao {
    @Query("SELECT * FROM module_default_pcs WHERE moduleId = :moduleId ORDER BY sortOrder, createTime")
    fun getByModuleId(moduleId: String): Flow<List<ModuleDefaultPcEntity>>

    @Query("SELECT * FROM module_default_pcs WHERE id = :id")
    suspend fun getById(id: String): ModuleDefaultPcEntity?

    @Query("SELECT COUNT(*) FROM module_default_pcs WHERE moduleId = :moduleId")
    suspend fun getCount(moduleId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ModuleDefaultPcEntity)

    @Update
    suspend fun update(entity: ModuleDefaultPcEntity)

    @Query("DELETE FROM module_default_pcs WHERE id = :id")
    suspend fun deleteById(id: String)
}
