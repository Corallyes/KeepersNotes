package com.example.keepersnotes.data.local.dao

import androidx.room.*
import com.example.keepersnotes.data.local.entity.ImageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ImageDao {

    @Query("SELECT * FROM images")
    fun getAll(): Flow<List<ImageEntity>>

    @Query("SELECT * FROM images WHERE collectionId = :collectionId ORDER BY sortOrder ASC, createTime ASC")
    fun getByCollectionId(collectionId: String): Flow<List<ImageEntity>>

    @Query("SELECT * FROM images WHERE imageId = :imageId")
    fun getById(imageId: String): Flow<ImageEntity?>

    @Query("SELECT * FROM images WHERE imageId = :imageId")
    suspend fun getByIdOnce(imageId: String): ImageEntity?

    @Query("SELECT COUNT(*) FROM images WHERE collectionId = :collectionId")
    fun getCountByCollectionId(collectionId: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(image: ImageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(images: List<ImageEntity>)

    @Update
    suspend fun update(image: ImageEntity)

    @Delete
    suspend fun delete(image: ImageEntity)

    @Query("DELETE FROM images WHERE imageId = :imageId")
    suspend fun deleteById(imageId: String)

    @Query("DELETE FROM images WHERE collectionId = :collectionId")
    suspend fun deleteByCollectionId(collectionId: String)

    @Query("DELETE FROM images")
    suspend fun deleteAll()
}
