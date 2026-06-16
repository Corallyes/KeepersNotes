package com.example.keepersnotes.data.local.dao

import androidx.room.*
import com.example.keepersnotes.data.local.entity.ModuleRelationshipEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ModuleRelationshipDao {
    @Query("SELECT * FROM module_relationships WHERE moduleId = :moduleId ORDER BY createTime")
    fun getByModuleId(moduleId: String): Flow<List<ModuleRelationshipEntity>>

    @Query("SELECT * FROM module_relationships WHERE id = :id")
    suspend fun getById(id: String): ModuleRelationshipEntity?

    @Query("SELECT COUNT(*) FROM module_relationships WHERE moduleId = :moduleId")
    suspend fun getCount(moduleId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ModuleRelationshipEntity)

    @Update
    suspend fun update(entity: ModuleRelationshipEntity)

    @Query("DELETE FROM module_relationships WHERE id = :id")
    suspend fun deleteById(id: String)
}
