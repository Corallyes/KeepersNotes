package com.example.keepersnotes.data.local.dao

import androidx.room.*
import com.example.keepersnotes.data.local.entity.GroupRelationshipEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupRelationshipDao {

    @Query("SELECT * FROM group_relationships WHERE groupId = :groupId ORDER BY createTime DESC")
    fun getByGroupId(groupId: String): Flow<List<GroupRelationshipEntity>>

    @Query("SELECT * FROM group_relationships WHERE id = :id")
    suspend fun getById(id: String): GroupRelationshipEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: GroupRelationshipEntity)

    @Update
    suspend fun update(entity: GroupRelationshipEntity)

    @Query("DELETE FROM group_relationships WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM group_relationships WHERE groupId = :groupId")
    suspend fun deleteByGroupId(groupId: String)
}
