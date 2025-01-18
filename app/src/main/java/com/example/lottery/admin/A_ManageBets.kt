package com.example.lottery.admin

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.firebaseadminandroid.FCMSender
import com.example.lottery.R
import com.example.lottery.data.FirebaseRepository
import com.example.lottery.data.Result
import com.example.lottery.data.model.Bet
import com.example.lottery.databinding.ActivityAmanageBetsBinding
import com.example.lottery.utils.BetResultAlgorithm.determineBetResult
import com.example.lottery.utils.Constants.ACTIVE_BETS_PATH
import com.example.lottery.utils.Constants.RESULTS_PATH
import com.example.lottery.utils.Constants.STATUS_APPROVED
import com.example.lottery.utils.Constants.STATUS_PENDING
import com.example.lottery.utils.Constants.STATUS_REJECTED
import com.example.lottery.utils.Constants.USERS_PATH
import com.example.lottery.utils.DateTimeUtils.getCurrentBetTimeSlot
import com.example.lottery.utils.DateTimeUtils.getCurrentDateInLocalFormat
import com.example.lottery.utils.Extensions.hide
import com.example.lottery.utils.Extensions.show
import com.example.lottery.utils.Extensions.toResult
import com.google.firebase.firestore.FirebaseFirestore

@RequiresApi(Build.VERSION_CODES.O)
class A_ManageBets : AppCompatActivity() {

    private val binding by lazy { ActivityAmanageBetsBinding.inflate(layoutInflater) }
    private val firebaseRepository by lazy { FirebaseRepository() }

    private lateinit var lvBets: ListView
    private lateinit var btnDeclareResult: Button

    private var bets = listOf<Bet>()
    private val betsList = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


        FCMSender.sendFCM(this)

        initializeUI()
        setupListeners()
        initializeData()
    }

    private fun initializeUI() {
        lvBets = binding.lvBets
        btnDeclareResult = binding.btnDeclareResult
    }

    private fun setupListeners() {
        btnDeclareResult.setOnClickListener {
            val slot = when {
                binding.rbMorning.isChecked -> "morning"
                binding.rbNoon.isChecked -> "afternoon"
                binding.rbEvening.isChecked -> "evening"
                else -> null
            }

            slot?.let { slot ->
                val winningBet = determineBetResult(bets.filter { it.slot == slot })
                showToast("Winner: ${winningBet}")
                winningBet?.let {
                    updateResults(slot, it)
                } ?: showToast("Could not determine any winner, make sure there are active bets")
            } ?: showToast("Please select a valid slot")
        }

        binding.rbGroupRequests.setOnCheckedChangeListener { _, checkedId ->
            val slot = when (checkedId) {
                R.id.rbMorning -> "morning"
                R.id.rbNoon -> "afternoon"
                R.id.rbEvening -> "evening"
                else -> null
            }

            slot?.let {
                loadBets(it)
                binding.btnDeclareResult.text = "Declare ${it.capitalize()} Results"
            }
        }
    }

    private fun updateResults(slot: String, winningBet: Bet) {

        val firestore = FirebaseFirestore.getInstance()

        val betsCollection = firestore.collection(ACTIVE_BETS_PATH)
        val resultsCollection = firestore.collection(RESULTS_PATH)
        val userDoc = firestore.collection(USERS_PATH).document(winningBet.userId)

        // Step 1: Fetch necessary data outside the transaction
        betsCollection.whereEqualTo("slot", slot).get()
            .addOnSuccessListener { betsQuerySnapshot ->
                userDoc.get()
                    .addOnSuccessListener { userSnapshot ->
                        // Step 2: Perform the transaction with pre-fetched data
                        firestore.runTransaction { transaction ->
                            // Delete all bets for this slot
                            for (betDoc in betsQuerySnapshot.documents) {
                                transaction.delete(betDoc.reference)
                            }

                            // Add the winning bet to the results collection
                            transaction.set(resultsCollection.document(), winningBet.toResult())

                            // Update the winner's coin balance
                            val currentCoins = userSnapshot.getLong("coins") ?: 0L
                            val winningAmount = winningBet.choice.toInt() * 50
                            transaction.update(userDoc, "coins", currentCoins + winningAmount)
                        }.addOnSuccessListener {
                            showToast("Results updated successfully")
                            loadBets(slot) // Moved outside the transaction

                            // ------------- Send FCM Notification to all users of result declaration ---------------
                            // send winning notification to winningBet.userId.getFCMToken()



                        }.addOnFailureListener { exception ->
                            showToast("Error updating results: ${exception.message}")
                        }
                    }
                    .addOnFailureListener { exception ->
                        showToast("Error fetching user data: ${exception.message}")
                    }
            }
            .addOnFailureListener { exception ->
                showToast("Error fetching bets: ${exception.message}")
            }
    }



    private fun initializeData() {
        getCurrentBetTimeSlot()?.let {
            loadBets(it.displayName)
//            binding.btnDeclareResult.isEnabled = it.status == "Closed for results"
            binding.tvDeclareResults.text = when (it.status) {
                "Closed for results" -> "Betting closed, Declare results for ${it.displayName} slot"
                "Open" -> "Betting is going for ${it.displayName} slot"
                else -> ""
            }
        }
    }

    private fun loadBets(slot: String) {
        betsList.clear()
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, betsList)
        lvBets.adapter = adapter

        firebaseRepository.getBets(slot) { response, bets, errorMsg ->
            if (response) {
                bets?.let {
                    this.bets = it
                    betsList.addAll(it.map { bet ->
                        "User: ${bet.userId}, Choice: ${bet.choice}, Amount: ${bet.amount}, Slot: ${bet.slot}"
                    })

                    if (betsList.isEmpty()) binding.tvNoActiveBets.show() else binding.tvNoActiveBets.hide()
                }
                adapter.notifyDataSetChanged()
            } else {
                Toast.makeText(this, "Error loading bets: $errorMsg", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        initializeData()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

}
