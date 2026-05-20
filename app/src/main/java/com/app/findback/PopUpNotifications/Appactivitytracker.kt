package com.app.findback.PopUpNotifications

import android.app.Activity
import android.app.Application
import android.os.Bundle
import java.lang.ref.WeakReference

object AppActivityTracker : Application.ActivityLifecycleCallbacks {

    private var currentActivity: WeakReference<Activity>? = null


    val activeActivity: Activity?
        get() = currentActivity?.get()?.takeIf { !it.isFinishing && !it.isDestroyed }

    override fun onActivityResumed(activity: Activity) {
        currentActivity = WeakReference(activity)
    }

    override fun onActivityPaused(activity: Activity) {
        if (currentActivity?.get() === activity) {
            currentActivity = null
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}
}