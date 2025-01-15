package com.example.lottery.admin

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.lottery.R
import com.example.lottery.utils.NotificationUtils
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class A_Notifications : AppCompatActivity() {
    private lateinit var etNotificationMessage: EditText
    private lateinit var spinnerAudience: Spinner
    private lateinit var btnSendNotification: Button

    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var notificationsRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_anotifications)
        etNotificationMessage = findViewById(R.id.etNotificationMessage)
        spinnerAudience = findViewById(R.id.spinnerAudience)
        btnSendNotification = findViewById(R.id.btnSendNotification)

        firebaseDatabase = FirebaseDatabase.getInstance()
        notificationsRef = firebaseDatabase.getReference("notifications")

        setupAudienceSpinner()

        btnSendNotification.setOnClickListener {
            sendNotification()
        }
    }

    private fun setupAudienceSpinner() {
        val audiences = listOf("All Players", "All Retailers", "Specific Player", "Specific Retailer")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, audiences)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerAudience.adapter = adapter
    }

    private fun sendNotification() {
        val message = etNotificationMessage.text.toString().trim()
        val audience = spinnerAudience.selectedItem.toString()

        if (message.isEmpty()) {
            Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show()
            return
        }

        val notificationData = mapOf(
            "message" to message,
            "audience" to audience,
            "timestamp" to System.currentTimeMillis()
        )

        notificationsRef.push().setValue(notificationData).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Notification sent successfully", Toast.LENGTH_SHORT).show()

                // Show local notification
                NotificationUtils.showNotification(
                    this,
                    "New Notification",
                    message,
                    (System.currentTimeMillis() % 10000).toInt() // Unique notification ID
                )

                etNotificationMessage.text.clear()
            } else {
                Toast.makeText(this, "Failed to send notification", Toast.LENGTH_SHORT).show()
            }
        }
    }
}