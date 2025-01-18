package com.example.lottery.data.model

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.lottery.data.Result
import com.example.lottery.utils.DateTimeUtils.getCurrentDateInLocalFormat

data class Bet(
    val userId: String ,
    val choice: String,
    val slot: String,
    val amount: Int,
    val timestamp: Long = System.currentTimeMillis()
) {
    constructor() : this(
        userId = "",
        choice = "",
        slot = "",
        amount = 0,
        timestamp = System.currentTimeMillis()
    )

}

