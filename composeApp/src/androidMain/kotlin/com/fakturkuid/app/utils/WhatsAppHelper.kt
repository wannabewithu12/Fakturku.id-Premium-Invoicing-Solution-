package com.fakturkuid.app.utils

/**
 * Formats a phone number to international WhatsApp format (e.g. 6281234567890).
 * Handles common Indonesian formats:
 *   - "08xx..." → "628xx..."
 *   - "+62 8xx" → "628xx..."
 *   - "628xx..." → unchanged
 */
fun formatPhoneForWhatsApp(phone: String): String {
    val digits = phone.filter { it.isDigit() }
    return when {
        digits.startsWith("62") -> digits
        digits.startsWith("0")  -> "62" + digits.substring(1)
        else                    -> "62$digits"
    }
}

/**
 * Formats a message template by replacing variables.
 * Variables: {customer}, {invoice}, {amount}
 */
fun formatWhatsAppMessage(
    template: String,
    customerName: String,
    invoiceNumber: String,
    amount: String
): String {
    return template
        .replace("{customer}", customerName, ignoreCase = true)
        .replace("{invoice}", invoiceNumber, ignoreCase = true)
        .replace("{amount}", amount, ignoreCase = true)
}
