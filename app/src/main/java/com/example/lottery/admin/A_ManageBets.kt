package com.example.lottery.admin

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.lottery.R
import com.google.firebase.database.*

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

        // Initialize UI components
        lvBets = findViewById(R.id.lvBets)
        btnDeclareResult = findViewById(R.id.btnDeclareResult)
        btnResolveIssues = findViewById(R.id.btnResolveIssues)

        // Initialize Firebase references
        firebaseDatabase = FirebaseDatabase.getInstance()
        betsRef = firebaseDatabase.getReference("bets")

        // Load bets dynamically
        loadBets()

        // Handle bet selection
        lvBets.setOnItemClickListener { _, _, position, _ ->
            selectedBetId = betIds[position]
            Toast.makeText(this, "Selected Bet: ${betsList[position]}", Toast.LENGTH_SHORT).show()
        }

        // Declare result for the selected bet
        btnDeclareResult.setOnClickListener {
            if (selectedBetId == null) {
                Toast.makeText(this, "Please select a bet first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            declareResult(selectedBetId!!)
        }

        // Resolve issues for the selected bet
        btnResolveIssues.setOnClickListener {
            if (selectedBetId == null) {
                Toast.makeText(this, "Please select a bet first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            resolveIssues(selectedBetId!!)
        }
    }

    /**
     * Load all bets dynamically from Firebase and update the ListView.
     */
    private fun loadBets() {
        betsList.clear()
        betIds.clear()

        val slots = listOf("morning", "afternoon", "evening")
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, betsList)
        lvBets.adapter = adapter

        slots.forEach { slot ->
            betsRef.child(slot).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (bet in snapshot.children) {
                        val betKey = bet.key ?: "Unknown"
                        val amount = bet.child("amount").value ?: "N/A"
                        val betId = "$slot|$betKey"

                        betsList.add("Slot: $slot, User: $betKey, Amount: $amount")
                        betIds.add(betId)
                    }
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@A_ManageBets, "Failed to load bets: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    /**
     * Declare a result for the selected bet.
     */
    private fun declareResult(betId: String) {
        val slotAndId = betId.split("|")
        if (slotAndId.size < 2) {
            Toast.makeText(this, "Invalid bet ID", Toast.LENGTH_SHORT).show()
            return
        }
        val slot = slotAndId[0]
        val userId = slotAndId[1]

        betsRef.child(slot).child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val resultNumber = (0..99).random() // Generate a random result

                    val updates = mapOf(
                        "result" to resultNumber,
                        "status" to "resolved"
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
                Toast.makeText(this@A_ManageBets, "Failed to fetch bet details: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    /**
     * Resolve issues for the selected bet manually.
     */
    private fun resolveIssues(betId: String) {
        val slotAndId = betId.split("|")
        if (slotAndId.size < 2) {
            Toast.makeText(this, "Invalid bet ID", Toast.LENGTH_SHORT).show()
            return
        }
        val slot = slotAndId[0]
        val userId = slotAndId[1]

        AlertDialog.Builder(this)
            .setTitle("Resolve Bet Issues")
            .setMessage("Manual resolution required for Bet ID: $betId.")
            .setPositiveButton("Resolve") { _, _ ->
                betsRef.child(slot).child(userId).child("status").setValue("resolved")
                    .addOnSuccessListener {
                        Toast.makeText(this, "Bet issue resolved successfully", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to resolve issue: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
