package com.example.lottery.retailer

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.lottery.R
import com.example.lottery.data.FirebaseRepository
import com.example.lottery.data.PaymentHandler
import com.example.lottery.data.PurchaseRequest

class R_CoinPurchase : AppCompatActivity() {
    private lateinit var etCoinAmount: EditText
    private lateinit var btnRequestPurchase: Button
    private lateinit var lvPendingRequests: ListView

    private lateinit var firebaseRepository: FirebaseRepository
    private lateinit var paymentHandler: PaymentHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rcoin_purchase)

        // Initialize helpers
        firebaseRepository = FirebaseRepository()
        paymentHandler = PaymentHandler(this)

        // Initialize UI elements
        etCoinAmount = findViewById(R.id.etCoinAmount)
        btnRequestPurchase = findViewById(R.id.btnRequestPurchase)
        lvPendingRequests = findViewById(R.id.lvPendingRequests)

        loadPendingRequests()

        btnRequestPurchase.setOnClickListener {
            val coinAmount = etCoinAmount.text.toString().toIntOrNull()
            if (coinAmount == null || coinAmount <= 0) {
                Toast.makeText(this, "Enter a valid coin amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            initiatePurchaseRequest(coinAmount)
        }
    }

    private fun loadPendingRequests() {
        val retailerId = firebaseRepository.getCurrentUserId() ?: return

        firebaseRepository.getPurchaseRequests(retailerId) { requests, error ->
            if (requests != null) {
                val requestsList = requests.map {
                    "Amount: ${it.coinAmount} - Status: ${it.status}"
                }
                val adapter = ArrayAdapter(
                    this@R_CoinPurchase,
                    android.R.layout.simple_list_item_1,
                    requestsList
                )
                lvPendingRequests.adapter = adapter
            } else {
                Toast.makeText(this, "Failed to load requests: $error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initiatePurchaseRequest(coinAmount: Int) {
        val retailerId = firebaseRepository.getCurrentUserId() ?: return

        // Handle payment
        paymentHandler.initiatePayment(
            recipientId = "admin123", // Assuming admin is the recipient
            amount = coinAmount,
            paymentMethod = "Credit Card",
            onSuccess = {
                // Create purchase request model
                val purchaseRequest = PurchaseRequest(
                    retailerId = retailerId,
                    coinAmount = coinAmount,
                    status = "Pending"
                )

                // Save the request to Firebase
                data class PurchaseRequest(
                    val retailerId: String,
                    val coinAmount: Int,
                    val status: String,
                    val additionalData: Map<String, Any> = emptyMap()
                )

                // Extension function to convert PurchaseRequest to Map
                fun PurchaseRequest.toMap(): Map<String, Any> {
                    return mapOf(
                        "retailerId" to retailerId,
                        "coinAmount" to coinAmount,
                        "status" to status,
                        "additionalData" to additionalData
                    )
                }

// Usage in your FirebaseRepository
                firebaseRepository.addPurchaseRequest(purchaseRequest.toMap()) { success, error ->
                    if (success) {
                        Toast.makeText(this, "Purchase request sent successfully", Toast.LENGTH_SHORT).show()
                        etCoinAmount.text.clear()
                    } else {
                        Toast.makeText(this, "Failed to send purchase request: $error", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onFailure = { errorMessage ->
                Toast.makeText(this, "Payment failed: $errorMessage", Toast.LENGTH_SHORT).show()
            }
        )
    }
}
