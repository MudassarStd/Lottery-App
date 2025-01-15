package com.example.lottery.player

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.lottery.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class P_Refund : AppCompatActivity() {
    private lateinit var etRefundAmount: EditText
    private lateinit var btnSubmitRefund: Button
    private lateinit var lvPendingRefunds: ListView

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var refundsRef: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prefund)
        etRefundAmount = findViewById(R.id.etRefundAmount)
        btnSubmitRefund = findViewById(R.id.btnSubmitRefund)
        lvPendingRefunds = findViewById(R.id.lvPendingRefunds)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()
        refundsRef = firebaseDatabase.getReference("refunds")

        loadPendingRefunds()

        btnSubmitRefund.setOnClickListener { submitRefundRequest() }
    }

    private fun loadPendingRefunds() {
        val playerId = firebaseAuth.currentUser?.uid ?: return

        refundsRef.orderByChild("playerId").equalTo(playerId).addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val refundList = mutableListOf<String>()

                snapshot.children.forEach { refund ->
                    val amount = refund.child("amount").value.toString()
                    val status = refund.child("status").value.toString()
                    refundList.add("Amount: $amount - Status: $status")
                }

                val adapter = ArrayAdapter(this@P_Refund, android.R.layout.simple_list_item_1, refundList)
                lvPendingRefunds.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@P_Refund, "Failed to load refunds", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun submitRefundRequest() {
        val refundAmount = etRefundAmount.text.toString().toDoubleOrNull()
        if (refundAmount == null || refundAmount <= 0) {
            Toast.makeText(this, "Enter a valid amount", Toast.LENGTH_SHORT).show()
            return
        }

        val playerId = firebaseAuth.currentUser?.uid ?: return

        val refundRequest = mapOf(
            "playerId" to playerId,
            "amount" to refundAmount,
            "status" to "Pending"
        )

        refundsRef.push().setValue(refundRequest).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Refund request submitted", Toast.LENGTH_SHORT).show()
                etRefundAmount.text.clear()
            } else {
                Toast.makeText(this, "Failed to submit refund request", Toast.LENGTH_SHORT).show()
            }
        }
    }
}