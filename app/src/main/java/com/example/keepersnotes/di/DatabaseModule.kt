package com.example.keepersnotes.di

import android.content.Context
import androidx.room.Room
import com.example.keepersnotes.data.local.dao.*
import com.example.keepersnotes.data.local.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "keepers_notes.db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideGroupDao(db: AppDatabase): GroupDao = db.groupDao()

    @Provides
    fun providePlayerCharacterDao(db: AppDatabase): PlayerCharacterDao = db.playerCharacterDao()

    @Provides
    fun provideNpcDao(db: AppDatabase): NpcDao = db.npcDao()

    @Provides
    fun provideModuleDao(db: AppDatabase): ModuleDao = db.moduleDao()

    @Provides
    fun provideSessionDao(db: AppDatabase): SessionDao = db.sessionDao()

    @Provides
    fun provideKpMemoDao(db: AppDatabase): KpMemoDao = db.kpMemoDao()

    @Provides
    fun provideArchiveDao(db: AppDatabase): ArchiveDao = db.archiveDao()

    @Provides
    fun provideImageDao(db: AppDatabase): ImageDao = db.imageDao()

    @Provides
    fun provideImageGroupDao(db: AppDatabase): ImageGroupDao = db.imageGroupDao()

    @Provides
    fun provideHighlightDao(db: AppDatabase): HighlightDao = db.highlightDao()

    @Provides
    fun provideAnnotationDao(db: AppDatabase): AnnotationDao = db.annotationDao()

    @Provides
    fun provideReadingProgressDao(db: AppDatabase): ReadingProgressDao = db.readingProgressDao()

    @Provides
    fun provideBookmarkDao(db: AppDatabase): BookmarkDao = db.bookmarkDao()

    @Provides
    fun provideSearchDao(db: AppDatabase): SearchDao = db.searchDao()

    @Provides
    fun provideModuleDefaultPcDao(db: AppDatabase): ModuleDefaultPcDao = db.moduleDefaultPcDao()

    @Provides
    fun provideModuleDefaultNpcDao(db: AppDatabase): ModuleDefaultNpcDao = db.moduleDefaultNpcDao()

    @Provides
    fun provideModuleLocationDao(db: AppDatabase): ModuleLocationDao = db.moduleLocationDao()

    @Provides
    fun provideModuleOrganizationDao(db: AppDatabase): ModuleOrganizationDao = db.moduleOrganizationDao()

    @Provides
    fun provideModuleRelationshipDao(db: AppDatabase): ModuleRelationshipDao = db.moduleRelationshipDao()

    @Provides
    fun provideModuleClueDao(db: AppDatabase): ModuleClueDao = db.moduleClueDao()

    @Provides
    fun provideGroupRelationshipDao(db: AppDatabase): GroupRelationshipDao = db.groupRelationshipDao()

    @Provides
    fun provideCalendarEventDao(db: AppDatabase): CalendarEventDao = db.calendarEventDao()
}
