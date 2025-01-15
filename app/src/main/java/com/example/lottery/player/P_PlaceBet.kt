package com.example.lottery.player

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.GridLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lottery.BetsAdapter
import com.example.lottery.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class P_PlaceBet : AppCompatActivity() {
    private lateinit var tvCoinBalance: TextView
    private lateinit var etBetAmount: EditText
    private lateinit var btnSubmitBet: Button
    private lateinit var rvBetsList: RecyclerView
    private lateinit var tvNoBetsMessage: TextView
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var betsRef: DatabaseReference
    private lateinit var gridLayout: GridLayout

    private var selectedNumber: String? = null
    private val bets = mutableListOf<Pair<String, Int>>() // Stores (Number, Amount)
    private lateinit var betsAdapter: BetsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pplace_bet)

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance()
        betsRef = FirebaseDatabase.getInstance().getReference("bets")

        // Initialize UI components
        tvCoinBalance = findViewById(R.id.tvCoinBalance)
        etBetAmount = findViewById(R.id.etBetAmount)
        btnSubmitBet = findViewById(R.id.btnSubmitBet)
        rvBetsList = findViewById(R.id.rvBetsList)
        tvNoBetsMessage = findViewById(R.id.tvNoBetsMessage)
        gridLayout = findViewById(R.id.gridNumbers)

        // Initialize RecyclerView
        betsAdapter = BetsAdapter(bets)
        rvBetsList.layoutManager = LinearLayoutManager(this)
        rvBetsList.adapter = betsAdapter

        // Load Player's Coin Balance
        loadCoinBalance()

        // Set up number selection grid
        setupNumberSelection()

        // Handle submit bet
        btnSubmitBet.setOnClickListener {
            submitBet()
        }

        // Start a new betting round if no round exists
        startNewBetRound()

        // Update visibility of bets
        updateBetsVisibility()
    }

    /**
     * Starts a new betting round by creating a new entry in the database.
     */
    private fun startNewBetRound() {
        val currentRoundRef = betsRef.child("currentRound")
        currentRoundRef.get().addOnSuccessListener { snapshot ->
            if (!snapshot.exists()) {
                val roundId = betsRef.push().key ?: "round_${System.currentTimeMillis()}"
                val roundData = mapOf(
                    "betId" to roundId,
                    "startTime" to System.currentTimeMillis(),
                    "endTime" to System.currentTimeMillis() + 300000, // 5 minutes later
                    "entries" to mapOf<String, Any>(),
                    "result" to null
                )
                currentRoundRef.setValue(roundData)
            }
        }
    }

    /**
     * Loads the player's current coin balance from the Firebase database.
     */
    private fun loadCoinBalance() {
        val userId = firebaseAuth.currentUser?.uid ?: return
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId)
        userRef.child("coins").get().addOnSuccessListener {
            val coins = it.value.toString()
            tvCoinBalance.text = "Available Coins: $coins"
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to load coin balance", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Sets up click listeners for each number in the grid layout.
     */
    private fun setupNumberSelection() {
        for (i in 0 until gridLayout.childCount) {
            val child = gridLayout.getChildAt(i)
            if (child is TextView) {
                child.setOnClickListener {
                    resetNumberSelection() // Reset previous selection
                    child.setBackgroundColor(ContextCompat.getColor(this, R.color.black)) // Highlight selected number
                    child.setTextColor(ContextCompat.getColor(this, R.color.white))
                    selectedNumber = child.text.toString()
                    Toast.makeText(this, "Selected Number: $selectedNumber", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * Resets the number selection in the grid layout.
     */
    private fun resetNumberSelection() {
        for (i in 0 until gridLayout.childCount) {
            val child = gridLayout.getChildAt(i)
            if (child is TextView) {
                child.setBackgroundResource(R.drawable.selector_bg_with_border) // Reset background
                child.setTextColor(ContextCompat.getColor(this, R.color.black))
            }
        }
        selectedNumber = null
    }

    /**
     * Submits a bet to the current round.
     */
    private fun submitBet() {
        val userId = firebaseAuth.currentUser?.uid ?: return
        val betAmount = etBetAmount.text.toString().toIntOrNull()

        if (selectedNumber == null || betAmount == null || betAmount <= 0) {
            Toast.makeText(this, "Please select a number and enter a valid bet amount", Toast.LENGTH_SHORT).show()
            return
        }

        val currentRoundRef = betsRef.child("currentRound")
        currentRoundRef.child("entries").child(userId).setValue(
            mapOf(
                "choice" to selectedNumber,
                "amount" to betAmount
            )
        ).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Bet placed successfully!", Toast.LENGTH_SHORT).show()
                bets.add(Pair(selectedNumber!!, betAmount))
                betsAdapter.notifyDataSetChanged()
                updateBetsVisibility()
                etBetAmount.text.clear()
                resetNumberSelection()
            } else {
                val errorMessage = task.exception?.message ?: "Unknown error"
                Toast.makeText(this, "Failed to place bet: $errorMessage", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Calculates the results of the current betting round and updates the database.
     */
    private fun calculateBetResults() {
        val currentRoundRef = betsRef.child("currentRound")

        currentRoundRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val entries = snapshot.child("entries").children
                val winningChoice = determineWinner()

                val payouts = mutableMapOf<String, Int>()
                for (entry in entries) {
                    val userId = entry.key ?: continue
                    val choice = entry.child("choice").value.toString()
                    val amount = entry.child("amount").value.toString().toInt()

                    payouts[userId] = if (choice == winningChoice) amount * 2 else 0
                }

                val resultData = mapOf(
                    "status" to "completed",
                    "result" to winningChoice,
                    "payouts" to payouts
                )
                currentRoundRef.updateChildren(resultData)

                // Archive the completed round
                val roundId = snapshot.child("betId").value.toString()
                betsRef.child("history").child(roundId).setValue(snapshot.value)

                // Start a new round
                startNewBetRound()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to calculate bet results", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Determines the winning number randomly.
     */
    private fun determineWinner(): String {
        return (1..10).random().toString() // Replace with your logic
    }

    /**
     * Updates the visibility of the bets list or "No Bets" message.
     */
    private fun updateBetsVisibility() {
        if (bets.isEmpty()) {
            tvNoBetsMessage.visibility = View.VISIBLE
            rvBetsList.visibility = View.GONE
        } else {
            tvNoBetsMessage.visibility = View.GONE
            rvBetsList.visibility = View.VISIBLE
        }
    }
}
