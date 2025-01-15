package com.example.lottery.admin

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.lottery.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class A_ManagePlayers : AppCompatActivity() {
    private lateinit var lvUsers: ListView
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var usersRef: DatabaseReference

    private var selectedUserId: String? = null
    private var selectedUserType: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_amanage_players)

        lvUsers = findViewById(R.id.lvPlayers)

        // Initialize Firebase
        firebaseDatabase = FirebaseDatabase.getInstance()
        usersRef = firebaseDatabase.getReference("users") // Common node for players and retailers

        loadUsers()

        lvUsers.setOnItemClickListener { _, _, position, _ ->
            selectedUserId = lvUsers.getItemAtPosition(position).toString().split("|")[0].trim()
            selectedUserType = lvUsers.getItemAtPosition(position).toString().split("|")[1].trim()
            showUserOptions(selectedUserId!!, selectedUserType!!)
        }
    }

    private fun loadUsers() {
        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userList = mutableListOf<String>()

                for (user in snapshot.children) {
                    val name = user.child("name").value.toString()
                    val userType = user.child("type").value.toString() // "player" or "retailer"
                    val userId = user.key ?: ""

                    userList.add("$userId | $userType: $name")
                }

                val adapter = ArrayAdapter(this@A_ManagePlayers, android.R.layout.simple_list_item_1, userList)
                lvUsers.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@A_ManagePlayers, "Failed to load users", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showUserOptions(userId: String, userType: String) {
        val options = arrayOf("View Details", "Update Information", "Remove User")

        val dialog = AlertDialog.Builder(this)
            .setTitle("User Options")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> viewUserDetails(userId)
                    1 -> updateUserInformation(userId)
                    2 -> removeUser(userId)
                }
            }
            .create()

        dialog.show()
    }

    private fun viewUserDetails(userId: String) {
        usersRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val name = snapshot.child("name").value.toString()
                val email = snapshot.child("email").value.toString()
                val type = snapshot.child("type").value.toString() // "player" or "retailer"
                val coins = snapshot.child("coins").value.toString()

                AlertDialog.Builder(this@A_ManagePlayers)
                    .setTitle("User Details")
                    .setMessage("Name: $name\nEmail: $email\nType: $type\nCoins: $coins")
                    .setPositiveButton("Close", null)
                    .show()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@A_ManagePlayers, "Failed to load user details", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateUserInformation(userId: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_update_player, null)
        val etName = dialogView.findViewById<EditText>(R.id.etPlayerName)
        val etEmail = dialogView.findViewById<EditText>(R.id.etPlayerEmail)

        usersRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                etName.setText(snapshot.child("name").value.toString())
                etEmail.setText(snapshot.child("email").value.toString())
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@A_ManagePlayers, "Failed to load user information", Toast.LENGTH_SHORT).show()
            }
        })

        AlertDialog.Builder(this)
            .setTitle("Update User Information")
            .setView(dialogView)
            .setPositiveButton("Update") { _, _ ->
                val newName = etName.text.toString()
                val newEmail = etEmail.text.toString()

                usersRef.child(userId).updateChildren(mapOf("name" to newName, "email" to newEmail))
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "User information updated", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Failed to update user information", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }

    private fun removeUser(userId: String) {
        usersRef.child(userId).removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "User removed successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to remove user", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
