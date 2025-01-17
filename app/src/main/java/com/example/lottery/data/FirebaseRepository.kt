package com.example.lottery.data

import android.util.Log
import android.widget.Toast
import com.example.lottery.utils.Constants.ACTIVE_BETS_PATH
import com.example.lottery.utils.Constants.TRANSACTIONS_PATH
import com.example.lottery.utils.Constants.USERS_PATH

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore


class FirebaseRepository {
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val realtimeDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference
    private val databaseRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("users")
    private val db = FirebaseFirestore.getInstance()

    // Get current user ID
    fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }

    // Sign up new user
    fun signUpUser(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, null)
                } else {
                    callback(false, task.exception?.localizedMessage)
                }
            }
    }

    // Log in existing user
    fun loginUser(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, null)
                } else {
                    callback(false, task.exception?.localizedMessage)
                }
            }
    }

    // Logout current user
    fun logoutUser() {
        firebaseAuth.signOut()
    }
   // is sy agy check krna
    // Add user details to Firestore
    fun addUserDetails(userId: String, userDetails: Map<String, Any>, callback: (Boolean, String?) -> Unit) {
        firestore.collection("users").document(userId).set(userDetails)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, null)
                } else {
                    callback(false, task.exception?.localizedMessage)
                }
            }
    }
    // Fetch a user by username and phone number
    fun getUserByNameAndPhone(
        name: String,
        phone: String,
        callback: (Map<String, Any>?, String?) -> Unit
    ) {
        db.collection("users")
            .whereEqualTo("name", name)  // Matches "name" field
            .whereEqualTo("phone", phone)  // Matches "phone" field
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val document = querySnapshot.documents[0]
                    Log.d("FirebaseRepository", "User found: ${document.data}")
                    callback(document.data, null)
                } else {
                    Log.e("FirebaseRepository", "No matching user found")
                    callback(null, "No user found")
                }
            }

            .addOnFailureListener { exception ->
                Log.e("FirebaseRepository", "Error fetching user: $exception")
                callback(null, exception.message)  // Pass error back
            }
    }
    fun queryUserByNameAndPhone(name: String, phone: String, callback: (Boolean, String?) -> Unit) {
        firestore.collection("users")
            .whereEqualTo("name", name)
            .whereEqualTo("phone", phone)
            .limit(1) // Limit to a single result
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val user = querySnapshot.documents[0] // Get the first matching document
                    callback(true, user.id) // Pass the user ID back to the caller
                } else {
                    callback(false, "No user found with the provided name and phone")
                }
            }
            .addOnFailureListener { exception ->
                callback(false, exception.message)
            }
    }



    fun loadCoinBalance(callback: (Boolean, String?) -> Unit) {
        val userId = firebaseAuth.currentUser?.uid ?: return
        firestore.collection(USERS_PATH).document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val coins = document.getLong("coins")?.toInt() ?: 0
                    callback(true, coins.toString())
                } else {
                    callback(false, "No found")
                }
            }
            .addOnFailureListener {
                callback(false, "Failed")
            }
    }



    // Get all users of a specific role
    fun getUsersByRole(role: String, callback: (List<Map<String, Any>>?, String?) -> Unit) {
        firestore.collection("users").whereEqualTo("role", role).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val users = task.result?.documents?.mapNotNull { it.data }
                    callback(users, null)
                } else {
                    callback(null, task.exception?.localizedMessage)
                }
            }
    }

    // Get transaction history
    fun getTransactionHistory(userId: String, callback: (List<Map<String, Any>>?, String?) -> Unit) {
        firestore.collection("transactions")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val transactions = task.result?.documents?.mapNotNull { it.data }
                    callback(transactions, null)
                } else {
                    callback(null, task.exception?.localizedMessage)
                }
            }
    }

    fun getBets(slot: String ,callback: (Boolean, List<com.example.lottery.data.model.Bet>?, String?) -> Unit) {
        firestore.collection(ACTIVE_BETS_PATH)
            .whereEqualTo("slot", slot)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val bets = task.result?.toObjects(com.example.lottery.data.model.Bet::class.java)
                    callback(true, bets, null)
                } else {
                    callback(false, null, task.exception?.localizedMessage)
                }
            }
    }
    // Add transaction details
    fun addTransaction(transaction: com.example.lottery.data.model.Transaction, callback: (Boolean, String?) -> Unit) {
        firestore.collection(TRANSACTIONS_PATH).add(transaction)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, null)
                } else {
                    callback(false, task.exception?.localizedMessage)
                }
            }
    }

    fun getTransactionHistoryByUID(userId: String, callback: (Boolean, List<com.example.lottery.data.model.Transaction>?, String?) -> Unit, status: String) {
        firestore.collection(TRANSACTIONS_PATH)
            .whereEqualTo("userId", userId)
            .whereEqualTo("status", status)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val transactions = task.result?.toObjects(com.example.lottery.data.model.Transaction::class.java)
                    callback(true, transactions, null)
                } else {
                    callback(false, null, task.exception?.localizedMessage)
                }
            }
    }


    // Retrieve results from Firestore
    fun getResults(callback: (List<Map<String, Any>>?, String?) -> Unit) {
        database.child("results").get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val results = task.result?.children?.map { snapshot ->
                        snapshot.value as Map<String, Any>
                    }
                    callback(results, null)
                } else {
                    callback(null, task.exception?.message)
                }
            }
    }

    // Add result to Firestore
    fun addResult(resultData: Map<String, Any>, callback: (Boolean, String?) -> Unit) {
        // Generate a unique key for the result
        val resultId = database.child("results").push().key

        if (resultId != null) {
            // Add result to the database
            database.child("results").child(resultId).setValue(resultData)
                .addOnSuccessListener {
                    callback(true, null)
                }
                .addOnFailureListener { error ->
                    callback(false, error.message)
                }
        } else {
            callback(false, "Failed to generate result ID")
        }
    }

    // Get refund requests
    fun getRefundRequests(callback: (List<Map<String, Any>>?, String?) -> Unit) {
        firestore.collection("refunds").get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val refunds = task.result?.documents?.mapNotNull { it.data }
                    callback(refunds, null)
                } else {
                    callback(null, task.exception?.localizedMessage)
                }
            }
    }

    // Approve refund
    fun approveRefund(refundId: String, callback: (Boolean, String?) -> Unit) {
        firestore.collection("refunds").document(refundId).update("status", "approved")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, null)
                } else {
                    callback(false, task.exception?.localizedMessage)
                }
            }
    }
    // Fetch requests
    fun getRequests(requestType: String, callback: (List<Request>?, String?) -> Unit) {
        val requestsRef = realtimeDatabase.getReference(requestType)
        requestsRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val requests = task.result?.children?.map { snapshot ->
                    Request(
                        requestId =  snapshot.key ?: "",
                        userId = snapshot.child("userId").value.toString(),
                        amount = snapshot.child("amount").value.toString().toInt()
                    )
                }
                callback(requests, null)
            } else {
                callback(null, task.exception?.message)
            }
        }
    }

    // Add coins to user
    fun addCoinsToUser(userId: String, amount: Int, callback: (Boolean, String?) -> Unit) {
        val userRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId).child("coins")

        userRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                // Get the current number of coins or default to 0
                val currentCoins = currentData.getValue(Int::class.java) ?: 0

                // Update the coins
                currentData.value = currentCoins + amount

                return Transaction.success(currentData) // Return success
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?) {
                if (committed) {
                    callback(true, null) // Transaction was successful
                } else {
                    callback(false, error?.message) // Transaction failed
                }
            }
        })
    }

    // Remove request
    fun removeRequest(requestId: String, requestType: String, callback: (Boolean, String?) -> Unit) {
        val requestRef = realtimeDatabase.getReference(requestType).child(requestId)
        requestRef.removeValue().addOnCompleteListener { task ->
            callback(task.isSuccessful, task.exception?.message)
        }
    }
    fun getPurchaseRequests(retailerId: String, callback: (List<PurchaseRequest>?, String?) -> Unit) {
        database.child("purchaseRequests").orderByChild("retailerId").equalTo(retailerId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val requestsList = mutableListOf<PurchaseRequest>()
                    for (requestSnapshot in snapshot.children) {
                        val request = requestSnapshot.getValue(PurchaseRequest::class.java)
                        if (request != null) {
                            // Assuming PurchaseRequest has a field called coinAmount
                            val coinAmount = requestSnapshot.child("coinAmount").getValue(Int::class.java) ?: 0
                            request.coinAmount = coinAmount // Set the coinAmount in the request object
                            requestsList.add(request)
                        }
                    }
                    callback(requestsList, null)
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(null, error.message)
                }
            })
    }
    fun addPurchaseRequest(purchaseRequest: Map<String, Any>, callback: (Boolean, String?) -> Unit) {
        // Generate a unique key for the purchase request
        val requestId = database.child("purchaseRequests").push().key

        // Check if the key is not null
        if (requestId != null) {
            // Set the purchase request data in the database
            database.child("purchaseRequests").child(requestId).setValue(purchaseRequest)
                .addOnSuccessListener {
                    // Successfully added the purchase request
                    callback(true, null)
                }
                .addOnFailureListener { error ->
                    // Failed to add the purchase request
                    callback(false, error.message)
                }
        } else {
            // Handle the case where the request ID is null
            callback(false, "Failed to generate request ID")
        }
    }
    fun startNewBetRound() {
        val roundId = FirebaseDatabase.getInstance().reference.push().key ?: "round_${System.currentTimeMillis()}"
        val roundData = mapOf(
            "betId" to roundId,
            "status" to "ongoing",
            "startTime" to System.currentTimeMillis(),
            "endTime" to System.currentTimeMillis() + 300000, // 5 minutes later
            "entries" to mapOf<String, Any>()
        )
        FirebaseDatabase.getInstance().reference.child("bets").child("currentRound").setValue(roundData)
    }
}