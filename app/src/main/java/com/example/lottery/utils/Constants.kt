package com.example.lottery.utils

object Constants {
    // Firebase paths
    const val USERS_COLLECTION = "users"
    const val TRANSACTIONS_COLLECTION = "transactions"
    const val PURCHASE_REQUESTS_PATH = "purchaseRequests"
    const val PLAYER_COIN_REQUESTS_COLLECTION = "playerRequests"
    const val RETAILER_REQUESTS_PATH = "retailerRequests"
    const val RESULTS_PATH = "results"
    const val NOTIFICATIONS_PATH = "notifications"
    const val REFUNDS_PATH = "refunds"

    // User roles
    const val ROLE_ADMIN = "admin"
    const val ROLE_RETAILER = "retailer"
    const val ROLE_PLAYER = "player"

    // Transaction fields
    const val FIELD_PLAYER_ID = "playerId"
    const val FIELD_RETAILER_ID = "retailerId"
    const val FIELD_COIN_AMOUNT = "coinAmount"
    const val FIELD_STATUS = "status"
    const val FIELD_REQUESTED_FROM = "requestedFrom"

    // Request statuses
    const val STATUS_PENDING = "pending"
    const val STATUS_APPROVED = "approved"
    const val STATUS_REJECTED = "rejected"

    // Notifications
    const val NOTIFICATION_TITLE = "title"
    const val NOTIFICATION_MESSAGE = "message"
    const val NOTIFICATION_AUDIENCE = "audience"

    // Error messages
    const val ERROR_INVALID_AMOUNT = "Enter a valid amount"
    const val ERROR_AUTH_FAILED = "Authentication failed"

    // App-wide defaults
    const val DEFAULT_COIN_BALANCE = 0
    const val DEFAULT_CURRENCY = "$"
    const val PAYMENT_METHOD_CREDIT_CARD = "Credit Card"
    const val PAYMENT_METHOD_PAYPAL = "PayPal"

    // Result generation
    const val RESULT_TIME_SLOT_MORNING = "Morning"
    const val RESULT_TIME_SLOT_AFTERNOON = "Afternoon"
    const val RESULT_TIME_SLOT_EVENING = "Evening"
}