# Implementation Plan: Security & Reliability Hardening

This plan outlines the incremental hardening of the File Download Manager, following the principles defined in `specs/002-security-hardening-spec/spec.md`.

## Incremental Implementation Layers

### 1. Download Validation
- **Files**: `ContentValidator.kt`, `DownloadManager.kt`
- **Plan**: Strengthen `validateDownloadUrl` to explicitly reject `http`.
- **Failure Behavior**: Graceful `FAILED` status with user-friendly error.
- **Test**: Unit tests covering scheme validation.

### 2. Filename Security
- **Files**: `FileNameSanitizer.kt`, `DownloadManager.kt`
- **Plan**: Ensure full sanitization of dangerous chars (`../`, `\0`, etc.).
- **Security Boundary**: Input sanitization at boundary of UI/Net.

### 3. Filesystem Containment
- **Files**: `DownloadFileOperations.kt`
- **Plan**: Create `ensureInDirectory(file: File)` utility using `getCanonicalPath`.
- **Test**: Path traversal test cases.

### 4. Secure Download Directory
- **Files**: `DownloadStoragePaths.kt`
- **Plan**: Lock down directory permissions (internal storage).

### 5. SAF URI Architecture (High Priority/Risk)
- **Files**: `DownloadEntity.kt`, `DownloadRepositoryImpl.kt`
- **Data Model**: Change `filePath` (String) to `uri` (String) in `DownloadEntity`.
- **Migration**: Data migration required to convert existing local paths to `content://` URIs.
- **Rollback**: Backup database before migration.

### 6. FileProvider
- **Files**: `AndroidManifest.xml`
- **Plan**: Audit `provider` element; ensure `exported=false`.

### 7. HTTPS and Redirect Security
- **Files**: `DownloadManager.kt`
- **Plan**: Configure `OkHttpClient` to explicitly forbid `http` redirects.

### 8. HTTP/OkHttp Reliability
- **Files**: `DownloadManager.kt`
- **Plan**: Enforce strict timeouts (30s).

### 9. Streaming and Size Limits
- **Files**: `DownloadManager.kt`
- **Plan**: Check `totalBytes` vs `MAX_FILE_SIZE` continuously.

### 10. Temporary Files and Atomic Finalization
- **Files**: `DownloadManager.kt`
- **Plan**: Download to `.tmp` -> `renameTo` atomic finalize.

### 11. Queue State Machine
- **Files**: `DownloadQueueManager.kt`
- **Plan**: Strictly enforce transitions (`QUEUED` -> `DOWNLOADING`, etc.).

### 12. Cancellation
- **Files**: `DownloadManager.kt`
- **Plan**: Ensure Coroutine context cancellation properly closes streams.

### 13. Database Path Safety
- **Files**: `DownloadRepositoryImpl.kt`
- **Plan**: Validate all paths against `DownloadStoragePaths` before IO.

### 14. Privacy and Logging
- **Files**: Entire Project
- **Plan**: Audit `Log.d/e`, ensure no sensitive metadata is logged.

### 15. Backup Configuration
- **Files**: `AndroidManifest.xml`
- **Plan**: Define `fullBackupContent` to exclude sensitive metadata.

### 16. Android Permissions
- **Files**: `AndroidManifest.xml`
- **Plan**: Remove unused permissions (e.g., legacy storage).

### 17. Exported Component Security
- **Files**: `AndroidManifest.xml`
- **Plan**: Ensure all Activities/Services/Receivers have explicit `android:exported` set.

### 18. Optional SHA-256 Verification
- **Files**: `DownloadItem.kt`, `DownloadManager.kt`
- **Data Model**: Add `expectedHash` field.
- **Plan**: Implement post-download hash verification.

### 19-22. Testing Strategy
- **Strategy**: Unit/Integration for every layer. Security regression tests for P0 issues.

---
## General Strategy
- **Incremental Implementation**: Apply one layer at a time.
- **Rollback Strategy**: Git revert/Database backup before every layer.
- **Testing**: Regression tests must pass before merging any hardening layer.
