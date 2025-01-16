package com.example.lottery.player

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.lottery.R
import com.example.lottery.data.FirebaseRepository
import com.example.lottery.utils.ValidationUtils

class P_Signup : AppCompatActivity() {
    private lateinit var etPlayerName: EditText
    private lateinit var etPlayerEmail: EditText
    private lateinit var etPlayerPhone: EditText
    private lateinit var etPlayerPassword: EditText
    private lateinit var etPlayerConfirmPassword: EditText
    private lateinit var btnPlayerSignup: Button

    private lateinit var firebaseRepository: FirebaseRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_psignup)

        // Initialize Firebase Repository
        firebaseRepository = FirebaseRepository()

        // Initialize UI components
        etPlayerName = findViewById(R.id.etPlayerName)
        etPlayerEmail = findViewById(R.id.etPlayerEmail)
        etPlayerPhone = findViewById(R.id.etPlayerPhone)
        etPlayerPassword = findViewById(R.id.etPlayerPassword)
        etPlayerConfirmPassword = findViewById(R.id.etPlayerConfirmPassword)
        btnPlayerSignup = findViewById(R.id.btnPlayerSignup)

        // Handle Signup Button Click
        btnPlayerSignup.setOnClickListener {
            registerPlayer()
        }
    }

    private fun registerPlayer() {
        val name = etPlayerName.text.toString().trim()
        val email = etPlayerEmail.text.toString().trim()
        val phone = etPlayerPhone.text.toString().trim()
        val password = etPlayerPassword.text.toString().trim()
        val confirmPassword = etPlayerConfirmPassword.text.toString().trim()

        // Validate Inputs using ValidationUtils
        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (!ValidationUtils.isValidEmail(email)) {
            Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        // Register User with Firebase Repository
        firebaseRepository.signUpUser(email, password) { success, message ->
            if (success) {
                val userId = firebaseRepository.getCurrentUserId()
                if (userId != null) {
                    val userData = mapOf(
                        "name" to name,
                        "email" to email,
                        "phone" to phone,
                        "coins" to 0,
                        "role" to "player"
                    )

                    // Save User Data to Firebase Database
                    firebaseRepository.addUserDetails(userId, userData) { success, error ->
                        if (success) {
                            Toast.makeText(this, "Signup successful", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, P_Login::class.java))
                            finish()
                        } else {
                            Toast.makeText(this, "Failed to save user data: $error", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Error retrieving user ID", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Signup failed: $message", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
