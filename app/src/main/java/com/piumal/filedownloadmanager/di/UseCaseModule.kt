package com.piumal.filedownloadmanager.di

import com.piumal.filedownloadmanager.domain.repository.DownloadRepository
import com.piumal.filedownloadmanager.domain.usecase.download.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideDeleteDownloadUseCase(
        repository: DownloadRepository
    ): DeleteDownloadUseCase = DeleteDownloadUseCaseImpl(repository)

    @Provides
    @Singleton
    fun provideOpenDownloadUseCase(
        repository: DownloadRepository
    ): OpenDownloadUseCase = OpenDownloadUseCaseImpl(repository)

    @Provides
    @Singleton
    fun provideRenameDownloadUseCase(
        repository: DownloadRepository
    ): RenameDownloadUseCase = RenameDownloadUseCaseImpl(repository)

    @Provides
    @Singleton
    fun provideShareDownloadUseCase(
        repository: DownloadRepository
    ): ShareDownloadUseCase = ShareDownloadUseCaseImpl(repository)

    @Provides
    @Singleton
    fun provideShowInFolderUseCase(
        repository: DownloadRepository
    ): ShowInFolderUseCase = ShowInFolderUseCaseImpl(repository)

    @Provides
    @Singleton
    fun provideShowInfoUseCase(
        repository: DownloadRepository
    ): ShowInfoUseCase = ShowInfoUseCaseImpl(repository)

    @Provides
    @Singleton
    fun provideMoveToUseCase(
        repository: DownloadRepository
    ): MoveToUseCase = MoveToUseCaseImpl(repository)

    @Provides
    @Singleton
    fun provideRemoveFromListUseCase(
        repository: DownloadRepository
    ): RemoveFromListUseCase = RemoveFromListUseCaseImpl(repository)
}
