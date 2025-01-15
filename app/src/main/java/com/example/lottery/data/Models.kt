package com.example.lottery.data

data class User(
    val userId: String = "",
    val username: String = "",
    val email: String = "",
    val phone: String = "",
    val role: String = "", // Can be "admin", "retailer", or "player"
    val businessName: String? = null, // Only for retailers
    val businessAddress: String? = null, // Only for retailers
    val coins: Int = 0 // Coin balance
)

// Transaction model
data class Transaction(
    val transactionId: String = "",
    val userId: String = "", // The user initiating the transaction
    val recipientId: String = "", // The recipient of the transaction (admin, retailer, or player)
    val amount: Int = 0, // Number of coins
    val timestamp: Long = System.currentTimeMillis(),
    val type: String = "" // e.g., "purchase", "refund", "transfer"
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "transactionId" to transactionId,
            "userId" to userId,
            "recipientId" to recipientId,
            "amount" to amount,
            "timestamp" to timestamp,
            "type" to type
        )

    }
}

// Bet model
data class Bet(
    val betId: String = "",
    val playerId: String = "",
    val betNumber: Int = 0,
    val betAmount: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val resultId: String? = null, // Associated result
    val status: String = "pending" // Can be "pending", "won", or "lost"
)

// Refund model
data class Refund(
    val refundId: String = "",
    val userId: String = "",
    val amount: Int = 0,
    val status: String = "pending", // Can be "pending", "approved", or "rejected"
    val timestamp: Long = System.currentTimeMillis()
)

// Result model
data class Result(
    val resultId: String = "",
    val winningNumber: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val slot: String = "" // Morning, Afternoon, or Evening
)

// Notification model
data class Notification(
    val notificationId: String = "",
    val message: String = "",
    val audience: String = "", // e.g., "all", "players", "retailers"
    val timestamp: Long = System.currentTimeMillis()
)

// Request model (for coin requests)
data class Request(
    val requestId: String = "",
    val userId: String = "",
    val retailerId: String? = null, // Optional, if request is for a retailer
    val amount: Int = 0,
    val status: String = "pending", // Can be "pending", "approved", or "rejected"
    val timestamp: Long = System.currentTimeMillis()
)
data class PurchaseRequest(
    val retailerId: String,
    var coinAmount: Int,
    val status: String,
    val additionalData: Map<String, Any> = emptyMap() // Add map property
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "retailerId" to retailerId,
            "coinAmount" to coinAmount,
            "status" to status
        )
    }
}

