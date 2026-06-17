package com.example.keepersnotes.data.local.dao

import androidx.room.*
import com.example.keepersnotes.data.local.entity.ModuleClueEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ModuleClueDao {
    @Query("SELECT * FROM module_clues WHERE moduleId = :moduleId ORDER BY sortOrder, createTime")
    fun getByModuleId(moduleId: String): Flow<List<ModuleClueEntity>>

    @Query("SELECT * FROM module_clues WHERE id = :id")
    suspend fun getById(id: String): ModuleClueEntity?

    @Query("SELECT COUNT(*) FROM module_clues WHERE moduleId = :moduleId")
    suspend fun getCount(moduleId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ModuleClueEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<ModuleClueEntity>)

    @Update
    suspend fun update(entity: ModuleClueEntity)

    @Query("DELETE FROM module_clues WHERE id = :id")
    suspend fun deleteById(id: String)
}
