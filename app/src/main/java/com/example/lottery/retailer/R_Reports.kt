package com.example.lottery.retailer

import android.os.Bundle
import android.widget.ListView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.lottery.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class R_Reports : AppCompatActivity() {
    private lateinit var lvTransactionReports: ListView
    private lateinit var lvPlayerReports: ListView
    private lateinit var lvSystemPerformanceReports: ListView

    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var reportsRef: DatabaseReference
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_rreports)
        lvTransactionReports = findViewById(R.id.lvTransactionReports)
        lvPlayerReports = findViewById(R.id.lvPlayerReports)
        lvSystemPerformanceReports = findViewById(R.id.lvSystemPerformanceReports)

        firebaseDatabase = FirebaseDatabase.getInstance()
        reportsRef = firebaseDatabase.getReference("reports")
        auth = FirebaseAuth.getInstance()

        loadTransactionReports()
        loadPlayerReports()
        loadSystemPerformanceReports()
    }

    private fun loadTransactionReports() {
        reportsRef.child("transactions").addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val reports = mutableListOf<String>()
                for (report in snapshot.children) {
                    val detail = report.child("detail").value.toString()
                    reports.add(detail)
                }
                lvTransactionReports.adapter =
                    android.widget.ArrayAdapter(this@R_Reports, android.R.layout.simple_list_item_1, reports)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@R_Reports, "Failed to load transaction reports", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadPlayerReports() {
        reportsRef.child("playerActivity").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val reports = mutableListOf<String>()
                for (report in snapshot.children) {
                    val detail = report.child("detail").value.toString()
                    reports.add(detail)
                }
                lvPlayerReports.adapter =
                    android.widget.ArrayAdapter(this@R_Reports, android.R.layout.simple_list_item_1, reports)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@R_Reports, "Failed to load player activity reports", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadSystemPerformanceReports() {
        reportsRef.child("systemPerformance").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val reports = mutableListOf<String>()
                for (report in snapshot.children) {
                    val detail = report.child("detail").value.toString()
                    reports.add(detail)
                }
                lvSystemPerformanceReports.adapter =
                    android.widget.ArrayAdapter(this@R_Reports, android.R.layout.simple_list_item_1, reports)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@R_Reports, "Failed to load system performance reports", Toast.LENGTH_SHORT).show()
            }
        })
    }
}