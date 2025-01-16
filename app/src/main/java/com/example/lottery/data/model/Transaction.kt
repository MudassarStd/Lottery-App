package com.example.lottery.data.model

import com.example.lottery.utils.Constants.STATUS_PENDING

data class Transaction(
    val userId: String = "",
    val userRole: String = "",
    val recipientId: String = "",
    val recipientType: String = "",
    val amount: Int = 0,
    val transactionType: String = "",
    val status: String = STATUS_PENDING,
    val timeStamp: Long = System.currentTimeMillis()
) {
    // No-argument constructor for deserialization
    constructor() : this(
        userId = "",
        userRole = "",
        recipientId = "",
        recipientType = "",
        amount = 0,
        transactionType = "",
        status = STATUS_PENDING,
        timeStamp = System.currentTimeMillis()
    )
}
