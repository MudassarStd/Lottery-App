package com.example.lottery.player

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.lottery.R
import com.example.lottery.utils.FirebaseHelper
import com.example.lottery.utils.ResultUtils

class P_Result : AppCompatActivity() {
    private lateinit var lvResults: ListView
    private lateinit var tvResultHeader: TextView

    private val firebaseHelper = FirebaseHelper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_presult)

        // Initialize UI components
        lvResults = findViewById(R.id.lvResults)
        tvResultHeader = findViewById(R.id.tvResultHeader)

        // Load results from Firebase
        loadResults()
    }

    private fun loadResults() {
        firebaseHelper.getResults { results, error ->
            if (error != null) {
                Toast.makeText(this@P_Result, "Failed to load results: $error", Toast.LENGTH_SHORT).show()
                return@getResults
            }

            if (results != null) {
                val resultsList = mutableListOf<String>()

                for (result in results) {
                    val slot = result["slot"].toString()
                    val winningNumber = result["winningNumber"].toString().toIntOrNull()
                    val timestamp = result["timestamp"].toString()

                    // Validate the winning number before adding to the list
                    if (winningNumber != null && ResultUtils.validateResult(winningNumber)) {
                        val formattedResult = ResultUtils.formatResult(winningNumber)
                        resultsList.add("Slot: $slot\n$formattedResult\nDate: $timestamp")
                    } else {
                        resultsList.add("Slot: $slot\nWinning Number: Invalid\nDate: $timestamp")
                    }
                }

                // Populate the ListView with results
                val adapter = ArrayAdapter(this@P_Result, android.R.layout.simple_list_item_1, resultsList)
                lvResults.adapter = adapter
            }
        }
    }
}
