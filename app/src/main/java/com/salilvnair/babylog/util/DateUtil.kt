package com.salilvnair.babylog.util

import java.text.SimpleDateFormat
import java.util.*

class DateUtil {
    companion object {
        fun  formattedDateTime(date: Date?): String? {
            val dateFormat = SimpleDateFormat(
                "yyyy-MM-dd hh:mm:ss aa", Locale.getDefault())
            return dateFormat.format(date)
        }

        fun  formattedDate(date: Date?): String? {
            val dateFormat = SimpleDateFormat(
                    "yyyy-MM-dd", Locale.getDefault())
            return dateFormat.format(date)
        }

        fun dateTimeAfter(inputDateString: String): Boolean {
            val dateFormat = SimpleDateFormat(
                    "HH:mm", Locale.getDefault())
            val currentDate =  dateFormat.format(Date())
            return dateFormat.parse(currentDate).after(dateFormat.parse(inputDateString))
        }
    }
}