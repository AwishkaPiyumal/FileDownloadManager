<!--
=== SYNC IMPACT REPORT ===
- Version Change: uninitialized -> v1.0.0
- List of Modified Principles:
  * PRINCIPLE_1_NAME -> I. Clean Architecture & Layer Separation (Domain-First)
  * PRINCIPLE_2_NAME -> II. Reactive State Flow & Single Source of Truth
  * PRINCIPLE_3_NAME -> III. Unidirectional Data Flow & Material Design 3
  * PRINCIPLE_4_NAME -> IV. Controlled Concurrency & Lifecycle-Aware Async
  * PRINCIPLE_5_NAME -> V. Verification-Driven Quality (Test-First)
- Added Sections:
  * Platform Constraints and System Integration
  * Code Quality and Dependency Gates
- Removed Sections: None
- Templates requiring updates:
  * .specify/templates/plan-template.md (✅ updated / no changes required as it uses generic constitution checks)
  * .specify/templates/spec-template.md (✅ updated / no changes required)
  * .specify/templates/tasks-template.md (✅ updated / no changes required)
- Follow-up TODOs: None
-->

# File Download Manager Constitution

## Core Principles

### I. Clean Architecture & Layer Separation (Domain-First)
The application MUST be divided into three distinct, decoupled layers: Presentation, Domain, and Data. The Domain layer is the core of the application and MUST remain pure Kotlin with zero dependencies on Android, Jetpack Compose, or storage frameworks (such as Room or Retrofit). Presentation and Data layers MUST depend on the Domain layer. Business logic must reside strictly inside Domain Use Cases. Translators/Mappers must be used to map between Domain models and UI/Data models at layer boundaries to avoid coupling.

### II. Reactive State Flow & Single Source of Truth
All UI states MUST be managed reactively using Kotlin Coroutines, Flow, and StateFlow inside ViewModels. ViewModels must expose a single read-only StateFlow of state objects (e.g., UiState) to Jetpack Compose views. Views must not modify state directly and must propagate UI actions back to ViewModels. The Data repository is the Single Source of Truth (SSOT) for data domains, coordinating network fetches and disk persistence predictably.

### III. Unidirectional Data Flow & Material Design 3
UI development MUST strictly adhere to the Jetpack Compose Unidirectional Data Flow (UDF) pattern: state flows down, events flow up. Jetpack Compose UI components must be stateless, modular, and reuse-oriented. Styling and layout must strictly follow Material Design 3 guidelines, supporting automatic dark theme, accessibility, and fluid response to configuration changes.

### IV. Controlled Concurrency & Lifecycle-Aware Async
All asynchronous and background execution MUST use Kotlin Coroutines and Kotlin Flow. Thread safety is mandatory: disk I/O and network operations must run on Dispatchers.IO, UI updates on Dispatchers.Main, and CPU-heavy tasks on Dispatchers.Default. ViewModels must launch coroutines inside viewModelScope. Long-running, persistent background tasks (e.g., download queues, file downloading, scheduling) MUST utilize Android WorkManager to guarantee task completion and handle battery, network type, and system restrictions correctly.

### V. Verification-Driven Quality (Test-First)
A comprehensive test suite MUST be maintained. Business logic in Use Cases, ViewModels, and repositories MUST have unit tests using mock frameworks (such as MockK or Mockito) and kotlinx-coroutines-test for coroutine control. Compose UI components must have test coverage using Compose Test Rules. Every new feature or bug fix must include relevant test coverage. Integration and instrumentation tests must verify proper Room database and HTTP client operations.

## Platform Constraints and System Integration
- **Android SDK Targets**: Target SDK 34+ and Minimum SDK 24+. Ensure modern Android permission requirements (e.g., post-Android 13 post-notifications, storage, clipboard access) are checked at runtime.
- **Storage and File Buffering**: Downloads must be streamed directly to files with a buffer size of at least 8KB to prevent memory exhaustion (OOM). Download files should reside in scoped storage or app-private storage.
- **Network Transition Resilience**: The application must monitor network connectivity and support automatic download pause, resume, and fail-safe recovery on network drops.

## Code Quality and Dependency Gates
- **Kotlin Standard**: Follow the Kotlin Coding Conventions and Android Kotlin Style Guide. Code formatting must be validated via ktlint or detekt.
- **Gradle and Dependency Management**: All library dependencies must be centralized in 'gradle/libs.versions.toml'. Hardcoding versions in 'build.gradle.kts' files is strictly prohibited.
- **Quality Gates**: Every pull request must compile cleanly without Kotlin compiler errors, pass all lint checks, and pass all unit tests before merge.

## Governance
This constitution governs all design, implementation, and review decisions for the File Download Manager. It supersedes any unwritten practices or preferences.

All PRs/reviews must verify compliance with these core principles. Complexity must be justified. Use specs/ templates and plan files for runtime development guidance.

**Version**: 1.0.0 | **Ratified**: 2026-07-13 | **Last Amended**: 2026-07-13
