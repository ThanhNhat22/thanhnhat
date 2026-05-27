package com.app.findback.domain.usecase

import com.app.findback.data.source.remote.FirebaseCircleZoneDataSource
import com.app.findback.domain.models.CircleZone
import com.app.findback.domain.models.Post
import com.google.firebase.database.FirebaseDatabase
import kotlin.math.*
import android.util.Log
import com.app.findback.data.source.remote.FcmApiClient

object CircleZoneNotificationChecker {

    private val circleZoneDataSource = FirebaseCircleZoneDataSource()

    // =========================================================
    // ENTRY POINT
    // =========================================================

    fun checkAndNotify(post: Post) {
        // Bỏ qua nếu post không có tọa độ hợp lệ
        if (post.latitude == 0.0 && post.longitude == 0.0) return

        circleZoneDataSource.getAllCircleZones { allZones ->
            allZones.forEach { zone ->
                // Không gửi cho chính người đăng bài
                if (zone.userId == post.userId) return@forEach

                if (isInsideZone(post.latitude, post.longitude, zone)) {
                    notifyUser(
                        targetUserId = zone.userId,
                        post = post,
                        zone = zone
                    )
                }
            }
        }
    }

    // =========================================================
    // HAVERSINE — khoảng cách 2 tọa độ (mét)
    // =========================================================

    private fun isInsideZone(
        postLat: Double,
        postLng: Double,
        zone: CircleZone
    ): Boolean {
        val distance = haversineMeters(
            postLat, postLng,
            zone.centerLat,   // đúng field
            zone.centerLon    // đúng field
        )
        return distance <= zone.radius
    }

    private fun haversineMeters(
        lat1: Double, lng1: Double,
        lat2: Double, lng2: Double
    ): Double {
        val R = 6_371_000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) *
                cos(Math.toRadians(lat2)) *
                sin(dLng / 2).pow(2)
        return R * 2 * atan2(sqrt(a), sqrt(1 - a))
    }

    // =========================================================
    // FCM — lấy token → gọi Cloud Function
    // =========================================================

    private fun notifyUser(
        targetUserId: String,
        post: Post,
        zone: CircleZone
    ) {
        FirebaseDatabase.getInstance().reference
            .child("users")
            .child(targetUserId)
            .child("fcmToken")
            .get()
            .addOnSuccessListener { snap ->
                val token = snap.getValue(String::class.java)
                    ?: return@addOnSuccessListener

                // 👇 Gọi thẳng FCM, không cần Cloud Function
                FcmApiClient.sendNotification(
                    token    = token,
                    title    = buildTitle(post),
                    body     = buildBody(post, zone),
                    data     = mapOf(
                        "postId"   to post.postId,
                        "postType" to post.postType
                    ),
                    onResult = { success ->
                        Log.d(
                            "ZoneChecker",
                            if (success) "✅ Đã gửi thông báo đến $targetUserId"
                            else         "❌ Gửi thất bại đến $targetUserId"
                        )
                    }
                )
            }
    }    // =========================================================
    // MESSAGE BUILDER
    // =========================================================

    private fun buildTitle(post: Post) = when (post.postType) {
        "lost"  -> "🔴 Có bài đăng mất đồ gần vùng của bạn!"
        "found" -> "🟢 Có bài đăng nhặt được đồ gần vùng của bạn!"
        else    -> "📍 Có bài đăng mới gần vùng của bạn!"
    }

    private fun buildBody(post: Post, zone: CircleZone) =
        "\"${post.title}\" tại ${post.locationText} — trong vùng \"${zone.name}\""
}