package com.example.lottery

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.lottery.admin.A_Login
import com.example.lottery.player.P_Login
import com.example.lottery.retailer.R_Login
import com.google.firebase.FirebaseApp

class MainActivity : AppCompatActivity() {

    private var adminClickCount = 0 // Counter to track the number of clicks

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        setContentView(R.layout.activity_main)

        val btnAdminLogin: ImageView = findViewById(R.id.adlogin)
        val btnPlayerLogin: Button = findViewById(R.id.btnPlayerLogin)
        val btnRetailerLogin: Button = findViewById(R.id.btnRetailerLogin)

        // Navigate to Admin Login after 3 clicks
        btnAdminLogin.setOnClickListener {
            adminClickCount++ // Increment the click counter

            if (adminClickCount == 3) {
                val intent = Intent(this, A_Login::class.java)
                startActivity(intent)
            } else {
                // Optionally, you can show a message or toast indicating more clicks are needed
                // abhi na lgana : Toast.makeText(this, "Click ${3 - adminClickCount} more times to access Admin Login", Toast.LENGTH_SHORT).show()
            }
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
