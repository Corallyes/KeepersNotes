package com.example.keepersnotes.data.local.dao

import androidx.room.*
import com.example.keepersnotes.data.local.entity.ArchiveEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ArchiveDao {

    @Query("SELECT * FROM archives")
    fun getAll(): Flow<List<ArchiveEntity>>

    @Query("SELECT * FROM archives WHERE collectionId = :collectionId ORDER BY sortOrder ASC, createTime ASC")
    fun getByCollectionId(collectionId: String): Flow<List<ArchiveEntity>>

    @Query("SELECT * FROM archives WHERE archiveId = :archiveId")
    fun getById(archiveId: String): Flow<ArchiveEntity?>

    @Query("SELECT * FROM archives WHERE archiveId = :archiveId")
    suspend fun getByIdOnce(archiveId: String): ArchiveEntity?

    @Query("SELECT COUNT(*) FROM archives WHERE collectionId = :collectionId")
    fun getCountByCollectionId(collectionId: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(archive: ArchiveEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(archives: List<ArchiveEntity>)

    @Update
    suspend fun update(archive: ArchiveEntity)

    @Delete
    suspend fun delete(archive: ArchiveEntity)

    @Query("DELETE FROM archives WHERE archiveId = :archiveId")
    suspend fun deleteById(archiveId: String)

    @Query("DELETE FROM archives WHERE collectionId = :collectionId")
    suspend fun deleteByCollectionId(collectionId: String)

    @Query("DELETE FROM archives")
    suspend fun deleteAll()
}
