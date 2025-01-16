package com.example.lottery.admin

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.lottery.R
import com.example.lottery.data.model.Transaction
import com.example.lottery.databinding.ActivityAcoinManageBinding
import com.example.lottery.utils.Constants.ROLE_ADMIN
import com.example.lottery.utils.Constants.ROLE_PLAYER
import com.example.lottery.utils.Constants.STATUS_APPROVED
import com.example.lottery.utils.Constants.STATUS_PENDING
import com.example.lottery.utils.Constants.TRANSACTIONS_COLLECTION
import com.google.firebase.firestore.FirebaseFirestore

data class Request(
    val requestId: String = "",
    val userId: String = "",
    val amount: Int = 0
)

class A_CoinManage : AppCompatActivity() {

    private val binding by lazy { ActivityAcoinManageBinding.inflate(layoutInflater)}
    private lateinit var lvRetailerRequests: ListView
    private lateinit var lvPlayerRequests: ListView

    private val firestore = FirebaseFirestore.getInstance()

    private val requestTransactionIds by lazy { listOf<String>() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        lvRetailerRequests = findViewById(R.id.lvRetailerRequests)
        lvPlayerRequests = findViewById(R.id.lvPlayerRequests)

//        loadRequests(lvRetailerRequests, "retailerRequests", "Retailer")
        loadRequests(lvPlayerRequests, ROLE_PLAYER)
    }

    private fun loadRequests(listView: ListView, userType: String) {
        firestore.collection(TRANSACTIONS_COLLECTION)
            .whereEqualTo("userRole", userType)  // Filter by required user role
            .whereEqualTo("status", STATUS_PENDING)  // Filter by status = Pending
            .whereEqualTo("recipientType", ROLE_ADMIN)  // Filter by status = Pending
            .get()
            .addOnSuccessListener { snapshot ->
                val requests = snapshot.documents.mapNotNull {
                    it.toObject(Transaction::class.java)?.let { transaction ->
                        Request(
                            requestId = it.id,
                            userId = transaction.userId,
                            amount = transaction.amount
                        )
                    }
                }

                val requestDetails = requests.map {
                    "User: ${it.userId}, docId: ${it.requestId} Amount: ${it.amount}"
                }

                val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, requestDetails)
                listView.adapter = adapter

                listView.setOnItemClickListener { _, _, position, _ ->
                    val selectedRequest = requests[position]
                    showApprovalDialog(selectedRequest, userType)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load $userType requests.", Toast.LENGTH_SHORT).show()
            }
    }


//    private fun loadRequests(listView: ListView, userType: String) {
//        firestore.collection(TRANSACTIONS_COLLECTION).get()
//            .addOnSuccessListener { snapshot ->
//                val requests = snapshot.documents.mapNotNull {
//                    Request(
//                        requestId = it.id,
//                        userId = it.getString("userId") ?: "",
//                        amount = it.getLong("amount")?.toInt() ?: 0
//                    )
//                }
//
//                val requestDetails = requests.map {
//                    "User: ${it.userId}, Amount: ${it.amount}"
//                }
//
//                val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, requestDetails)
//                listView.adapter = adapter
//
//                listView.setOnItemClickListener { _, _, position, _ ->
//                    val selectedRequest = requests[position]
//                    showApprovalDialog(selectedRequest, collection, userType)
//                }
//            }
//            .addOnFailureListener {
//                Toast.makeText(this, "Failed to load $userType requests.", Toast.LENGTH_SHORT).show()
//            }
//    }

    private fun showApprovalDialog(request: Request, userType: String) {
        AlertDialog.Builder(this)
            .setTitle("$userType Request Approval")
            .setMessage("Approve the following request?\n\nUser: ${request.userId}\nAmount: ${request.amount}")
            .setPositiveButton("Approve") { _, _ ->
                approveRequest(request)
            }
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }

    private fun approveRequest(request: Request) {
        val userRef = firestore.collection("users").document(request.userId)
        val transactionRef = firestore.collection(TRANSACTIONS_COLLECTION).document(request.requestId)

        firestore.runTransaction { transaction ->
            val userSnapshot = transaction.get(userRef)
            val currentCoins = userSnapshot.getLong("coins")?.toInt() ?: 0
            transaction.update(userRef, "coins", currentCoins + request.amount)
            transaction.update(transactionRef, "status", STATUS_APPROVED)
        }.addOnSuccessListener {
            Toast.makeText(this, "Coins successfully added to ${request.userId}.", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to approve request: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
