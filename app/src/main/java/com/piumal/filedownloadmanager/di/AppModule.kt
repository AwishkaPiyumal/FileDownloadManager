package com.piumal.filedownloadmanager.di

import android.content.Context
import androidx.room.Room
import com.piumal.filedownloadmanager.data.download.DownloadManager
import com.piumal.filedownloadmanager.data.local.DownloadDatabase
import com.piumal.filedownloadmanager.data.local.dao.DownloadDao
import com.piumal.filedownloadmanager.data.repository.DownloadRepositoryImpl
import com.piumal.filedownloadmanager.domain.repository.DownloadRepository
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
 * - Download Manager
 *
 * Note: Use Cases with @Inject constructor are automatically provided by Hilt.
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
        @ApplicationContext context: Context,
        downloadDao: DownloadDao,
        downloadManager: DownloadManager
    ): DownloadRepository {
        return DownloadRepositoryImpl(context, downloadDao, downloadManager)
    }

    // Note: StartDownloadUseCase, GetClipboardUrlUseCase, ExtractFileNameUseCase,
    // and other use cases with @Inject constructor are automatically provided by Hilt.
    // No need to manually provide them here.
}

