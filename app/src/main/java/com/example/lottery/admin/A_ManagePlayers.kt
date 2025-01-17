package com.example.lottery.admin

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.lottery.R
import com.google.firebase.database.*

class A_ManagePlayers : AppCompatActivity() {
    private lateinit var lvUsers: ListView
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var usersRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_amanage_players)

        lvUsers = findViewById(R.id.lvPlayers)

        // Initialize Firebase
        firebaseDatabase = FirebaseDatabase.getInstance()
        usersRef = firebaseDatabase.getReference("users") // Common node for players and retailers

        // Load Users
        loadUsers()

        // Handle ListView item click
        lvUsers.setOnItemClickListener { _, _, position, _ ->
            val selectedItem = lvUsers.getItemAtPosition(position).toString()
            val userId = selectedItem.substringBefore(" | ").trim()
            val userType = selectedItem.substringAfter(" | ").substringBefore(":").trim()
            showUserOptions(userId, userType)
        }
    }

    /**
     * Load users from Firebase and display them in the ListView.
     */
    private fun loadUsers() {
        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userList = mutableListOf<String>()

                for (user in snapshot.children) {
                    val userId = user.key ?: continue
                    val name = user.child("name").getValue(String::class.java) ?: "Unknown"
                    val userType = user.child("type").getValue(String::class.java) ?: "Unknown"

                    userList.add("$userId | $userType: $name")
                }

                val adapter = ArrayAdapter(this@A_ManagePlayers, android.R.layout.simple_list_item_1, userList)
                lvUsers.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@A_ManagePlayers, "Failed to load users: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    /**
     * Show options for the selected user.
     */
    private fun showUserOptions(userId: String, userType: String) {
        val options = arrayOf("View Details", "Update Information", "Remove User")

        AlertDialog.Builder(this)
            .setTitle("User Options")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> viewUserDetails(userId)
                    1 -> updateUserInformation(userId)
                    2 -> removeUser(userId)
                }
            }
            .create()
            .show()
    }

    /**
     * View details of a specific user.
     */
    private fun viewUserDetails(userId: String) {
        usersRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val name = snapshot.child("name").getValue(String::class.java) ?: "Unknown"
                val email = snapshot.child("email").getValue(String::class.java) ?: "Unknown"
                val type = snapshot.child("type").getValue(String::class.java) ?: "Unknown"
                val coins = snapshot.child("coins").getValue(String::class.java) ?: "0"

                AlertDialog.Builder(this@A_ManagePlayers)
                    .setTitle("User Details")
                    .setMessage("Name: $name\nEmail: $email\nType: $type\nCoins: $coins")
                    .setPositiveButton("Close", null)
                    .show()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@A_ManagePlayers, "Failed to load user details: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    /**
     * Update the information of a specific user.
     */
    private fun updateUserInformation(userId: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_update_player, null)
        val etName = dialogView.findViewById<EditText>(R.id.etPlayerName)
        val etEmail = dialogView.findViewById<EditText>(R.id.etPlayerEmail)

        usersRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                etName.setText(snapshot.child("name").getValue(String::class.java) ?: "")
                etEmail.setText(snapshot.child("email").getValue(String::class.java) ?: "")
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@A_ManagePlayers, "Failed to load user data for update: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })

        AlertDialog.Builder(this)
            .setTitle("Update User Information")
            .setView(dialogView)
            .setPositiveButton("Update") { _, _ ->
                val updatedName = etName.text.toString()
                val updatedEmail = etEmail.text.toString()

                usersRef.child(userId).updateChildren(mapOf("name" to updatedName, "email" to updatedEmail))
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "User information updated successfully", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Failed to update user information", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Remove a specific user from Firebase.
     */
    private fun removeUser(userId: String) {
        usersRef.child(userId).removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "User removed successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to remove user", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
