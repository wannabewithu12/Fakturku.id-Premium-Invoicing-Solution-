package com.fakturkuid.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val address: String? = null,
    val phone: String? = null,
    val whatsapp: String? = null,
    val email: String? = null,
    val memberNumber: String? = null,
    val isMember: Boolean = false,
    val status: String = "Active" // "Active", "Inactive"
)
