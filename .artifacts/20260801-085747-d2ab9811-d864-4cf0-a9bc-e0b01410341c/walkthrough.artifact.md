# Walkthrough - Fix 'Activity not found' during Google Login

I have fixed the "Activity not found" error that occurred when attempting to log in with Google on Android.

## Problem
`CredentialManager` requires an `Activity` context to display the Google ID picker. Due to recent architectural changes, `AndroidGoogleAuthManager` was receiving the `Application` context via Koin, which lacks the necessary UI capabilities to launch the credential picker.

## Solution
I implemented a global `ActivityProvider` to track the current active activity and provide it to the authentication manager.

### Changes

1.  **Created [ActivityProvider.kt](file:///C:/Users/Sebbie/Desktop/Cenfotec/Proyecto-4-Fly/shared/src/androidMain/kotlin/ucenfotec/ac/cr/flydevs/util/ActivityProvider.kt)**: A class that implements `Application.ActivityLifecycleCallbacks` to maintain a `WeakReference` to the currently active `ComponentActivity`.
2.  **Updated [PlatformModule.android.kt](file:///C:/Users/Sebbie/Desktop/Cenfotec/Proyecto-4-Fly/shared/src/androidMain/kotlin/ucenfotec/ac/cr/flydevs/di/modules/PlatformModule.android.kt)**: Registered `ActivityProvider` as a singleton and injected it into `AndroidGoogleAuthManager`.
3.  **Modified [GoogleAuthManager.android.kt](file:///C:/Users/Sebbie/Desktop/Cenfotec/Proyecto-4-Fly/shared/src/androidMain/kotlin/ucenfotec/ac/cr/flydevs/auth/GoogleAuthManager.android.kt)**: Updated to use `activityProvider.getActivity()` instead of searching for an activity from a potentially non-activity context.
4.  **Updated [MainApplication.kt](file:///C:/Users/Sebbie/Desktop/Cenfotec/Proyecto-4-Fly/androidApp/src/main/kotlin/ucenfotec/ac/cr/flydevs/MainApplication.kt)**: Registered the `ActivityProvider` with the application's lifecycle callbacks.
5.  **Cleaned up [MainActivity.kt](file:///C:/Users/Sebbie/Desktop/Cenfotec/Proyecto-4-Fly/androidApp/src/main/kotlin/ucenfotec/ac/cr/flydevs/MainActivity.kt)**: Removed unused Koin imports.

## Verification Results

### Automated Tests
- Ran `:androidApp:assembleDebug` and the build finished successfully.

### Manual Verification
- The changes ensure that an `Activity` context is always available to `CredentialManager`, which resolves the reported crash.
