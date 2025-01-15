package com.example.lottery.retailer

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
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

class R_Refund : AppCompatActivity() {
    private lateinit var lvRefundRequests: ListView
    private lateinit var btnApproveRefund: Button
    private lateinit var btnDenyRefund: Button

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var refundsRef: DatabaseReference

    private var selectedRefundId: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rrefund)
        // Initialize UI components
        lvRefundRequests = findViewById(R.id.lvRefundRequests)
        btnApproveRefund = findViewById(R.id.btnApproveRefund)
        btnDenyRefund = findViewById(R.id.btnDenyRefund)

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance()
        refundsRef = FirebaseDatabase.getInstance().getReference("refunds")

        loadRefundRequests()

        btnApproveRefund.setOnClickListener {
            if (selectedRefundId == null) {
                Toast.makeText(this, "Please select a refund request first", Toast.LENGTH_SHORT).show()
            } else {
                updateRefundStatus(selectedRefundId!!, "Approved")
            }
        }

        btnDenyRefund.setOnClickListener {
            if (selectedRefundId == null) {
                Toast.makeText(this, "Please select a refund request first", Toast.LENGTH_SHORT).show()
            } else {
                updateRefundStatus(selectedRefundId!!, "Denied")
            }
        }
    }

    private fun loadRefundRequests() {
        val retailerId = firebaseAuth.currentUser?.uid ?: return

        refundsRef.orderByChild("retailerId").equalTo(retailerId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val refundList = mutableListOf<String>()
                    val refundIds = mutableListOf<String>()

                    for (refund in snapshot.children) {
                        val playerId = refund.child("playerId").value.toString()
                        val refundAmount = refund.child("amount").value.toString()
                        refundList.add("Player: $playerId, Amount: $refundAmount")
                        refundIds.add(refund.key ?: "")
                    }

                    val adapter = ArrayAdapter(
                        this@R_Refund,
                        android.R.layout.simple_list_item_1,
                        refundList
                    )
                    lvRefundRequests.adapter = adapter

                    lvRefundRequests.setOnItemClickListener { _, _, position, _ ->
                        selectedRefundId = refundIds[position]
                        Toast.makeText(this@R_Refund, "Selected refund request: ${refundList[position]}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@R_Refund, "Failed to load refund requests", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun updateRefundStatus(refundId: String, status: String) {
        refundsRef.child(refundId).child("status").setValue(status)
            .addOnSuccessListener {
                Toast.makeText(this, "Refund $status successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to update refund status", Toast.LENGTH_SHORT).show()
            }
    }
}