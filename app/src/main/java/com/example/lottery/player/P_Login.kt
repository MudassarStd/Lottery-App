package com.example.lottery.player

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.lottery.R
import com.example.lottery.data.FirebaseRepository
import com.example.lottery.data.SharedPreferencesHelper
import com.example.lottery.utils.Constants
import com.example.lottery.utils.ValidationUtils

class P_Login : AppCompatActivity() {
    private lateinit var etPlayerEmail: EditText
    private lateinit var etPlayerPassword: EditText
    private lateinit var btnPlayerLogin: Button
    private lateinit var tvSignUp: TextView

    private lateinit var firebaseRepository: FirebaseRepository
    private lateinit var sharedPreferencesHelper: SharedPreferencesHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_plogin)

        // Initialize helpers
        firebaseRepository = FirebaseRepository()
        sharedPreferencesHelper = SharedPreferencesHelper(this)

        // Initialize UI elements
        etPlayerEmail = findViewById(R.id.etPlayerEmail)
        etPlayerPassword = findViewById(R.id.etPlayerPassword)
        btnPlayerLogin = findViewById(R.id.btnPlayerLogin)
        tvSignUp = findViewById(R.id.tvSignUp)

        // Handle Login
        btnPlayerLogin.setOnClickListener {
            loginPlayer()
        }

        // Redirect to Signup
        tvSignUp.setOnClickListener {
            val intent = Intent(this, P_Signup::class.java)
            startActivity(intent)
        }
    }

    private fun loginPlayer() {
        val email = etPlayerEmail.text.toString().trim()
        val password = etPlayerPassword.text.toString().trim()

        // Validate email and password using ValidationUtils
        if (!ValidationUtils.isValidEmail(email)) {
            Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show()
            return
        }
        if (password.isEmpty()) {
            Toast.makeText(this, "Password cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        // Use FirebaseRepository to handle login
        firebaseRepository.loginUser(email, password) { success, message ->
            if (success) {
                val userId = firebaseRepository.getCurrentUserId()
                if (userId != null) {
                    // Save session details using SharedPreferencesHelper
                    sharedPreferencesHelper.saveUserId(userId)
                    sharedPreferencesHelper.saveUserRole(Constants.ROLE_PLAYER)
                    sharedPreferencesHelper.saveLoginState(true)

                    Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, P_Dashboard::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "Error retrieving user ID", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Login failed: $message", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
