package com.example.lottery.utils

import android.os.Build
import androidx.annotation.RequiresApi
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.util.*


@RequiresApi(Build.VERSION_CODES.O)
object DateTimeUtils {

    fun formatTimestamp(timestamp: Long): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val date = Date(timestamp)
        return dateFormat.format(date)
    }


    fun getCurrentBetTimeSlot(currentTime: LocalTime = LocalTime.now()): TimeSlot? {
        return when {
            // 9 AM to 12 PM or 12 PM to 1 PM → SLOT_1 or RESULT_1
            currentTime.isAfter(LocalTime.of(8, 59)) && currentTime.isBefore(LocalTime.of(12, 0)) -> TimeSlot.SLOT_1
            currentTime.isAfter(LocalTime.of(11, 59)) && currentTime.isBefore(LocalTime.of(13, 0)) -> TimeSlot.RESULT_1

//            currentTime.isAfter(LocalTime.of(8, 59)) && currentTime.isBefore(LocalTime.of(10, 30)) -> TimeSlot.SLOT_1
//            currentTime.isAfter(LocalTime.of(10, 29)) && currentTime.isBefore(LocalTime.of(13, 0)) -> TimeSlot.RESULT_1

            // 1 PM to 3 PM or 3 PM to 4 PM → SLOT_2 or RESULT_2
            currentTime.isAfter(LocalTime.of(12, 59)) && currentTime.isBefore(LocalTime.of(15, 0)) -> TimeSlot.SLOT_2
            currentTime.isAfter(LocalTime.of(14, 59)) && currentTime.isBefore(LocalTime.of(16, 0)) -> TimeSlot.RESULT_2

            // 4 PM to 5 PM or 5 PM to 6 PM → SLOT_3 or RESULT_3
            currentTime.isAfter(LocalTime.of(15, 59)) && currentTime.isBefore(LocalTime.of(17, 0)) -> TimeSlot.SLOT_3
            currentTime.isAfter(LocalTime.of(16, 59)) && currentTime.isBefore(LocalTime.of(18, 0)) -> TimeSlot.RESULT_3

            else -> null
        }
    }

}

enum class TimeSlot(val displayName: String, val status: String) {
    SLOT_1("morning", "Open"),
    RESULT_1("morning", "Closed for results"),
    SLOT_2("afternoon", "Open"),
    RESULT_2("afternoon", "Closed for results"),
    SLOT_3("evening", "Open"),
    RESULT_3("evening", "Closed for results")
}