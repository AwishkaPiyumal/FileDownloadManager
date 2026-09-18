# Quickstart Guide: Download Context Menu Actions Logic

## Validation Scenarios

1. **Verify Disabled State:**
   - Initiate a download.
   - Observe download list entry.
   - Verify context menu actions (Open, Share, etc.) are disabled.

2. **Verify Enabled State:**
   - Wait for download to reach COMPLETED status.
   - Observe download list entry.
   - Verify context menu actions are enabled and functional.

3. **Verify Action Functionality:**
   - Select COMPLETED download.
   - Trigger "Delete" action.
   - Verify item is removed from UI and data source.

## Setup & Run

1. **Build Project**: `./gradlew assembleDebug`
2. **Run Tests**: `./gradlew test`
3. **Run App**: Select debug configuration in Android Studio and run on device/emulator.
