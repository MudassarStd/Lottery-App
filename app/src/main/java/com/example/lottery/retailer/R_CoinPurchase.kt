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
import com.example.lottery.data.model.Transaction
import com.example.lottery.databinding.ActivityRcoinPurchaseBinding
import com.example.lottery.utils.Constants.ROLE_ADMIN
import com.example.lottery.utils.Constants.ROLE_PLAYER
import com.example.lottery.utils.Constants.ROLE_RETAILER
import com.example.lottery.utils.Constants.STATUS_APPROVED
import com.example.lottery.utils.Constants.STATUS_PENDING
import com.example.lottery.utils.Constants.STATUS_REJECTED
import com.example.lottery.utils.DateTimeUtils.formatTimestamp
import com.example.lottery.utils.Extensions.hide
import com.example.lottery.utils.Extensions.show
import com.example.lottery.utils.ValidationUtils
import com.google.firebase.auth.FirebaseAuth

class R_CoinPurchase : AppCompatActivity() {

    private val binding by lazy { ActivityRcoinPurchaseBinding.inflate(layoutInflater) }


    private val firebaseRepository by lazy { FirebaseRepository() }
    private lateinit var paymentHandler: PaymentHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Initialize helpers
//        paymentHandler = PaymentHandler(this)

        // Initialize UI elements

        loadTransactionHistory(STATUS_PENDING)

        binding.rbGroupRequests.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.rbPending -> {
                    loadTransactionHistory(STATUS_PENDING)
                }
                R.id.rbApproved -> {
                    loadTransactionHistory(STATUS_APPROVED)
                }
                R.id.rbRejected -> {
                    loadTransactionHistory(STATUS_REJECTED)
                }
                else -> {
                    // Handle the case where no radio button is selected (optional)
                }
            }
        }

        binding.btnRequestPurchase.setOnClickListener {
            requestCoins()
        }
    }

    private fun loadTransactionHistory(status: String) {
        val retailerId = firebaseRepository.getCurrentUserId() ?: return
        firebaseRepository.getTransactionHistoryByUID(retailerId, callback = {response, transactions, errorMessage ->
            if (response) {
                val transactionDetails: List<String> = transactions?.map {
                    "Transaction Type: ${it.transactionType}\nAmount: ${it.amount} coins\nTo: ${it.recipientType}\n" +
                            "Status: ${it.status}\nTimeStamp: ${formatTimestamp(it.timeStamp)}"
                }?: emptyList()

                if (transactionDetails.isEmpty()) {
                    binding.lvPendingRequests.hide()
                    binding.tvNoRequestsFound.show()
                } else {
                    binding.lvPendingRequests.show()
                    binding.tvNoRequestsFound.hide()
                }

                val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, transactionDetails)
                binding.lvPendingRequests.adapter = adapter

            } else {
                Toast.makeText(this, "Error fetching transaction history: $errorMessage", Toast.LENGTH_SHORT).show()
            }
        }, status)
    }

//    private fun initiatePurchaseRequest(coinAmount: Int) {
//        val retailerId = firebaseRepository.getCurrentUserId() ?: return
//
//        // Handle payment
//        paymentHandler.initiatePayment(
//            recipientId = "admin123", // Assuming admin is the recipient
//            amount = coinAmount,
//            paymentMethod = "Credit Card",
//            onSuccess = {
//                // Create purchase request model
//                val purchaseRequest = PurchaseRequest(
//                    retailerId = retailerId,
//                    coinAmount = coinAmount,
//                    status = "Pending"
//                )
//
//                // Save the request to Firebase
//                data class PurchaseRequest(
//                    val retailerId: String,
//                    val coinAmount: Int,
//                    val status: String,
//                    val additionalData: Map<String, Any> = emptyMap()
//                )
//
//                // Extension function to convert PurchaseRequest to Map
//                fun PurchaseRequest.toMap(): Map<String, Any> {
//                    return mapOf(
//                        "retailerId" to retailerId,
//                        "coinAmount" to coinAmount,
//                        "status" to status,
//                        "additionalData" to additionalData
//                    )
//                }
//
//                // Usage in your FirebaseRepository
//                firebaseRepository.addPurchaseRequest(purchaseRequest.toMap()) { success, error ->
//                    if (success) {
//                        Toast.makeText(this, "Purchase request sent successfully", Toast.LENGTH_SHORT).show()
//                        etCoinAmount.text.clear()
//                    } else {
//                        Toast.makeText(this, "Failed to send purchase request: $error", Toast.LENGTH_SHORT).show()
//                    }
//                }
//            },
//            onFailure = { errorMessage ->
//                Toast.makeText(this, "Payment failed: $errorMessage", Toast.LENGTH_SHORT).show()
//            }
//        )
//    }

    fun requestCoins() {
        val coinAmount = binding.etCoinAmount.text.toString().toIntOrNull()
        if (coinAmount == null || coinAmount <= 0) {
            Toast.makeText(this, "Enter a valid coin amount", Toast.LENGTH_SHORT).show()
            return
        }

        if (!ValidationUtils.isValidCoinAmount(coinAmount)) {
            Toast.makeText(this, "Invalid coin amount", Toast.LENGTH_SHORT).show()
            return
        }

        val retailerId = firebaseRepository.getCurrentUserId() ?: return

        val transaction = Transaction(
            userId = retailerId,
            userRole = ROLE_RETAILER,
            recipientId = "Admin",
            amount = coinAmount,
            transactionType = "request",
            recipientType = ROLE_ADMIN,
        )

        firebaseRepository.addTransaction(transaction, callback = {status, msg ->
            if (status) {
                Toast.makeText(this, "Requested to admin successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Request failed: exception message -> $msg", Toast.LENGTH_SHORT).show()
            }
        })

    }

}
