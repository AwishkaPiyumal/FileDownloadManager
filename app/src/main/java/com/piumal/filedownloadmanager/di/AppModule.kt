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

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

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

    @Provides
    @Singleton
    fun provideDownloadDao(database: DownloadDatabase): DownloadDao {
        return database.downloadDao()
    }

    @Provides
    @Singleton
    fun provideDownloadManager(
        @ApplicationContext context: Context
    ): DownloadManager {
        return DownloadManager(context)
    }

    @Provides
    @Singleton
    fun provideDownloadRepository(
        @ApplicationContext context: Context,
        downloadDao: DownloadDao,
        downloadManager: DownloadManager
    ): DownloadRepository {
        return DownloadRepositoryImpl(context, downloadDao, downloadManager)
    }

    // Provide SettingsRepository implementation
    @Provides
    @Singleton
    fun provideSettingsRepository(
        @ApplicationContext context: Context
    ): com.piumal.filedownloadmanager.domain.repository.SettingsRepository {
        return com.piumal.filedownloadmanager.data.settings.SettingsRepositoryImpl(context)
    }
}
