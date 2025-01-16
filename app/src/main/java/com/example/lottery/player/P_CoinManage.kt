package com.example.lottery.player

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.lottery.R
import com.example.lottery.utils.ValidationUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class P_CoinManage : AppCompatActivity() {
    private lateinit var etCoinAmount: EditText
    private lateinit var btnRequestFromRetailer: Button
    private lateinit var btnRequestFromAdmin: Button
    private lateinit var lvTransactionHistory: ListView

    private val db = FirebaseFirestore.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser

    // Predefined retailer ID
    private val predefinedRetailerId = "Retailer123" // Replace with your actual retailer ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pcoin_manage)

        // Initialize UI elements
        etCoinAmount = findViewById(R.id.etCoinAmount)
        btnRequestFromRetailer = findViewById(R.id.btnRequestFromRetailer)
        btnRequestFromAdmin = findViewById(R.id.btnRequestFromAdmin)
        lvTransactionHistory = findViewById(R.id.lvTransactionHistory)

        loadTransactionHistory()

        // Set button click listeners
        btnRequestFromRetailer.setOnClickListener { requestCoins(predefinedRetailerId) }
        btnRequestFromAdmin.setOnClickListener { requestCoins("Admin") }
    }

    private fun loadTransactionHistory() {
        val playerId = currentUser?.uid ?: return

        db.collection("transactions")
            .whereEqualTo("userId", playerId)
            .get()
            .addOnSuccessListener { result ->
                val transactionDetails = result.map {
                    "${it.getString("type")}: ${it.getLong("amount")} coins to ${it.getString("recipientId")}"
                }
                val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, transactionDetails)
                lvTransactionHistory.adapter = adapter
            }
            .addOnFailureListener { error ->
                Toast.makeText(this, "Failed to load transactions: ${error.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun requestCoins(recipientId: String) {
        val coinAmount = etCoinAmount.text.toString().toIntOrNull()
        if (coinAmount == null || coinAmount <= 0) {
            Toast.makeText(this, "Enter a valid coin amount", Toast.LENGTH_SHORT).show()
            return
        }

        if (!ValidationUtils.isValidCoinAmount(coinAmount)) {
            Toast.makeText(this, "Invalid coin amount", Toast.LENGTH_SHORT).show()
            return
        }

        val playerId = currentUser?.uid ?: return

        val transaction = Transaction(
            userId = playerId,
            recipientId = recipientId,
            amount = coinAmount,
            type = "request"
        )

        db.collection("transactions")
            .add(transaction.toMap())
            .addOnSuccessListener {
                etCoinAmount.text.clear()
                Toast.makeText(this, "Request successfully sent to $recipientId.", Toast.LENGTH_SHORT).show()
                loadTransactionHistory()
            }
            .addOnFailureListener { error ->
                Toast.makeText(this, "Failed to send request: ${error.message}", Toast.LENGTH_SHORT).show()
            }
    }
}

// Transaction data class

data class Transaction(
    val userId: String,
    val recipientId: String,
    val amount: Int,
    val type: String
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "userId" to userId,
            "recipientId" to recipientId,
            "amount" to amount,
            "type" to type
        )
    }
}
