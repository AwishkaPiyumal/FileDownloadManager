# Implementation Tasks: Security & Reliability Hardening

This document outlines granular, independently testable tasks for the hardening project, categorized by security domain.

## Domain 1: URL Security
- **T-001**: Enforce HTTPS
  - Phase: P0
  - Affected Files: `app/src/main/java/com/piumal/network/UrlValidator.kt` (New)
  - Dependencies: None
  - Objective: Enforce HTTPS for all new download requests. Reject insecure HTTP.
  - Acceptance Criteria: `https` downloads proceed, `http` downloads fail before network initiation.
  - Security Invariant: No insecure HTTP downgrades allowed.
  - Regression Tests: Unit tests for `UrlValidator` covering various URI schemes.

- **T-002**: Redirect Security
  - Phase: P0
  - Affected Files: `app/src/main/java/com/piumal/network/DownloadClient.kt`
  - Dependencies: T-001
  - Objective: Prevent HTTPS to HTTP redirect downgrade. Limit redirect chains to 5.
  - Acceptance Criteria: Redirects to HTTP fail; chains > 5 fail.
  - Security Invariant: Redirect chains must not degrade security or loop indefinitely.
  - Regression Tests: OkHttp interceptor test with mocked HTTPS->HTTP redirect.

## Domain 2: Filename Security
- **T-003**: Sanitization of Filenames
  - Phase: P0
  - Affected Files: `app/src/main/java/com/piumal/utils/FileNameSanitizer.kt` (New)
  - Dependencies: None
  - Objective: Sanitize URL/Content-Disposition derived filenames (remove path traversal/control characters).
  - Acceptance Criteria: `../`, `\`, control chars removed/replaced. Maximum length enforced.
  - Security Invariant: Filenames cannot cause path traversal or exceed file system limits.
  - Regression Tests: Unit tests with malicious filenames (traversal, control chars, absolute paths).

## Domain 3: Filesystem Security
- **T-004**: Filesystem Containment
  - Phase: P0
  - Affected Files: `app/src/main/java/com/piumal/storage/FileOperations.kt` (New)
  - Dependencies: T-003
  - Objective: Implement canonical path check for target files against approved download directory.
  - Acceptance Criteria: All file operations fail if canonical path is outside the download directory.
  - Security Invariant: No attacker-controlled or corrupted path may cause access outside the approved storage location.
  - Regression Tests: Unit tests simulating path traversal attacks using various encoding.

## Domain 4: Streaming
- **T-005**: Size Enforcement
  - Phase: P0
  - Affected Files: `app/src/main/java/com/piumal/network/DownloadManager.kt`
  - Dependencies: None
  - Objective: Implement continuous streaming size enforcement using `Content-Length`.
  - Acceptance Criteria: Downloads exceeding the maximum size fail gracefully during streaming; disk-full is handled.
  - Security Invariant: Prevent disk exhaustion.
  - Regression Tests: Mock server tests returning Content-Length > limit.

## Domain 5: SAF
- **T-006**: SAF Storage Abstraction
  - Phase: P1
  - Affected Files: `app/src/main/java/com/piumal/storage/StorageManager.kt` (New)
  - Dependencies: None
  - Objective: Abstract storage behind SAF to handle persistent permissions.
  - Acceptance Criteria: Downloads create/access files via `content://` URIs.
  - Security Invariant: Never convert `content://` URIs into guessed absolute paths.
  - Regression Tests: Instrumentation tests verifying SAF file operations.

## Domain 6: FileProvider
- **T-007**: FileProvider Hardening
  - Phase: P0
  - Affected Files: `app/src/main/AndroidManifest.xml`, `app/src/main/res/xml/file_paths.xml`
  - Dependencies: None
  - Objective: Remove broad external-path exposure, set `exported=false`.
  - Acceptance Criteria: FileProvider only exposes minimal required directories.
  - Security Invariant: FileProvider cannot expose arbitrary files.
  - Regression Tests: Test file sharing with unapproved paths.

## Domain 7: Queue/Cancellation
- **T-008**: Deterministic Cancellation
  - Phase: P0
  - Affected Files: `app/src/main/java/com/piumal/network/DownloadManager.kt`
  - Dependencies: None
  - Objective: Ensure reliable cancellation terminates network/file stream and cleans up temp files.
  - Acceptance Criteria: CANCELLED state does not equal FAILED; temp files are deleted.
  - Security Invariant: Failed/cancelled downloads must clean up temporary files.
  - Regression Tests: Concurrency tests for cancellation during network activity.

## Domain 8: Database Security
- **T-009**: Database Path Validation
  - Phase: P0
  - Affected Files: `app/src/main/java/com/piumal/data/DownloadRepository.kt`
  - Dependencies: T-003, T-004
  - Objective: Validate stored paths before any file operations.
  - Acceptance Criteria: Invalid/malicious database paths cause safe failure, not crash/access.
  - Security Invariant: Database-stored paths must be treated as untrusted input.
  - Regression Tests: Unit tests with corrupted/malicious database entries.

## Domain 9: Privacy/Logging
- **T-010**: Sensitive Data Redaction
  - Phase: P1
  - Affected Files: Entire Project (Logging audit)
  - Dependencies: None
  - Objective: Audit all logs and redact sensitive information (tokens, cookies, credentials, full URLs).
  - Acceptance Criteria: No secrets/tokens in logs.
  - Security Invariant: Protect user privacy; never log secrets.
  - Regression Tests: Verify log output with sensitive data inputs.

## Domain 10: Backup/Permissions
- **T-011**: Manifest Audit
  - Phase: P1
  - Affected Files: `app/src/main/AndroidManifest.xml`
  - Dependencies: None
  - Objective: Audit manifest, remove legacy storage permissions, configure backup policy.
  - Acceptance Criteria: Minimal necessary permissions used.
  - Security Invariant: Principle of least privilege.
  - Regression Tests: Verify app functionality after permission reduction.

## Domain 11: Component/Intent Security
- **T-012**: Exported Component Audit
- Phase: P1
- Affected Files: `app/src/main/AndroidManifest.xml`
- Dependencies: None
- Objective: Set `android:exported="false"` on all components; validate Intent filter usage.
- Acceptance Criteria: Components not intended for external use are not exported.
- Security Invariant: Prevent unauthorized component access.
- Regression Tests: Attempt to launch internal components from external app.

## Domain 12: SHA-256
- **T-013**: Integrity Verification
  - Phase: P2
  - Affected Files: `app/src/main/java/com/piumal/network/DownloadManager.kt`
  - Dependencies: T-004
  - Objective: Implement post-download SHA-256 integrity verification.
  - Acceptance Criteria: Mismatched hash triggers failure and file deletion.
  - Security Invariant: Ensure downloaded content integrity.
  - Regression Tests: Test with known-bad hash.

## Domain 13: Security Testing
- **T-014**: Comprehensive Regression Suite
  - Phase: P0-P2
  - Affected Files: `app/src/test/java/com/piumal/security/...`
  - Dependencies: All
  - Objective: Add dedicated regression tests for all identified security issues.
  - Acceptance Criteria: High-risk scenarios (traversal, downgrade, etc.) are covered.
  - Security Invariant: High-risk security vulnerabilities must have associated regression tests.
  - Regression Tests: Aggregate of all security test cases.

## Domain 14: Final Verification
- **T-015**: Security Hardening Audit
  - Phase: P2
  - Affected Files: All
  - Dependencies: All
  - Objective: Final verification of hardening implementation, linting, and structural review.
  - Acceptance Criteria: All security criteria satisfied; no regression.
  - Security Invariant: All specified invariants maintained.
  - Regression Tests: Final security sweep.
