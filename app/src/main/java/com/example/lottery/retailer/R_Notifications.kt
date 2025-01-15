package com.example.lottery.retailer

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.lottery.R
import com.example.lottery.utils.NotificationUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class R_Notifications : AppCompatActivity() {
    private lateinit var lvNotifications: ListView
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var notificationsRef: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_rnotifications)

        lvNotifications = findViewById(R.id.lvNotifications)
        firebaseDatabase = FirebaseDatabase.getInstance()
        notificationsRef = firebaseDatabase.getReference("notifications")
        auth = FirebaseAuth.getInstance()

        loadNotifications()
    }

    private fun loadNotifications() {
        val userId = auth.currentUser?.uid ?: return

        notificationsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val notificationsList = mutableListOf<String>()

                for (notification in snapshot.children) {
                    val audience = notification.child("audience").value.toString()
                    val message = notification.child("message").value.toString()

                    // Add notifications meant for all or for this retailer
                    if (audience == "All Retailers" || audience == "All Users") {
                        notificationsList.add(message)

                        // Show local notification for each new message received
                        NotificationUtils.showNotification(
                            this@R_Notifications,
                            "New Notification",
                            message,
                            (System.currentTimeMillis() % 10000).toInt() // Unique notification ID
                        )
                    }
                }

                val adapter = ArrayAdapter(this@R_Notifications, android.R.layout.simple_list_item_1, notificationsList)
                lvNotifications.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@R_Notifications, "Failed to load notifications", Toast.LENGTH_SHORT).show()
            }
        })
    }
}