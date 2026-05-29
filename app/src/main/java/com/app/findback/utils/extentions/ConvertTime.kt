package com.app.findback.utils.extentions

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object ConvertTime {

    fun formatTime(time: Long): String {

        if (time <= 0) return "Vừa xong"

        val now = System.currentTimeMillis()
        val diff = now - time

        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
        val hours = TimeUnit.MILLISECONDS.toHours(diff)
        val days = TimeUnit.MILLISECONDS.toDays(diff)

        return when {
            minutes < 1 -> "Vừa xong"
            minutes < 60 -> "$minutes phút trước"
            hours < 24 -> "$hours giờ trước"
            days < 7 -> "$days ngày trước"
            else -> SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                .format(Date(time))
        }
    }
}