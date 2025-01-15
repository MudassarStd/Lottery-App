package com.example.lottery.admin

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
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

class A_Refund : AppCompatActivity() {
    private lateinit var lvPlayerRefunds: ListView
    private lateinit var lvRetailerRefunds: ListView

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var refundRequestsRef: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_arefund)
        lvPlayerRefunds = findViewById(R.id.lvPlayerRefunds)
        lvRetailerRefunds = findViewById(R.id.lvRetailerRefunds)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()
        refundRequestsRef = firebaseDatabase.getReference("refundRequests")

        loadRefundRequests("players", lvPlayerRefunds)
        loadRefundRequests("retailers", lvRetailerRefunds)

        lvPlayerRefunds.setOnItemClickListener { _, _, position, _ ->
            handleRefundRequest("players", position)
        }

        lvRetailerRefunds.setOnItemClickListener { _, _, position, _ ->
            handleRefundRequest("retailers", position)
        }
    }

    private fun loadRefundRequests(userType: String, listView: ListView) {
        refundRequestsRef.orderByChild("userType").equalTo(userType)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val requestList = mutableListOf<String>()
                    val requestIds = mutableListOf<String>()

                    for (request in snapshot.children) {
                        val userId = request.child("userId").value.toString()
                        val amount = request.child("amount").value.toString()
                        requestList.add("User: $userId | Amount: $amount")
                        requestIds.add(request.key ?: "")
                    }

                    val adapter = ArrayAdapter(
                        this@A_Refund,
                        android.R.layout.simple_list_item_1,
                        requestList
                    )
                    listView.adapter = adapter

                    listView.tag = requestIds
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@A_Refund, "Failed to load requests", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun handleRefundRequest(userType: String, position: Int) {
        val listView = if (userType == "players") lvPlayerRefunds else lvRetailerRefunds
        val requestIds = listView.tag as List<String>
        val selectedRequestId = requestIds[position]

        AlertDialog.Builder(this)
            .setTitle("Approve Refund")
            .setMessage("Do you want to approve this refund request?")
            .setPositiveButton("Approve") { _, _ ->
                approveRefund(selectedRequestId)
            }
            .setNegativeButton("Deny") { _, _ ->
                denyRefund(selectedRequestId)
            }
            .setNeutralButton("Cancel", null)
            .show()
    }

    private fun approveRefund(requestId: String) {
        refundRequestsRef.child(requestId).child("status").setValue("approved")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Refund approved successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to approve refund", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun denyRefund(requestId: String) {
        refundRequestsRef.child(requestId).child("status").setValue("denied")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Refund denied successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to deny refund", Toast.LENGTH_SHORT).show()
                }
            }
    }
}