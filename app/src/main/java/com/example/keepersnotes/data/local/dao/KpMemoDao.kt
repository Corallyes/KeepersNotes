package com.example.keepersnotes.data.local.dao

import androidx.room.*
import com.example.keepersnotes.data.local.entity.KpMemoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface KpMemoDao {

    @Query("SELECT * FROM kp_memos ORDER BY updateTime DESC")
    fun getAllMemos(): Flow<List<KpMemoEntity>>

    @Query("SELECT * FROM kp_memos WHERE groupId = :groupId ORDER BY updateTime DESC")
    fun getMemosByGroupId(groupId: String): Flow<List<KpMemoEntity>>

    @Query("SELECT * FROM kp_memos WHERE groupId = :groupId AND type = :type ORDER BY updateTime DESC")
    fun getMemosByType(groupId: String, type: String): Flow<List<KpMemoEntity>>

    @Query("SELECT * FROM kp_memos WHERE groupId = :groupId AND isHidden = 1 ORDER BY updateTime DESC")
    fun getHiddenMemos(groupId: String): Flow<List<KpMemoEntity>>

    @Query("SELECT * FROM kp_memos WHERE groupId = :groupId AND type = 'todo' AND isCompleted = 0 ORDER BY priority DESC, updateTime DESC")
    fun getPendingTodos(groupId: String): Flow<List<KpMemoEntity>>

    @Query("SELECT * FROM kp_memos WHERE memoId = :memoId")
    fun getMemoById(memoId: String): Flow<KpMemoEntity?>

    @Query("SELECT * FROM kp_memos WHERE moduleId = :moduleId ORDER BY updateTime DESC")
    fun getMemosByModuleId(moduleId: String): Flow<List<KpMemoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemo(memo: KpMemoEntity)

    @Update
    suspend fun updateMemo(memo: KpMemoEntity)

    @Delete
    suspend fun deleteMemo(memo: KpMemoEntity)

    @Query("DELETE FROM kp_memos WHERE memoId = :memoId")
    suspend fun deleteMemoById(memoId: String)

    @Query("DELETE FROM kp_memos")
    suspend fun deleteAll()
}
