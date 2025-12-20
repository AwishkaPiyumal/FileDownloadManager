# File Download Manager

A modern Android download manager application built with Jetpack Compose following Clean Architecture and MVVM patterns.

[![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android)](https://www.android.com)
[![Language](https://img.shields.io/badge/Language-Kotlin-7F52FF?logo=kotlin)](https://kotlinlang.org)
[![API](https://img.shields.io/badge/API-24%2B-brightgreen.svg)](https://android-arsenal.com/api?level=24)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

> **Status:** Work in Progress - Under active development

## Overview

File Download Manager is a feature-rich Android application that provides efficient download management with a modern Material Design 3 interface. Built with industry-standard architecture patterns for scalability and maintainability.

## Features

### Current Implementation
- Material Design 3 UI with automatic dark theme support
- Smart download dialog with clipboard URL detection
- Automatic filename extraction from URLs
- Advanced sorting capabilities (date, name, size, A-Z, Z-A)
- Download state management (All, Active, Completed)
- MVVM + Clean Architecture implementation
- Reactive state management with StateFlow

### Planned Features
- Background downloads with WorkManager
- Download scheduling functionality
- Pause/Resume download support
- Real-time progress tracking with live updates
- System notifications for download status
- Download queue management
- Multi-threaded parallel downloads
- Download speed limiter
- WiFi-only download option
- Automatic retry on failure
- Download history with Room database
- File integrity verification (checksum)
- Download categories and organization
- Search and filter downloads
- Batch download support
- **In-app web browser** with download interception
- Bookmark management in browser
- Download from cloud services integration
- Export/Import download list
- And many more features coming soon...

## Architecture

This project implements **Clean Architecture** with **MVVM pattern**:

```
┌─────────────────────────────────────┐
│   Presentation (UI + ViewModel)    │  ← Jetpack Compose, Material 3
├─────────────────────────────────────┤
│   Domain (Use Cases + Models)      │  ← Business Logic
├─────────────────────────────────────┤
│   Data (Repository Implementations) │  ← Data Sources
└─────────────────────────────────────┘
```

**Key Principles:**
- Separation of Concerns
- Dependency Inversion
- Single Responsibility
- Testability

For detailed architecture documentation, see [ARCHITECTURE.md](ARCHITECTURE.md)

## Tech Stack

**Core:**
- Kotlin
- Jetpack Compose
- Material Design 3
- Kotlin Coroutines
- StateFlow

**Architecture Components:**
- ViewModel
- Navigation Compose
- Lifecycle-aware Components

**Planned:**
- Room Database
- Retrofit
- WorkManager
- Hilt for DI

## Project Structure

```
app/src/main/java/com/piumal/filedownloadmanager/
├── data/
│   └── repository/          # Repository implementations
├── domain/
│   ├── model/              # Domain models
│   ├── repository/         # Repository interfaces
│   └── usecase/            # Business use cases
└── ui/
    ├── downloads/          # Download management UI
    ├── browser/            # Browser screen
    ├── settings/           # Settings screen
    ├── components/         # Reusable UI components
    ├── navigation/         # Navigation setup
    └── theme/              # Theme configuration
```

See [PROJECT_STRUCTURE.md](PROJECT_STRUCTURE.md) for complete details.

## Getting Started

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- JDK 17 or higher
- Android SDK 24 (Minimum) - 34 (Target)
- Gradle 8.0+

### Installation

1. Clone the repository
   ```bash
   git clone https://github.com/AwishkaPiyumal/FileDownloadManager.git
   cd FileDownloadManager
   ```

2. Open the project in Android Studio
   - File → Open → Select project directory
   - Wait for Gradle sync to complete

3. Build and run
   - Select a device or emulator
   - Click Run or press `Shift + F10`

### Build Variants

- **debug** - Development build with logging enabled
- **release** - Production build with ProGuard optimizations

## Development

### Code Style

This project follows:
- [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- [Android Kotlin Style Guide](https://developer.android.com/kotlin/style-guide)
- Clean Architecture principles
- SOLID principles

### Testing

```bash
# Run unit tests
./gradlew test

# Run instrumentation tests
./gradlew connectedAndroidTest

# Generate coverage report
./gradlew jacocoTestReport
```

## Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

Please ensure your code follows the project's coding standards and includes appropriate tests.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for complete details.

## Author

**Avishka Piumal**

- GitHub: [@AwishkaPiyumal](https://github.com/AwishkaPiyumal)

## Acknowledgments

- Material Design 3 by Google
- Android Jetpack libraries
- Kotlin Coroutines team
- Clean Architecture concepts by Robert C. Martin

---

If you find this project useful, please consider giving it a star ⭐

