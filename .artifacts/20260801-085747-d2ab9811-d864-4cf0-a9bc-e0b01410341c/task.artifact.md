# Tasks

- [x] Fix 'Activity not found' during Google Login
    - [x] Create `ActivityProvider` in `shared` module
    - [x] Register `ActivityProvider` in Koin `PlatformModule.android.kt`
    - [x] Register `ActivityProvider` in `MainApplication.kt`
    - [x] Update `AndroidGoogleAuthManager` to use `ActivityProvider`
    - [x] Cleanup `MainActivity.kt`
    - [x] Verify fix with a build and manual check
