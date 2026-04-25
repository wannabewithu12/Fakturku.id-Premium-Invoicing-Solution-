package com.fakturkuid.app.data.entity

enum class InvoiceStatus(val value: String) {
    PAID("paid"),
    UNPAID("unpaid"),
    OVERDUE("overdue")
}

enum class DiscountType(val value: String) {
    NOMINAL("nominal"),
    PERCENTAGE("percentage")
}
