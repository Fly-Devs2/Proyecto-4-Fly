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
