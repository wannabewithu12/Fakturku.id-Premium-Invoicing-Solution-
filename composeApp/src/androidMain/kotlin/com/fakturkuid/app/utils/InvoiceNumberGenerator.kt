package com.fakturkuid.app.utils

import java.text.SimpleDateFormat
import java.util.*

object InvoiceNumberGenerator {
    fun generate(lastNumber: Int, format: String = "INV/%DD%/%MM%/%YYYY%/%NUM%"): String {
        val now = Date()
        val year = SimpleDateFormat("yyyy", Locale.getDefault()).format(now)
        val month = SimpleDateFormat("MM", Locale.getDefault()).format(now)
        val day = SimpleDateFormat("dd", Locale.getDefault()).format(now)
        
        val nextNum = (lastNumber + 1).toString().padStart(3, '0')
        
        return format
            .replace("%YYYY%", year)
            .replace("%MM%", month)
            .replace("%DD%", day)
            .replace("%NUM%", nextNum)
    }
}
