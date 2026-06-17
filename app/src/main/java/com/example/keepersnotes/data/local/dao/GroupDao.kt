package com.example.keepersnotes.data.local.dao

import androidx.room.*
import com.example.keepersnotes.data.local.entity.GroupEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupDao {

    @Query("SELECT * FROM groups ORDER BY lastPlayTime DESC, createTime DESC")
    fun getAllGroups(): Flow<List<GroupEntity>>

    @Query("DELETE FROM groups")
    suspend fun deleteAll()

    @Query("SELECT * FROM groups WHERE status = 'active' ORDER BY lastPlayTime DESC")
    fun getActiveGroups(): Flow<List<GroupEntity>>

    @Query("SELECT * FROM groups WHERE status = 'paused'")
    fun getPausedGroups(): Flow<List<GroupEntity>>

    @Query("SELECT * FROM groups WHERE status = 'completed'")
    fun getCompletedGroups(): Flow<List<GroupEntity>>

    @Query("SELECT * FROM groups WHERE groupId = :groupId")
    fun getGroupById(groupId: String): Flow<GroupEntity?>

    @Query("SELECT * FROM groups WHERE groupId = :groupId")
    suspend fun getGroupByIdOnce(groupId: String): GroupEntity?

    @Query("SELECT COUNT(*) FROM groups WHERE status = 'active'")
    fun getActiveGroupCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM groups WHERE status = 'completed'")
    fun getCompletedGroupCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: GroupEntity)

    @Update
    suspend fun updateGroup(group: GroupEntity)

    @Delete
    suspend fun deleteGroup(group: GroupEntity)

    @Query("DELETE FROM groups WHERE groupId = :groupId")
    suspend fun deleteGroupById(groupId: String)
}
