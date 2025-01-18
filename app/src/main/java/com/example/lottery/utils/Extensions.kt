package com.example.lottery.utils

import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi
import com.example.lottery.data.Result
import com.example.lottery.data.model.Bet
import com.example.lottery.utils.DateTimeUtils.getCurrentDateInLocalFormat

object Extensions {
    fun View.hide() {
        this.visibility = View.GONE
    }

    fun View.show() {
        this.visibility = View.VISIBLE
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun Bet.toResult(): Result {
        return Result(
            winner = this.userId,
            slot = this.slot,
            amountWon = this.choice.toInt() * 50,
            date = getCurrentDateInLocalFormat(),
        )
    }
}
