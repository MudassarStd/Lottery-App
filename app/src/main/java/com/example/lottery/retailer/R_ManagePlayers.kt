package com.example.lottery.retailer

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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class R_ManagePlayers : AppCompatActivity() {
    private lateinit var lvPlayers: ListView
    private lateinit var btnViewDetails: Button
    private lateinit var btnApproveCoinRequest: Button
    private lateinit var btnManageRefunds: Button

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var playersRef: DatabaseReference

    private var selectedPlayerId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_rmanage_players)
        // Initialize UI components
        lvPlayers = findViewById(R.id.lvPlayers)
        btnViewDetails = findViewById(R.id.btnViewDetails)
        btnApproveCoinRequest = findViewById(R.id.btnApproveCoinRequest)
        btnManageRefunds = findViewById(R.id.btnManageRefunds)

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance()
        playersRef = FirebaseDatabase.getInstance().getReference("players")

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

        playersRef.orderByChild("retailerId").equalTo(retailerId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val playersList = mutableListOf<String>()
                    val playerIds = mutableListOf<String>()

                    for (player in snapshot.children) {
                        val playerName = player.child("name").value.toString()
                        playersList.add(playerName)
                        playerIds.add(player.key ?: "")
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

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@R_ManagePlayers, "Failed to load players", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun viewPlayerDetails(playerId: String) {
        playersRef.child(playerId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val name = snapshot.child("name").value.toString()
                val email = snapshot.child("email").value.toString()
                val coins = snapshot.child("coins").value.toString()

                AlertDialog.Builder(this@R_ManagePlayers)
                    .setTitle("Player Details")
                    .setMessage("Name: $name\nEmail: $email\nCoins: $coins")
                    .setPositiveButton("Close", null)
                    .show()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@R_ManagePlayers, "Failed to load details", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun approveCoinRequest(playerId: String) {
        playersRef.child(playerId).child("coinRequests").addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    playersRef.child(playerId).child("coins")
                        .setValue(snapshot.value.toString().toInt())
                        .addOnSuccessListener {
                            Toast.makeText(this@R_ManagePlayers, "Coin request approved", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this@R_ManagePlayers, "Failed to approve request", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this@R_ManagePlayers, "No coin requests found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@R_ManagePlayers, "Error approving request", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun manageRefunds(playerId: String) {
        // Refund management logic here
        Toast.makeText(this, "Managing refunds for $playerId", Toast.LENGTH_SHORT).show()
    }
}