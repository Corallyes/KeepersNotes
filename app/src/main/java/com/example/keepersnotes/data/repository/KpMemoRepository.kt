package com.example.keepersnotes.data.repository

import com.example.keepersnotes.data.local.dao.KpMemoDao
import com.example.keepersnotes.data.local.entity.KpMemoEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KpMemoRepository @Inject constructor(
    private val kpMemoDao: KpMemoDao
) {
    fun getMemosByGroupId(groupId: String): Flow<List<KpMemoEntity>> =
        kpMemoDao.getMemosByGroupId(groupId)

    fun getMemosByType(groupId: String, type: String): Flow<List<KpMemoEntity>> =
        kpMemoDao.getMemosByType(groupId, type)

    fun getHiddenMemos(groupId: String): Flow<List<KpMemoEntity>> =
        kpMemoDao.getHiddenMemos(groupId)

    fun getPendingTodos(groupId: String): Flow<List<KpMemoEntity>> =
        kpMemoDao.getPendingTodos(groupId)

    fun getMemoById(memoId: String): Flow<KpMemoEntity?> = kpMemoDao.getMemoById(memoId)

    fun getMemosByModuleId(moduleId: String): Flow<List<KpMemoEntity>> =
        kpMemoDao.getMemosByModuleId(moduleId)

    suspend fun createMemo(
        groupId: String,
        type: String,
        title: String = "",
        content: String = "",
        isHidden: Boolean = false
    ): String {
        val memoId = UUID.randomUUID().toString()
        kpMemoDao.insertMemo(
            KpMemoEntity(
                memoId = memoId,
                groupId = groupId,
                type = type,
                title = title,
                content = content,
                isHidden = isHidden
            )
        )
        return memoId
    }

    suspend fun updateMemo(memo: KpMemoEntity) = kpMemoDao.updateMemo(memo)

    suspend fun toggleCompleted(memoId: String) {
        kpMemoDao.getMemoById(memoId).collect { memo ->
            memo?.let {
                kpMemoDao.updateMemo(it.copy(isCompleted = !it.isCompleted))
            }
            return@collect
        }
    }

    suspend fun deleteMemo(memoId: String) = kpMemoDao.deleteMemoById(memoId)
}
