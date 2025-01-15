package com.example.lottery.data

import android.content.Context
import android.content.SharedPreferences

class SharedPreferencesHelper (context: Context){
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "MyAppPreferences"
        private const val KEY_USER_ID = "userId"
        private const val KEY_USER_ROLE = "userRole"
        private const val KEY_IS_LOGGED_IN = "isLoggedIn"
    }

    // Save user ID
    fun saveUserId(userId: String) {
        sharedPreferences.edit().putString(KEY_USER_ID, userId).apply()
    }

    // Retrieve user ID
    fun getUserId(): String? {
        return sharedPreferences.getString(KEY_USER_ID, null)
    }

    // Save user role
    fun saveUserRole(role: String) {
        sharedPreferences.edit().putString(KEY_USER_ROLE, role).apply()
    }

    // Retrieve user role
    fun getUserRole(): String? {
        return sharedPreferences.getString(KEY_USER_ROLE, null)
    }

    // Save login state
    fun saveLoginState(isLoggedIn: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_IS_LOGGED_IN, isLoggedIn).apply()
    }

    // Retrieve login state
    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    // Clear all stored preferences (for logout)
    fun clearAll() {
        sharedPreferences.edit().clear().apply()
    }
}