package com.example.firebaseadminandroid

import android.content.Context
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage
import java.util.concurrent.Executors

object FCMSender {
    fun sendFCM(context: Context) {
        val executorService = Executors.newSingleThreadExecutor()

        executorService.execute {
            try {
                // Build RemoteMessage with Notification Data (for background notifications)
                val remoteMessage = RemoteMessage.Builder("cSTQYYr_TFOcXZCE10ZoKa:APA91bH2LRupJnSr-lFGACt8ZnraLsyT3VBh_JvruzwL9xOC5cun2Z_dGljsBaI7suOCDDouJql2dBW8vMkwRjX85xfzhSKC5DgAhDtBO67q0S-FywPQxiM")
                    .addData("title", "Test FCM Notification")
                    .addData("body", "Test FCM Message")
                    .build()

                // Send the message via FirebaseMessaging
                FirebaseMessaging.getInstance().send(remoteMessage)

                Log.d("TestFCMPushCommand", "Message sent successfully!")
            } catch (e: Exception) {
                Log.e("TestFCMPushCommand", "Error sending message: ", e)
            }
        }
        executorService.shutdown()
    }
}
