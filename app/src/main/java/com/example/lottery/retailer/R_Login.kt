package com.example.lottery.retailer

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

class R_Login : AppCompatActivity() {
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvSignup: TextView

    private lateinit var firebaseRepository: FirebaseRepository
    private lateinit var sharedPreferencesHelper: SharedPreferencesHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rlogin)

        // Initialize helpers
        firebaseRepository = FirebaseRepository()
        sharedPreferencesHelper = SharedPreferencesHelper(this)

        // Initialize UI components
        etEmail = findViewById(R.id.et_retailer_email)
        etPassword = findViewById(R.id.et_retailer_password)
        btnLogin = findViewById(R.id.btn_retailer_login)
        tvSignup = findViewById(R.id.tv_retailer_signup)

        // Login button click listener
        btnLogin.setOnClickListener {
            loginRetailer()
        }

        // Signup navigation click listener
        tvSignup.setOnClickListener {
            navigateToSignup()
        }
    }

    private fun loginRetailer() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        // Validate input using ValidationUtils
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
                    sharedPreferencesHelper.saveUserRole(Constants.ROLE_RETAILER)
                    sharedPreferencesHelper.saveLoginState(true)

                    Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                    navigateToDashboard()
                } else {
                    Toast.makeText(this, "Error retrieving user ID", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Login failed: $message", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToSignup() {
        val intent = Intent(this, R_Signup::class.java)
        startActivity(intent)
    }

    private fun navigateToDashboard() {
        val intent = Intent(this, R_Dashboard::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }
}
