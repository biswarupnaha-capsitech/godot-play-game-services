# Godot Play Games Plugin – In-App Updates Module

---

> 
> 
> 
> **Target:** Godot 4+
> 
> **Language:** Kotlin, GdScript
> 
> **Android API:** Google Play Core In-App Updates
> 
> **Maintainer:** Biswarup Naha
> 

---

# Overview

The goal of this module is to extend the existing **Godot Play Games Services Plugin** with support for **Google Play In-App Updates** using Google's official Play Core API.

Instead of creating a standalone plugin, the update functionality is integrated into the existing Play Games plugin so users only need a single Android plugin for all Google Play related services.

---

# Why this module?

Google Play Games Services already provides:

- Sign In
- Achievements
- Leaderboards
- Saved Games
- Events

However, it does **not** include support for:

- Flexible Updates
- Immediate Updates
- Update Progress
- Update Installation
- Resume Interrupted Updates

This module fills that gap.

---

# Plugin Architecture

```
Godot
      │
      ▼
GodotAndroidPlugin
      │
      ├── SignInProxy
      ├── AchievementsProxy
      ├── LeaderboardsProxy
      ├── PlayersProxy
      ├── SnapshotsProxy
      ├── EventsProxy
      └── InAppUpdateProxy   ← New
```

The architecture follows the same Proxy pattern already used throughout the plugin.

Each feature is responsible for its own Google API implementation.

---

# Folder Structure

```
plugin
└── src/main/java/com/jacobibanez/plugin/android/godotplaygameservices

        achievements/
        events/
        games/
        leaderboards/
        players/
        signin/
        snapshots/

        updates/
            InAppUpdateProxy.kt

        signals/
            UpdateSignals.kt

        GodotAndroidPlugin.kt
```

No existing package structure is modified.

---

# Dependencies

Added to `plugin/build.gradle.kts`

```kotlin
implementation("com.google.android.play:app-update:2.1.0")
implementation("com.google.android.play:app-update-ktx:2.1.0")
```

These are Google's official Play Core libraries.

---

# Design Philosophy

The plugin follows the same design principles as the rest of the repository.

- One feature = One Proxy
- No business logic inside `GodotAndroidPlugin`
- All Google API interactions remain inside the proxy
- Communication with Godot is performed through signals
- No blocking calls
- Everything is asynchronous

---

# Why a Proxy?

Instead of writing update logic inside

```
GodotAndroidPlugin.kt
```

all update functionality lives in

```
InAppUpdateProxy.kt
```

Responsibilities include:

- Creating `AppUpdateManager`
- Checking for updates
- Starting Immediate Updates
- Starting Flexible Updates
- Registering install listeners
- Completing updates
- Resuming interrupted updates

---

# AppUpdateManager

The central Play Core object is

```kotlin
AppUpdateManager
```

It is lazily initialized.

```kotlin
private val appUpdateManager by lazy {
    AppUpdateManagerFactory.create(activity)
}
```

Using `lazy` prevents lifecycle problems during plugin initialization.

---

# Why Lazy Initialization?

When the plugin is constructed,

```
GodotAndroidPlugin

↓

SignInProxy

↓

AchievementsProxy

↓

InAppUpdateProxy
```

the Activity may not yet be completely initialized.

Creating `AppUpdateManager` only when it is first used avoids lifecycle issues.

---

# Signals

Unlike synchronous APIs, Play Core uses asynchronous Tasks.

Therefore, the plugin communicates through Godot signals.

Current signals:

```
update_checked

update_check_failed

update_started

update_download_progress

update_download_completed

update_install_completed

update_cancelled

update_failed
```

---

# Why One update_checked Signal?

Instead of

```
update_available

no_update_available
```

the plugin emits

```
update_checked(...)
```

This provides a single callback for every update check.

Example:

```
available = false
```

means

"No update available."

```
available = true
```

means

"An update exists."

This simplifies the GDScript API.

---

# Current Flow

```
Godot

↓

checkForUpdate()

↓

AppUpdateManager.appUpdateInfo

↓

Task<AppUpdateInfo>

↓

update_checked(...)
```

---

# Current API

```
checkForUpdate()
```

Implemented.

Future methods:

```
startImmediateUpdate()

startFlexibleUpdate()

completeUpdate()

resumeUpdateIfNeeded()

startBestUpdate()
```

---

# Why Everything Is Asynchronous

Google Play returns

```
Task<AppUpdateInfo>
```

instead of

```
AppUpdateInfo
```

Therefore the plugin cannot return update information immediately.

Signals are the correct solution.

---

# Current Update Information

During a successful update check the plugin gathers

```
available

versionCode

priority

stalenessDays

immediateAllowed

flexibleAllowed
```

These are emitted to Godot.

---

# Consent-Based Updates

Example flow:

```
Game Starts

↓

checkForUpdate()

↓

Update Available

↓

Popup

↓

Player presses Update

↓

startFlexibleUpdate()
```

The player chooses whether to update.

---

# Forced Updates

Forced updates are **not decided by Play Core**.

The game decides.

Example:

```
checkForUpdate()

↓

priority >= 5

↓

startImmediateUpdate()
```

or

```
Remote Config

↓

minimumVersion > currentVersion

↓

startImmediateUpdate()
```

The plugin simply exposes update information.

---

# Plugin Philosophy

The plugin should remain generic.

It should never contain logic such as

```
if(priority >= 4)
```

or

```
forceUpdate = true
```

Instead, it exposes data and lets the game decide.

---

# Recommended Remote Config Architecture

```
Firebase Remote Config

↓

minimum_supported_version

↓

Game compares versions

↓

Force update if required
```

Advantages

- No new APK required
- Rollback possible
- Easy testing
- Dynamic policy

---

# Future Architecture

```
Godot

↓

checkForUpdate()

↓

update_checked()

↓

User chooses

↓

startFlexibleUpdate()

or

↓

startImmediateUpdate()

↓

Play Store UI

↓

InstallStateUpdatedListener

↓

Signals

↓

completeUpdate()
```

---

# Future Components

## InAppUpdateProxy

Responsible for

- Play Core
- Update Manager
- Activity Result Launcher
- Install State Listener

---

## UpdateSignals

Defines all Godot signals.

---

## InstallStateUpdatedListener

Will monitor

```
DOWNLOADING

DOWNLOADED

INSTALLING

INSTALLED

FAILED

CANCELLED
```

---

## Activity Result Launcher

Will replace the deprecated

```
startUpdateFlowForResult()
```

implementation.

Uses

```
ActivityResultLauncher<IntentSenderRequest>
```

recommended by Google.

---

# Planned Public API

```
checkForUpdate()

startImmediateUpdate()

startFlexibleUpdate()

startBestUpdate()

completeUpdate()

resumeUpdateIfNeeded()
```

---

# Why startBestUpdate()?

Instead of forcing users to decide

```
Immediate?

Flexible?
```

the plugin can automatically select

```
Immediate if allowed

↓

Otherwise Flexible

↓

Otherwise emit failure
```

The game still decides **when** to call it.

---

# Current Progress:-

## Completed

- Plugin architecture decided
- Dependency setup
- Folder structure
- Proxy architecture
- Signal architecture
- `InAppUpdateProxy` created
- `AppUpdateManager` initialization
- `checkForUpdate()` implementation
- `UpdateSignals` design

---

## In Progress

- Activity Result Launcher
- Immediate Updates
- Flexible Updates

---

## Planned

- InstallStateUpdatedListener
- Download progress
- `completeUpdate()`
- Resume interrupted updates
- Demo scene
- Documentation
- Unit tests
- Packaging for release