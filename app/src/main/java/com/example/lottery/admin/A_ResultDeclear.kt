package com.example.lottery.admin

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.lottery.R
import com.example.lottery.data.FirebaseRepository
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlin.random.Random

class A_ResultDeclear : AppCompatActivity() {
    private lateinit var spinnerSlot: Spinner
    private lateinit var btnGenerateNumber: Button
    private lateinit var tvGeneratedNumber: TextView
    private lateinit var etManualNumber: EditText
    private lateinit var btnPublishResults: Button

    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var betsRef: DatabaseReference

    private val firebaseRepository = FirebaseRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_aresult_declear)

        // Initialize UI components
        spinnerSlot = findViewById(R.id.spinnerSlot)
        btnGenerateNumber = findViewById(R.id.btnGenerateNumber)
        tvGeneratedNumber = findViewById(R.id.tvGeneratedNumber)
        etManualNumber = findViewById(R.id.etManualNumber)
        btnPublishResults = findViewById(R.id.btnPublishResults)

        // Initialize Firebase
        firebaseDatabase = FirebaseDatabase.getInstance()
        betsRef = firebaseDatabase.getReference("bets")

        // Populate slot options
        val slots = listOf("Morning", "Afternoon", "Evening")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, slots)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSlot.adapter = adapter

        // Generate winning number
        btnGenerateNumber.setOnClickListener {
            generateWinningNumber()
        }

        // Publish result
        btnPublishResults.setOnClickListener {
            publishResult()
        }
    }

    private fun generateWinningNumber() {
        val selectedSlot = spinnerSlot.selectedItem.toString()
        betsRef.orderByChild("slot").equalTo(selectedSlot)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val betCounts = mutableMapOf<Int, Int>()

                    for (bet in snapshot.children) {
                        val number = bet.child("number").value.toString().toInt()
                        val amount = bet.child("amount").value.toString().toInt()
                        betCounts[number] = betCounts.getOrDefault(number, 0) + amount
                    }

                    if (betCounts.isEmpty()) {
                        Toast.makeText(this@A_ResultDeclear, "No bets found for this slot", Toast.LENGTH_SHORT).show()
                        return
                    }

                    val winningNumber = calculateWinningNumber(betCounts)
                    tvGeneratedNumber.text = "Generated Number: $winningNumber"
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@A_ResultDeclear, "Failed to retrieve bets", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun calculateWinningNumber(betCounts: Map<Int, Int>): Int {
        // Filter out numbers with zero bets
        val nonZeroBets = betCounts.filter { it.value > 0 }

        // If no numbers have bets, select a random number
        if (nonZeroBets.isEmpty()) {
            return Random.nextInt(0, 100)
        }

        // Find the least betted number
        val leastBettedNumber = nonZeroBets.minByOrNull { it.value }?.key ?: Random.nextInt(0, 100)

        return leastBettedNumber
    }

    private fun publishResult() {
        val selectedSlot = spinnerSlot.selectedItem.toString()
        val generatedNumber = tvGeneratedNumber.text.toString().split(":").last().trim()
        val manualNumber = etManualNumber.text.toString().trim()

        val winningNumber = if (manualNumber.isNotEmpty()) manualNumber else generatedNumber
        if (winningNumber.isEmpty()) {
            Toast.makeText(this, "Please generate or enter a winning number", Toast.LENGTH_SHORT).show()
            return
        }

        // Prepare the result data to publish
        val resultData = mapOf(
            "slot" to selectedSlot,
            "winningNumber" to winningNumber,
            "timestamp" to System.currentTimeMillis()
        )

        // Use FirebaseRepository to add result
        firebaseRepository.addResult(resultData) { success, error ->
            if (success) {
                Toast.makeText(this, "Result published successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to publish result: $error", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
