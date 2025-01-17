package com.example.lottery.player

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.lottery.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class P_Dashboard : AppCompatActivity() {

    private lateinit var tvWelcome: TextView
    private lateinit var tvCoinBalance: TextView
    private lateinit var btnPlaceBet: LinearLayout
    private lateinit var btnViewResults: LinearLayout
    private lateinit var btnManageCoins: LinearLayout
    private lateinit var btnNotifications: LinearLayout
    private lateinit var btnProfile: LinearLayout
    private lateinit var btnHelpSupport: LinearLayout
    private lateinit var btnLogout: LinearLayout

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_pdashboard)

        // Initialize UI Components
        initializeUI()

        // Initialize Firebase
        initializeFirebase()

        // Load Player Details
        loadPlayerDetails()

        // Set Up Navigation
        setupNavigation()
    }

    private fun initializeUI() {
        tvWelcome = findViewById(R.id.tvWelcome)
        tvCoinBalance = findViewById(R.id.tvCoinBalance)
        btnPlaceBet = findViewById(R.id.btnPlaceBet)
        btnViewResults = findViewById(R.id.btnViewResults)
        btnManageCoins = findViewById(R.id.btnManageCoins)
        btnNotifications = findViewById(R.id.btnNotifications)
        btnProfile = findViewById(R.id.btnProfile)
        btnHelpSupport = findViewById(R.id.btnHelpSupport)
        btnLogout = findViewById(R.id.btnLogout)
    }

    private fun initializeFirebase() {
        firebaseAuth = FirebaseAuth.getInstance()
    }

    private fun loadPlayerDetails() {
        val userId = firebaseAuth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "User not authenticated. Redirecting to login.", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, P_Login::class.java))
            finish()
            return
        }

        // Fetch user data from Firestore
        FirebaseFirestore.getInstance().collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val name = document.getString("name") ?: "Player"
                    val coinBalance = document.getLong("coins")?.toInt() ?: 0

                    // Update UI
                    tvWelcome.text = "Welcome, $name"
                    tvCoinBalance.text = "Coin Balance: $coinBalance"
                } else {
                    // Handle no user data case
                    tvWelcome.text = "Welcome, Player"
                    tvCoinBalance.text = "Coin Balance: 0"
                    Toast.makeText(this, "User data not found.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { error ->
                // Handle Firestore error
                Log.e("P_Dashboard", "Error loading user data: ${error.message}")
                tvWelcome.text = "Welcome, Player"
                tvCoinBalance.text = "Coin Balance: 0"
                Toast.makeText(this, "Failed to load data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupNavigation() {
        btnPlaceBet.setOnClickListener {
            startActivity(Intent(this, P_PlaceBet::class.java))
        }

        btnViewResults.setOnClickListener {
            startActivity(Intent(this, P_Result::class.java))
        }

        btnManageCoins.setOnClickListener {
            startActivity(Intent(this, P_CoinManage::class.java))
        }

        btnNotifications.setOnClickListener {
            startActivity(Intent(this, P_Notifications::class.java))
        }

        btnProfile.setOnClickListener {
            startActivity(Intent(this, P_Profile::class.java))
        }

        btnHelpSupport.setOnClickListener {
            startActivity(Intent(this, P_Help::class.java))
        }

        btnLogout.setOnClickListener {
            firebaseAuth.signOut()
            Toast.makeText(this, "Logged out successfully.", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, P_Login::class.java))
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        loadPlayerDetails()
    }
}



/**
 *
 * Just load player's coins on resume, instead of loading whole profile
 *
 * **/
