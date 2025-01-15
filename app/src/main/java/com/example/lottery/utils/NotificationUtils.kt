package com.example.lottery.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.lottery.R

object NotificationUtils {

    private const val CHANNEL_ID = "lottery_notifications"
    private const val CHANNEL_NAME = "Lottery Notifications"
    private const val CHANNEL_DESCRIPTION = "Notifications related to Lottery App events"

    /**
     * Creates a notification channel for Android Oreo and above.
     *
     * @param context The application context.
     */
    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESCRIPTION
            }
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    /**
     * Displays a local notification.
     *
     * @param context The application context.
     * @param title The notification title.
     * @param message The notification message.
     * @param notificationId A unique ID for the notification.
     */
    fun showNotification(context: Context, title: String, message: String, notificationId: Int) {
        createNotificationChannel(context)

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    /**
     * Sends a notification to specific users using Firebase.
     *
     * @param recipientId The ID of the recipient.
     * @param message The notification message.
     */
    fun sendFirebaseNotification(recipientId: String, message: String) {
        // This method would require server-side integration with Firebase Cloud Messaging (FCM).
        // Placeholder logic to show what needs to happen.
        // Ideally, you would make a network call to your backend to send the FCM notification.
    }
}