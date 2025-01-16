package com.example.lottery.player

import android.os.Bundle
import android.os.CountDownTimer
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lottery.BetsAdapter
import com.example.lottery.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class P_PlaceBet : AppCompatActivity() {
    private lateinit var tvCoinBalance: TextView
    private lateinit var etBetAmount: EditText
    private lateinit var btnSubmitBet: Button
    private lateinit var rvBetsList: RecyclerView
    private lateinit var tvTimer: TextView
    private lateinit var gridLayout: GridLayout

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private var selectedNumber: String? = null
    private val bets = mutableListOf<Pair<String, Int>>()
    private lateinit var betsAdapter: BetsAdapter

    private var countdownTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pplace_bet)

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Initialize UI components
        tvCoinBalance = findViewById(R.id.tvCoinBalance)
        etBetAmount = findViewById(R.id.etBetAmount)
        btnSubmitBet = findViewById(R.id.btnSubmitBet)
        rvBetsList = findViewById(R.id.rvBetsList)
        tvTimer = findViewById(R.id.tvTimer)
        gridLayout = findViewById(R.id.gridNumbers)

        // Set up RecyclerView
        betsAdapter = BetsAdapter(bets)
        rvBetsList.layoutManager = LinearLayoutManager(this)
        rvBetsList.adapter = betsAdapter

        // Load user's coin balance
        loadCoinBalance()

        // Set up number selection grid
        setupNumberSelection()

        // Start the timer for a new bet round
        startNewBetRound()

        // Handle bet submission
        btnSubmitBet.setOnClickListener {
            submitBet()
        }
    }

    private fun loadCoinBalance() {
        val userId = firebaseAuth.currentUser?.uid ?: return
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val coins = document.getLong("coins")?.toInt() ?: 0
                    tvCoinBalance.text = "Available Coins: $coins"
                } else {
                    Toast.makeText(this, "User data not found.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load coin balance.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupNumberSelection() {
        for (i in 0 until gridLayout.childCount) {
            val child = gridLayout.getChildAt(i)
            if (child is TextView) {
                child.setOnClickListener {
                    resetNumberSelection()
                    child.setBackgroundColor(ContextCompat.getColor(this, R.color.black))
                    child.setTextColor(ContextCompat.getColor(this, R.color.white))
                    selectedNumber = child.text.toString()
                    Toast.makeText(this, "Selected Number: $selectedNumber", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun resetNumberSelection() {
        for (i in 0 until gridLayout.childCount) {
            val child = gridLayout.getChildAt(i)
            if (child is TextView) {
                child.setBackgroundResource(R.drawable.selector_bg_with_border)
                child.setTextColor(ContextCompat.getColor(this, R.color.black))
            }
        }
        selectedNumber = null
    }

    private fun startNewBetRound() {
        val currentRoundRef = firestore.collection("bets").document("currentRound")
        currentRoundRef.get().addOnSuccessListener { document ->
            if (!document.exists()) {
                val roundData = mapOf(
                    "startTime" to System.currentTimeMillis(),
                    "endTime" to System.currentTimeMillis() + 120000, // 2 minutes later
                    "entries" to mapOf<String, Any>()
                )
                currentRoundRef.set(roundData)
                startCountdown(120000)
            } else {
                val endTime = document.getLong("endTime") ?: 0L
                startCountdown(endTime - System.currentTimeMillis())
            }
        }
    }

    private fun startCountdown(timeInMillis: Long) {
        countdownTimer?.cancel()
        countdownTimer = object : CountDownTimer(timeInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = (millisUntilFinished / 1000) / 60
                val seconds = (millisUntilFinished / 1000) % 60
                tvTimer.text = "Time Remaining: $minutes:${seconds.toString().padStart(2, '0')}"
            }

            override fun onFinish() {
                tvTimer.text = "Round Over"
                calculateBetResults()
            }
        }.start()
    }

    private fun submitBet() {
        val userId = firebaseAuth.currentUser?.uid ?: return
        val betAmount = etBetAmount.text.toString().toIntOrNull()

        if (selectedNumber == null || betAmount == null || betAmount <= 0) {
            Toast.makeText(this, "Please select a number and enter a valid bet amount.", Toast.LENGTH_SHORT).show()
            return
        }

        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                val currentCoins = document.getLong("coins")?.toInt() ?: 0
                if (currentCoins < betAmount) {
                    Toast.makeText(this, "Insufficient coins. Please top up.", Toast.LENGTH_SHORT).show()
                } else {
                    val currentRoundRef = firestore.collection("bets").document("currentRound")
                    val betData = mapOf(
                        "choice" to selectedNumber,
                        "amount" to betAmount
                    )
                    currentRoundRef.collection("entries").document(userId).set(betData)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Bet placed successfully!", Toast.LENGTH_SHORT).show()
                            bets.add(Pair(selectedNumber!!, betAmount))
                            betsAdapter.notifyDataSetChanged()
                            etBetAmount.text.clear()
                            resetNumberSelection()
                            firestore.collection("users").document(userId)
                                .update("coins", currentCoins - betAmount)
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed to place bet.", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load coin balance.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun calculateBetResults() {
        val currentRoundRef = firestore.collection("bets").document("currentRound")
        currentRoundRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val entries = document.get("entries") as? Map<String, Map<String, Any>>
                val winningChoice = determineWinner()

                val payouts = entries?.mapValues { (_, bet) ->
                    val choice = bet["choice"] as? String
                    val amount = bet["amount"] as? Int ?: 0
                    if (choice == winningChoice) amount * 2 else 0
                }

                val resultData = mapOf(
                    "status" to "completed",
                    "result" to winningChoice,
                    "payouts" to payouts
                )
                currentRoundRef.update(resultData)
                Toast.makeText(this, "Round completed. Winner: $winningChoice", Toast.LENGTH_SHORT).show()
                startNewBetRound()
            }
        }
    }

    private fun determineWinner(): String {
        return (1..100).random().toString()
    }
}
