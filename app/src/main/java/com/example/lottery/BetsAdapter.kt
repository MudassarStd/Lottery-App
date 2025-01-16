package com.example.lottery

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class BetsAdapter(private val bets: List<Pair<String, Int>>) :
    RecyclerView.Adapter<BetsAdapter.BetViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BetViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bet, parent, false)
        return BetViewHolder(view)
    }

    override fun onBindViewHolder(holder: BetViewHolder, position: Int) {
        val (number, amount) = bets[position]
        holder.tvBetDetails.text = "Number: $number, Amount: $amount"
    }

    override fun getItemCount(): Int = bets.size

    inner class BetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvBetDetails: TextView = itemView.findViewById(R.id.tvBetNumber)
    }
}
