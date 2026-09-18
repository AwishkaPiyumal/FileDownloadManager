# Data Model: Download Context Menu Actions Logic

## Entities

### DownloadItem
Represents the core data entity for a download.

| Field | Type | Description |
| :--- | :--- | :--- |
| id | String | Unique identifier |
| url | String | Source URL |
| fileName | String | Extracted/assigned filename |
| status | DownloadStatus | Current state (ACTIVE, PAUSED, COMPLETED, FAILED) |
| filePath | String | Local path (if completed) |

## Enums / Types

### DownloadStatus
- `ACTIVE`
- `PAUSED`
- `COMPLETED`
- `FAILED`

### ActionState
- `ENABLED`
- `DISABLED`

## State Transitions
- UI Action Enablement follows `DownloadStatus`:
  - `ACTIVE` | `PAUSED` | `FAILED` -> Actions `DISABLED`
  - `COMPLETED` -> Actions `ENABLED`
