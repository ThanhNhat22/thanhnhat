package com.app.findback

import android.app.Application
import com.app.findback.data.repositories.CloudinaryManager
import com.onesignal.OneSignal

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        CloudinaryManager.init(this)

        OneSignal.initWithContext(this, "bdf5516d-cd73-4dd2-b189-c429f8311bd5")

    }
}