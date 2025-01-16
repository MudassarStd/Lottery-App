package com.example.lottery.player

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.*
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
    private lateinit var tvTimer: TextView
    private lateinit var gridLayout: GridLayout

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var userRef: DatabaseReference
    private lateinit var betsRef: DatabaseReference

    private var selectedNumber: String? = null
    private val bets = mutableListOf<Pair<String, Int>>()
    private lateinit var betsAdapter: BetsAdapter

    private var countdownTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pplace_bet)

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        userRef = database.getReference("users").child(firebaseAuth.currentUser?.uid ?: "")
        betsRef = database.getReference("bets")

        // Initialize UI components
        tvCoinBalance = findViewById(R.id.tvCoinBalance)
        etBetAmount = findViewById(R.id.etBetAmount)
        btnSubmitBet = findViewById(R.id.btnSubmitBet)
        rvBetsList = findViewById(R.id.rvBetsList)
        tvNoBetsMessage = findViewById(R.id.tvNoBetsMessage)
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

        // Start the timer initially
        startNewBetRound()

        // Handle bet submission
        btnSubmitBet.setOnClickListener {
            submitBet()
        }
    }

    private fun loadCoinBalance() {
        userRef.child("coins").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val coins = snapshot.getValue(Int::class.java) ?: 0
                tvCoinBalance.text = "Available Coins: $coins"
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@P_PlaceBet, "Failed to load coin balance", Toast.LENGTH_SHORT).show()
            }
        })
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
        val currentRoundRef = betsRef.child("currentRound")
        currentRoundRef.get().addOnSuccessListener { snapshot ->
            if (!snapshot.exists()) {
                val roundId = betsRef.push().key ?: "round_${System.currentTimeMillis()}"
                val roundData = mapOf(
                    "betId" to roundId,
                    "startTime" to System.currentTimeMillis(),
                    "endTime" to System.currentTimeMillis() + 120000, // 2 minutes later
                    "entries" to mapOf<String, Any>(),
                    "result" to null
                )
                currentRoundRef.setValue(roundData)
                startCountdown(120000)
            } else {
                val endTime = snapshot.child("endTime").getValue(Long::class.java) ?: 0L
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

        userRef.child("coins").get().addOnSuccessListener { snapshot ->
            val currentCoins = snapshot.getValue(Int::class.java) ?: 0

            if (currentCoins < betAmount) {
                Toast.makeText(this, "Insufficient coins. Please top up.", Toast.LENGTH_SHORT).show()
                return@addOnSuccessListener
            }

            val currentRoundRef = betsRef.child("currentRound")
            val betData = mapOf(
                "choice" to selectedNumber,
                "amount" to betAmount
            )

            currentRoundRef.child("entries").child(userId).setValue(betData).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Bet placed successfully!", Toast.LENGTH_SHORT).show()
                    bets.add(Pair(selectedNumber!!, betAmount))
                    betsAdapter.notifyDataSetChanged()
                    etBetAmount.text.clear()
                    resetNumberSelection()

                    // Deduct coins
                    userRef.child("coins").setValue(currentCoins - betAmount)
                } else {
                    Toast.makeText(this, "Failed to place bet.", Toast.LENGTH_SHORT).show()
                }
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to load coin balance.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun calculateBetResults() {
        val currentRoundRef = betsRef.child("currentRound")
        currentRoundRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val entries = snapshot.child("entries").children
                val winningChoice = determineWinner()

                val payouts = mutableMapOf<String, Int>()
                for (entry in entries) {
                    val userId = entry.key ?: continue
                    val choice = entry.child("choice").getValue(String::class.java) ?: continue
                    val amount = entry.child("amount").getValue(Int::class.java) ?: continue

                    payouts[userId] = if (choice == winningChoice) amount * 2 else 0
                }

                val resultData = mapOf(
                    "status" to "completed",
                    "result" to winningChoice,
                    "payouts" to payouts
                )
                currentRoundRef.updateChildren(resultData)

                val roundId = snapshot.child("betId").getValue(String::class.java) ?: ""
                betsRef.child("history").child(roundId).setValue(snapshot.value)

                startNewBetRound()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to calculate results.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun determineWinner(): String {
        return (1..100).random().toString()
    }
}
