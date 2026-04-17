package com.app.findback.utils.extentions

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object ConvertTime {

    fun formatTime(time: String): String {
        val date = parseDate(time) ?: return ""

        val now = Date()
        val diff = now.time - date.time

        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
        val hours = TimeUnit.MILLISECONDS.toHours(diff)
        val days = TimeUnit.MILLISECONDS.toDays(diff)

        return when {
            minutes < 1 -> "Vừa xong"
            minutes < 60 -> "$minutes phút trước"
            hours < 24 -> "$hours giờ trước"
            days < 7 -> "$days ngày trước"
            else -> SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)
        }
    }

    private fun parseDate(time: String): Date? {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            format.parse(time)
        } catch (e: Exception) {
            null
        }
    }
}