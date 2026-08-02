# Implementation Plan - Fix 'Activity not found' during Google Login

The "Activity not found" error during Google Login is caused by `AndroidGoogleAuthManager` receiving an `Application` context from Koin instead of an `Activity` context. `CredentialManager` requires an `Activity` context to show the Google ID picker. This issue arose after `MainActivity` and Koin initialization were moved.

## User Review Required

> [!IMPORTANT]
> The solution involves tracking the current `Activity` globally within the Android app using `ActivityLifecycleCallbacks`. This is a standard pattern in KMP to bridge the gap between platform-independent ViewModels and platform-specific UI requirements.

## Proposed Changes

### Shared Module (Android Main)

---

#### [NEW] [ActivityProvider.kt](file:///C:/Users/Sebbie/Desktop/Cenfotec/Proyecto-4-Fly/shared/src/androidMain/kotlin/ucenfotec/ac/cr/flydevs/util/ActivityProvider.kt)

- Create a class that implements `Application.ActivityLifecycleCallbacks` to track the currently active `ComponentActivity`.
- Uses a `WeakReference` to avoid memory leaks.

```kotlin
package ucenfotec.ac.cr.flydevs.util

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import java.lang.ref.WeakReference

class ActivityProvider : Application.ActivityLifecycleCallbacks {
    private var currentActivity: WeakReference<ComponentActivity>? = null

    fun getActivity(): ComponentActivity? = currentActivity?.get()

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        if (activity is ComponentActivity) currentActivity = WeakReference(activity)
    }

    override fun onActivityStarted(activity: Activity) {
        if (activity is ComponentActivity) currentActivity = WeakReference(activity)
    }

    override fun onActivityResumed(activity: Activity) {
        if (activity is ComponentActivity) currentActivity = WeakReference(activity)
    }

    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {
        if (currentActivity?.get() == activity) currentActivity = null
    }
}
```

---

#### [PlatformModule.android.kt](file:///C:/Users/Sebbie/Desktop/Cenfotec/Proyecto-4-Fly/shared/src/androidMain/kotlin/ucenfotec/ac/cr/flydevs/di/modules/PlatformModule.android.kt)

- Register `ActivityProvider` as a singleton.
- Update `AndroidGoogleAuthManager` injection to include the `ActivityProvider`.

```diff
-    single<GoogleAuthManager> {
-        AndroidGoogleAuthManager(
-            context = get(),
-            serverClientId = "..."
-        )
-    }
+    single { ucenfotec.ac.cr.flydevs.util.ActivityProvider() }
+    single<GoogleAuthManager> {
+        AndroidGoogleAuthManager(
+            context = get(),
+            activityProvider = get(),
+            serverClientId = "502216327523-pu8ebco1p7gm4sktb4m46ovj5sufdpfb.apps.googleusercontent.com"
+        )
+    }
```

---

#### [GoogleAuthManager.android.kt](file:///C:/Users/Sebbie/Desktop/Cenfotec/Proyecto-4-Fly/shared/src/androidMain/kotlin/ucenfotec/ac/cr/flydevs/auth/GoogleAuthManager.android.kt)

- Update constructor to receive `ActivityProvider`.
- Use `activityProvider.getActivity()` to get the context for `credentialManager.getCredential`.
- Remove the internal `findActivity` helper as it's no longer reliable with `Application` context.

### Android App Module

---

#### [MainApplication.kt](file:///C:/Users/Sebbie/Desktop/Cenfotec/Proyecto-4-Fly/androidApp/src/main/kotlin/ucenfotec/ac/cr/flydevs/MainApplication.kt)

- Retrieve `ActivityProvider` from Koin and register it with `registerActivityLifecycleCallbacks`.

```diff
+        val activityProvider: ucenfotec.ac.cr.flydevs.util.ActivityProvider = getKoin().get()
+        registerActivityLifecycleCallbacks(activityProvider)
```

---

#### [MainActivity.kt](file:///C:/Users/Sebbie/Desktop/Cenfotec/Proyecto-4-Fly/androidApp/src/main/kotlin/ucenfotec/ac/cr/flydevs/MainActivity.kt)

- Remove unused `import org.koin.android.ext.koin.androidContext`.

## Verification Plan

### Automated Tests
- I will run a build to ensure no compilation errors: `./gradlew :androidApp:assembleDebug`

### Manual Verification
- Deploy the app to an Android device/emulator.
- Go to the Login screen.
- Tap the "Continue with Google" button.
- Verify that the Google ID selection UI appears (instead of throwing an "Activity not found" exception).
