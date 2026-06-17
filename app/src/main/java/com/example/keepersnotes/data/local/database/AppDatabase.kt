package com.example.keepersnotes.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.keepersnotes.data.local.dao.*
import com.example.keepersnotes.data.local.entity.*

@Database(
    entities = [
        GroupEntity::class,
        PlayerCharacterEntity::class,
        NpcEntity::class,
        ModuleEntity::class,
        SessionEntity::class,
        KpMemoEntity::class,
        ArchiveEntity::class,
        ImageEntity::class,
        ImageGroupEntity::class,
        HighlightEntity::class,
        AnnotationEntity::class,
        ReadingProgressEntity::class,
        BookmarkEntity::class,
        ModuleDefaultPcEntity::class,
        ModuleDefaultNpcEntity::class,
        ModuleLocationEntity::class,
        ModuleOrganizationEntity::class,
        ModuleRelationshipEntity::class,
        ModuleClueEntity::class,
        GroupRelationshipEntity::class,
        CalendarEventEntity::class
    ],
    version = 13,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun groupDao(): GroupDao
    abstract fun playerCharacterDao(): PlayerCharacterDao
    abstract fun npcDao(): NpcDao
    abstract fun moduleDao(): ModuleDao
    abstract fun sessionDao(): SessionDao
    abstract fun kpMemoDao(): KpMemoDao
    abstract fun archiveDao(): ArchiveDao
    abstract fun imageDao(): ImageDao
    abstract fun highlightDao(): HighlightDao
    abstract fun annotationDao(): AnnotationDao
    abstract fun readingProgressDao(): ReadingProgressDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun searchDao(): SearchDao
    abstract fun moduleDefaultPcDao(): ModuleDefaultPcDao
    abstract fun moduleDefaultNpcDao(): ModuleDefaultNpcDao
    abstract fun moduleLocationDao(): ModuleLocationDao
    abstract fun moduleOrganizationDao(): ModuleOrganizationDao
    abstract fun moduleRelationshipDao(): ModuleRelationshipDao
    abstract fun moduleClueDao(): ModuleClueDao
    abstract fun groupRelationshipDao(): GroupRelationshipDao
    abstract fun calendarEventDao(): CalendarEventDao
}
