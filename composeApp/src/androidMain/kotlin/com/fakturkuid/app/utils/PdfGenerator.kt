package com.fakturkuid.app.utils

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import androidx.core.graphics.createBitmap
import androidx.core.graphics.toColorInt
import androidx.core.graphics.withRotation
import com.fakturkuid.app.R
import com.fakturkuid.app.data.entity.BusinessProfile
import com.fakturkuid.app.data.entity.InvoiceWithItems
import java.io.File
import java.io.OutputStream
import kotlin.math.max
import kotlin.math.min

object PdfGenerator {
    
    fun generateInvoicePdf(
        context: Context,
        outputStream: OutputStream,
        invoiceData: InvoiceWithItems,
        profile: BusinessProfile?,
        currency: String
    ) {
        val rate = CurrencyFormatter.getConversionRate(currency)
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint()
        val textPaint = Paint().apply {
            color = Color.BLACK
            textSize = 12f
            isAntiAlias = true
        }

        var yPos = 50f

        // Watermark logic moved to the end to be on top of everything

        // Draw Logo if available
        profile?.logoUri?.takeIf { it.isNotBlank() }?.let { logoUri ->
            try {
                val file = File(logoUri)
                if (file.exists()) {
                    var bitmap = BitmapFactory.decodeFile(file.absolutePath)
                    if (bitmap != null) {
                        bitmap = removeWhiteBackground(bitmap)
                        val targetSize = 140f // Increased from 100f
                        val ratio = bitmap.width.toFloat() / bitmap.height.toFloat()
                        val drawWidth = if (ratio > 1) targetSize else targetSize * ratio
                        val drawHeight = if (ratio > 1) targetSize / ratio else targetSize
                        
                        val rect = RectF(50f, 40f, 50f + drawWidth, 40f + drawHeight)
                        canvas.drawBitmap(bitmap, null, rect, Paint().apply { isFilterBitmap = true; isAntiAlias = true })
                        yPos = 40f + drawHeight + 25f // Adjusted spacing
                    }
                }
            } catch (_: Exception) {
            }
        }

        // --- TWO COLUMN HEADER ---
        // Column 1: Business Info (Left)
        textPaint.textSize = 18f
        textPaint.isFakeBoldText = true
        canvas.drawText(profile?.businessName ?: context.getString(R.string.pdf_invoice_label), 50f, yPos, textPaint)
        
        // Save initial yPos for right column alignment
        val headerStartY = yPos
        
        yPos += 20f
        textPaint.textSize = 9f
        textPaint.isFakeBoldText = false
        textPaint.color = Color.GRAY
        
        // Draw wrapped address to avoid collision
        val maxAddressWidth = 300f
        val address = profile?.address ?: ""
        var addressY = yPos
        if (address.isNotEmpty()) {
            val words = address.split(" ")
            var line = ""
            for (word in words) {
                val testLine = if (line.isEmpty()) word else "$line $word"
                if (textPaint.measureText(testLine) > maxAddressWidth) {
                    canvas.drawText(line, 50f, addressY, textPaint)
                    line = word
                    addressY += 12f
                } else {
                    line = testLine
                }
            }
            canvas.drawText(line, 50f, addressY, textPaint)
            addressY += 12f
        }
        
        yPos = addressY
        canvas.drawText("${context.getString(R.string.whatsapp_number)}: ${profile?.phone ?: "-"}", 50f, yPos, textPaint)
        yPos += 12f
        canvas.drawText("${context.getString(R.string.email_label)}: ${profile?.email ?: "-"}", 50f, yPos, textPaint)

        // Column 2: Invoice Info (Right)
        val rightColX = 380f
        var rightY = headerStartY
        textPaint.color = Color.BLACK
        textPaint.textSize = 14f
        textPaint.isFakeBoldText = true
        canvas.drawText(context.getString(R.string.pdf_invoice_label), rightColX, rightY, textPaint)
        rightY += 18f
        textPaint.textSize = 10f
        textPaint.isFakeBoldText = false
        canvas.drawText("${context.getString(R.string.pdf_number_label)} ${invoiceData.invoice.invoiceNumber}", rightColX, rightY, textPaint)
        rightY += 14f
        val dateFormat = context.getString(R.string.date_format)
        val locale = context.resources.configuration.locales[0]
        canvas.drawText("${context.getString(R.string.pdf_date_label)} ${DateFormatter.formatDate(invoiceData.invoice.issueDate, dateFormat, locale)}", rightColX, rightY, textPaint)
        if (invoiceData.invoice.dueDate != null) {
            rightY += 14f
            canvas.drawText("${context.getString(R.string.pdf_due_date_label)} ${DateFormatter.formatDate(invoiceData.invoice.dueDate, "dd MMM yyyy, HH:mm", locale)}", rightColX, rightY, textPaint)
        }
        
        yPos = max(yPos, rightY) + 40f

        // Invoice To
        textPaint.textSize = 11f
        textPaint.isFakeBoldText = true
        textPaint.color = Color.DKGRAY
        canvas.drawText(context.getString(R.string.pdf_invoice_to_label), 50f, yPos, textPaint)
        yPos += 18f
        textPaint.textSize = 12f
        textPaint.isFakeBoldText = true
        textPaint.color = Color.BLACK
        canvas.drawText(invoiceData.invoice.customerName, 50f, yPos, textPaint)
        yPos += 14f
        textPaint.textSize = 10f
        textPaint.isFakeBoldText = false
        textPaint.color = Color.GRAY
        canvas.drawText(invoiceData.invoice.customerAddress ?: "", 50f, yPos, textPaint)
        yPos += 14f
        canvas.drawText(invoiceData.invoice.customerPhone ?: "", 50f, yPos, textPaint)
        
        if (!invoiceData.invoice.customerMemberNumber.isNullOrBlank()) {
            yPos += 14f
            canvas.drawText("${context.getString(R.string.member_number)}: ${invoiceData.invoice.customerMemberNumber}", 50f, yPos, textPaint)
        }
        
        if (!invoiceData.invoice.customerEmail.isNullOrBlank()) {
            yPos += 14f
            canvas.drawText("${context.getString(R.string.email_label)}: ${invoiceData.invoice.customerEmail}", 50f, yPos, textPaint)
        }
        
        yPos += 30f

        // --- ITEMS TABLE ---
        // Header
        paint.color = "#F1F5F9".toColorInt()
        canvas.drawRect(50f, yPos - 15f, 545f, yPos + 10f, paint)
        textPaint.isFakeBoldText = true
        textPaint.color = Color.BLACK
        canvas.drawText(context.getString(R.string.pdf_description_label), 60f, yPos, textPaint)
        canvas.drawText(context.getString(R.string.pdf_qty_label), 330f, yPos, textPaint)
        canvas.drawText(context.getString(R.string.pdf_price_label), 390f, yPos, textPaint)
        canvas.drawText(context.getString(R.string.pdf_total_label), 480f, yPos, textPaint)
        yPos += 25f

        // List
        var subtotal = 0.0
        invoiceData.items.forEachIndexed { index, item ->
            if (index % 2 != 0) {
                paint.color = "#F8FAFC".toColorInt()
                canvas.drawRect(50f, yPos - 15f, 545f, yPos + 5f, paint)
            }
            textPaint.isFakeBoldText = false
            canvas.drawText(item.description, 60f, yPos, textPaint)
            canvas.drawText(item.quantity.toString(), 330f, yPos, textPaint)
            canvas.drawText(CurrencyFormatter.formatCurrency(item.unitPrice * rate, currency), 390f, yPos, textPaint)
            canvas.drawText(CurrencyFormatter.formatCurrency(item.total * rate, currency), 480f, yPos, textPaint)
            subtotal += item.total
            yPos += 20f
        }

        yPos += 10f
        paint.color = Color.LTGRAY
        canvas.drawLine(350f, yPos, 545f, yPos, paint)
        yPos += 20f

        // --- TOTALS ---
        val subtotalAll = invoiceData.calculateSubtotal()
        val diskon = invoiceData.calculateDiscountAmount()
        val pajak = invoiceData.calculateTaxAmount()
        val grandTotal = invoiceData.calculateTotal()

        textPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText(context.getString(R.string.pdf_subtotal_label), 460f, yPos, textPaint)
        canvas.drawText(CurrencyFormatter.formatCurrency(subtotal * rate, currency), 540f, yPos, textPaint)
        yPos += 18f
        canvas.drawText(context.getString(R.string.pdf_discount_label), 460f, yPos, textPaint)
        canvas.drawText("- ${CurrencyFormatter.formatCurrency(diskon * rate, currency)}", 540f, yPos, textPaint)
        yPos += 18f
        canvas.drawText(context.getString(R.string.pdf_tax_label, invoiceData.invoice.pajak.toInt().toString()), 460f, yPos, textPaint)
        canvas.drawText(CurrencyFormatter.formatCurrency(pajak * rate, currency), 540f, yPos, textPaint)
        yPos += 25f

        // Grand Total Box
        val status = invoiceData.invoice.status.lowercase()
        paint.color = when (status) {
            "paid" -> "#10B981".toColorInt() // Green
            "unpaid" -> "#EF4444".toColorInt() // Red
            else -> "#0F172A".toColorInt() // Default Dark
        }
        
        // Adjusted box size (slightly wider and taller)
        canvas.drawRect(340f, yPos - 22f, 545f, yPos + 18f, paint)
        textPaint.color = Color.WHITE
        textPaint.isFakeBoldText = true
        textPaint.textSize = 13f
        canvas.drawText(context.getString(R.string.pdf_grand_total_label), 455f, yPos, textPaint)
        canvas.drawText(CurrencyFormatter.formatCurrency(grandTotal * rate, currency), 540f, yPos, textPaint)
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.color = Color.BLACK
        
        // Notes
        if (!invoiceData.invoice.notes.isNullOrBlank()) {
            yPos += 60f
            textPaint.isFakeBoldText = true
            textPaint.textSize = 10f
            canvas.drawText(context.getString(R.string.pdf_notes_label), 50f, yPos, textPaint)
            yPos += 14f
            textPaint.isFakeBoldText = false
            canvas.drawText(invoiceData.invoice.notes, 50f, yPos, textPaint)
        }

        // Footer
        yPos = 780f
        paint.color = Color.LTGRAY
        canvas.drawLine(50f, yPos, 545f, yPos, paint)
        yPos += 20f
        textPaint.textSize = 9f
        textPaint.color = Color.GRAY
        textPaint.textAlign = Paint.Align.CENTER
        canvas.drawText(profile?.defaultFooter ?: "Thank you for your business!", pageInfo.pageWidth / 2f, yPos, textPaint)
        yPos += 12f
        canvas.drawText("Generated by Fakturku.id", pageInfo.pageWidth / 2f, yPos, textPaint)

        // --- DRAW WATERMARK/STAMP ON TOP ---
        if (status == "paid" || status == "unpaid") {
            val statusText = if (status == "paid") {
                context.getString(R.string.status_paid).uppercase()
            } else {
                context.getString(R.string.status_unpaid).uppercase()
            }
            
            val stampPaint = Paint().apply {
                color = if (status == "paid") "#22C55E".toColorInt() else "#EF4444".toColorInt()
                textSize = 70f
                isFakeBoldText = true
                alpha = 60
                style = Paint.Style.STROKE
                strokeWidth = 4f
            }
            
            canvas.withRotation(-30f, 300f, 450f) {
                val textWidth = stampPaint.measureText(statusText)
                val rect = RectF(300f - textWidth / 2 - 20f, 450f - 60f, 300f + textWidth / 2 + 20f, 450f + 20f)
                drawRoundRect(rect, 10f, 10f, stampPaint)
                
                stampPaint.style = Paint.Style.FILL
                drawText(statusText, 300f - textWidth / 2, 440f, stampPaint)
            }
        }

        pdfDocument.finishPage(page)
        
        // Write to stream
        pdfDocument.writeTo(outputStream)
        pdfDocument.close()
    }

    private fun removeWhiteBackground(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        
        val outBitmap = createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        
        for (i in pixels.indices) {
            val color = pixels[i]
            val r = Color.red(color)
            val g = Color.green(color)
            val b = Color.blue(color)
            
            if (r > 200 && g > 200 && b > 200) {
                pixels[i] = Color.TRANSPARENT
            }
        }
        outBitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        
        val finalBitmap = createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(finalBitmap)
        val paint = Paint().apply { isAntiAlias = true }
        
        val radius = (min(width, height) / 2).toFloat()
        canvas.drawCircle((width / 2).toFloat(), (height / 2).toFloat(), radius, paint)
        
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(outBitmap, 0f, 0f, paint)
        
        return finalBitmap
    }

    fun generateDailyReportPdf(
        context: Context,
        outputStream: OutputStream,
        invoices: List<InvoiceWithItems>,
        profile: BusinessProfile?,
        currency: String
    ) {
        val rate = CurrencyFormatter.getConversionRate(currency)
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(842, 595, 1).create() // A4 Landscape
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        val paint = Paint()
        val textPaint = Paint().apply { color = Color.BLACK; textSize = 10f; isAntiAlias = true }
        
        var yPos = 50f
        
        // Draw Logo if available
        profile?.logoUri?.takeIf { it.isNotBlank() }?.let { logoUri ->
            try {
                val file = File(logoUri)
                if (file.exists()) {
                    var bitmap = BitmapFactory.decodeFile(file.absolutePath)
                    if (bitmap != null) {
                        bitmap = removeWhiteBackground(bitmap)
                        val targetSize = 110f // Increased from 80f
                        val ratio = bitmap.width.toFloat() / bitmap.height.toFloat()
                        val drawWidth = if (ratio > 1) targetSize else targetSize * ratio
                        val drawHeight = if (ratio > 1) targetSize / ratio else targetSize
                        
                        val rightMargin = 842f - 50f - drawWidth
                        val rect = RectF(rightMargin, 40f, rightMargin + drawWidth, 40f + drawHeight)
                        canvas.drawBitmap(bitmap, null, rect, Paint().apply { isFilterBitmap = true; isAntiAlias = true })
                    }
                }
            } catch (_: Exception) {}
        }
        
        // Draw Header
        textPaint.textSize = 16f
        textPaint.isFakeBoldText = true
        canvas.drawText(context.getString(R.string.report_daily_detail).uppercase(), 50f, yPos, textPaint)
        yPos += 20f
        
        textPaint.textSize = 10f
        textPaint.isFakeBoldText = true
        canvas.drawText(profile?.businessName ?: "Fakturku.id", 50f, yPos, textPaint)
        yPos += 14f
        
        textPaint.isFakeBoldText = false
        textPaint.color = Color.DKGRAY
        
        // Draw wrapped address to avoid collision with logo
        val maxAddressWidth = 400f
        val address = profile?.address ?: ""
        if (address.isNotEmpty()) {
            val words = address.split(" ")
            var line = ""
            for (word in words) {
                val testLine = if (line.isEmpty()) word else "$line $word"
                if (textPaint.measureText(testLine) > maxAddressWidth) {
                    canvas.drawText(line, 50f, yPos, textPaint)
                    line = word
                    yPos += 12f
                } else {
                    line = testLine
                }
            }
            canvas.drawText(line, 50f, yPos, textPaint)
            yPos += 14f
        }
        
        val contactStr = listOfNotNull(profile?.phone?.takeIf { it.isNotBlank() }, profile?.email?.takeIf { it.isNotBlank() }).joinToString(" | ")
        if (contactStr.isNotEmpty()) {
            canvas.drawText(contactStr, 50f, yPos, textPaint)
            yPos += 14f
        }
        
        canvas.drawText("Generated: ${DateFormatter.formatDate(System.currentTimeMillis(), context.getString(R.string.date_format), context.resources.configuration.locales[0])}", 50f, yPos, textPaint)
        
        // Ensure yPos is below the logo (Logo is at Y=40, targetSize=80 -> Ends at 120)
        yPos = kotlin.math.max(yPos + 30f, 150f)

        // Table Header
        paint.color = "#475569".toColorInt()
        canvas.drawRect(50f, yPos - 15f, 792f, yPos + 10f, paint)
        textPaint.color = Color.WHITE
        textPaint.isFakeBoldText = true
        val colX = listOf(55f, 130f, 220f, 370f, 450f, 520f, 590f, 680f)
        canvas.drawText(context.getString(R.string.date_caps), colX[0], yPos, textPaint)
        canvas.drawText("No. Faktur", colX[1], yPos, textPaint)
        canvas.drawText(context.getString(R.string.customer_caps), colX[2], yPos, textPaint)
        canvas.drawText(context.getString(R.string.subtotal), colX[3], yPos, textPaint)
        canvas.drawText(context.getString(R.string.discount), colX[4], yPos, textPaint)
        canvas.drawText(context.getString(R.string.tax_amount), colX[5], yPos, textPaint)
        canvas.drawText(context.getString(R.string.pdf_total_label), colX[6], yPos, textPaint)
        canvas.drawText("Status", colX[7], yPos, textPaint)
        
        yPos += 25f
        textPaint.color = Color.BLACK
        textPaint.isFakeBoldText = false
        
        var grandTotal = 0.0
        
        invoices.forEachIndexed { index, data ->
            if (yPos > 540f) {
                pdfDocument.finishPage(page)
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                yPos = 50f
            }
            
            if (index % 2 != 0) {
                paint.color = "#F8FAFC".toColorInt()
                canvas.drawRect(50f, yPos - 15f, 792f, yPos + 5f, paint)
            }
            
            val inv = data.invoice
            val subtotal = data.calculateSubtotal()
            val diskon = data.calculateDiscountAmount()
            val pajak = data.calculateTaxAmount()
            val total = data.calculateTotal()
            grandTotal += total
            
            canvas.drawText(DateFormatter.formatDate(inv.issueDate, context.getString(R.string.date_format), context.resources.configuration.locales[0]), colX[0], yPos, textPaint)
            canvas.drawText(inv.invoiceNumber, colX[1], yPos, textPaint)
            
            val customerName = if (inv.customerName.length > 20) inv.customerName.take(17) + "..." else inv.customerName
            canvas.drawText(customerName, colX[2], yPos, textPaint)
            
            canvas.drawText(CurrencyFormatter.formatCurrency(subtotal * rate, currency), colX[3], yPos, textPaint)
            canvas.drawText(CurrencyFormatter.formatCurrency(diskon * rate, currency), colX[4], yPos, textPaint)
            canvas.drawText(CurrencyFormatter.formatCurrency(pajak * rate, currency), colX[5], yPos, textPaint)
            
            textPaint.isFakeBoldText = true
            canvas.drawText(CurrencyFormatter.formatCurrency(total * rate, currency), colX[6], yPos, textPaint)
            textPaint.isFakeBoldText = false
            
            val statusColor = if (inv.status == "paid") "#16A34A".toColorInt() else "#DC2626".toColorInt()
            val oldColor = textPaint.color
            textPaint.color = statusColor
            textPaint.isFakeBoldText = true
            canvas.drawText(if (inv.status == "paid") context.getString(R.string.status_paid).uppercase() else context.getString(R.string.status_unpaid).uppercase(), colX[7], yPos, textPaint)
            textPaint.color = oldColor
            textPaint.isFakeBoldText = false
            
            yPos += 20f
        }
        
        if (yPos > 520f) {
            pdfDocument.finishPage(page)
            page = pdfDocument.startPage(pageInfo)
            canvas = page.canvas
            yPos = 50f
        }
        
        yPos += 10f
        paint.color = "#E2E8F0".toColorInt()
        canvas.drawRect(520f, yPos - 15f, 792f, yPos + 15f, paint)
        textPaint.isFakeBoldText = true
        textPaint.textSize = 12f
        canvas.drawText("GRAND TOTAL:", 530f, yPos + 5f, textPaint)
        canvas.drawText(CurrencyFormatter.formatCurrency(grandTotal * rate, currency), 660f, yPos + 5f, textPaint)

        // Footer
        val footerY = 550f
        paint.color = Color.LTGRAY
        canvas.drawLine(50f, footerY, 792f, footerY, paint)
        textPaint.textSize = 9f
        textPaint.color = Color.GRAY
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.isFakeBoldText = false
        canvas.drawText(profile?.defaultFooter ?: "Solusi Cerdas Manajemen Faktur", pageInfo.pageWidth / 2f, footerY + 15f, textPaint)
        textPaint.textAlign = Paint.Align.LEFT

        pdfDocument.finishPage(page)
        pdfDocument.writeTo(outputStream)
        pdfDocument.close()
    }

    fun generateMonthlySummaryPdf(
        context: Context,
        outputStream: OutputStream,
        invoices: List<InvoiceWithItems>,
        profile: BusinessProfile?,
        currency: String
    ) {
        val rate = CurrencyFormatter.getConversionRate(currency)
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Portrait
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        val paint = Paint()
        val textPaint = Paint().apply { color = Color.BLACK; textSize = 11f; isAntiAlias = true }
        
        var yPos = 50f
        
        // Draw Logo if available
        profile?.logoUri?.takeIf { it.isNotBlank() }?.let { logoUri ->
            try {
                val file = File(logoUri)
                if (file.exists()) {
                    var bitmap = BitmapFactory.decodeFile(file.absolutePath)
                    if (bitmap != null) {
                        bitmap = removeWhiteBackground(bitmap)
                        val targetSize = 110f // Increased from 80f
                        val ratio = bitmap.width.toFloat() / bitmap.height.toFloat()
                        val drawWidth = if (ratio > 1) targetSize else targetSize * ratio
                        val drawHeight = if (ratio > 1) targetSize / ratio else targetSize
                        
                        val rightMargin = 595f - 50f - drawWidth
                        val rect = RectF(rightMargin, 40f, rightMargin + drawWidth, 40f + drawHeight)
                        canvas.drawBitmap(bitmap, null, rect, Paint().apply { isFilterBitmap = true; isAntiAlias = true })
                    }
                }
            } catch (_: Exception) {}
        }
        
        textPaint.textSize = 18f
        textPaint.isFakeBoldText = true
        canvas.drawText(context.getString(R.string.report_monthly_summary).uppercase(), 50f, yPos, textPaint)
        yPos += 22f
        
        textPaint.textSize = 11f
        textPaint.isFakeBoldText = true
        canvas.drawText(profile?.businessName ?: "Fakturku.id", 50f, yPos, textPaint)
        yPos += 15f
        
        textPaint.isFakeBoldText = false
        textPaint.color = Color.DKGRAY
        
        val maxAddressWidth = 300f
        val address = profile?.address ?: ""
        if (address.isNotEmpty()) {
            val words = address.split(" ")
            var line = ""
            for (word in words) {
                val testLine = if (line.isEmpty()) word else "$line $word"
                if (textPaint.measureText(testLine) > maxAddressWidth) {
                    canvas.drawText(line, 50f, yPos, textPaint)
                    line = word
                    yPos += 13f
                } else {
                    line = testLine
                }
            }
            canvas.drawText(line, 50f, yPos, textPaint)
            yPos += 15f
        }
        
        val contactStr = listOfNotNull(profile?.phone?.takeIf { it.isNotBlank() }, profile?.email?.takeIf { it.isNotBlank() }).joinToString(" | ")
        if (contactStr.isNotEmpty()) {
            canvas.drawText(contactStr, 50f, yPos, textPaint)
            yPos += 15f
        }
        
        canvas.drawText("Generated: ${DateFormatter.formatDate(System.currentTimeMillis(), context.getString(R.string.date_format), context.resources.configuration.locales[0])}", 50f, yPos, textPaint)
        
        yPos = kotlin.math.max(yPos + 40f, 160f)

        paint.color = "#475569".toColorInt()
        canvas.drawRect(50f, yPos - 15f, 545f, yPos + 10f, paint)
        textPaint.color = Color.WHITE
        textPaint.isFakeBoldText = true
        val colX = listOf(60f, 250f, 420f)
        canvas.drawText(context.getString(R.string.date_caps), colX[0], yPos, textPaint)
        canvas.drawText(context.getString(R.string.total_transactions), colX[1], yPos, textPaint)
        canvas.drawText(context.getString(R.string.pdf_total_label), colX[2], yPos, textPaint)
        
        yPos += 25f
        textPaint.color = Color.BLACK
        textPaint.isFakeBoldText = false
        
        val grouped = invoices
            .groupBy { DateFormatter.formatDate(it.invoice.issueDate, context.getString(R.string.date_format), context.resources.configuration.locales[0]) }
            .mapValues { (_, its) ->
                val sumTotal = its.sumOf { it.calculateTotal() }
                Pair(its.size, sumTotal)
            }
            
        var grandTotal = 0.0
        var count = 0
        
        grouped.forEach { (date, stats) ->
            if (yPos > 780f) {
                pdfDocument.finishPage(page)
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                yPos = 50f
            }
            
            if (count % 2 != 0) {
                paint.color = "#F8FAFC".toColorInt()
                canvas.drawRect(50f, yPos - 15f, 545f, yPos + 5f, paint)
            }
            
            canvas.drawText(date, colX[0], yPos, textPaint)
            canvas.drawText(stats.first.toString(), colX[1], yPos, textPaint)
            
            textPaint.isFakeBoldText = true
            canvas.drawText(CurrencyFormatter.formatCurrency(stats.second * rate, currency), colX[2], yPos, textPaint)
            textPaint.isFakeBoldText = false
            
            grandTotal += stats.second
            count++
            yPos += 20f
        }
        
        if (yPos > 760f) {
            pdfDocument.finishPage(page)
            page = pdfDocument.startPage(pageInfo)
            canvas = page.canvas
            yPos = 50f
        }
        
        yPos += 15f
        paint.color = "#E2E8F0".toColorInt()
        canvas.drawRect(230f, yPos - 15f, 545f, yPos + 15f, paint)
        textPaint.isFakeBoldText = true
        textPaint.textSize = 13f
        canvas.drawText("GRAND TOTAL:", 240f, yPos + 5f, textPaint)
        canvas.drawText(CurrencyFormatter.formatCurrency(grandTotal * rate, currency), 410f, yPos + 5f, textPaint)

        // Footer
        val footerY = 800f
        paint.color = Color.LTGRAY
        canvas.drawLine(50f, footerY, 545f, footerY, paint)
        textPaint.textSize = 9f
        textPaint.color = Color.GRAY
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.isFakeBoldText = false
        canvas.drawText(profile?.defaultFooter ?: "Solusi Cerdas Manajemen Faktur", pageInfo.pageWidth / 2f, footerY + 15f, textPaint)
        textPaint.textAlign = Paint.Align.LEFT

        pdfDocument.finishPage(page)
        pdfDocument.writeTo(outputStream)
        pdfDocument.close()
    }
}
