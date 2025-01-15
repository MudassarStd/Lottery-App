package com.example.lottery.player

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.lottery.R
import com.example.lottery.data.FirebaseRepository
import com.example.lottery.data.PaymentHandler
import com.example.lottery.data.Transaction
import com.example.lottery.utils.ValidationUtils

class P_CoinManage : AppCompatActivity() {
    private lateinit var etCoinAmount: EditText
    private lateinit var spinnerRetailers: Spinner
    private lateinit var btnRequestFromRetailer: Button
    private lateinit var btnRequestFromAdmin: Button
    private lateinit var lvTransactionHistory: ListView

    private lateinit var firebaseRepository: FirebaseRepository
    private lateinit var paymentHandler: PaymentHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pcoin_manage)

        // Initialize FirebaseRepository and PaymentHandler
        firebaseRepository = FirebaseRepository()
        paymentHandler = PaymentHandler(this)

        // Initialize UI elements
        etCoinAmount = findViewById(R.id.etCoinAmount)
        spinnerRetailers = findViewById(R.id.spinnerRetailers)
        btnRequestFromRetailer = findViewById(R.id.btnRequestFromRetailer)
        btnRequestFromAdmin = findViewById(R.id.btnRequestFromAdmin)
        lvTransactionHistory = findViewById(R.id.lvTransactionHistory)

        loadRetailers()
        loadTransactionHistory()

        // Set button click listeners
        btnRequestFromRetailer.setOnClickListener { requestCoins("retailer") }
        btnRequestFromAdmin.setOnClickListener { requestCoins("admin") }
    }

    private fun loadRetailers() {
        firebaseRepository.getUsersByRole("retailer") { retailers, error ->
            if (retailers != null) {
                val retailerNames = retailers.map { it["name"].toString() }
                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, retailerNames)
                spinnerRetailers.adapter = adapter
            } else {
                Toast.makeText(this, "Failed to load retailers: $error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadTransactionHistory() {
        val playerId = firebaseRepository.getCurrentUserId() ?: return

        firebaseRepository.getTransactionHistory(playerId) { transactions, error ->
            if (transactions != null) {
                val transactionDetails = transactions.map { it["detail"].toString() }
                val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, transactionDetails)
                lvTransactionHistory.adapter = adapter
            } else {
                Toast.makeText(this, "Failed to load transactions: $error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun requestCoins(requestType: String) {
        val coinAmount = etCoinAmount.text.toString().toIntOrNull()
        if (coinAmount == null || coinAmount <= 0) {
            Toast.makeText(this, "Enter a valid coin amount", Toast.LENGTH_SHORT).show()
            return
        }

        if (!ValidationUtils.isValidCoinAmount(coinAmount)) {
            Toast.makeText(this, "Invalid coin amount", Toast.LENGTH_SHORT).show()
            return
        }

        val playerId = firebaseRepository.getCurrentUserId() ?: return
        val recipientId = if (requestType == "retailer") spinnerRetailers.selectedItem?.toString() else "Admin"

        // Validate recipient
        if (recipientId.isNullOrEmpty()) {
            Toast.makeText(this, "Please select a retailer or choose Admin.", Toast.LENGTH_SHORT).show()
            return
        }

        // Initiate payment
        paymentHandler.initiatePayment(
            recipientId = recipientId,
            amount = coinAmount,
            paymentMethod = "Credit Card",
            onSuccess = {
                // Create transaction
                val transaction = Transaction(
                    userId = playerId,
                    recipientId = recipientId,
                    amount = coinAmount,
                    type = "purchase"
                )

                // Save transaction to Firebase
                firebaseRepository.addTransaction(transaction.toMap()) { success, error ->
                    if (success) {
                        // Clear input field
                        etCoinAmount.text.clear()

                        // Toast message
                        Toast.makeText(this, "Request successfully sent to $recipientId.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Failed to send request: $error", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onFailure = { errorMessage ->
                Toast.makeText(this, "Payment failed: $errorMessage", Toast.LENGTH_SHORT).show()
            }
        )
    }
}
