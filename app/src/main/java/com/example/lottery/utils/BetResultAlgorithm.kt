package com.example.lottery.utils

import com.example.lottery.data.model.Bet
import kotlin.random.Random

object BetResultAlgorithm {

    fun determineBetResult(
        bets: List<Bet>,
        specialConditionMostBetted: String = "44",
        specialConditionLeastBetted: String = "12"
    ): Bet? {


        if (bets.isEmpty()) return null

        // Input validation: Ensure all numbers (00-99) have corresponding bet values
        val betMap = (0..99).associate { it.toString().padStart(2, '0') to 0 }.toMutableMap()

        // Populate bet amounts from input
        bets.forEach { bet ->
            val choice = bet.choice
            if (choice in betMap) {
                betMap[choice] = betMap[choice]!! + bet.amount
            }
        }

        // Identify most betted number
        val mostBettedNumber = betMap.maxByOrNull { it.value }?.key ?: "00"

        // Identify least betted number with non-zero bets
        val leastBettedNumbers = betMap.filter { it.value > 0 }
        val leastBettedNumber = leastBettedNumbers.minByOrNull { it.value }?.key

        // If specific conditions are met (configurable), return the least betted special number
        if (mostBettedNumber == specialConditionMostBetted && leastBettedNumber == specialConditionLeastBetted) {
            return bets.find { it.choice == specialConditionLeastBetted }
        }

        // If no least betted numbers exist, handle fallback logic
        return when {
            leastBettedNumber != null -> bets.find { it.choice == leastBettedNumber }
            else -> {
                val randomChoice = (0..99).map { it.toString().padStart(2, '0') }[Random.nextInt(100)]
                bets.find { it.choice == randomChoice }
            }
        }
    }
}