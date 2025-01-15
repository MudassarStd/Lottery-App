package com.example.lottery.player

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.lottery.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class P_Notifications : AppCompatActivity() {
    private lateinit var lvNotifications: ListView
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var notificationsRef: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pnotifications)
        lvNotifications = findViewById(R.id.lvNotifications)

        // Firebase Initialization
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()
        notificationsRef = firebaseDatabase.getReference("notifications")

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
}