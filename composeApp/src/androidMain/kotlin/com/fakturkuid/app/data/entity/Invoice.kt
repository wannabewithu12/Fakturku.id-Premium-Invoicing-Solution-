package com.fakturkuid.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "invoices")
data class Invoice(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceNumber: String,
    val customerName: String,
    val customerAddress: String?,
    val customerPhone: String?,
    val customerWhatsapp: String?,
    val customerEmail: String? = null,
    val customerMemberNumber: String? = null,
    val issueDate: Long,
    val dueDate: Long?,
    val notes: String?,
    val footer: String?,
    val diskon: Double = 0.0,
    val diskonType: String = "nominal", // "nominal" or "percentage"
    val pajak: Double = 0.0,
    val status: String = "unpaid", // "paid", "unpaid", "overdue"
    val customerId: Long? = null
)
