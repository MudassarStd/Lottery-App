package com.example.lottery.player

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.lottery.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.UUID

class P_Help : AppCompatActivity() {
    private lateinit var tvFAQs: TextView
    private lateinit var etSupportMessage: EditText
    private lateinit var btnSendSupport: Button

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var supportRequestsRef: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_phelp)
        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance()
        supportRequestsRef = FirebaseDatabase.getInstance().getReference("supportRequests")

        // Initialize UI components
        tvFAQs = findViewById(R.id.tvFAQs)
        etSupportMessage = findViewById(R.id.etSupportMessage)
        btnSendSupport = findViewById(R.id.btnSendSupport)

        // Load FAQs
        loadFAQs()

        // Send Support Message
        btnSendSupport.setOnClickListener {
            sendSupportMessage()
        }
    }

    private fun loadFAQs() {
        // For now, hardcoded FAQs, can be fetched from Firebase if required
        val faqs = """
            1. How can I place a bet?
            2. How do I purchase coins?
            3. What happens if I win a game?
            For more questions, contact support.
        """.trimIndent()

        tvFAQs.text = faqs
    }

    private fun sendSupportMessage() {
        val message = etSupportMessage.text.toString().trim()

        if (message.isEmpty()) {
            Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = firebaseAuth.currentUser?.uid ?: return
        val supportRequestId = UUID.randomUUID().toString()

        val supportRequest = mapOf(
            "userId" to userId,
            "message" to message,
            "timestamp" to System.currentTimeMillis()
        )

        supportRequestsRef.child(supportRequestId).setValue(supportRequest)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Support request sent", Toast.LENGTH_SHORT).show()
                    etSupportMessage.text.clear()
                } else {
                    Toast.makeText(this, "Failed to send support request", Toast.LENGTH_SHORT).show()
                }
            }
    }
}