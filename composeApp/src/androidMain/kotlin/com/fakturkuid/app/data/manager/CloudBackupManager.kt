package com.fakturkuid.app.data.manager

import android.content.Context
import android.net.Uri
import android.util.Log
import com.fakturkuid.app.R
import com.fakturkuid.app.data.entity.*
import com.fakturkuid.app.data.repository.BusinessProfileRepository
import com.fakturkuid.app.data.repository.InvoiceRepository
import com.fakturkuid.app.data.repository.CustomerRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

sealed class BackupResult {
    data class Success(val invoiceCount: Int) : BackupResult()
    data class Failure(val error: String) : BackupResult()
}

class CloudBackupManager(
    private val context: Context,
    private val invoiceRepository: InvoiceRepository,
    private val profileRepository: BusinessProfileRepository,
    private val customerRepository: CustomerRepository,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val json = Json { 
        ignoreUnknownKeys = true 
        encodeDefaults = true
    }

    private fun getCurrentUserId(): String? = auth.currentUser?.uid

    suspend fun performBackup(): BackupResult = withContext(Dispatchers.IO) {
        val userId = getCurrentUserId() ?: return@withContext BackupResult.Failure(context.getString(R.string.msg_auth_failed, "No User"))
        
        try {
            // 1. Collect Data
            val invoices = invoiceRepository.getAllInvoicesSync()
            val originalProfile = profileRepository.getProfileSync()
            val customers = customerRepository.getAllCustomersSync()
            
            // OPTIMIZATION: Compress logo if it's too large to fit in Firestore (1MB limit)
            val profile = originalProfile?.let { prof ->
                if (prof.logoBlob != null && prof.logoBlob.size > 500_000) {
                    prof.copy(logoBlob = compressImage(prof.logoBlob))
                } else prof
            }
            
            val backupData = BackupData(
                appVersion = "1.1",
                backupTime = System.currentTimeMillis(),
                profile = profile,
                invoices = invoices,
                customers = customers
            )

            // 2. Serialize and Compress to Base64
            val jsonData = json.encodeToString(backupData)
            val compressedData = compress(jsonData)
            val base64Data = android.util.Base64.encodeToString(compressedData, android.util.Base64.NO_WRAP)

            // Check if still too large for Firestore
            if (base64Data.length > 1_000_000) {
                 return@withContext BackupResult.Failure(context.getString(R.string.msg_backup_too_large))
            }

            // 3. Save to Firestore
            val fileName = "backup_${System.currentTimeMillis()}"
            val metadata = mapOf(
                "fileName" to fileName,
                "timestamp" to backupData.backupTime,
                "size" to base64Data.length.toLong(),
                "companyName" to (profile?.businessName ?: "Unknown"),
                "hasLogo" to (profile?.logoBlob != null),
                "invoiceCount" to invoices.size,
                "data" to base64Data
            )

            firestore.collection("users")
                .document(userId)
                .collection("backups")
                .document(fileName)
                .set(metadata)
                .await()

            BackupResult.Success(invoices.size)
        } catch (e: Exception) {
            Log.e("CloudBackup", "Backup failed", e)
            BackupResult.Failure(e.message ?: "Unknown error")
        }
    }

    private fun compressImage(imageBytes: ByteArray): ByteArray {
        return try {
            val bitmap = android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            val out = ByteArrayOutputStream()
            // Resize to max 500px and compress to 70% quality
            val scaledBitmap = if (bitmap.width > 500 || bitmap.height > 500) {
                val ratio = bitmap.width.toFloat() / bitmap.height.toFloat()
                val width = if (ratio > 1) 500 else (500 * ratio).toInt()
                val height = if (ratio > 1) (500 / ratio).toInt() else 500
                android.graphics.Bitmap.createScaledBitmap(bitmap, width, height, true)
            } else bitmap
            
            scaledBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 70, out)
            out.toByteArray()
        } catch (e: Exception) {
            imageBytes // Return original if failed
        }
    }

    suspend fun getBackupHistory(): List<BackupMetadata> = withContext(Dispatchers.IO) {
        val userId = getCurrentUserId() ?: return@withContext emptyList()
        
        try {
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("backups")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()
            
            snapshot.documents.mapNotNull { doc ->
                BackupMetadata(
                    fileName = doc.getString("fileName") ?: "",
                    timestamp = doc.getLong("timestamp") ?: 0L,
                    size = doc.getLong("size") ?: 0L,
                    companyName = doc.getString("companyName") ?: "Unknown",
                    hasLogo = doc.getBoolean("hasLogo") ?: false,
                    invoiceCount = doc.getLong("invoiceCount")?.toInt() ?: 0
                )
            }
        } catch (e: Exception) {
            Log.e("CloudBackup", "Failed to fetch history", e)
            emptyList()
        }
    }

    suspend fun restoreFromCloud(fileName: String): BackupResult = withContext(Dispatchers.IO) {
        val userId = getCurrentUserId() ?: return@withContext BackupResult.Failure(context.getString(R.string.msg_auth_failed, "No User"))
        
        try {
            val doc = firestore.collection("users")
                .document(userId)
                .collection("backups")
                .document(fileName)
                .get()
                .await()

            val base64Data = doc.getString("data") ?: return@withContext BackupResult.Failure(context.getString(R.string.msg_restore_failed, "Data not found"))

            val compressedBytes = android.util.Base64.decode(base64Data, android.util.Base64.DEFAULT)
            val jsonData = decompress(compressedBytes)
            val backupData = json.decodeFromString<BackupData>(jsonData)

            if (backupData.profile != null) {
                profileRepository.saveProfile(backupData.profile)
            }
            
            // Restore customers first to avoid FK issues if any
            customerRepository.deleteAllCustomers()
            customerRepository.insertCustomers(backupData.customers)
            
            invoiceRepository.deleteAllInvoices()
            backupData.invoices.forEach { 
                invoiceRepository.restoreInvoice(it.invoice, it.items)
            }

            BackupResult.Success(backupData.invoices.size)
        } catch (e: Exception) {
            Log.e("CloudBackup", "Restore failed", e)
            BackupResult.Failure(e.message ?: "Restore failed")
        }
    }

    suspend fun deleteBackup(fileName: String): Boolean = withContext(Dispatchers.IO) {
        val userId = getCurrentUserId() ?: return@withContext false
        try {
            firestore.collection("users")
                .document(userId)
                .collection("backups")
                .document(fileName)
                .delete()
                .await()
            true
        } catch (e: Exception) {
            Log.e("CloudBackup", "Delete failed", e)
            false
        }
    }

    private fun compress(data: String): ByteArray {
        val baos = ByteArrayOutputStream()
        GZIPOutputStream(baos).use { gzip ->
            gzip.write(data.toByteArray(Charsets.UTF_8))
        }
        return baos.toByteArray()
    }

    private fun decompress(data: ByteArray): String {
        return GZIPInputStream(data.inputStream()).bufferedReader(Charsets.UTF_8).use { it.readText() }
    }
}
