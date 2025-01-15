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
import com.google.firebase.database.*

class P_Dashboard : AppCompatActivity() {

    // UI Components
    private lateinit var tvWelcome: TextView
    private lateinit var tvCoinBalance: TextView
    private lateinit var btnPlaceBet: LinearLayout
    private lateinit var btnViewResults: LinearLayout
    private lateinit var btnManageCoins: LinearLayout
    private lateinit var btnNotifications: LinearLayout
    private lateinit var btnProfile: LinearLayout
    private lateinit var btnHelpSupport: LinearLayout
    private lateinit var btnLogout: LinearLayout

    // Firebase Instances
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var usersRef: DatabaseReference

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
        firebaseDatabase = FirebaseDatabase.getInstance()
        usersRef = firebaseDatabase.getReference("users")
    }

    private fun loadPlayerDetails() {
        val userId = firebaseAuth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "User not authenticated. Redirecting to login.", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, P_Login::class.java))
            finish()
            return
        }

        // Fetch user data from Firebase
        usersRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val name = snapshot.child("name").value?.toString() ?: "Player"
                    val coinBalance = snapshot.child("coins").value?.toString() ?: "0"

                    // Update UI
                    tvWelcome.text = "Welcome, $name"
                    tvCoinBalance.text = "Coin Balance: $coinBalance"
                } else {
                    // Handle no user data case
                    tvWelcome.text = "Welcome, Player"
                    tvCoinBalance.text = "Coin Balance: 0"
                    Toast.makeText(this@P_Dashboard, "User data not found.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle Firebase error
                Log.e("P_Dashboard", "Error loading user data: ${error.message}")
                tvWelcome.text = "Welcome, Player"
                tvCoinBalance.text = "Coin Balance: 0"
                Toast.makeText(this@P_Dashboard, "Failed to load data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
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
}
