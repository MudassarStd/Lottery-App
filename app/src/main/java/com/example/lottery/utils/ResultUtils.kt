package com.example.lottery.utils

import kotlin.random.Random

object ResultUtils {

    /**
     * Generates a winning number based on a pseudo-random algorithm.
     *
     * @param range The range of numbers to generate from (e.g., 0 to 99).
     * @return The generated winning number.
     */
    fun generateWinningNumber(range: IntRange = 0..99): Int {
        return Random.nextInt(range.first, range.last + 1)
    }

    /**
     * Validates if a result falls within an acceptable range.
     *
     * @param result The result to validate.
     * @param range The acceptable range.
     * @return True if valid, false otherwise.
     */
    fun validateResult(result: Int, range: IntRange = 0..99): Boolean {
        return result in range
    }

    /**
     * Formats the result for display purposes.
     *
     * @param result The result number.
     * @return A formatted string.
     */
    fun formatResult(result: Int): String {
        return "Winning Number: $result"
    }

    /**
     * Schedules automatic result declaration at predefined intervals.
     *
     * @param slotTimes List of times (in milliseconds) for result declarations.
     * @param callback A callback function to execute the result declaration logic.
     */
    fun scheduleResultDeclaration(slotTimes: List<Long>, callback: (Int) -> Unit) {
        val currentTime = System.currentTimeMillis()
        for (slotTime in slotTimes) {
            if (currentTime < slotTime) {
                val delay = slotTime - currentTime
                Thread.sleep(delay) // Simplified for demonstration; use better scheduling in production.
                val result = generateWinningNumber()
                callback(result)
            }
        }
    }
}