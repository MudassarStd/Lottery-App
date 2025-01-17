package com.example.lottery.admin

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.lottery.R
import com.google.firebase.database.*

class A_Reports : AppCompatActivity() {
    private lateinit var spinnerReportType: Spinner
    private lateinit var lvReports: ListView
    private lateinit var progressBar: ProgressBar

    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var reportsRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_areports)

        // Initialize UI components
        spinnerReportType = findViewById(R.id.spinnerReportType)
        lvReports = findViewById(R.id.lvReports)
        progressBar = findViewById(R.id.progressBar)

        // Initialize Firebase
        firebaseDatabase = FirebaseDatabase.getInstance()
        reportsRef = firebaseDatabase.getReference("reports")

        // Setup Spinner with report types
        setupSpinner()

        // Setup Spinner Listener
        setupSpinnerListener()
    }

    /**
     * Set up the Spinner with predefined report types.
     */
    private fun setupSpinner() {
        val reportTypes = listOf("Player Activity", "Transactions", "System Performance")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, reportTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerReportType.adapter = adapter
    }

    /**
     * Set up a listener to handle Spinner item selection.
     */
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
                // No action needed for no selection
            }
        }
    }

    /**
     * Load reports from Firebase based on the selected report type.
     */
    private fun loadReports(reportType: String) {
        // Show the progress bar while loading reports
        progressBar.visibility = View.VISIBLE

        // Query the reports based on the selected type
        reportsRef.orderByChild("type").equalTo(reportType)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    progressBar.visibility = View.GONE // Hide progress bar

                    val reportList = mutableListOf<String>()
                    for (report in snapshot.children) {
                        val reportDetail = report.child("detail").getValue(String::class.java)
                        reportDetail?.let { reportList.add(it) }
                    }

                    if (reportList.isEmpty()) {
                        Toast.makeText(
                            this@A_Reports,
                            "No reports found for \"$reportType\".",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        // Populate the ListView with report details
                        val adapter = ArrayAdapter(
                            this@A_Reports,
                            android.R.layout.simple_list_item_1,
                            reportList
                        )
                        lvReports.adapter = adapter
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    progressBar.visibility = View.GONE // Hide progress bar
                    Toast.makeText(
                        this@A_Reports,
                        "Failed to load reports: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }
}
