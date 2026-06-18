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

    @Query("SELECT * FROM image_groups WHERE imageGroupId = :imageGroupId")
    suspend fun getById(imageGroupId: String): ImageGroupEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(imageGroup: ImageGroupEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(imageGroups: List<ImageGroupEntity>)

    @Update
    suspend fun update(imageGroup: ImageGroupEntity)

    @Delete
    suspend fun delete(imageGroup: ImageGroupEntity)

    @Query("DELETE FROM image_groups WHERE imageGroupId = :imageGroupId")
    suspend fun deleteById(imageGroupId: String)

    @Query("DELETE FROM image_groups")
    suspend fun deleteAll()
}
