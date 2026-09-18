# Research: Download Context Menu Actions Logic

- Decision: Use individual Domain Use Cases for each management action (Open, Share, Rename, Delete, etc.).
- Rationale: Strictly adheres to the Single Responsibility Principle and Clean Architecture, facilitating better testability and maintenance.
- Alternatives considered: A single `DownloadActionsUseCase` class with multiple methods. Rejected because it violates ISP and SRP, making the class harder to test and maintain as actions grow.

- Decision: ViewModel exposes a `DownloadUiState` object (or similar wrapper) containing a map/list of `ActionStatus` (ENABLED/DISABLED) for each menu item, derived from the download status.
- Rationale: Keeps the UI logic minimal (Compose only renders based on `enabled` state) and ensures reactivity via StateFlow.
- Alternatives considered: Exposing only raw status and calculating enablement in UI. Rejected because it leaks business logic into the UI layer.

- Decision: Use MockK for mocking Repository interfaces and JUnit 5 for domain/ViewModel unit tests.
- Rationale: Established best practice for Kotlin/Android testing.
- Alternatives considered: PowerMock (too heavy/outdated).
