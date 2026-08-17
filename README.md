# File Download Manager

A robust, feature-rich Android application for managing your downloads, built with modern Android development practices.

## Features

*   **Background Download Management**: Efficiently handle downloads in the background with robust status tracking.
*   **Scoped Storage Compliant**: Reliable file exporting ("Copy to" feature) fully compliant with Android 11+ Scoped Storage requirements.
*   **Help & Support**: Built-in guidance to help you navigate and use the application effectively.
*   **Dark/Light Mode Support**: Seamless adaptation to your device's system theme preferences.

## Tech Stack

*   **Language**: 100% Kotlin
*   **UI**: Jetpack Compose
*   **Architecture**: Clean Architecture (Data, Domain, Presentation) with MVVM
*   **Dependency Injection**: Dagger Hilt
*   **Local Storage**: Room Database
*   **Asynchrony**: Kotlin Coroutines & Flow

## Product Flavors

This project is configured with two distinct build flavors:

*   **`playstore`**: A version compliant with Google Play Store policies. Downloads from restricted platforms (e.g., YouTube, Facebook, TikTok) are blocked.
*   **`personal`**: An unrestricted version intended for personal use, allowing downloads from all supported sources.

## Installation & Setup

1.  **Clone the Repository**:
    ```bash
    git clone https://github.com/AwishkaPiyumal/FileDownloadManager.git
    ```
2.  **Open in Android Studio**:
    *   Open the project folder in Android Studio.
    *   Wait for the project to sync with Gradle.
3.  **Build Variants**:
    *   In Android Studio, open the **Build Variants** tool window (usually on the left sidebar).
    *   Select the desired flavor and build type for your build (e.g., `playstoreDebug` or `personalDebug`).
4.  **Run/Build**:
    *   Click the **Run** button to install the app on your emulator or connected device.

## Legal Disclaimer

This application is provided for personal use only. The developer is not responsible for any copyright violations or misuse of this application. It is the user's sole responsibility to ensure they have the necessary rights and permissions to download any content.
