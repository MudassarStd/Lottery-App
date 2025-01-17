package com.example.lottery.retailer

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.lottery.R
import com.example.lottery.admin.Request
import com.example.lottery.data.FirebaseRepository
import com.example.lottery.data.model.Transaction
import com.example.lottery.databinding.ActivityRcoinSellBinding
import com.example.lottery.utils.Constants.ROLE_ADMIN
import com.example.lottery.utils.Constants.ROLE_RETAILER
import com.example.lottery.utils.Constants.STATUS_APPROVED
import com.example.lottery.utils.Constants.STATUS_PENDING
import com.example.lottery.utils.Constants.TRANSACTIONS_PATH
import com.example.lottery.utils.Constants.USERS_PATH
import com.example.lottery.utils.Extensions.hide
import com.example.lottery.utils.Extensions.show
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class R_CoinSell : AppCompatActivity() {

    private lateinit var etCoinAmount: EditText
    private lateinit var etReceiverId: EditText
    private lateinit var btnSellCoins: Button
    private lateinit var lvPendingRequests: ListView

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private val firebaseRepository by lazy { FirebaseRepository() }
    private val binding by lazy { ActivityRcoinSellBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Initialize UI components
        etCoinAmount = findViewById(R.id.etCoinAmount)
//        etReceiverId = findViewById(R.id.etReceiverId)
        btnSellCoins = findViewById(R.id.btnSellCoins)
        lvPendingRequests = findViewById(R.id.lvPendingRequests)

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        loadCoinBalance()
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

    private fun loadCoinBalance() {
        firebaseRepository.loadCoinBalance(callback = {response, data ->
            if (response) {
                binding.tvAvailableCoins.text = data
            } else {
                binding.tvAvailableCoins.text = "0"
            }
        })
    }

    private fun loadPendingRequests() {
        val retailerId = firebaseAuth.currentUser?.uid ?: return

        firestore.collection(TRANSACTIONS_PATH)
            .whereEqualTo("recipientId", retailerId)  // Filter by required user role
            .whereEqualTo("status", STATUS_PENDING)  // Filter by status = Pending
            .whereEqualTo("recipientType", ROLE_RETAILER)  // Filter by status = Pending
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

                if (requestDetails.isEmpty()) {
                    binding.lvPendingRequests.hide()
                    binding.tvNoPendingRequests.show()
                } else {
                    binding.lvPendingRequests.show()
                    binding.tvNoPendingRequests.hide()
                }

                val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, requestDetails)
                binding.lvPendingRequests.adapter = adapter

                binding.lvPendingRequests.setOnItemClickListener { _, _, position, _ ->
                    val selectedRequest = requests[position]
                    showApprovalDialog(selectedRequest)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load  requests.", Toast.LENGTH_SHORT).show()
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


    private fun showApprovalDialog(request: Request) {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Player Request Approval")
            .setMessage("Approve the following request?\n\nUser: ${request.userId}\nAmount: ${request.amount}")
            .setPositiveButton("Approve") { _, _ ->
                if(checkBalance(request.amount)) {
                    approveRequest(request)
                } else {
                    Toast.makeText(this, "Insufficient balance", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }

    private fun checkBalance(amount: Int): Boolean {
        return binding.tvAvailableCoins.text.toString().toInt() >= amount
    }




    private fun approveRequest(request: Request) {
        val userRef = firestore.collection(USERS_PATH).document(request.userId)
        val retailerRef =
            firebaseRepository.getCurrentUserId()
                ?.let { firestore.collection(USERS_PATH).document(it) }

        val transactionRef = firestore.collection(TRANSACTIONS_PATH).document(request.requestId)

        val newAmount = binding.tvAvailableCoins.text.toString().toInt() - request.amount

        firestore.runTransaction { transaction ->
            val userSnapshot = transaction.get(userRef)
            val currentCoins = userSnapshot.getLong("coins")?.toInt() ?: 0

            transaction.update(userRef, "coins", currentCoins + request.amount)
            if (retailerRef != null) {
                transaction.update(retailerRef, "coins", newAmount)
            }
            transaction.update(transactionRef, "status", STATUS_APPROVED)
        }.addOnSuccessListener {
            Toast.makeText(this, "Coins successfully added to ${request.userId}.", Toast.LENGTH_SHORT).show()
            loadCoinBalance()
            loadPendingRequests()
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to approve request: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
