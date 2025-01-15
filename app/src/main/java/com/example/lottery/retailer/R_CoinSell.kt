package com.example.lottery.retailer

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.lottery.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class R_CoinSell : AppCompatActivity() {
    private lateinit var etCoinAmount: EditText
    private lateinit var etReceiverId: EditText
    private lateinit var btnSellCoins: Button
    private lateinit var lvPendingRequests: ListView

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var requestsRef: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rcoin_sell)
        etCoinAmount = findViewById(R.id.etCoinAmount)
        etReceiverId = findViewById(R.id.etReceiverId)
        btnSellCoins = findViewById(R.id.btnSellCoins)
        lvPendingRequests = findViewById(R.id.lvPendingRequests)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()
        requestsRef = firebaseDatabase.getReference("coinRequests")

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

        requestsRef.orderByChild("retailerId").equalTo(retailerId).addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val requestsList = mutableListOf<String>()

                for (request in snapshot.children) {
                    val playerId = request.child("playerId").value.toString()
                    val amount = request.child("coinAmount").value.toString()
                    requestsList.add("Player ID: $playerId - Coins: $amount")
                }

                val adapter = ArrayAdapter(
                    this@R_CoinSell,
                    android.R.layout.simple_list_item_1,
                    requestsList
                )
                lvPendingRequests.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@R_CoinSell, "Failed to load requests", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun sellCoins(coinAmount: Int, receiverId: String) {
        val retailerId = firebaseAuth.currentUser?.uid ?: return

        val request = mapOf(
            "retailerId" to retailerId,
            "playerId" to receiverId,
            "coinAmount" to coinAmount
        )

        val newRequestRef = requestsRef.push()
        newRequestRef.setValue(request).addOnSuccessListener {
            Toast.makeText(this, "Coins successfully sold", Toast.LENGTH_SHORT).show()
            etCoinAmount.text.clear()
            etReceiverId.text.clear()
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to sell coins", Toast.LENGTH_SHORT).show()
        }
    }
}