package com.example.lottery.utils

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class FirebaseHelper {
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference

    // Function to get results from Firebase
    fun getResults(callback: (List<Map<String, Any>>?, String?) -> Unit) {
        database.child("results").get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val results = task.result?.children?.map { snapshot ->
                        snapshot.value as Map<String, Any>
                    }
                    callback(results, null)
                } else {
                    callback(null, task.exception?.message)
                }
            }
    }
}
