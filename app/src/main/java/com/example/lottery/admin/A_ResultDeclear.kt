package com.example.lottery.admin

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.lottery.R
import com.google.firebase.database.*
import kotlin.random.Random

class A_ResultDeclear : AppCompatActivity() {
    private lateinit var spinnerSlot: Spinner
    private lateinit var btnGenerateNumber: Button
    private lateinit var tvGeneratedNumber: TextView
    private lateinit var etManualNumber: EditText
    private lateinit var btnPublishResults: Button

    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var betsRef: DatabaseReference
    private lateinit var resultsRef: DatabaseReference

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
        resultsRef = firebaseDatabase.getReference("results")

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
        betsRef.child(selectedSlot).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val betCounts = mutableMapOf<Int, Int>()

                // Collect data for the selected slot
                for (bet in snapshot.children) {
                    val number = bet.child("number").value.toString().toIntOrNull() ?: continue
                    val amount = bet.child("amount").value.toString().toIntOrNull() ?: continue
                    betCounts[number] = betCounts.getOrDefault(number, 0) + amount
                }

                if (betCounts.isEmpty()) {
                    Toast.makeText(this@A_ResultDeclear, "No bets found for this slot", Toast.LENGTH_SHORT).show()
                    return
                }

                // Calculate the winning number and display it
                val winningNumber = calculateWinningNumber(betCounts)
                tvGeneratedNumber.text = "Generated Number: $winningNumber"
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@A_ResultDeclear, "Failed to retrieve bets: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun calculateWinningNumber(betCounts: Map<Int, Int>): Int {
        // Filter bets with non-zero amounts
        val validBets = betCounts.filter { it.value > 0 }

        return if (validBets.isNotEmpty()) {
            // Choose the number with the least total bet amount
            validBets.minByOrNull { it.value }?.key ?: Random.nextInt(0, 100)
        } else {
            // If no valid bets, generate a random number
            Random.nextInt(0, 100)
        }
    }

    private fun publishResult() {
        val selectedSlot = spinnerSlot.selectedItem.toString()
        val generatedNumber = tvGeneratedNumber.text.toString().split(":").last().trim()
        val manualNumber = etManualNumber.text.toString().trim()

        // Choose the number to publish (manual or generated)
        val winningNumber = manualNumber.ifEmpty { generatedNumber }
        if (winningNumber.isEmpty()) {
            Toast.makeText(this, "Please generate or enter a winning number", Toast.LENGTH_SHORT).show()
            return
        }

        // Prepare the result to publish
        val resultData = mapOf(
            "slot" to selectedSlot,
            "winningNumber" to winningNumber,
            "timestamp" to System.currentTimeMillis()
        )

        resultsRef.child(selectedSlot).setValue(resultData)
            .addOnSuccessListener {
                Toast.makeText(this, "Result published successfully for $selectedSlot", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to publish result: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
