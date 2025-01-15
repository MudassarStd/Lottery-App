package com.example.lottery.admin

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.lottery.R
import com.example.lottery.data.FirebaseRepository
import com.example.lottery.data.PaymentHandler
import com.example.lottery.data.Request


class A_CoinManage : AppCompatActivity() {
    private lateinit var lvRetailerRequests: ListView
    private lateinit var lvPlayerRequests: ListView

    private lateinit var firebaseRepository: FirebaseRepository
    private lateinit var paymentHandler: PaymentHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_acoin_manage)

        lvRetailerRequests = findViewById(R.id.lvRetailerRequests)
        lvPlayerRequests = findViewById(R.id.lvPlayerRequests)

        // Initialize helpers
        firebaseRepository = FirebaseRepository()
        paymentHandler = PaymentHandler(this)

        // Load requests
        loadRequests(lvRetailerRequests, "retailerRequests", "Retailer")
        loadRequests(lvPlayerRequests, "playerRequests", "Player")
    }

    private fun loadRequests(listView: ListView, requestType: String, userType: String) {
        firebaseRepository.getRequests(requestType) { requests, error ->
            if (requests != null) {
                val requestDetails = requests.map {
                    "User: ${it.userId}, Amount: ${it.amount}"
                }
                val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, requestDetails)
                listView.adapter = adapter

                listView.setOnItemClickListener { _, _, position, _ ->
                    val selectedRequest = requests[position]
                    showApprovalDialog(selectedRequest, userType)
                }
            } else {
                Toast.makeText(this, "Failed to load $userType requests: $error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showApprovalDialog(request: Request, userType: String) {
        val dialog = AlertDialog.Builder(this)
            .setTitle("$userType Request Approval")
            .setMessage("Approve the following request?\n\nUser: ${request.userId}\nAmount: ${request.amount}")
            .setPositiveButton("Approve") { _, _ ->
                approveRequest(request, userType)
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    private fun approveRequest(request: Request, userType: String) {
        firebaseRepository.addCoinsToUser(request.userId, request.amount) { success, error ->
            if (success) {
                firebaseRepository.removeRequest(request.requestId, if (userType == "Retailer") "retailerRequests" else "playerRequests") { removeSuccess, removeError ->
                    if (removeSuccess) {
                        Toast.makeText(this, "Coins successfully added to ${request.userId}", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Failed to remove request: $removeError", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Failed to approve request: $error", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
