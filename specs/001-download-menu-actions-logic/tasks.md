# Tasks: Download Context Menu Actions Logic

## Phase 1: Setup
- [ ] T001 Initialize domain and presentation package structure for download management in `app/src/main/java/com/piumal/filedownloadmanager/`

## Phase 2: Foundational (Blocking)
- [ ] T002 Define `DownloadRepository` interface in `app/src/main/java/com/piumal/filedownloadmanager/domain/repository/DownloadRepository.kt`
- [ ] T003 Define `DownloadItem` entity and `DownloadStatus` enum in `app/src/main/java/com/piumal/filedownloadmanager/domain/model/DownloadItem.kt`

## Phase 3: Domain Layer (TDD) [US1]
- [ ] T004 [P] Create unit test suite for `OpenDownloadUseCase` in `app/src/test/java/com/piumal/filedownloadmanager/domain/usecase/OpenDownloadUseCaseTest.kt`
- [ ] T005 [P] Create unit test suite for `ShareDownloadUseCase` in `app/src/test/java/com/piumal/filedownloadmanager/domain/usecase/ShareDownloadUseCaseTest.kt`
- [ ] T006 [P] Create unit test suite for `RenameDownloadUseCase` in `app/src/test/java/com/piumal/filedownloadmanager/domain/usecase/RenameDownloadUseCaseTest.kt`
- [ ] T007 [P] Create unit test suite for `DeleteDownloadUseCase` in `app/src/test/java/com/piumal/filedownloadmanager/domain/usecase/DeleteDownloadUseCaseTest.kt`
- [ ] T008 [P] Implement `OpenDownloadUseCase` in `app/src/main/java/com/piumal/filedownloadmanager/domain/usecase/OpenDownloadUseCase.kt` to pass tests
- [ ] T009 [P] Implement `ShareDownloadUseCase` in `app/src/main/java/com/piumal/filedownloadmanager/domain/usecase/ShareDownloadUseCase.kt` to pass tests
- [ ] T010 [P] Implement `RenameDownloadUseCase` in `app/src/main/java/com/piumal/filedownloadmanager/domain/usecase/RenameDownloadUseCase.kt` to pass tests
- [ ] T011 [P] Implement `DeleteDownloadUseCase` in `app/src/main/java/com/piumal/filedownloadmanager/domain/usecase/DeleteDownloadUseCase.kt` to pass tests

## Phase 4: Presentation Layer (TDD) [US1]
- [ ] T012 Create unit test suite for `DownloadListViewModel` StateFlow logic in `app/src/test/java/com/piumal/filedownloadmanager/ui/downloads/DownloadListViewModelTest.kt`
- [ ] T013 Implement `DownloadListViewModel` logic to compute `actionsEnabled` based on `DownloadStatus` in `app/src/main/java/com/piumal/filedownloadmanager/ui/downloads/DownloadListViewModel.kt`

## Phase 5: UI/Compose Integration [US1]
- [ ] T014 Connect ViewModel StateFlow to Jetpack Compose menu item `enabled` state in `app/src/main/java/com/piumal/filedownloadmanager/ui/downloads/DownloadListScreen.kt`
- [ ] T015 Wire up menu item click events to trigger ViewModel actions in `app/src/main/java/com/piumal/filedownloadmanager/ui/downloads/DownloadListScreen.kt`

## Phase 6: Polish
- [ ] T016 Verify integration with all completed scenarios in `quickstart.md`
