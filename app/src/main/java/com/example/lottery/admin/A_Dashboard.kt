package com.example.lottery.admin

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.lottery.R

class A_Dashboard : AppCompatActivity() {
    private lateinit var btnManageUsers: LinearLayout
    private lateinit var btnManageBets: LinearLayout
    private lateinit var btnManageRefunds: LinearLayout
    private lateinit var btnManageCoins: LinearLayout
    private lateinit var btnDeclareResults: LinearLayout
    private lateinit var btnViewReports: LinearLayout
    private lateinit var btnNotifications: LinearLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adashboard)
        btnManageUsers = findViewById(R.id.btnManageUsers)
        btnManageBets = findViewById(R.id.btnManageBets)
        btnManageRefunds = findViewById(R.id.btnManageRefunds)
        btnManageCoins = findViewById(R.id.btnManageCoins)
        btnDeclareResults = findViewById(R.id.btnDeclareResults)
        btnViewReports = findViewById(R.id.btnViewReports)
        btnNotifications = findViewById(R.id.btnNotifications)

        btnManageUsers.setOnClickListener {
            startActivity(Intent(this, A_ManagePlayers::class.java))
        }

        btnManageBets.setOnClickListener {
            startActivity(Intent(this, A_ManageBets::class.java))
        }

        btnManageRefunds.setOnClickListener {
            startActivity(Intent(this, A_Refund::class.java))
        }

        btnManageCoins.setOnClickListener {
            startActivity(Intent(this, A_CoinManage::class.java))
        }

        btnDeclareResults.setOnClickListener {
            startActivity(Intent(this, A_ResultDeclear::class.java))
        }

        btnViewReports.setOnClickListener {
            startActivity(Intent(this, A_Reports::class.java))
        }

        btnNotifications.setOnClickListener {
            startActivity(Intent(this, A_Notifications::class.java))
        }
    }
}