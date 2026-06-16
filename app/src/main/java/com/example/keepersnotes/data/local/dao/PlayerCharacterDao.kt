package com.example.keepersnotes.data.local.dao

import androidx.room.*
import com.example.keepersnotes.data.local.entity.PlayerCharacterEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerCharacterDao {

    @Query("SELECT * FROM player_characters ORDER BY characterName ASC")
    fun getAllPcs(): Flow<List<PlayerCharacterEntity>>

    @Query("SELECT * FROM player_characters WHERE groupId = :groupId ORDER BY characterName ASC")
    fun getPcsByGroupId(groupId: String): Flow<List<PlayerCharacterEntity>>

    @Query("SELECT * FROM player_characters WHERE pcId = :pcId")
    fun getPcById(pcId: String): Flow<PlayerCharacterEntity?>

    @Query("SELECT * FROM player_characters WHERE pcId = :pcId")
    suspend fun getPcByIdOnce(pcId: String): PlayerCharacterEntity?

    @Query("SELECT COUNT(*) FROM player_characters WHERE groupId = :groupId")
    fun getPcCountByGroupId(groupId: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM player_characters")
    fun getTotalPcCount(): Flow<Int>

    @Query("SELECT * FROM player_characters WHERE characterName LIKE '%' || :query || '%' OR playerName LIKE '%' || :query || '%'")
    fun searchPcs(query: String): Flow<List<PlayerCharacterEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPc(pc: PlayerCharacterEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPcs(pcs: List<PlayerCharacterEntity>)

    @Update
    suspend fun updatePc(pc: PlayerCharacterEntity)

    @Delete
    suspend fun deletePc(pc: PlayerCharacterEntity)

    @Query("DELETE FROM player_characters WHERE pcId = :pcId")
    suspend fun deletePcById(pcId: String)

    @Query("DELETE FROM player_characters")
    suspend fun deleteAll()
}
