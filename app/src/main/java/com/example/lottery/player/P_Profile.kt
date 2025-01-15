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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class P_Profile : AppCompatActivity() {
    private lateinit var tvCurrentName: TextView
    private lateinit var tvCurrentEmail: TextView
    private lateinit var tvCurrentContact: TextView
    private lateinit var etPlayerName: EditText
    private lateinit var etPlayerEmail: EditText
    private lateinit var etPlayerContact: EditText
    private lateinit var btnSaveProfile: Button

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var playersRef: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pprofile)
        tvCurrentName = findViewById(R.id.tvCurrentName)
        tvCurrentEmail = findViewById(R.id.tvCurrentEmail)
        tvCurrentContact = findViewById(R.id.tvCurrentContact)
        etPlayerName = findViewById(R.id.etPlayerName)
        etPlayerEmail = findViewById(R.id.etPlayerEmail)
        etPlayerContact = findViewById(R.id.etPlayerContact)
        btnSaveProfile = findViewById(R.id.btnSaveProfile)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()
        playersRef = firebaseDatabase.getReference("players")

        loadCurrentProfile()

        btnSaveProfile.setOnClickListener { saveProfileChanges() }
    }

    private fun loadCurrentProfile() {
        val playerId = firebaseAuth.currentUser?.uid ?: return

        playersRef.child(playerId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val name = snapshot.child("name").value.toString()
                val email = snapshot.child("email").value.toString()
                val contact = snapshot.child("contact").value.toString()

                tvCurrentName.text = "Name: $name"
                tvCurrentEmail.text = "Email: $email"
                tvCurrentContact.text = "Contact Number: $contact"

                etPlayerName.setText(name)
                etPlayerEmail.setText(email)
                etPlayerContact.setText(contact)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@P_Profile, "Failed to load profile", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun saveProfileChanges() {
        val playerId = firebaseAuth.currentUser?.uid ?: return

        val updatedName = etPlayerName.text.toString()
        val updatedEmail = etPlayerEmail.text.toString()
        val updatedContact = etPlayerContact.text.toString()

        if (updatedName.isBlank() || updatedEmail.isBlank() || updatedContact.isBlank()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
            return
        }

        val updates = mapOf(
            "name" to updatedName,
            "email" to updatedEmail,
            "contact" to updatedContact
        )

        playersRef.child(playerId).updateChildren(updates).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                loadCurrentProfile()
            } else {
                Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show()
            }
        }
    }
}