package com.example.lottery.retailer

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.lottery.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class R_CoinSell : AppCompatActivity() {
    private lateinit var etCoinAmount: EditText
    private lateinit var etReceiverId: EditText
    private lateinit var btnSellCoins: Button
    private lateinit var lvPendingRequests: ListView

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rcoin_sell)

        // Initialize UI components
        etCoinAmount = findViewById(R.id.etCoinAmount)
        etReceiverId = findViewById(R.id.etReceiverId)
        btnSellCoins = findViewById(R.id.btnSellCoins)
        lvPendingRequests = findViewById(R.id.lvPendingRequests)

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        loadPendingRequests()

        btnSellCoins.setOnClickListener {
            val coinAmount = etCoinAmount.text.toString().toIntOrNull()
            val receiverId = etReceiverId.text.toString()

            if (coinAmount == null || coinAmount <= 0) {
                Toast.makeText(this, "Enter a valid coin amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (receiverId.isBlank()) {
                Toast.makeText(this, "Enter a valid receiver ID", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            sellCoins(coinAmount, receiverId)
        }
    }

    private fun loadPendingRequests() {
        val retailerId = firebaseAuth.currentUser?.uid ?: return

        firestore.collection("coinRequests")
            .whereEqualTo("retailerId", retailerId)
            .get()
            .addOnSuccessListener { snapshot ->
                val requestsList = mutableListOf<String>()

                for (document in snapshot) {
                    val playerId = document.getString("playerId") ?: "Unknown"
                    val amount = document.getLong("coinAmount") ?: 0
                    requestsList.add("Player ID: $playerId - Coins: $amount")
                }

                val adapter = ArrayAdapter(
                    this@R_CoinSell,
                    android.R.layout.simple_list_item_1,
                    requestsList
                )
                lvPendingRequests.adapter = adapter
            }
            .addOnFailureListener {
                Toast.makeText(this@R_CoinSell, "Failed to load requests", Toast.LENGTH_SHORT).show()
            }
    }

    private fun sellCoins(coinAmount: Int, receiverId: String) {
        val retailerId = firebaseAuth.currentUser?.uid ?: return

        val request = hashMapOf(
            "retailerId" to retailerId,
            "playerId" to receiverId,
            "coinAmount" to coinAmount
        )

        firestore.collection("coinRequests")
            .add(request)
            .addOnSuccessListener {
                Toast.makeText(this, "Coins successfully sold", Toast.LENGTH_SHORT).show()
                etCoinAmount.text.clear()
                etReceiverId.text.clear()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to sell coins", Toast.LENGTH_SHORT).show()
            }
    }
}
