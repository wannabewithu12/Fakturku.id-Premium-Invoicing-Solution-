package com.fakturkuid.app.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateFormatter {
    fun formatDate(timestamp: Long, pattern: String = "dd MMMM yyyy", locale: Locale = Locale.getDefault()): String {
        val formatter = SimpleDateFormat(pattern, locale)
        return formatter.format(Date(timestamp))
    }

    fun isToday(timestamp: Long): Boolean {
        val now = java.util.Calendar.getInstance()
        val date = java.util.Calendar.getInstance().apply { timeInMillis = timestamp }
        return now.get(java.util.Calendar.YEAR) == date.get(java.util.Calendar.YEAR) &&
                now.get(java.util.Calendar.DAY_OF_YEAR) == date.get(java.util.Calendar.DAY_OF_YEAR)
    }

    fun isThisMonth(timestamp: Long): Boolean {
        val now = java.util.Calendar.getInstance()
        val date = java.util.Calendar.getInstance().apply { timeInMillis = timestamp }
        return now.get(java.util.Calendar.YEAR) == date.get(java.util.Calendar.YEAR) &&
                now.get(java.util.Calendar.MONTH) == date.get(java.util.Calendar.MONTH)
    }
}
