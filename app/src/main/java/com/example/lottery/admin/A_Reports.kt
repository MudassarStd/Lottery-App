package com.example.lottery.admin

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.lottery.R
import com.google.firebase.database.*

class A_Reports : AppCompatActivity() {
    private lateinit var spinnerReportType: Spinner
    private lateinit var lvReports: ListView

    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var reportsRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_areports)

        spinnerReportType = findViewById(R.id.spinnerReportType)
        lvReports = findViewById(R.id.lvReports)

        // Initialize Firebase
        firebaseDatabase = FirebaseDatabase.getInstance()
        reportsRef = firebaseDatabase.getReference("reports")

        // Setup Spinner
        setupSpinner()

        // Setup Spinner Listener
        setupSpinnerListener()
    }

    // Method to set up the spinner with report types
    private fun setupSpinner() {
        val reportTypes = listOf("Player Activity", "Transactions", "System Performance")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, reportTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerReportType.adapter = adapter
    }

    // Method to handle spinner item selection and load reports accordingly
    private fun setupSpinnerListener() {
        spinnerReportType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedType = parent?.getItemAtPosition(position).toString()
                loadReports(selectedType)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Handle case where no item is selected (if needed)
            }
        }
    }

    // Method to load reports from Firebase based on the selected report type
    private fun loadReports(reportType: String) {
        // Listen to the Firebase database and fetch reports of the selected type
        reportsRef.orderByChild("type").equalTo(reportType)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val reportList = mutableListOf<String>()

                    // Loop through reports and add their details to the list
                    for (report in snapshot.children) {
                        val reportDetail = report.child("detail").getValue(String::class.java)
                        reportDetail?.let { reportList.add(it) }
                    }

                    // Show reports in the ListView
                    if (reportList.isEmpty()) {
                        Toast.makeText(this@A_Reports, "No reports found for this type.", Toast.LENGTH_SHORT).show()
                    } else {
                        val adapter = ArrayAdapter(this@A_Reports, android.R.layout.simple_list_item_1, reportList)
                        lvReports.adapter = adapter
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@A_Reports, "Failed to load reports: ${error.message}", Toast.LENGTH_LONG).show()
                }
            })
    }
}
