package com.example.lottery.admin

import android.os.Build
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.lottery.R
import com.example.lottery.data.FirebaseRepository
import com.example.lottery.databinding.ActivityAmanageBetsBinding
import com.example.lottery.utils.Constants.STATUS_APPROVED
import com.example.lottery.utils.Constants.STATUS_PENDING
import com.example.lottery.utils.Constants.STATUS_REJECTED
import com.example.lottery.utils.DateTimeUtils.getCurrentBetTimeSlot
import com.example.lottery.utils.Extensions.hide
import com.example.lottery.utils.Extensions.show
import com.google.firebase.database.*

@RequiresApi(Build.VERSION_CODES.O)
class A_ManageBets : AppCompatActivity() {
    private val binding by lazy { ActivityAmanageBetsBinding.inflate(layoutInflater) }
    private lateinit var lvBets: ListView
    private lateinit var btnDeclareResult: Button
//    private lateinit var btnResolveIssues: Button

    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var betsRef: DatabaseReference

    private val firebaseRepository by lazy { FirebaseRepository() }

    private var selectedBetId: String? = null
    private val betsList = mutableListOf<String>()
    private val betIds = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Initialize UI components
        lvBets = findViewById(R.id.lvBets)
        btnDeclareResult = findViewById(R.id.btnDeclareResult)
//        btnResolveIssues = findViewById(R.id.btnResolveIssues)

        // Initialize Firebase references
        firebaseDatabase = FirebaseDatabase.getInstance()
        betsRef = firebaseDatabase.getReference("bets")

        // Load bets dynamically

        // Handle bet selection
//        lvBets.setOnItemClickListener { _, _, position, _ ->
//            selectedBetId = betIds[position]
//            Toast.makeText(this, "Selected Bet: ${betsList[position]}", Toast.LENGTH_SHORT).show()
//        }

        // Declare result for the selected bet
        btnDeclareResult.setOnClickListener {
            if (selectedBetId == null) {
                Toast.makeText(this, "Please select a bet first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            declareResult(selectedBetId!!)
        }

        // Resolve issues for the selected bet
//        btnResolveIssues.setOnClickListener {
//            if (selectedBetId == null) {
//                Toast.makeText(this, "Please select a bet first", Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }
//            resolveIssues(selectedBetId!!)
//        }
        val currentSlot = getCurrentBetTimeSlot()

        currentSlot?.let {
            loadBets(it.displayName)
            if (it.displayName == "morning") {
                binding.rbMorning.isChecked = true
            }
            if (it.displayName == "afternoon") {
                binding.rbMorning.isChecked = true
            }
            if (it.displayName == "evening") {
                binding.rbMorning.isChecked = true
            }
        }

        binding.rbGroupRequests.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.rbMorning -> {
                    loadBets("morning")
                    binding.btnDeclareResult.text = "Declare Morning Results"
                }

                R.id.rbNoon -> {
                    loadBets("afternoon")
                    binding.btnDeclareResult.text = "Declare Afternoon Results"
                }

                R.id.rbEvening -> {
                    loadBets("evening")
                    binding.btnDeclareResult.text = "Declare Evening Results"
                }

                else -> {
                    // Handle the case where no radio button is selected (optional)
                }
            }
        }
    }

    private fun loadBets(slot: String) {
        betsList.clear()
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, betsList)
        lvBets.adapter = adapter
            firebaseRepository.getBets(slot) { response, bets, errorMsg ->
                if (response) {
                    val betStrings = bets?.map { bet ->
                        "User: ${1}, Choice: ${bet.choice}, Amount: ${bet.amount}, Slot: ${bet.slot}"
                    }

                    // Update the betsList with the mapped strings
                    if (betStrings != null) {
                        if (betStrings.isEmpty()) {
                            binding.tvNoActiveBets.show()
                        } else {
                            betsList.addAll(betStrings)
                            binding.tvNoActiveBets.hide()
                        }
                    }

                    adapter.notifyDataSetChanged()
                } else {
                    // Handle error (e.g., show a Toast with the error message)
                    Toast.makeText(this, "Error loading bets: $errorMsg", Toast.LENGTH_SHORT).show()
                }
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

    override fun onResume() {
        super.onResume()
        setupBetTimeAllocation()
    }

    private fun setupBetTimeAllocation() {
        val currentSlot = getCurrentBetTimeSlot()
        currentSlot?.let {
            if (currentSlot.status == "Closed for results") {
                binding.tvDeclareResults.text = "Betting closed, Declare results for ${currentSlot.displayName} slot"
                binding.btnDeclareResult.isEnabled = true
            }

            if (currentSlot.status == "Open") {
                binding.tvDeclareResults.text = "Betting is going for ${currentSlot.displayName} slot"
                binding.btnDeclareResult.isEnabled = false
            }
        }
    }
}
