package com.fakturkuid.app.utils

import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {
    fun formatRupiah(amount: Double): String {
        return formatCurrency(amount, "id")
    }

    fun getConversionRate(languageCode: String): Double {
        // IDR is the base currency
        return when (languageCode) {
            "ja" -> 1.0 / 105.0     // 1 JPY = ~105 IDR
            "en" -> 1.0 / 16000.0   // 1 USD = ~16000 IDR
            "zh" -> 1.0 / 2200.0    // 1 CNY = ~2200 IDR
            else -> 1.0
        }
    }

    fun formatCurrency(amount: Double, languageCode: String): String {
        val locale = when (languageCode) {
            "ja" -> Locale.JAPAN
            "en" -> Locale.US
            "zh" -> Locale.CHINA
            else -> Locale("in", "ID")
        }
        val formatter = java.text.NumberFormat.getCurrencyInstance(locale)
        // Mencegah pembulatan paksa dengan memastikan angka desimal tetap ditampilkan jika ada
        formatter.maximumFractionDigits = 2
        formatter.minimumFractionDigits = if (languageCode == "id" || languageCode == "ja") 0 else 2
        return formatter.format(amount)
    }

    fun formatWithRate(amount: Double, languageCode: String): String {
        val rate = getConversionRate(languageCode)
        return formatCurrency(amount * rate, languageCode)
    }
}
