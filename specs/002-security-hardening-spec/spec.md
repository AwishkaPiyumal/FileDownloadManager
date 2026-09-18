# Security & Reliability Hardening Specification - File Download Manager

This document defines the requirements for hardening the File Download Manager to improve security, privacy, and reliability while preserving core functionality. It builds on the fixes implemented for `instant-download-failure`.

## 1. URL Security
- **Validation**: All URLs must be parsed using `java.net.URL` or similar to validate structure.
- **Schemes**: Only `https` allowed by default. `http` must be rejected for new downloads (unless specifically authorized for backward compatibility, which is discouraged).
- **Redirects**: Must be validated. HTTPS to HTTP downgrade is strictly prohibited. Redirect chains must be limited to 5.
- **Privacy**: Never log sensitive query parameters, tokens, or credentials to logs or local databases.

## 2. Filename & Filesystem Security
- **Normalization**: All filenames must be sanitized (`FileNameSanitizer`) to remove dangerous characters, path separators (`../`, `/`, `\`), and control characters.
- **Canonicalization**: All filesystem paths must be resolved to their canonical form and verified to be within the intended download directory.
- **Storage Access**: Prefer SAF `content://` URIs. Avoid conversion to guessed filesystem paths.
- **Atomic Operations**: Downloads must use temporary files (`.part` or similar) and be finalized atomically (renamed) upon completion.

## 3. Networking & Streaming
- **OkHttp Hardening**: Timeouts (connect, read, write) must be configured conservatively (e.g., 30s).
- **Streaming**: Downloads must stream to disk, not buffer into memory.
- **Size Enforcement**: Maximum download size (5 GB) must be checked continuously during streaming.
- **Cleanup**: Temporary files must be explicitly deleted on failure or cancellation.

## 4. Reliability & Queue Management
- **Deterministic States**: `QUEUED`, `DOWNLOADING`, `PAUSED`, `COMPLETED`, `FAILED`, `CANCELLED`.
- **Concurrency**: Only one active connection per download ID.
- **Service Lifecycle**: Ensure foreground service handles process death gracefully, resuming based on persisted state.

## 5. Privacy, Content Safety, & Integrity
- **Privacy**: No logging of sensitive metadata (tokens, cookies, Authorization headers).
- **Inert Content**: Downloaded content is untrusted. No auto-execution.
- **Integrity**: Future-proofing: Implement optional SHA-256 verification when hash is provided.

## 6. Testing & Acceptance Criteria
- **Regression**: All P0/P1 security issues require automated regression tests (unit/integration).
- **Criteria**:
    - Functional: All legitimate downloads (HTTPS, no extension, query params, etc.) must succeed.
    - Security: Path traversal attempts must be blocked.
    - Reliability: Cancellation/failures must leave no partial files in the final folder.

## Residual Risk
This specification significantly reduces the attack surface but does not guarantee 100% security. Users remain responsible for content they download.
