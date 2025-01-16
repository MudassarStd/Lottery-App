package com.example.lottery.admin

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.lottery.R
import com.example.lottery.data.FirebaseRepository
import com.example.lottery.data.SharedPreferencesHelper
import com.example.lottery.utils.Constants
import com.example.lottery.utils.ValidationUtils

class A_Login : AppCompatActivity() {
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var firebaseRepository: FirebaseRepository
    private lateinit var sharedPreferencesHelper: SharedPreferencesHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alogin)

        // Initialize UI components
        etEmail = findViewById(R.id.etAdminEmail)
        etPassword = findViewById(R.id.etAdminPassword)
        btnLogin = findViewById(R.id.btnAdminLogin)
        progressBar = findViewById(R.id.progressBar)

        // Hardcoded admin credentials
        etEmail.setText("admin@mail.com")  // Replace with the desired email
        etPassword.setText("admin")      // Replace with the desired password

        // Initialize helpers
        firebaseRepository = FirebaseRepository()
        sharedPreferencesHelper = SharedPreferencesHelper(this)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (!ValidationUtils.isValidEmail(email)) {
                Toast.makeText(this, "Invalid email address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                Toast.makeText(this, "Password cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            progressBar.visibility = ProgressBar.VISIBLE

            firebaseRepository.loginUser(email, password) { success, message ->
                progressBar.visibility = ProgressBar.GONE
                if (success) {
                    val userId = firebaseRepository.getCurrentUserId()
                    if (userId != null) {
                        sharedPreferencesHelper.saveUserId(userId)
                        sharedPreferencesHelper.saveUserRole(Constants.ROLE_ADMIN)
                        sharedPreferencesHelper.saveLoginState(true)

                        Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, A_Dashboard::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Error retrieving user ID", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Login Failed: $message", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
