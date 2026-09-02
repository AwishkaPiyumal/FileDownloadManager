package com.piumal.filedownloadmanager.di

import android.content.Context
import androidx.room.Room
import com.piumal.filedownloadmanager.data.download.DownloadManager
import com.piumal.filedownloadmanager.data.local.DownloadDatabase
import com.piumal.filedownloadmanager.data.local.dao.DownloadDao
import com.piumal.filedownloadmanager.data.security.DatabasePassphraseManager
import com.piumal.filedownloadmanager.data.repository.DownloadRepositoryImpl
import com.piumal.filedownloadmanager.domain.repository.DownloadRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import androidx.sqlite.db.SupportSQLiteOpenHelper
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDownloadDatabase(
        @ApplicationContext context: Context
    ): DownloadDatabase {
        val passphrase = DatabasePassphraseManager.getOrCreatePassphrase(context)
        val openHelperFactory = Class.forName("net.sqlcipher.database.SupportFactory")
            .getConstructor(ByteArray::class.java)
            .newInstance(passphrase) as SupportSQLiteOpenHelper.Factory
        return Room.databaseBuilder(
            context,
            DownloadDatabase::class.java,
            DownloadDatabase.DATABASE_NAME
        )
            .openHelperFactory(openHelperFactory)
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

    // Provide ClipboardRepository implementation
    @Provides
    @Singleton
    fun provideClipboardRepository(
        @ApplicationContext context: Context
    ): com.piumal.filedownloadmanager.domain.repository.ClipboardRepository {
        return com.piumal.filedownloadmanager.data.repository.ClipboardRepositoryImpl(context)
    }
}
