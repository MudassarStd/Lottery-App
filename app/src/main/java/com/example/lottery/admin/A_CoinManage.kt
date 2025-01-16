package com.example.lottery.admin

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.lottery.R
import com.google.firebase.firestore.FirebaseFirestore

data class Request(
    val requestId: String = "",
    val userId: String = "",
    val amount: Int = 0
)

class A_CoinManage : AppCompatActivity() {
    private lateinit var lvRetailerRequests: ListView
    private lateinit var lvPlayerRequests: ListView

    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_acoin_manage)

        lvRetailerRequests = findViewById(R.id.lvRetailerRequests)
        lvPlayerRequests = findViewById(R.id.lvPlayerRequests)

        loadRequests(lvRetailerRequests, "retailerRequests", "Retailer")
        loadRequests(lvPlayerRequests, "playerRequests", "Player")
    }

    private fun loadRequests(listView: ListView, collection: String, userType: String) {
        firestore.collection(collection).get()
            .addOnSuccessListener { snapshot ->
                val requests = snapshot.documents.mapNotNull {
                    Request(
                        requestId = it.id,
                        userId = it.getString("userId") ?: "",
                        amount = it.getLong("amount")?.toInt() ?: 0
                    )
                }

                val requestDetails = requests.map {
                    "User: ${it.userId}, Amount: ${it.amount}"
                }

                val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, requestDetails)
                listView.adapter = adapter

                listView.setOnItemClickListener { _, _, position, _ ->
                    val selectedRequest = requests[position]
                    showApprovalDialog(selectedRequest, collection, userType)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load $userType requests.", Toast.LENGTH_SHORT).show()
            }
    }


    private fun showApprovalDialog(request: Request, collection: String, userType: String) {
        AlertDialog.Builder(this)
            .setTitle("$userType Request Approval")
            .setMessage("Approve the following request?\n\nUser: ${request.userId}\nAmount: ${request.amount}")
            .setPositiveButton("Approve") { _, _ ->
                approveRequest(request, collection)
            }
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }

    private fun approveRequest(request: Request, collection: String) {
        val userRef = firestore.collection("users").document(request.userId)

        firestore.runTransaction { transaction ->
            val userSnapshot = transaction.get(userRef)
            val currentCoins = userSnapshot.getLong("coins")?.toInt() ?: 0
            transaction.update(userRef, "coins", currentCoins + request.amount)
            transaction.delete(firestore.collection(collection).document(request.requestId))
        }.addOnSuccessListener {
            Toast.makeText(this, "Coins successfully added to ${request.userId}.", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to approve request: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
