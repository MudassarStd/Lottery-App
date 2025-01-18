package com.example.lottery.retailer

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.lottery.R
import com.example.lottery.player.P_PlaceBet
import com.example.lottery.player.P_Result

class R_Dashboard : AppCompatActivity() {
    private lateinit var btnManagePlayers: LinearLayout
    private lateinit var btnSellCoins: LinearLayout
    private lateinit var btnPurchaseCoins: LinearLayout
    private lateinit var btnViewResults: LinearLayout
    private lateinit var btnRefundRequests: LinearLayout
    private lateinit var btnReports: LinearLayout
    private lateinit var btnNotifications: LinearLayout
    private lateinit var btnLogout: LinearLayout
    private lateinit var btnPlayLogin: LinearLayout // New button
    private lateinit var btnPlaceBetRetailer: LinearLayout // New button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_rdashboard)

        // Initialize buttons
        btnManagePlayers = findViewById(R.id.btnManagePlayers)
        btnSellCoins = findViewById(R.id.btnSellCoins)
        btnPurchaseCoins = findViewById(R.id.btnPurchaseCoins)
        btnViewResults = findViewById(R.id.btnViewResults)
        btnRefundRequests = findViewById(R.id.btnRefundRequests)
        btnReports = findViewById(R.id.btnReports)
        btnNotifications = findViewById(R.id.btnNotifications)
        btnLogout = findViewById(R.id.btnLogout)
        btnPlayLogin = findViewById(R.id.btnPlayLogin) // Initialize new button
        btnPlaceBetRetailer = findViewById(R.id.btnPlaceBetRetailer) // Initialize new button

        // Set up click listeners
        btnManagePlayers.setOnClickListener {
            startActivity(Intent(this, R_ManagePlayers::class.java))
        }

        btnSellCoins.setOnClickListener {
            startActivity(Intent(this, R_CoinSell::class.java))
        }

        btnPurchaseCoins.setOnClickListener {
            startActivity(Intent(this, R_CoinPurchase::class.java))
        }

        btnViewResults.setOnClickListener {
            startActivity(Intent(this, P_Result::class.java))
        }

        btnRefundRequests.setOnClickListener {
            startActivity(Intent(this, R_Refund::class.java))
        }

        btnReports.setOnClickListener {
            startActivity(Intent(this, R_Reports::class.java))
        }

        btnNotifications.setOnClickListener {
            startActivity(Intent(this, R_Notifications::class.java))
        }

        btnLogout.setOnClickListener {
            finish()
        }

        btnPlayLogin.setOnClickListener {
            startActivity(Intent(this, R_play_login::class.java)) // Navigate to R_Play_Login
        }

        btnPlaceBetRetailer.setOnClickListener {
            startActivity(Intent(this, P_PlaceBet::class.java)) // Navigate to R_Play_Login
        }
    }
}
