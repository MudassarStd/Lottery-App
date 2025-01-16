package com.example.lottery.player

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lottery.BetsAdapter
import com.example.lottery.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class P_PlaceBet : AppCompatActivity() {

    // UI components
    private lateinit var tvCoinBalance: TextView
    private lateinit var etBetAmount: EditText
    private lateinit var btnSubmitBet: Button
    private lateinit var rvBetsList: RecyclerView
    private lateinit var tvTimer: TextView
    private lateinit var gridLayout: GridLayout
    private lateinit var slotSpinner: Spinner

    // Firebase
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private var selectedNumber: String? = null
    private val bets = mutableListOf<Pair<String, Int>>()
    private lateinit var betsAdapter: BetsAdapter
    private var selectedSlot: String = "morning"

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
        slotSpinner = findViewById(R.id.slotSpinner)

        // Setup UI and functionality
        setupSlotSpinner()
        loadCoinBalance()
        setupNumberSelection()
        setupRecyclerView()
        scheduleHourlyNotifications()

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

    private fun setupSlotSpinner() {
        val slots = listOf("morning", "afternoon", "evening")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, slots)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        slotSpinner.adapter = adapter

        slotSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedSlot = slots[position]
                loadBetsForSlot()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupRecyclerView() {
        betsAdapter = BetsAdapter(bets)
        rvBetsList.layoutManager = LinearLayoutManager(this)
        rvBetsList.adapter = betsAdapter
    }

    private fun loadBetsForSlot() {
        firestore.collection("bets").document(selectedSlot).get()
            .addOnSuccessListener { document ->
                bets.clear() // Clear old bets
                if (document.exists()) {
                    val entries = document.get("entries") as? Map<String, Map<String, Any>> ?: emptyMap()
                    for ((_, bet) in entries) {
                        val choice = bet["choice"] as? String ?: continue
                        val amount = (bet["amount"] as? Long)?.toInt() ?: continue
                        bets.add(Pair(choice, amount))
                    }
                    betsAdapter.notifyDataSetChanged()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load bets for $selectedSlot.", Toast.LENGTH_SHORT).show()
            }
    }



    private fun submitBet() {
        val betAmountText = etBetAmount.text.toString()
        val betAmount = betAmountText.toIntOrNull()
        val userId = firebaseAuth.currentUser?.uid

        if (selectedNumber == null) {
            Toast.makeText(this, "Please select a number.", Toast.LENGTH_SHORT).show()
            return
        }

        if (betAmount == null || betAmount <= 0) {
            Toast.makeText(this, "Enter a valid bet amount.", Toast.LENGTH_SHORT).show()
            return
        }

        if (userId == null) {
            Toast.makeText(this, "User not authenticated.", Toast.LENGTH_SHORT).show()
            return
        }

        firestore.collection("bets").document(selectedSlot).get()
            .addOnSuccessListener { document ->
                val betEntry = mapOf(
                    "choice" to selectedNumber!!,
                    "amount" to betAmount,
                    "timestamp" to System.currentTimeMillis()
                )

                // Check if document exists
                if (document.exists()) {
                    firestore.collection("bets").document(selectedSlot)
                        .update("entries.$userId", betEntry)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Bet placed successfully!", Toast.LENGTH_SHORT).show()
                            bets.add(Pair(selectedNumber!!, betAmount))
                            betsAdapter.notifyDataSetChanged()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed to place bet.", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    val initialData = mapOf(
                        "entries" to mapOf(userId to betEntry)
                    )
                    firestore.collection("bets").document(selectedSlot)
                        .set(initialData)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Bet placed successfully!", Toast.LENGTH_SHORT).show()
                            bets.add(Pair(selectedNumber!!, betAmount))
                            betsAdapter.notifyDataSetChanged()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed to place bet.", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error accessing Firestore.", Toast.LENGTH_SHORT).show()
            }

    }

    private fun placeBet(userId: String, betAmount: Int) {
        firestore.collection("users").document(userId).get().addOnSuccessListener { document ->
            val currentCoins = document.getLong("coins")?.toInt() ?: 0

            if (betAmount > currentCoins) {
                Toast.makeText(this, "Insufficient coins.", Toast.LENGTH_SHORT).show()
                return@addOnSuccessListener
            }

            val newBalance = currentCoins - betAmount
            val betEntry = mapOf(
                "choice" to selectedNumber!!,
                "amount" to betAmount,
                "timestamp" to System.currentTimeMillis()
            )

            // Update coins only if bet update is successful
            firestore.collection("bets").document(selectedSlot)
                .update("entries.$userId", betEntry)
                .addOnSuccessListener {
                    firestore.collection("users").document(userId).update("coins", newBalance)
                    tvCoinBalance.text = "Available Coins: $newBalance"
                    Toast.makeText(this, "Bet placed successfully!", Toast.LENGTH_SHORT).show()
                    bets.add(Pair(selectedNumber!!, betAmount))
                    betsAdapter.notifyDataSetChanged()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to place bet.", Toast.LENGTH_SHORT).show()
                }
        }
    }


    private fun scheduleHourlyNotifications() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val notificationIntent = Intent(this, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.HOUR, 1)
        }

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_HOUR,
            pendingIntent
        )
    }
}

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "BET_NOTIFICATIONS"

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Bet Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle("Hourly Bet Reminder")
            .setContentText("Place your bets now!")
            .setSmallIcon(R.drawable.ic_notification)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1, notification)
    }
}
