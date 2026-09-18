# Engineering Constitution - File Download Manager

This document defines the core principles for the development and maintenance of the File Download Manager application. All contributors must adhere to these standards.

## Core Priorities
1. USER SAFETY
2. USER PRIVACY
3. SECURITY BY DEFAULT
4. DATA MINIMIZATION
5. DEFENSE IN DEPTH
6. SECURE ANDROID STORAGE
7. RELIABLE DOWNLOADS
8. BACKWARD COMPATIBILITY
9. TESTABILITY
10. MAINTAINABILITY

## Security Principles
- Treat all external input as untrusted.
- URLs, HTTP headers, Content-Disposition, filenames, database paths, and Intent extras are all untrusted.
- SAF URIs are untrusted until permission and scope are verified.
- Downloaded content is untrusted.
- Never allow attacker-controlled input to escape an approved filesystem boundary.
- Never automatically execute downloaded content.
- Never expose unnecessary user data.
- Never log secrets, credentials, tokens, cookies, or sensitive query parameters.
- Follow the principle of least privilege and use secure defaults.
- Prefer fail-closed behavior for security-sensitive operations.
- Preserve legitimate user functionality when rejecting unsafe input.

## Networking Principles
- Enforce HTTPS by default; no insecure HTTP downgrades.
- Validate all redirects.
- HEAD requests are not a mandatory prerequisite for downloading.
- Downloads must stream; never load entire files into memory.
- Enforce maximum download size during streaming.
- Treat network responses as untrusted.

## Filesystem Principles
- Never trust filenames; prevent path traversal and absolute-path injection.
- Enforce canonical path containment.
- Use temporary files for incomplete downloads; never expose partial downloads as completed.
- Finalize files atomically where supported.
- Never blindly trust filesystem paths stored in the database.

## Android Principles
- Follow modern Android storage architecture.
- Use Storage Access Framework (SAF) correctly; prefer `content://` URIs over guessed filesystem paths.
- Minimize permissions; review every exported component.
- FileProvider must expose only the minimum required directories.
- Avoid broad external storage exposure.

## Reliability Principles
- Download state transitions must be deterministic.
- Prevent duplicate simultaneous downloads.
- Cancellation must terminate the underlying network operation.
- CANCELLED state must not be represented as FAILED.
- Handle process death and service restart safely.
- Failed downloads must clean up temporary files.

## Development Principles
- Do not rewrite working architecture without evidence.
- Make small, reviewable changes.
- Preserve existing features.
- Add regression tests for every bug fixed.
- Security fixes require tests.
- Never claim the application is "100% secure"; document residual risks.
- Do not weaken security merely to make a test pass.
- Evidence-based changes and regression testing are **required** for all P0/P1 security issues.
