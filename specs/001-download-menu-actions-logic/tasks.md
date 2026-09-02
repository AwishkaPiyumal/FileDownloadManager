# Tasks: Download Context Menu Actions Logic

## Phase 1: Setup
- [X] T001 Initialize domain and presentation package structure for download management in `app/src/main/java/com/piumal/filedownloadmanager/`

## Phase 2: Foundational (Blocking)
- [X] T002 Define `DownloadRepository` interface in `app/src/main/java/com/piumal/filedownloadmanager/domain/repository/DownloadRepository.kt`
- [X] T003 Define `DownloadItem` entity and `DownloadStatus` enum in `app/src/main/java/com/piumal/filedownloadmanager/domain/model/DownloadItem.kt`

## Phase 3: Domain Layer (TDD) [US1]
- [X] T004 [P] Create unit test suite for `OpenDownloadUseCase` in `app/src/test/java/com/piumal/filedownloadmanager/domain/usecase/OpenDownloadUseCaseTest.kt`
- [X] T005 [P] Create unit test suite for `ShareDownloadUseCase` in `app/src/test/java/com/piumal/filedownloadmanager/domain/usecase/ShareDownloadUseCaseTest.kt`
- [X] T006 [P] Create unit test suite for `RenameDownloadUseCase` in `app/src/test/java/com/piumal/filedownloadmanager/domain/usecase/RenameDownloadUseCaseTest.kt`
- [X] T007 [P] Create unit test suite for `DeleteDownloadUseCase` in `app/src/test/java/com/piumal/filedownloadmanager/domain/usecase/DeleteDownloadUseCaseTest.kt`
- [X] T008 [P] Implement `OpenDownloadUseCase` in `app/src/main/java/com/piumal/filedownloadmanager/domain/usecase/OpenDownloadUseCase.kt` to pass tests
- [X] T009 [P] Implement `ShareDownloadUseCase` in `app/src/main/java/com/piumal/filedownloadmanager/domain/usecase/ShareDownloadUseCase.kt` to pass tests
- [X] T010 [P] Implement `RenameDownloadUseCase` in `app/src/main/java/com/piumal/filedownloadmanager/domain/usecase/RenameDownloadUseCase.kt` to pass tests
- [X] T011 [P] Implement `DeleteDownloadUseCase` in `app/src/main/java/com/piumal/filedownloadmanager/domain/usecase/DeleteDownloadUseCase.kt` to pass tests

## Phase 4: Presentation Layer (TDD) [US1]
- [X] T012 Create unit test suite for `DownloadListViewModel` StateFlow logic in `app/src/test/java/com/piumal/filedownloadmanager/ui/downloads/DownloadListViewModelTest.kt`
- [X] T013 Implement `DownloadListViewModel` logic to compute `actionsEnabled` based on `DownloadStatus` in `app/src/main/java/com/piumal/filedownloadmanager/ui/downloads/DownloadListViewModel.kt`

## Phase 5: UI/Compose Integration [US1]
- [X] T014 Connect ViewModel StateFlow to Jetpack Compose menu item `enabled` state in `app/src/main/java/com/piumal/filedownloadmanager/ui/downloads/DownloadListScreen.kt`
- [X] T015 Wire up menu item click events to trigger ViewModel actions in `app/src/main/java/com/piumal/filedownloadmanager/ui/downloads/DownloadListScreen.kt`

## Phase 7: Convergence
- [X] T017 Implement `OpenDownloadUseCase` with Android Intent functionality per FR.1 (missing)
- [X] T018 Implement `ShareDownloadUseCase` with Android Intent functionality per FR.1 (missing)
- [X] T019 Implement `ShowInFolder` action and UseCase per FR.1 (missing)
- [X] T020 Implement `ShowInfo` action and UseCase per FR.1 (missing)
- [X] T021 Implement `MoveTo` action and UseCase per FR.1 (missing)
- [X] T022 Implement `RemoveFromList` action and UseCase per FR.1 (missing)
- [X] T023 Implement Rename action UI dialog and ViewModel handler per FR.1 (partial)
- [X] T024 Update `DownloadScreenViewModel` to handle all menu action events per FR.1 (partial)

## Phase 8: Convergence
- [X] T025 Refactor `DownloadFileOperations.kt` to remove manual string manipulation and use proper java.io.File APIs for the Move operation (partial)
- [X] T026 Update `DownloadScreenViewModel.kt` to handle error messaging without direct, potentially malformed error.message access (partial)

## Phase 9: Convergence
- [X] T027 Replace `DownloadRepositoryImpl.showInFolder` with the requested simple intent-based implementation to resolve `StringIndexOutOfBoundsException` (contradicts)
- [X] T028 Refactor `MoveTo` operation in `DownloadRepositoryImpl` and/or `DownloadFileOperations` to ensure robust, full-path destination file construction (partial)

## Phase 10: Convergence
- [X] T029 Expose exact error message from `MoveTo` operation in `MoveToUseCase.kt` and `DownloadScreenViewModel.kt` per user instruction
- [X] T030 Implement guaranteed writable fallback (app-specific directory) in `DownloadFileOperations.kt` for `movePhysicalFile` per user instruction

## Phase 11: Convergence
- [X] T031 Rename `MoveToUseCase` to `CopyToUseCase` and update repository interface to `copyDownload` per prompt
- [X] T032 Update UI components (`DownloadItemMoreOption`, `DownloadItemCard`) to rename `Moveto` action to `Copyto` per prompt
- [X] T033 Implement new `copyDownload` logic in `DownloadRepositoryImpl` without source deletion or database updates per prompt
- [X] T034 Refactor `DownloadScreenViewModel` to handle `CopyTo` events and cleanup `MoveTo` logic per prompt
- [X] T035 Update UI text "Move to..." to "Copy to..." in all relevant components per prompt

## Phase 12: Convergence
- [X] T036 Rename `MoveToUseCaseTest.kt` to `CopyToUseCaseTest.kt` and update to test correct Copy behavior (retaining source file) per SC-001 (partial)

## Phase 13: Convergence
- [X] T037 Wire "How to download?" menu item to navigate to `Screen.Support.route` with `focus=how_to_download` argument and implement additive UI section in `SupportScreen` per user request (missing)

## Phase 14: Convergence - File Existence Fixes
- [X] T038 Refactor DownloadRepositoryImpl.openDownload and shareDownload to use a safer check for file existence via FileProvider or ContentResolver to support Scoped Storage per user requirement (HIGH)
- [X] T039 Synchronize filePath in Room Database with actual storage path including 'FileDownloadManager' directory in StartDownloadUseCase and DownloadManager per user requirement (HIGH)

## Phase 15: Convergence - Security Audit Remediation
- [X] T040 Implement dynamic MIME-type allowlist validation in `ContentValidator.kt` to replace static extension blocklist per security audit (HIGH)
- [X] T041 Update `DownloadManager.kt` to enforce strict `Content-Type` header validation against the allowlist before/during download initiation, throwing `SecurityException` on violation per security audit (HIGH)
- [X] T042 Audit and refactor `StartDownloadUseCase`, URL parsing, and any `Content-Disposition` header handling to ensure universal, strict invocation of `FileNameSanitizer` before any file I/O operations per security audit (HIGH)

## Phase 16: Convergence - Magic Number Security Remediation
- [X] T043 Implement file signature ("Magic Number") validation in `DownloadManager.kt` streaming pipeline to detect and block malicious signatures (e.g., MZ, #!) upon the first buffer read, aborting and deleting the partial file per security request (CRITICAL)

## Phase 17: Convergence - MIME-type Allowlist Fix
- [X] T044 Update `ContentValidator.validateMimeType` to accept URL for extension fallback, remove `application/octet-stream` from `BLOCKED_MIME_TYPES`, and implement extension-based fallback for unknown or generic types (HIGH)
- [X] T045 Update `DownloadManager.kt` to use the updated `validateMimeType(contentType, url)` signature (HIGH)
