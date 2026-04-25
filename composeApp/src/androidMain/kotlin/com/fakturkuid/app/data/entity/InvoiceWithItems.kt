package com.fakturkuid.app.data.entity

import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.serialization.Serializable

@Serializable
data class InvoiceWithItems(
    @Embedded val invoice: Invoice,
    @Relation(
        parentColumn = "id",
        entityColumn = "invoiceId"
    )
    val items: List<InvoiceItem>
) {
    fun calculateSubtotal(): Double = items.sumOf { it.total }

    fun calculateDiscountAmount(): Double {
        val sub = calculateSubtotal()
        return if (invoice.diskonType == DiscountType.PERCENTAGE.value) sub * (invoice.diskon / 100.0) else invoice.diskon
    }

    fun calculateTaxAmount(): Double {
        val sub = calculateSubtotal()
        val d = calculateDiscountAmount()
        return (sub - d) * (invoice.pajak / 100.0)
    }

    fun calculateTotal(): Double {
        val sub = calculateSubtotal()
        val d = calculateDiscountAmount()
        val p = calculateTaxAmount()
        return sub - d + p
    }
}
