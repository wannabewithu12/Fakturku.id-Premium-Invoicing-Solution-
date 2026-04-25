package com.fakturkuid.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "business_profiles")
data class BusinessProfile(
    @PrimaryKey val id: Int = 1, // Singleton
    val businessName: String,
    val address: String,
    val phone: String,
    val email: String,
    val logoUri: String?, // Allow saving URI to image
    val logoBlob: ByteArray?, // Store image data directly for backup/restore purposes
    val defaultFooter: String?,
    // WhatsApp Direct Templates
    val waTemplatePaid: String? = null,
    val waTemplateUnpaid: String? = null,
    val waTemplateOverdue: String? = null
)
