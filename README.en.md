# Taggo

English | [简体中文](README.md)

Taggo is a local-first cross-platform file manager. It uses tags, thumbnails, search, and usage history to help users find files stored across their local devices.

![Taggo Home Preview](docs/screenshots/home.png)

## Overview

Traditional folders do not always represent files that belong to several contexts, such as study, work, design, or reference material. Taggo keeps original files in place and maintains a local layer of tags and usage information.

This is a personal learning and portfolio project. It is still under development and is not a finished release.

## Current Features

- Add and manage local file references
- View thumbnails and basic file information
- Add, remove, and browse tags
- Search files by keywords and tags
- View recently added and recently opened files
- Generate simple recommendations from local usage history
- Adapt file handling and UI for Android, Web, and other targets

## Screenshots

| Home | File Detail |
|---|---|
| ![Home](docs/screenshots/home.png) | ![File Detail](docs/screenshots/detail.png) |

| Search | Android |
|---|---|
| ![Search](docs/screenshots/search.png) | ![Android](docs/screenshots/android.png) |

## Tech Stack

- Kotlin Multiplatform
- Compose Multiplatform
- SQLDelight
- Kotlin Coroutines
- Kotlin Serialization

## Current Focus

- Android file picking, opening, and permission handling
- Rebuilding the local data layer with SQLDelight
- Image and video thumbnails
- Keyword and tag search
- Local recommendation logic based on opening intervals and file transitions

## Engineering Work

- Organizing shared models, repositories, and business rules in `commonMain`
- Isolating Android, JVM, and Web platform capabilities
- Testing SQLDelight repositories with in-memory SQLite
- Covering core search and recommendation behavior with unit tests
- Gradually replacing the previous JSON snapshot path with a structured local database

## Run the Project

Build the Android debug application:

```powershell
.\gradlew.bat :composeApp:assembleDebug
```

Run the desktop application:

```powershell
.\gradlew.bat :composeApp:run
```

Run the Web development application:

```powershell
.\gradlew.bat :composeApp:wasmJsBrowserDevelopmentRun
```