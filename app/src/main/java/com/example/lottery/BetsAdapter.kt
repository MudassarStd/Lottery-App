package com.example.lottery

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class BetsAdapter(private val bets: List<Pair<String, Int>>) :
    RecyclerView.Adapter<BetsAdapter.BetViewHolder>() {

    // ViewHolder class to hold references to the views
    class BetViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvBetNumber: TextView = view.findViewById(R.id.tvBetNumber)
        val tvBetAmount: TextView = view.findViewById(R.id.tvBetAmount)
    }

    // Inflate the item layout and return a ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BetViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bet, parent, false)
        return BetViewHolder(view)
    }

    // Bind data to the views in the ViewHolder
    override fun onBindViewHolder(holder: BetViewHolder, position: Int) {
        val (number, amount) = bets[position]
        holder.tvBetNumber.text = "Number: $number"
        holder.tvBetAmount.text = "Amount: $amount Coins"
    }

    // Return the total count of items
    override fun getItemCount(): Int = bets.size
}
