# FlutterIDE

**FlutterIDE is being rebuilt as a lightweight, Flutter-first IDE for Android.**

The goal is to make Dart/Flutter development practical on low-memory Android devices by keeping editing and Git workflows on-device while moving resource-intensive builds to GitHub Actions.

> **Current status:** This repository is in the transition phase from the archived AndroidIDE codebase. The existing application is not yet a completed Flutter IDE. Track the migration in [the FlutterIDE roadmap](docs/FLUTTER_IDE_ROADMAP.md).

## Product direction

- **Flutter/Dart-first:** Dart editing, syntax highlighting, diagnostics, formatting, project navigation and Flutter project creation.
- **Lightweight on-device workflow:** avoid requiring a full local Android/Flutter build toolchain for the normal edit-and-build loop.
- **Git integration:** clone/open repositories, inspect changes, commit, push and pull.
- **GitHub Actions dashboard:** show workflow status and logs, and download build artifacts such as APKs produced by the user project workflow.
- **Security:** least-privilege GitHub access, no tokens in logs, and explicit user control over pushes and workflow dispatches.

## Low-RAM build strategy

The IDE should not try to compile every Flutter project on the phone by default. A project GitHub repository can contain a workflow that runs flutter pub get, analysis, tests and an Android APK build on a GitHub-hosted runner. FlutterIDE will then display the run status and provide artifact downloads.

GitHub Actions usage depends on repository settings, runner availability and GitHub account limits. Artifact availability depends on the project workflow uploading an artifact.

## Development and CI

The existing Android application still uses Gradle while the migration is underway. The first milestone is to establish a reproducible baseline in GitHub Actions before removing legacy modules or replacing application screens.

- Main development branch: dev
- Migration branch: feature/flutter-ide-transition
- Roadmap and architecture decisions: [docs/FLUTTER_IDE_ROADMAP.md](docs/FLUTTER_IDE_ROADMAP.md)

Do not treat the current source tree or current APK as the finished FlutterIDE product. Features will be marked complete only after implementation and tests are verified.

## License

This repository retains the upstream project GPL-3.0-or-later license and notices. See [LICENSE](LICENSE).
