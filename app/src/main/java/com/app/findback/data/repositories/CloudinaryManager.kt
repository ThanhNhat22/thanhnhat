package com.app.findback.data.repositories

import android.content.Context
import com.cloudinary.android.MediaManager

object CloudinaryManager {

    private var initialized = false

    fun init(context: Context) {

        if (initialized) return

        val config = mapOf(

            "cloud_name" to "dzjfrupnh",

            "api_key" to "436473197357797",

            "api_secret" to "6o-7MXzpqWSBlH7SgP2D1kw2Ehs"

        )

        MediaManager.init(context, config)

        initialized = true
    }
}