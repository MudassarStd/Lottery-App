package com.example.lottery.player

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.lottery.R
import com.example.lottery.data.FirebaseRepository
import com.example.lottery.databinding.ActivityPresultBinding
import com.example.lottery.utils.FirebaseHelper
import com.example.lottery.utils.ResultUtils

@RequiresApi(Build.VERSION_CODES.O)
class P_Result : AppCompatActivity() {

    private val binding by lazy { ActivityPresultBinding.inflate(layoutInflater) }
    private val firebaseRepository by lazy { FirebaseRepository() }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        // Load results from Firebase
        loadResults()
    }

    private fun loadResults() {
                firebaseRepository.getResults("afternoon", callback = {responce, results, error ->
                    if (responce) {
                        Log.d("TestResultLoad", "Results: $results")
                    } else {
                        Log.e("TestResultLoad", "Result failed to get here, $error")
                    }
                })
//        val adapter = ArrayAdapter(this@P_Result, android.R.layout.simple_list_item_1)
//        binding.lvResults.adapter = adapter
    }
}
