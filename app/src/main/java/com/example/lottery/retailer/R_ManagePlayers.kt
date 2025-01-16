package com.example.lottery.retailer

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.lottery.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class R_ManagePlayers : AppCompatActivity() {
    private lateinit var lvPlayers: ListView
    private lateinit var btnViewDetails: Button
    private lateinit var btnApproveCoinRequest: Button
    private lateinit var btnManageRefunds: Button

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private var selectedPlayerId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rmanage_players)

        // Initialize UI components
        lvPlayers = findViewById(R.id.lvPlayers)
        btnViewDetails = findViewById(R.id.btnViewDetails)
        btnApproveCoinRequest = findViewById(R.id.btnApproveCoinRequest)
        btnManageRefunds = findViewById(R.id.btnManageRefunds)

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        loadPlayers()

        // Set up button click listeners
        btnViewDetails.setOnClickListener {
            if (selectedPlayerId == null) {
                Toast.makeText(this, "Please select a player first", Toast.LENGTH_SHORT).show()
            } else {
                viewPlayerDetails(selectedPlayerId!!)
            }
        }

        btnApproveCoinRequest.setOnClickListener {
            if (selectedPlayerId == null) {
                Toast.makeText(this, "Please select a player first", Toast.LENGTH_SHORT).show()
            } else {
                approveCoinRequest(selectedPlayerId!!)
            }
        }

        btnManageRefunds.setOnClickListener {
            if (selectedPlayerId == null) {
                Toast.makeText(this, "Please select a player first", Toast.LENGTH_SHORT).show()
            } else {
                manageRefunds(selectedPlayerId!!)
            }
        }
    }

    private fun loadPlayers() {
        val retailerId = firebaseAuth.currentUser?.uid ?: return

        firestore.collection("players")
            .whereEqualTo("retailerId", retailerId)  // Only load players associated with the retailer
            .get()
            .addOnSuccessListener { snapshot ->
                val playersList = mutableListOf<String>()
                val playerIds = mutableListOf<String>()

                for (document in snapshot) {
                    val playerName = document.getString("name") ?: "Unknown"
                    playersList.add(playerName)
                    playerIds.add(document.id)
                }

                val adapter = ArrayAdapter(
                    this@R_ManagePlayers,
                    android.R.layout.simple_list_item_1,
                    playersList
                )
                lvPlayers.adapter = adapter

                lvPlayers.setOnItemClickListener { _, _, position, _ ->
                    selectedPlayerId = playerIds[position]
                    Toast.makeText(this@R_ManagePlayers, "Selected: ${playersList[position]}", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this@R_ManagePlayers, "Failed to load players.", Toast.LENGTH_SHORT).show()
            }
    }


    private fun viewPlayerDetails(playerId: String) {
        firestore.collection("players").document(playerId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val name = document.getString("name") ?: "Unknown"
                    val email = document.getString("email") ?: "Unknown"
                    val coins = document.getLong("coins")?.toInt() ?: 0

                    AlertDialog.Builder(this@R_ManagePlayers)
                        .setTitle("Player Details")
                        .setMessage("Name: $name\nEmail: $email\nCoins: $coins")
                        .setPositiveButton("Close", null)
                        .show()
                } else {
                    Toast.makeText(this@R_ManagePlayers, "Player details not found.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this@R_ManagePlayers, "Failed to fetch player details.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun approveCoinRequest(playerId: String) {
        val playerRef = firestore.collection("players").document(playerId)

        playerRef.get()
            .addOnSuccessListener { document ->
                if (document != null && document.contains("coinRequests")) {
                    val requestedCoins = document.getLong("coinRequests")?.toInt() ?: 0
                    val currentCoins = document.getLong("coins")?.toInt() ?: 0

                    playerRef.update(
                        mapOf(
                            "coins" to currentCoins + requestedCoins,
                            "coinRequests" to 0  // Clear the request
                        )
                    ).addOnSuccessListener {
                        Toast.makeText(this@R_ManagePlayers, "Coin request approved.", Toast.LENGTH_SHORT).show()
                    }.addOnFailureListener {
                        Toast.makeText(this@R_ManagePlayers, "Failed to approve request.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@R_ManagePlayers, "No coin requests found.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this@R_ManagePlayers, "Failed to fetch coin requests.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun manageRefunds(playerId: String) {
        // Refund management logic here
        Toast.makeText(this, "Managing refunds for $playerId", Toast.LENGTH_SHORT).show()
    }
}
