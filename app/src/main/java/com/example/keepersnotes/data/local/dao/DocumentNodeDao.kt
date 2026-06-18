package com.example.keepersnotes.data.local.dao

import androidx.room.*
import com.example.keepersnotes.data.local.entity.DocumentNodeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentNodeDao {

    @Query("SELECT * FROM document_nodes")
    fun getAll(): Flow<List<DocumentNodeEntity>>

    @Query("DELETE FROM document_nodes")
    suspend fun deleteAll()

    @Query("SELECT * FROM document_nodes WHERE moduleId = :moduleId ORDER BY `order` ASC")
    fun getNodesByModule(moduleId: String): Flow<List<DocumentNodeEntity>>

    @Query("SELECT * FROM document_nodes WHERE moduleId = :moduleId AND type = 'heading' ORDER BY `order` ASC")
    fun getHeadingsByModule(moduleId: String): Flow<List<DocumentNodeEntity>>

    @Query("SELECT * FROM document_nodes WHERE moduleId = :moduleId ORDER BY `order` ASC")
    suspend fun getNodesByModuleOnce(moduleId: String): List<DocumentNodeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(nodes: List<DocumentNodeEntity>)

    @Query("DELETE FROM document_nodes WHERE moduleId = :moduleId")
    suspend fun deleteByModule(moduleId: String)

    @Query("SELECT COUNT(*) FROM document_nodes WHERE moduleId = :moduleId")
    suspend fun getNodeCount(moduleId: String): Int
}
