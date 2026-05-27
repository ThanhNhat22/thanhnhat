// data/source/remote/FcmApiClient.kt
package com.app.findback.data.source.remote

import android.util.Log
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object FcmApiClient {

    // 👇 Paste server key của bạn vào đây
    private const val SERVER_KEY = "1069229068834"
    private const val FCM_URL = "https://fcm.googleapis.com/fcm/send"

    fun sendNotification(
        token: String,
        title: String,
        body: String,
        data: Map<String, String> = emptyMap(),
        onResult: (success: Boolean) -> Unit = {}
    ) {
        Thread {
            try {
                val url = URL(FCM_URL)
                val conn = url.openConnection() as HttpURLConnection

                conn.apply {
                    requestMethod = "POST"
                    setRequestProperty("Authorization", "key=$SERVER_KEY")
                    setRequestProperty("Content-Type", "application/json")
                    doOutput = true
                    connectTimeout = 10_000
                    readTimeout = 10_000
                }

                // Build JSON payload
                val dataJson = JSONObject()
                data.forEach { (k, v) -> dataJson.put(k, v) }

                val payload = JSONObject().apply {
                    put("to", token)
                    put("notification", JSONObject().apply {
                        put("title", title)
                        put("body", body)
                        put("sound", "default")
                        put("android_channel_id", "zone_alerts")
                    })
                    put("data", dataJson)
                    put("priority", "high")
                }

                // Gửi request
                OutputStreamWriter(conn.outputStream).use { writer ->
                    writer.write(payload.toString())
                    writer.flush()
                }

                val responseCode = conn.responseCode
                Log.d("FCM", "Response: $responseCode")
                onResult(responseCode == 200)

                conn.disconnect()

            } catch (e: Exception) {
                Log.e("FCM", "Send failed: ${e.message}")
                onResult(false)
            }
        }.start()
    }
}