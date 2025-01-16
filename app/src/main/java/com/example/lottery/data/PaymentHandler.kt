package com.example.lottery.data

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class PaymentHandler (private val context: Context){
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val transactionsRef: DatabaseReference = firebaseDatabase.getReference("transactions")
    private val usersRef: DatabaseReference = firebaseDatabase.getReference("users")

    // Function to initiate a payment

    fun initiatePayment(
        recipientId: String,
        amount: Int,
        paymentMethod: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val currentUserId = firebaseAuth.currentUser?.uid

        if (currentUserId == null) {
            onFailure("User not authenticated.")
            return
        }

        if (amount <= 0) {
            onFailure("Invalid amount.")
            return
        }

        val transactionId = transactionsRef.push().key ?: ""
        val transaction = TransactionPayment(
            transactionId = transactionId,
            userId = currentUserId,
            recipientId = recipientId,
            amount = amount,
            type = "purchase"
        )

        transactionsRef.child(transactionId).setValue(transaction)
            .addOnSuccessListener {
                updateCoinBalance(recipientId, amount, onSuccess, onFailure)
            }
            .addOnFailureListener { exception ->
                onFailure("Transaction failed: ${exception.message}")
            }
    }

    // Function to update coin balance for recipient and sender
    private fun updateCoinBalance(
        recipientId: String,
        amount: Int,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        usersRef.child(recipientId).get().addOnSuccessListener { recipientSnapshot ->
            val currentRecipientCoins = recipientSnapshot.child("coins").value as? Int ?: 0
            val updatedRecipientCoins = currentRecipientCoins + amount

            usersRef.child(recipientId).child("coins").setValue(updatedRecipientCoins)
                .addOnSuccessListener {
                    onSuccess()
                }
                .addOnFailureListener { exception ->
                    onFailure("Failed to update recipient's coin balance: ${exception.message}")
                }
        }.addOnFailureListener { exception ->
            onFailure("Failed to retrieve recipient's coin balance: ${exception.message}")
        }
    }

    // Function to validate payment (mock implementation for demo purposes)
    fun validatePayment(paymentMethod: String, amount: Int): Boolean {
        // Add validation logic for specific payment methods if required
        return paymentMethod.isNotEmpty() && amount > 0
    }
}