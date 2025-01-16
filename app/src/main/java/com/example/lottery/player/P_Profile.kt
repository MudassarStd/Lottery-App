package com.example.lottery.player

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.lottery.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore

class P_Profile : AppCompatActivity() {
    private lateinit var tvCurrentName: TextView
    private lateinit var tvCurrentEmail: TextView
    private lateinit var tvCurrentContact: TextView
    private lateinit var etPlayerName: EditText
    private lateinit var etPlayerEmail: EditText
    private lateinit var etPlayerContact: EditText
    private lateinit var btnSaveProfile: Button

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var usersRef: CollectionReference  // Reference to the 'users' collection in Firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pprofile)

        // Initialize UI components
        tvCurrentName = findViewById(R.id.tvCurrentName)
        tvCurrentEmail = findViewById(R.id.tvCurrentEmail)
        tvCurrentContact = findViewById(R.id.tvCurrentContact)
        etPlayerName = findViewById(R.id.etPlayerName)
        etPlayerEmail = findViewById(R.id.etPlayerEmail)
        etPlayerContact = findViewById(R.id.etPlayerContact)
        btnSaveProfile = findViewById(R.id.btnSaveProfile)

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        usersRef = firestore.collection("users")  // Reference to the 'users' collection

        loadCurrentProfile()

        // Save profile changes when the button is clicked
        btnSaveProfile.setOnClickListener { saveProfileChanges() }
    }

    private fun loadCurrentProfile() {
        val playerId = firebaseAuth.currentUser?.uid ?: return  // Get current user's UID

        // Fetch user data from Firestore's 'users' collection using the UID as the document ID
        usersRef.document(playerId).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                if (document != null && document.exists()) {
                    val name = document.getString("name") ?: ""
                    val email = document.getString("email") ?: ""
                    val contact = document.getString("contact") ?: ""

                    // Display the data in the UI
                    tvCurrentName.text = "Name: $name"
                    tvCurrentEmail.text = "Email: $email"
                    tvCurrentContact.text = "Contact Number: $contact"

                    // Set the EditText fields with the current data
                    etPlayerName.setText(name)
                    etPlayerEmail.setText(email)
                    etPlayerContact.setText(contact)
                } else {
                    Toast.makeText(this, "No profile found", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveProfileChanges() {
        val playerId = firebaseAuth.currentUser?.uid ?: return  // Get current user's UID

        val updatedName = etPlayerName.text.toString()  // Get updated name from the EditText
        val updatedEmail = etPlayerEmail.text.toString()  // Get updated email from the EditText
        val updatedContact = etPlayerContact.text.toString()  // Get updated contact from the EditText

        if (updatedName.isBlank() || updatedEmail.isBlank() || updatedContact.isBlank()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
            return
        }

        // Prepare the updated data
        val updates = mapOf(
            "name" to updatedName,
            "email" to updatedEmail,
            "contact" to updatedContact
        )

        // Update the user data in Firestore's 'users' collection
        usersRef.document(playerId).update(updates).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                loadCurrentProfile()  // Reload the updated profile data
            } else {
                Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
