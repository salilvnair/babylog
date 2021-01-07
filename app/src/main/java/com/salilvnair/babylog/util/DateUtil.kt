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
    }
}