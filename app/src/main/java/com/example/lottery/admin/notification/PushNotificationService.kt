package com.example.lottery.admin.notification

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

object PushNotificationService {

    fun sendNotificationToToken(token: String, title: String, message: String, context: Context) {
        val notificationData = mapOf(
            "to" to token,
            "notification" to mapOf(
                "title" to title,
                "body" to message
            )
        )
        val requestBody = JSONObject(notificationData)

        val url = "https://fcm.googleapis.com/fcm/send"

        // Subclassing JsonObjectRequest to override getHeaders
        val jsonObjectRequest = object : JsonObjectRequest(
            Request.Method.POST, url, requestBody,
            Response.Listener { response ->
                Log.d("FCM", "Notification sent successfully: $response")
            },
            Response.ErrorListener { error ->
                Log.e("FCM", "Error sending notification: ${error.message}")
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                return mutableMapOf(
                    "Authorization" to "key=YOUR_SERVER_KEY", // Replace with your FCM server key
                    "Content-Type" to "application/json"
                )
            }
        }

        // Add the request to the Volley queue
        Volley.newRequestQueue(context).add(jsonObjectRequest)
    }
}
