package com.example.lottery

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.lottery.utils.Constants.USERS_PATH
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FCM : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCMTestingService", "New token: $token")
        // Send token to your server or save it locally
        saveTokenToFirestore(token)
    }
    // Notification Message Handler
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("FCMTestingService", "Message received: ${remoteMessage.data}")

        // Handle com.example.lottery.FCM notification messages
        remoteMessage.notification?.let {
            Log.d("FCMTestingService", "Notification Title: ${it.title}, Body: ${it.body}")
            showLocalNotification(it.title, it.body)
        }
    }

    private fun saveTokenToFirestore(token: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid // Replace with logic to get the logged-in user's ID
        val db = FirebaseFirestore.getInstance()

        val userRef = userId?.let { db.collection(USERS_PATH).document(it) } ?: return

        userRef.update("fcmToken", token)
            .addOnSuccessListener {
                Log.d("FCMTestingService", "Token successfully saved to Firestore.")
            }
            .addOnFailureListener { e ->
                Log.e("FCMTestingService", "Error saving token to Firestore", e)
            }
    }

    private fun showLocalNotification(title: String?, body: String?) {
        try {
            Log.d("FCMTestingService", "Attempting to show notification")

            val channelId = "default"
            val channelName = "Default Channel"
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                notificationManager.createNotificationChannel(channel)
            }

            val notification = NotificationCompat.Builder(this, channelId)
                .setContentTitle(title ?: "Default Title")
                .setContentText(body ?: "Default Body")
                .setSmallIcon(R.drawable.ic_help_support)
                .setAutoCancel(true)
                .build()

            notificationManager.notify(1, notification)

            Log.d("FCMTestingService", "Notification successfully shown")
        } catch (e: Exception) {
            Log.e("FCMTestingService", "Error showing notification", e)
        }
    }

}
