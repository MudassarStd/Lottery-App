package com.example.lottery.utils

import android.view.View

object Extensions {

    fun View.hide() {
        this.visibility = View.GONE
    }

    fun View.show() {
        this.visibility = View.VISIBLE
    }

}