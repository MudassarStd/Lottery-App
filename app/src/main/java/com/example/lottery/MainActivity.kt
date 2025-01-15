package com.example.lottery

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.lottery.admin.A_Login
import com.example.lottery.player.P_Login
import com.example.lottery.retailer.R_Login
import com.google.firebase.FirebaseApp

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        setContentView(R.layout.activity_main)

        val btnAdminLogin: Button = findViewById(R.id.btnAdminLogin)
        val btnPlayerLogin: Button = findViewById(R.id.btnPlayerLogin)
        val btnRetailerLogin: Button = findViewById(R.id.btnRetailerLogin)

        // Navigate to Admin Login
        btnAdminLogin.setOnClickListener {
            val intent = Intent(this, A_Login::class.java)
            startActivity(intent)
        }

        // Navigate to Player Login
        btnPlayerLogin.setOnClickListener {
            val intent = Intent(this, P_Login::class.java)
            startActivity(intent)
        }

        // Navigate to Retailer Login
        btnRetailerLogin.setOnClickListener {
            val intent = Intent(this, R_Login::class.java)
            startActivity(intent)
        }
    }
}