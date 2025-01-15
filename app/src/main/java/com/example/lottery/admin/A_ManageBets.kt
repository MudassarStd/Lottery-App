package com.example.lottery.admin

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.lottery.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class A_ManageBets : AppCompatActivity() {
    private lateinit var lvBets: ListView
    private lateinit var btnDeclareResult: Button
    private lateinit var btnResolveIssues: Button

    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var betsRef: DatabaseReference

    private var selectedBetId: String? = null
    private val betsList = mutableListOf<String>()
    private val betIds = mutableListOf<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_amanage_bets)
        lvBets = findViewById(R.id.lvBets)
        btnDeclareResult = findViewById(R.id.btnDeclareResult)
        btnResolveIssues = findViewById(R.id.btnResolveIssues)

        firebaseDatabase = FirebaseDatabase.getInstance()
        betsRef = firebaseDatabase.getReference("bets")

        loadBets()

        lvBets.setOnItemClickListener { _, _, position, _ ->
            selectedBetId = betIds[position]
            Toast.makeText(this, "Selected Bet: ${betsList[position]}", Toast.LENGTH_SHORT).show()
        }

        btnDeclareResult.setOnClickListener {
            if (selectedBetId == null) {
                Toast.makeText(this, "Please select a bet first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            declareResult(selectedBetId!!)
        }

        btnResolveIssues.setOnClickListener {
            if (selectedBetId == null) {
                Toast.makeText(this, "Please select a bet first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            resolveIssues(selectedBetId!!)
        }
    }

    private fun loadBets() {
        betsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                betsList.clear()
                betIds.clear()

                for (bet in snapshot.children) {
                    val betInfo = bet.child("info").value.toString()
                    betsList.add(betInfo)
                    betIds.add(bet.key ?: "")
                }

                val adapter = ArrayAdapter(this@A_ManageBets, android.R.layout.simple_list_item_1, betsList)
                lvBets.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@A_ManageBets, "Failed to load bets", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun declareResult(betId: String) {
        betsRef.child(betId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val resultNumber = (0..99).random()

                    // Debug logging
                    Toast.makeText(this@A_ManageBets, "Generated Result: $resultNumber", Toast.LENGTH_SHORT).show()

                    val updates = mapOf(
                        "result" to resultNumber,
                        "status" to "resolved" // Optional status update
                    )

                    snapshot.ref.updateChildren(updates).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this@A_ManageBets, "Result declared: $resultNumber", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@A_ManageBets, "Failed to declare result: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this@A_ManageBets, "Bet not found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@A_ManageBets, "Failed to fetch bet details", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun resolveIssues(betId: String) {
        AlertDialog.Builder(this)
            .setTitle("Resolve Bet Issues")
            .setMessage("Manual resolution required for Bet ID: $betId.")
            .setPositiveButton("Resolve") { _, _ ->
                Toast.makeText(this, "Bet issue resolved", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}