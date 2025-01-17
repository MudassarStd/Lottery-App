package com.example.lottery.player

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.lottery.R
import com.example.lottery.data.model.Transaction
import com.example.lottery.databinding.ActivityPcoinManageBinding
import com.example.lottery.utils.Constants.PLAYER_COIN_REQUESTS_COLLECTION
import com.example.lottery.utils.Constants.ROLE_ADMIN
import com.example.lottery.utils.Constants.ROLE_PLAYER
import com.example.lottery.utils.Constants.ROLE_RETAILER
import com.example.lottery.utils.Constants.STATUS_PENDING
import com.example.lottery.utils.Constants.TRANSACTIONS_PATH
import com.example.lottery.utils.Constants.USERS_PATH
import com.example.lottery.utils.Extensions.hide
import com.example.lottery.utils.Extensions.show
import com.example.lottery.utils.ValidationUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class P_CoinManage : AppCompatActivity() {

    private val binding by lazy { ActivityPcoinManageBinding.inflate(layoutInflater) }

    private lateinit var etCoinAmount: EditText
    private lateinit var btnRequestFromRetailer: Button
    private lateinit var btnRequestFromAdmin: Button
    private lateinit var lvTransactionHistory: ListView

    private val db = FirebaseFirestore.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser

    // Predefined retailer ID
    /**
     * fetch all users with role "Retailer" to show to user for selection of retailer
     * */

    private val predefinedRetailerId = "Retailer123" // Replace with your actual retailer ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Initialize UI elements
        etCoinAmount = findViewById(R.id.etCoinAmount)
        btnRequestFromRetailer = findViewById(R.id.btnRequestFromRetailer)
        btnRequestFromAdmin = findViewById(R.id.btnRequestFromAdmin)
        lvTransactionHistory = findViewById(R.id.lvTransactionHistory)

        loadTransactionHistory()

        // Set button click listeners
        btnRequestFromRetailer.setOnClickListener {
            loadRetailers() }
        // load registered retailers, select and request
        btnRequestFromAdmin.setOnClickListener { requestCoins("Admin", ROLE_ADMIN) }
    }

    private fun loadRetailers() {
        binding.tvHeaderAvailableRetailers.show()
        binding.lvAvailableRetailers.show()

        db.collection(USERS_PATH)
            .whereEqualTo("role", ROLE_RETAILER)
            .get()
            .addOnSuccessListener { result ->
                // Create a list of pairs (displayText, docId)
                val retailers = result.documents.map { document ->
                    Pair(
                        "RetailerName: ${document.getString("name")}\nEmail: ${document.getString("email")}\nBusinessName: ${document.getString("businessName")}",
                        document.id
                    )
                }

                if (retailers.isEmpty()) {
                    binding.lvAvailableRetailers.hide()

                }

                // Extract the display text for the list adapter
                val displayTexts = retailers.map { it.first }

                val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, displayTexts)
                binding.lvAvailableRetailers.adapter = adapter

                binding.lvAvailableRetailers.setOnItemClickListener { _, _, position, _ ->
                    val selectedRetailer = retailers[position]
                    val docId = selectedRetailer.second // Get the document ID
                    requestCoins(docId, ROLE_RETAILER)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load retailers: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }



    private fun loadTransactionHistory() {
        val playerId = currentUser?.uid ?: return

        db.collection(TRANSACTIONS_PATH)
            .whereEqualTo("userId", playerId)
            .get()
            .addOnSuccessListener { result ->
                val transactionDetails = result.map {
                    "Transaction Type: ${it.getString("transactionType")}\nAmount: ${it.getLong("amount")} coins\nTo: ${it.getString("recipientType")}\n" +
                            "Status: ${it.getString("status")}"
                }
                val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, transactionDetails)
                lvTransactionHistory.adapter = adapter
            }
            .addOnFailureListener { error ->
                Toast.makeText(this, "Failed to load transactions: ${error.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun requestCoins(recipientId: String, recipientType: String) {
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
            userRole = ROLE_PLAYER,
            recipientId = recipientId,
            amount = coinAmount,
            transactionType = "request",
            recipientType = recipientType,
        )

        db.collection(TRANSACTIONS_PATH)
            .add(transaction)
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

