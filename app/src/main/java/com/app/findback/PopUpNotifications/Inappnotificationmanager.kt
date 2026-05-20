package com.app.findback.PopUpNotifications

import android.app.Activity
import android.os.Handler
import android.os.Looper
import com.app.findback.domain.models.Notification
import java.lang.ref.WeakReference

object InAppNotificationManager {

    private val handler = Handler(Looper.getMainLooper())
    private var currentBanner: WeakReference<InAppNotificationBanner>? = null

    fun show(
        activity: Activity,
        notification: Notification,
        onClick: (Notification) -> Unit = {}
    ) {

        handler.post {

            currentBanner?.get()?.dismiss()

            val banner = InAppNotificationBanner(activity, notification, onClick)
            currentBanner = WeakReference(banner)
            banner.show()
        }
    }

    fun dismiss() {
        handler.post {
            currentBanner?.get()?.dismiss()
        }
    }
}