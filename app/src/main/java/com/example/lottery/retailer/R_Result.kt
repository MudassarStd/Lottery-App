package com.example.lottery.retailer

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.lottery.R
import com.example.lottery.utils.FirebaseHelper
import com.example.lottery.utils.ResultUtils
import java.util.Calendar

class R_Result : AppCompatActivity() {
    private lateinit var btnSelectDate: Button
    private lateinit var tvSelectedDate: TextView
    private lateinit var lvResults: ListView

    private val firebaseHelper = FirebaseHelper()
    private var selectedDate: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rresult)

        // Initialize UI components
        btnSelectDate = findViewById(R.id.btnSelectDate)
        tvSelectedDate = findViewById(R.id.tvSelectedDate)
        lvResults = findViewById(R.id.lvResults)

        // Handle date selection
        btnSelectDate.setOnClickListener {
            showDatePickerDialog()
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                selectedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                tvSelectedDate.text = selectedDate
                if (selectedDate != null) {
                    loadResultsForDate(selectedDate!!)
                }
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }

    private fun loadResultsForDate(date: String) {
        firebaseHelper.getResults { results, error ->
            if (error != null) {
                Toast.makeText(this@R_Result, "Failed to load results: $error", Toast.LENGTH_SHORT).show()
                return@getResults
            }

            if (results != null) {
                val resultsList = mutableListOf<String>()

                for (result in results) {
                    val slot = result["slot"].toString()
                    val winningNumber = result["winningNumber"].toString().toIntOrNull()
                    val resultDate = result["date"].toString()

                    // Only show results for the selected date
                    if (resultDate == date) {
                        val displayWinningNumber = if (winningNumber != null && ResultUtils.validateResult(winningNumber)) {
                            ResultUtils.formatResult(winningNumber)
                        } else {
                            "Invalid Winning Number"
                        }

                        resultsList.add("Slot: $slot, Winning Number: $displayWinningNumber")
                    }
                }

                // Populate the ListView with results for the selected date
                val adapter = ArrayAdapter(
                    this@R_Result,
                    android.R.layout.simple_list_item_1,
                    resultsList
                )
                lvResults.adapter = adapter
            }
        }
    }
}
