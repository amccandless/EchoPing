# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

EchoPing is a single-module Android app (`:app`, package `com.booknerd.echoping`) for location-based reminders: the user picks a place, and a geofence fires a notification on enter, exit, or both. It's built with Kotlin, Jetpack Compose (Material 3), Room, Hilt, and Google Play Services Location. minSdk 24, target/compile SDK 35, JVM 11.

## Commands

Use `./gradlew` from Git Bash or `.\gradlew.bat` from PowerShell.

```
./gradlew assembleDebug                 # build debug APK
./gradlew installDebug                  # build + install on connected device/emulator
./gradlew testDebugUnitTest             # JVM unit tests (app/src/test)
./gradlew testDebugUnitTest --tests "com.booknerd.echoping.ReminderMapperTest"   # single class
./gradlew testDebugUnitTest --tests "com.booknerd.echoping.ReminderMapperTest.someMethod"  # single test
./gradlew connectedDebugAndroidTest     # instrumented tests (needs device)
./gradlew lint
```

## Architecture

Layers, from top to bottom:

- **UI (`ui/`, `controllers/ReminderApp.kt`)**: `MainActivity` shows `LocationPermissionScreen` until `hasAllPermissions()` passes. That check requires fine location, background location (API 29+), and POST_NOTIFICATIONS (API 33+). After that, it shows `ReminderApp`, which owns the `NavHost` with string routes `list`, `create`, `detail/{id}`, and `edit/{id}`. One `ReminderViewModel` (from `hiltViewModel()` at the NavHost level) is shared by all routes. Screens are stateless and take lambdas. Detail and edit screens look up their reminder from the in-memory `reminders` list, not the DB. Delete-with-undo is done in `ReminderApp` by re-adding the reminder from a snackbar action.
- **Domain (`domain/model/`)**: `Reminder` (the ID is a UUID string), `TriggerType`, and `Schedule` (repeat type, days, optional time window). `ScheduleRules.kt` holds the pure, unit-tested logic that decides whether a trigger at a given time should notify: only inside a schedule window, and at most once per window except for `ALWAYS`. It uses `java.time`, which relies on core library desugaring on minSdk 24. The UI and repo work with domain models only.
- **Data (`data/`, `mapper/`)**: `ReminderEntity` (Room table `reminders`) is mapped to and from `Reminder` through the extension functions in `ReminderMapper`. The schedule is stored as flat columns, with weekdays as a bitmask. `AppDatabase` uses `exportSchema = false`, real migrations (`MIGRATION_2_3`), and `fallbackToDestructiveMigration` for anything older. Any change to `ReminderEntity` **must** bump the `version` in `AppDatabase` and add a migration whose SQL matches Room's generated schema, including `DEFAULT` values, which you can check in the generated `AppDatabase_Impl`. Otherwise opening the DB crashes the app, including from the background geofence and boot receivers. `GeocodingService` wraps the Android `Geocoder`, using the async listener API on 33+ and the deprecated sync call below that.
- **Repository (`data/repository/ReminderRepo.kt`)**: This is the single place that keeps the DB and the geofences in sync. Every add, update, delete, or setActive writes to Room and then registers or removes the matching geofence. Any new mutation path should go through here so geofence state stays consistent.
- **Geofencing (`geofence/`)**: `GeofenceManager` uses the reminder ID as the geofence request ID and one shared broadcast `PendingIntent`, with `setInitialTrigger(0)` so a geofence doesn't fire if the user is already inside it. `GeofenceBroadcastReceiver` passes each triggering request ID to `ReminderRepo.handleTrigger`. That call applies the schedule, records `lastTriggeredAt`, and turns off `ONCE` reminders. The receiver calls `ReminderNotifier.show` only when `handleTrigger` returns a reminder. Geofences don't fire while the user stays inside, so a window that opens while they're already there won't notify until they leave and come back. `BootReceiver` re-registers all active reminders after a reboot. Receivers aren't Hilt-injected; they get `ReminderRepo` through the `GeofenceEntryPoint` Hilt entry point and do their work with `goAsync()` plus a coroutine on `Dispatchers.IO`.
- **DI (`di/DatabaseModule.kt`)**: This module provides the database and DAO. `ReminderRepo`, `GeofenceManager`, and `GeocodingService` are `@Singleton` classes injected through their constructors.

Dependencies are a mix of the version catalog (`gradle/libs.versions.toml`) and hard-coded coordinates in `app/build.gradle.kts` (Room, Hilt, lifecycle-compose). Room and Hilt use KSP.
