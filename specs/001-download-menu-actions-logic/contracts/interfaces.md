# Interface Contracts: Download Management

## Repository Interfaces (Domain Layer)

### `DownloadRepository`
```kotlin
interface DownloadRepository {
    suspend fun getDownload(id: String): DownloadItem
    suspend fun deleteDownload(id: String)
    suspend fun renameDownload(id: String, newName: String)
    // ... other CRUD operations
}
```

## Use Case Contracts (Domain Layer)

### `DeleteDownloadUseCase`
```kotlin
interface DeleteDownloadUseCase {
    suspend operator fun invoke(id: String): Result<Unit>
}
```
*(Similar contracts for Open, Share, Rename, etc.)*

## ViewModel Contract (Presentation Layer)

### `DownloadListViewModel`
```kotlin
interface DownloadListUiState {
    val downloads: StateFlow<List<DownloadUiModel>>
}

data class DownloadUiModel(
    val id: String,
    val fileName: String,
    val status: DownloadStatus,
    val actionsEnabled: Boolean // Derived from status
)
```
