package com.app.findback

import android.app.Application
import com.app.findback.data.repositories.CloudinaryManager

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        CloudinaryManager.init(this)
    }
}