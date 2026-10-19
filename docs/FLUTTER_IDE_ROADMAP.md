# FlutterIDE migration roadmap

## Goal

Deliver a Flutter/Dart-first Android IDE that remains responsive on low-memory phones. Keep the native Android host implementation only where it is needed to run the IDE; Kotlin/Java Android application authoring is not the product focus.

## Baseline audit findings

- The current `dev` branch is still the archived AndroidIDE Gradle multi-module application, not a Flutter IDE.
- The root project includes many AndroidIDE-specific modules and composite builds; removing them in one sweep would make the project difficult to build and debug.
- The upstream release/publishing workflow, Crowdin contributor workflow, and manual workflow-run deletion utility were removed from this migration branch. A single least-privilege FlutterIDE baseline workflow remains for migration-branch pushes and manual runs, avoiding duplicate push/PR builds. Legacy source modules are intentionally retained until dependency/call-site analysis identifies safe removals.
- The repository is reported by GitHub as roughly 967 MB. Git repository size is not the same as installed APK size or runtime RAM, so these must be measured separately.
- Current source has a Git implementation dependency (JGit), but a user-facing GitHub Actions run dashboard is a separate feature and must be implemented and tested.

## Delivery phases

### Phase 1 — safe baseline and product definition

- Keep `dev` and `main` untouched; work in `feature/flutter-ide-transition`.
- Define Flutter-first scope and migration checkpoints.
- Keep one fork-specific CI workflow that builds the existing host app and runs available unit tests without release publishing or signing secrets.
- Capture the first CI result before structural refactors.

### Phase 2 — Flutter project foundation

- Add Flutter project creation/opening and validation for `pubspec.yaml`.
- Add Dart syntax highlighting and editor support using a maintained, Android-compatible editor component.
- Integrate Dart analysis/formatting in a way that does not require launching a full Flutter Android build on every edit.
- Add focused tests for project detection, file operations and command-result parsing.

### Phase 3 — Git workflow

- Provide repository status, diff, stage/unstage, commit, branch, clone, pull and push.
- Use Android secure storage for credentials; never log access tokens.
- Require user confirmation for remote writes and handle authentication/network errors clearly.

### Phase 4 — GitHub Actions dashboard

- Connect a selected repository and list workflow runs for its default branch and current commit.
- Show queued/in-progress/success/failure/cancelled states, job/step summaries and logs.
- Refresh run status with bounded polling and lifecycle-aware cancellation.
- Download only artifacts actually uploaded by the workflow; handle expired artifacts and permission errors.
- Use least-privilege GitHub permissions and never embed a personal access token in the app or repository.

### Phase 5 — remote Flutter build template

- Provide an opt-in project workflow for Flutter dependency resolution, formatting/analyze, tests and Android APK build on a GitHub-hosted Linux runner.
- Pin action major versions and Flutter/Java versions; cache only safe, reproducible dependency directories.
- Upload APK artifacts and expose download links in the IDE.
- Keep project build workflows separate from the IDE application build workflow.

### Phase 6 — lightweight and release validation

- Measure release APK size, cold start, editor memory and large-file behavior on representative devices.
- Remove legacy modules only after dependency/call-site analysis and CI confirms they are no longer required.
- Validate arm32/arm64 compatibility explicitly; do not assume Flutter SDK or Android build tools run on every 32-bit device.
- Verify release signing and artifact integrity separately from debug CI.

## Acceptance criteria

- The home/project flow is Flutter/Dart-oriented rather than Android Gradle project-oriented.
- A sample Flutter project can be opened and Dart files edited without an on-device Android build.
- Git status/diff/commit/push/pull operations have tests and visible error handling.
- A pushed sample project produces a GitHub Actions run that the IDE can display and whose APK artifact can be downloaded.
- CI results are checked before a phase is called complete.
- No branch is reported protected unless GitHub confirms the protection rule; no CI pass is claimed without a real successful run.

## Current status

Phase 1 product definition is in place. Phase 2 has started: a filesystem-only Flutter project inspector and a home-screen inspection action are implemented with unit tests. A new filesystem-only Dart file service now lists Dart files in stable relative-path order, skips generated/build directories, reads and writes UTF-8 Dart files, and rejects paths that escape the Flutter project root or target non-Dart files. The home screen now has a Dart file browser that lists project `.dart` files and opens a scrollable, selectable source preview; file discovery and reads run off the main thread. Unit tests cover project validation, traversal rejection, read/write behavior and generated-file filtering. The first CI attempt exposed an unpublished Sora editor snapshot dependency; the published 0.23.6 release introduced Kotlin 2.1 metadata incompatible with this project's Kotlin 1.9.24 compiler, so the dependency is now pinned to published Sora Editor 0.23.4; CI must verify the downgrade. Dart editing, project creation, Dart analysis/formatting and the Actions dashboard are not implemented yet.