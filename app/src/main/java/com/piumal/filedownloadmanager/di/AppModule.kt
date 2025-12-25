package com.piumal.filedownloadmanager.di

import android.content.Context
import androidx.room.Room
import com.piumal.filedownloadmanager.data.download.DownloadManager
import com.piumal.filedownloadmanager.data.local.DownloadDatabase
import com.piumal.filedownloadmanager.data.local.dao.DownloadDao
import com.piumal.filedownloadmanager.data.repository.DownloadRepositoryImpl
import com.piumal.filedownloadmanager.domain.repository.DownloadRepository
import com.piumal.filedownloadmanager.domain.usecase.ScheduledDownloadManager
import com.piumal.filedownloadmanager.domain.usecase.StartDownloadUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt Module for Dependency Injection
 *
 * Provides dependencies for:
 * - Database
 * - Repository
 * - Use Cases
 * - Download Manager
 *
 * Follows Clean Architecture with proper dependency direction
 *
 * @author File Download Manager Team
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * Provide Room Database
     */
    @Provides
    @Singleton
    fun provideDownloadDatabase(
        @ApplicationContext context: Context
    ): DownloadDatabase {
        return Room.databaseBuilder(
            context,
            DownloadDatabase::class.java,
            DownloadDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    /**
     * Provide Download DAO
     */
    @Provides
    @Singleton
    fun provideDownloadDao(database: DownloadDatabase): DownloadDao {
        return database.downloadDao()
    }

    /**
     * Provide Download Manager
     */
    @Provides
    @Singleton
    fun provideDownloadManager(
        @ApplicationContext context: Context
    ): DownloadManager {
        return DownloadManager(context)
    }

    /**
     * Provide Download Repository
     * Binds implementation to interface
     */
    @Provides
    @Singleton
    fun provideDownloadRepository(
        downloadDao: DownloadDao,
        downloadManager: DownloadManager
    ): DownloadRepository {
        return DownloadRepositoryImpl(downloadDao, downloadManager)
    }

    /**
     * Provide Start Download Use Case
     */
    @Provides
    @Singleton
    fun provideStartDownloadUseCase(
        repository: DownloadRepository,
        scheduledDownloadManager: ScheduledDownloadManager
    ): StartDownloadUseCase {
        return StartDownloadUseCase(repository, scheduledDownloadManager)
    }

    /**
     * Provide Get Clipboard URL Use Case
     */
    @Provides
    @Singleton
    fun provideGetClipboardUrlUseCase(
        @ApplicationContext context: Context
    ): com.piumal.filedownloadmanager.domain.usecase.GetClipboardUrlUseCase {
        return com.piumal.filedownloadmanager.domain.usecase.GetClipboardUrlUseCase(context)
    }

    /**
     * Provide Extract File Name Use Case
     */
    @Provides
    @Singleton
    fun provideExtractFileNameUseCase(): com.piumal.filedownloadmanager.domain.usecase.ExtractFileNameUseCase {
        return com.piumal.filedownloadmanager.domain.usecase.ExtractFileNameUseCase()
    }
}

