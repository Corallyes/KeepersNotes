package com.example.keepersnotes.data.local.dao

import androidx.room.*
import com.example.keepersnotes.data.local.entity.NpcEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NpcDao {

    @Query("SELECT * FROM npcs ORDER BY name ASC")
    fun getAllNpcs(): Flow<List<NpcEntity>>

    @Query("SELECT * FROM npcs WHERE groupId = :groupId ORDER BY name ASC")
    fun getNpcsByGroupId(groupId: String): Flow<List<NpcEntity>>

    @Query("SELECT * FROM npcs WHERE npcId = :npcId")
    fun getNpcById(npcId: String): Flow<NpcEntity?>

    @Query("SELECT * FROM npcs WHERE npcId = :npcId")
    suspend fun getNpcByIdOnce(npcId: String): NpcEntity?

    @Query("SELECT * FROM npcs WHERE groupId = :groupId AND (name LIKE '%' || :query || '%' OR alias LIKE '%' || :query || '%')")
    fun searchNpcs(groupId: String, query: String): Flow<List<NpcEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNpc(npc: NpcEntity)

    @Update
    suspend fun updateNpc(npc: NpcEntity)

    @Delete
    suspend fun deleteNpc(npc: NpcEntity)

    @Query("DELETE FROM npcs WHERE npcId = :npcId")
    suspend fun deleteNpcById(npcId: String)

    @Query("DELETE FROM npcs")
    suspend fun deleteAll()
}
