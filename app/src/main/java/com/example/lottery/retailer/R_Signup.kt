package com.example.lottery.retailer

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.lottery.R
import com.example.lottery.data.FirebaseRepository
import com.example.lottery.utils.ValidationUtils

class R_Signup : AppCompatActivity() {

    private lateinit var etRetailerName: EditText
    private lateinit var etRetailerEmail: EditText
    private lateinit var etRetailerPhone: EditText
    private lateinit var etRetailerPassword: EditText
    private lateinit var etRetailerConfirmPassword: EditText
    private lateinit var etRetailerBusinessName: EditText
    private lateinit var etRetailerAddress: EditText
    private lateinit var btnRetailerSignup: Button

    private lateinit var firebaseRepository: FirebaseRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rsignup)

        // Initialize FirebaseRepository
        firebaseRepository = FirebaseRepository()

        // Initialize UI components
        etRetailerName = findViewById(R.id.etRetailerName)
        etRetailerEmail = findViewById(R.id.etRetailerEmail)
        etRetailerPhone = findViewById(R.id.etRetailerPhone)
        etRetailerPassword = findViewById(R.id.etRetailerPassword)
        etRetailerConfirmPassword = findViewById(R.id.etRetailerConfirmPassword)
        etRetailerBusinessName = findViewById(R.id.etRetailerBusinessName)
        etRetailerAddress = findViewById(R.id.etRetailerAddress)
        btnRetailerSignup = findViewById(R.id.btnRetailerSignup)

        // Set up the sign up button
        btnRetailerSignup.setOnClickListener {
            if (validateInputs()) {
                registerRetailer()
            }
        }
    }

    private fun validateInputs(): Boolean {
        val name = etRetailerName.text.toString().trim()
        val email = etRetailerEmail.text.toString().trim()
        val phone = etRetailerPhone.text.toString().trim()
        val password = etRetailerPassword.text.toString().trim()
        val confirmPassword = etRetailerConfirmPassword.text.toString().trim()
        val businessName = etRetailerBusinessName.text.toString().trim()
        val address = etRetailerAddress.text.toString().trim()

        // Check if any field is empty
        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty() ||
            confirmPassword.isEmpty() || businessName.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return false
        }

        // Validate email format
        if (!ValidationUtils.isValidEmail(email)) {
            Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show()
            return false
        }

        // Check password length
        if (password.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters long", Toast.LENGTH_SHORT).show()
            return false
        }

        // Ensure passwords match
        if (password != confirmPassword) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun registerRetailer() {
        val name = etRetailerName.text.toString().trim()
        val email = etRetailerEmail.text.toString().trim()
        val phone = etRetailerPhone.text.toString().trim()
        val password = etRetailerPassword.text.toString().trim()
        val confirmPassword = etRetailerConfirmPassword.text.toString().trim()
        val businessName = etRetailerBusinessName.text.toString().trim()
        val address = etRetailerAddress.text.toString().trim()

        // Register the retailer using FirebaseRepository
        firebaseRepository.signUpUser(email, password) { success, message ->
            if (success) {
                val retailerId = firebaseRepository.getCurrentUserId()
                if (retailerId != null) {
                    val retailerData = mapOf(
                        "name" to name,
                        "email" to email,
                        "phone" to phone,
                        "businessName" to businessName,
                        "address" to address,
                        "role" to "retailer"
                    )

                    // Save retailer data in the database
                    firebaseRepository.addUserDetails(retailerId, retailerData) { dbSuccess, dbMessage ->
                        if (dbSuccess) {
                            Toast.makeText(this, "Signup successful!", Toast.LENGTH_SHORT).show()
                            navigateToLogin()
                        } else {
                            Toast.makeText(this, "Error saving data: $dbMessage", Toast.LENGTH_SHORT).show()
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

    private fun navigateToLogin() {
        val intent = Intent(this, R_Login::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }
}
