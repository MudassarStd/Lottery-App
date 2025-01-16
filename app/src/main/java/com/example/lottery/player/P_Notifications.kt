package com.example.lottery.player

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.example.lottery.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class P_Notifications : AppCompatActivity() {
    private lateinit var lvNotifications: ListView
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var notificationsRef: DatabaseReference
    private lateinit var notificationManager: NotificationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pnotifications)

        lvNotifications = findViewById(R.id.lvNotifications)

        // Firebase Initialization
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()
        notificationsRef = firebaseDatabase.getReference("notifications")

        // Notification Manager
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()

        loadNotifications()
    }

    private fun loadNotifications() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        notificationsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val notificationsList = mutableListOf<String>()

                for (notification in snapshot.children) {
                    val message = notification.child("message").value.toString()
                    val audience = notification.child("audience").value.toString()

                    // Filter notifications based on audience (Player-specific or All Players)
                    if (audience == "All Players" || audience == currentUser.uid) {
                        notificationsList.add(message)
                        sendNotification(message) // Trigger a system notification
                    }
                }

                val adapter = ArrayAdapter(
                    this@P_Notifications,
                    android.R.layout.simple_list_item_1,
                    notificationsList
                )
                lvNotifications.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@P_Notifications, "Failed to load notifications", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun sendNotification(message: String) {
        val intent = Intent(this, P_Notifications::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE // Use FLAG_IMMUTABLE unless you need a mutable PendingIntent
        )

        val notification = NotificationCompat.Builder(this, "notifications_channel")
            .setSmallIcon(R.drawable.ic_notification) // Replace with your app's notification icon
            .setContentTitle("New Notification")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "notifications_channel",
                "Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for player notifications"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
}
