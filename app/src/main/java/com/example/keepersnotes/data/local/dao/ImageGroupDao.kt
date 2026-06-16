package com.example.keepersnotes.data.local.dao

import androidx.room.*
import com.example.keepersnotes.data.local.entity.ImageGroupEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ImageGroupDao {

    @Query("SELECT * FROM image_groups")
    fun getAll(): Flow<List<ImageGroupEntity>>

    @Query("SELECT * FROM image_groups WHERE collectionId = :collectionId ORDER BY sortOrder ASC, createTime ASC")
    fun getByCollectionId(collectionId: String): Flow<List<ImageGroupEntity>>

    @Query("SELECT * FROM image_groups WHERE imageGroupId = :groupId")
    fun getById(groupId: String): Flow<ImageGroupEntity?>

    @Query("SELECT * FROM image_groups WHERE imageGroupId = :groupId")
    suspend fun getByIdOnce(groupId: String): ImageGroupEntity?

    @Query("SELECT COUNT(*) FROM image_groups WHERE collectionId = :collectionId")
    fun getCountByCollectionId(collectionId: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(group: ImageGroupEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(groups: List<ImageGroupEntity>)

    @Update
    suspend fun update(group: ImageGroupEntity)

    @Delete
    suspend fun delete(group: ImageGroupEntity)

    @Query("DELETE FROM image_groups WHERE imageGroupId = :groupId")
    suspend fun deleteById(groupId: String)

    @Query("DELETE FROM image_groups WHERE collectionId = :collectionId")
    suspend fun deleteByCollectionId(collectionId: String)

    @Query("DELETE FROM image_groups")
    suspend fun deleteAll()
}
