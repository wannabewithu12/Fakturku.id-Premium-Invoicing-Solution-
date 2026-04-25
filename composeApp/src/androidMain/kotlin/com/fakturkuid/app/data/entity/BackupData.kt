package com.fakturkuid.app.data.entity

import kotlinx.serialization.Serializable

@Serializable
data class BackupData(
    val appVersion: String,
    val backupTime: Long,
    val profile: BusinessProfile?,
    val invoices: List<InvoiceWithItems>,
    val customers: List<Customer> = emptyList()
)

@Serializable
data class BackupMetadata(
    val fileName: String,
    val timestamp: Long,
    val size: Long,
    val companyName: String,
    val hasLogo: Boolean,
    val invoiceCount: Int
)
