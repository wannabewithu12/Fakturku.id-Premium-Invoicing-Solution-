package com.fakturkuid.app.utils

import com.fakturkuid.app.R
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.fakturkuid.app.data.entity.BusinessProfile
import com.fakturkuid.app.data.entity.InvoiceWithItems
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object PdfShareHelper {

    /**
     * Generates a PDF invoice into the app's cache directory and shares it
     * directly via a system share sheet — no need for the user to pick a
     * save location first.
     *
     * @param context   Android context
     * @param invoiceData  The invoice + items to render
     * @param profile   Business profile (logo, name, etc.)
     * @param currency  Currency code for formatting
     * @param onError   Called on the main thread if generation fails
     */
    suspend fun shareInvoicePdf(
        context: Context,
        invoiceData: InvoiceWithItems,
        profile: BusinessProfile?,
        currency: String,
        message: String? = null,
        targetPhone: String? = null,
        onError: (String) -> Unit = {}
    ) {
        withContext(Dispatchers.IO) {
            try {
                val uri = generatePdfUri(context, invoiceData, profile, currency)

                // Launch the share thread on the main thread
                withContext(Dispatchers.Main) {
                    val jid = if (!targetPhone.isNullOrEmpty()) "$targetPhone@s.whatsapp.net" else null
                    
                    // Gunakan Intent SEND langsung yang paling stabil untuk PDF + Text
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/pdf"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        putExtra(Intent.EXTRA_TEXT, message)
                        if (jid != null) {
                            putExtra("jid", jid)
                        }
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }

                    val packageManager = context.packageManager
                    if (Intent(shareIntent).apply { setPackage("com.whatsapp.w4b") }.resolveActivity(packageManager) != null) {
                        shareIntent.setPackage("com.whatsapp.w4b")
                        context.startActivity(shareIntent)
                    } else if (Intent(shareIntent).apply { setPackage("com.whatsapp") }.resolveActivity(packageManager) != null) {
                        shareIntent.setPackage("com.whatsapp")
                        context.startActivity(shareIntent)
                    } else {
                        // Fallback chooser
                        context.startActivity(Intent.createChooser(shareIntent, "Kirim Invoice via..."))
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError(e.message ?: "Unknown error")
                }
            }
        }
    }

    /**
     * Shares the invoice PDF via a generic system chooser (Free Share)
     */
    suspend fun shareInvoiceFree(
        context: Context,
        invoiceData: InvoiceWithItems,
        profile: BusinessProfile?,
        currency: String,
        onError: (String) -> Unit = {}
    ) {
        withContext(Dispatchers.IO) {
            try {
                val uri = generatePdfUri(context, invoiceData, profile, currency)
                withContext(Dispatchers.Main) {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/pdf"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    val chooser = Intent.createChooser(intent, "Bagikan Invoice via...")
                    chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(chooser)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError(e.message ?: "Unknown error")
                }
            }
        }
    }

    /**
     * Copies the text message to clipboard.
     */
    fun copyMessageToClipboard(context: Context, message: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clip = android.content.ClipData.newPlainText("Invoice Message", message)
        clipboard.setPrimaryClip(clip)
        android.widget.Toast.makeText(context, context.getString(R.string.msg_message_copied), android.widget.Toast.LENGTH_SHORT).show()
    }

    private fun saveToDownloads(context: Context, sourceUri: android.net.Uri, invoiceNumber: String) {
        try {
            val resolver = context.contentResolver
            val contentValues = android.content.ContentValues().apply {
                put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, "invoice_${invoiceNumber.replace("/", "-")}.pdf")
                put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_DOWNLOADS)
                }
            }

            val mediaUri = resolver.insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            if (mediaUri != null) {
                resolver.openOutputStream(mediaUri)?.use { output ->
                    resolver.openInputStream(sourceUri)?.use { input ->
                        input.copyTo(output)
                    }
                }
            }
        } catch (e: Exception) {
            // Silently fail if MediaStore fails, clip data is still our primary method
        }
    }

    private fun generatePdfUri(
        context: Context,
        invoiceData: InvoiceWithItems,
        profile: BusinessProfile?,
        currency: String
    ): android.net.Uri {
        val fileName = "${invoiceData.invoice.invoiceNumber}.pdf".replace("/", "-")
        val tempFile = File(context.cacheDir, fileName)

        tempFile.outputStream().use { outputStream ->
            PdfGenerator.generateInvoicePdf(
                context = context,
                outputStream = outputStream,
                invoiceData = invoiceData,
                profile = profile,
                currency = currency
            )
        }

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            tempFile
        )
    }

    private fun copyToClipboard(context: Context, uri: android.net.Uri) {
        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        
        // Buat ClipData dengan MIME type yang spesifik
        val clip = android.content.ClipData.newUri(context.contentResolver, "Invoice PDF", uri)
        
        // Tambahkan metadata tambahan agar aplikasi target lebih mudah mengenali konten ini sebagai file
        clip.apply {
            description.apply {
                // Memberikan petunjuk bahwa ini adalah file PDF
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    // Set label dan MIME type di deskripsi
                }
            }
        }
        
        clipboard.setPrimaryClip(clip)
        
        // Penting: Pada Android 13+, sistem akan memunculkan preview clipboard sendiri.
        // Untuk versi di bawahnya, kita berikan Toast konfirmasi.
    }
}
