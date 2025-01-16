package com.example.lottery.retailer

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.lottery.R
import com.example.lottery.data.FirebaseRepository
import com.example.lottery.data.SharedPreferencesHelper
import com.example.lottery.player.P_PlaceBet
import com.example.lottery.utils.ValidationUtils

class R_play_login : AppCompatActivity() {
    private lateinit var etName: EditText
    private lateinit var etPhone: EditText
    private lateinit var btnLogin: Button

    private lateinit var firebaseRepository: FirebaseRepository
    private lateinit var sharedPreferencesHelper: SharedPreferencesHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rplay_login)

        // Initialize helpers
        firebaseRepository = FirebaseRepository()
        sharedPreferencesHelper = SharedPreferencesHelper(this)

        // Initialize UI components
        etName = findViewById(R.id.edtUsername)
        etPhone = findViewById(R.id.edtPhoneNumber)
        btnLogin = findViewById(R.id.btnLogin)

        // Login button click listener
        btnLogin.setOnClickListener {
            loginByNameAndPhone()
        }
    }

    private fun loginByNameAndPhone() {
        val name = etName.text.toString().trim()
        val phone = etPhone.text.toString().trim()

        // Validate input
        if (name.isEmpty()) {
            Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }
        if (!ValidationUtils.isValidPhone(phone)) {
            Toast.makeText(this, "Invalid phone number format", Toast.LENGTH_SHORT).show()
            return
        }

        // Query user by name and phone
        firebaseRepository.queryUserByNameAndPhone(name, phone) { success, userId ->
            if (success) {
                // Save session details using SharedPreferencesHelper
                sharedPreferencesHelper.saveUserId(userId!!)
                sharedPreferencesHelper.saveUserRole("retailer") // Adjust role if necessary
                sharedPreferencesHelper.saveLoginState(true)

                Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                navigateToDashboard()
            } else {
                Toast.makeText(this, "Login failed: $userId", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToDashboard() {
        val intent = Intent(this, P_PlaceBet::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }
}
