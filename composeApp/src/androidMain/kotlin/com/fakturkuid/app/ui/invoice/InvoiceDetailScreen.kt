package com.fakturkuid.app.ui.invoice

import com.fakturkuid.app.R
import androidx.compose.ui.res.stringResource

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.fakturkuid.app.data.entity.InvoiceStatus
import com.fakturkuid.app.ui.components.StatusBadge
import com.fakturkuid.app.ui.NavRoutes
import com.fakturkuid.app.ui.viewmodel.BusinessProfileViewModel
import com.fakturkuid.app.ui.viewmodel.InvoiceDashboardViewModel
import com.fakturkuid.app.ui.viewmodel.InvoiceEditorViewModel
import com.fakturkuid.app.ui.viewmodel.SettingsViewModel
import com.fakturkuid.app.utils.CurrencyFormatter
import com.fakturkuid.app.utils.DateFormatter
import com.fakturkuid.app.utils.PdfGenerator
import com.fakturkuid.app.utils.PdfShareHelper
import com.fakturkuid.app.utils.formatPhoneForWhatsApp
import com.fakturkuid.app.utils.formatWhatsAppMessage
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import androidx.core.net.toUri
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceDetailScreen(
    navController: NavController,
    invoiceId: Long,
    dashboardViewModel: InvoiceDashboardViewModel = koinViewModel(),
    editorViewModel: InvoiceEditorViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val invoicesWithItems by dashboardViewModel.invoicesWithItems.collectAsState()
    val currentInvoiceData = remember(invoicesWithItems, invoiceId) {
        invoicesWithItems.find { it.invoice.id == invoiceId }
    }

    val profileViewModel: BusinessProfileViewModel = koinViewModel()
    val profile by profileViewModel.profile.collectAsState()

    val settingsViewModel: SettingsViewModel = koinViewModel()
    val currency by settingsViewModel.currency.collectAsState()

    val showDeleteDialog   = remember { mutableStateOf(false) }
    val showMarkPaidDialog = remember { mutableStateOf(false) }

    val accentBlue  = Color(0xFF00D1FF)
    val accentCyan  = Color(0xFF2DD4BF)
    val accentGreen = Color(0xFF00E5A0)

    val strMarkAsPaidTitle = stringResource(R.string.mark_as_paid_btn)
    val strMarkAsPaidMsg   = stringResource(R.string.mark_as_paid_confirm_msg)
    val strDuplicated      = stringResource(R.string.msg_invoice_duplicated)
    val strMessageCopied   = stringResource(R.string.msg_message_copied)

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        uri?.let {
            scope.launch {
                try {
                    context.contentResolver.openOutputStream(it)?.use { outputStream ->
                        currentInvoiceData?.let { data ->
                            PdfGenerator.generateInvoicePdf(context, outputStream, data, profile, currency)
                        }
                    }
                    Toast.makeText(context, R.string.msg_pdf_exported, Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // ── Delete dialog ────────────────────────────────────────────────────────
    if (showDeleteDialog.value) {
        AlertDialog(
            containerColor = Color(0xFF1E293B),
            onDismissRequest = { showDeleteDialog.value = false },
            title = { Text(stringResource(R.string.delete_invoice_title), color = Color.White) },
            text  = { Text(stringResource(R.string.delete_invoice_msg, currentInvoiceData?.invoice?.invoiceNumber ?: ""), color = Color.Gray) },
            confirmButton = {
                TextButton(onClick = {
                    currentInvoiceData?.invoice?.let { editorViewModel.deleteInvoice(it) }
                    showDeleteDialog.value = false
                    navController.popBackStack()
                }) { Text(stringResource(R.string.delete), color = Color(0xFFFB7185)) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog.value = false }) {
                    Text(stringResource(R.string.cancel), color = Color.Gray)
                }
            }
        )
    }

    // ── Mark as Paid dialog ──────────────────────────────────────────────────
    if (showMarkPaidDialog.value) {
        AlertDialog(
            containerColor = Color(0xFF1E293B),
            onDismissRequest = { showMarkPaidDialog.value = false },
            title = { Text(strMarkAsPaidTitle, color = Color.White) },
            text  = { Text(strMarkAsPaidMsg, color = Color.Gray) },
            confirmButton = {
                TextButton(onClick = {
                    currentInvoiceData?.invoice?.let { editorViewModel.updateInvoiceStatus(it.id, InvoiceStatus.PAID.value) }
                    showMarkPaidDialog.value = false
                }) { Text(stringResource(R.string.mark_as_paid_btn), color = accentCyan, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showMarkPaidDialog.value = false }) {
                    Text(stringResource(R.string.cancel), color = Color.Gray)
                }
            }
        )
    }

    Scaffold(
        containerColor = Color(0xFF0B1222),
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        Icons.AutoMirrored.Rounded.ArrowBackIos,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    stringResource(R.string.invoice_detail_title).uppercase(),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp,
                    color = Color.White,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                // Edit shortcut
                IconButton(onClick = { navController.navigate(NavRoutes.editInvoice(invoiceId)) }) {
                    Icon(Icons.Rounded.Edit, contentDescription = "Edit", tint = accentBlue.copy(alpha = 0.8f), modifier = Modifier.size(20.dp))
                }
                // Duplicate shortcut in top bar
                IconButton(onClick = {
                    currentInvoiceData?.let { data ->
                        scope.launch {
                            val now   = Calendar.getInstance()
                            val start = now.apply {
                                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0)
                            }.timeInMillis
                            val count     = editorViewModel.getInvoiceCountInPeriod(start, System.currentTimeMillis())
                            val newNumber = com.fakturkuid.app.utils.InvoiceNumberGenerator.generate(count)
                            val duplicated = data.invoice.copy(
                                id = 0, invoiceNumber = newNumber,
                                issueDate = System.currentTimeMillis(), dueDate = null, status = "unpaid"
                            )
                            editorViewModel.saveInvoice(duplicated, data.items.map { it.copy(id = 0, invoiceId = 0) })
                            Toast.makeText(context, strDuplicated, Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        }
                    }
                }) {
                    Icon(Icons.Rounded.ContentCopy, contentDescription = "Duplicate", tint = accentBlue.copy(alpha = 0.8f), modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = { showDeleteDialog.value = true }) {
                    Icon(Icons.Rounded.Delete, contentDescription = null, tint = Color(0xFFFB7185).copy(alpha = 0.8f), modifier = Modifier.size(22.dp))
                }
            }
        }
    ) { padding ->
        currentInvoiceData?.let { data ->
            val invoice  = data.invoice
            val subtotal = data.calculateSubtotal()
            val disc     = data.calculateDiscountAmount()
            val tax      = data.calculateTaxAmount()
            val grand    = data.calculateTotal()

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp)
            ) {
                // ── Hero Card ────────────────────────────────────────────────
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp),
                        color = Color(0xFF131F35)
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            // Invoice number + badge
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(accentBlue.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Rounded.Receipt, contentDescription = null, tint = accentBlue, modifier = Modifier.size(24.dp))
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(invoice.invoiceNumber, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = Color.White)
                                    StatusBadge(invoice.status)
                                }
                            }

                            Spacer(modifier = Modifier.height(28.dp))

                            // Total Bill hero amount
                            Text(stringResource(R.string.total_bill), style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
                            Text(
                                CurrencyFormatter.formatWithRate(grand, currency),
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(20.dp))
                            HorizontalDivider(color = Color.White.copy(alpha = 0.06f))
                            Spacer(modifier = Modifier.height(20.dp))

                            // Customer + Date
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text(stringResource(R.string.customer_caps), style = MaterialTheme.typography.labelSmall, color = Color.Gray, letterSpacing = 1.sp)
                                    Text(invoice.customerName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold, color = Color.White)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(stringResource(R.string.date_caps), style = MaterialTheme.typography.labelSmall, color = Color.Gray, letterSpacing = 1.sp)
                                    val locale = java.util.Locale.getDefault()
                                    Text(
                                        DateFormatter.formatDate(invoice.issueDate, stringResource(R.string.date_format), locale),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                    
                                    if (invoice.dueDate != null) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(stringResource(R.string.invoice_deadline).uppercase(), style = MaterialTheme.typography.labelSmall, color = Color(0xFFFB7185), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                        Text(
                                            DateFormatter.formatDate(invoice.dueDate, "dd MMM yyyy, HH:mm", locale),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(28.dp))
                }

                // ── Transaction Details ──────────────────────────────────────
                item {
                    Text(stringResource(R.string.transaction_details_caps), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = accentBlue, letterSpacing = 1.5.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                items(data.items) { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.description, fontWeight = FontWeight.Bold, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                            Text("${item.quantity} x ${CurrencyFormatter.formatWithRate(item.unitPrice, currency)}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        }
                        Text(CurrencyFormatter.formatWithRate(item.total, currency), fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleSmall, color = accentBlue)
                    }
                }

                // Discount / tax summary (only if applicable)
                item {
                    if (invoice.diskon > 0 || invoice.pajak > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    if (invoice.diskon > 0) {
                        val dVal = data.calculateDiscountAmount()
                        SummaryRow(stringResource(R.string.discount), "- ${CurrencyFormatter.formatWithRate(dVal, currency)}", Color(0xFFFB7185))
                    }
                    if (invoice.pajak > 0) {
                        val taxLabel = stringResource(R.string.tax_percentage).replace("(%)", "(${invoice.pajak.toInt()}%)")
                        SummaryRow(taxLabel, CurrencyFormatter.formatWithRate(data.calculateTaxAmount(), currency))
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }

                // ── Action Buttons ───────────────────────────────────────────
                item {
                    // Row 1: SALIN PESAN + SHARE PDF
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        // SALIN PESAN
                        Button(
                            onClick = {
                                val template = when (invoice.status.lowercase()) {
                                    "paid"    -> profile?.waTemplatePaid
                                    "overdue" -> profile?.waTemplateOverdue
                                    else      -> profile?.waTemplateUnpaid
                                }
                                val amountStr = CurrencyFormatter.formatWithRate(grand, currency)
                                
                                // Variables: {customer}, {invoice}, {amount}
                                val finalMessage = (template ?: context.getString(R.string.whatsapp_share_message))
                                    .replace("{customer}", invoice.customerName)
                                    .replace("{invoice}", invoice.invoiceNumber)
                                    .replace("{amount}", amountStr)
                                
                                val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Invoice Message", finalMessage))
                                Toast.makeText(context, strMessageCopied, Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0085FF))
                        ) {
                            Icon(Icons.Rounded.ContentPaste, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.copy_blast_btn), fontWeight = FontWeight.ExtraBold, fontSize = 10.sp)
                        }

                        // SHARE PDF
                        Button(
                            onClick = { scope.launch { PdfShareHelper.shareInvoicePdf(context, data, profile, currency) } },
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A2B45))
                        ) {
                            Icon(Icons.Rounded.Share, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.share_pdf_btn), fontWeight = FontWeight.ExtraBold, fontSize = 10.sp, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Row 2: MARK AS PAID (full width, only if not already paid)
                    if (invoice.status != "paid") {
                        Button(
                            onClick = { showMarkPaidDialog.value = true },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = accentGreen)
                        ) {
                            Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = Color(0xFF0B1222), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.mark_as_paid_btn), fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp, color = Color(0xFF0B1222))
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Row 3: DUPLICATE + WHATSAPP (outlined)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        // DUPLICATE
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    val now   = Calendar.getInstance()
                                    val start = now.apply {
                                        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0)
                                    }.timeInMillis
                                    val count     = editorViewModel.getInvoiceCountInPeriod(start, System.currentTimeMillis())
                                    val newNumber = com.fakturkuid.app.utils.InvoiceNumberGenerator.generate(count)
                                    val duplicated = invoice.copy(
                                        id = 0, invoiceNumber = newNumber,
                                        issueDate = System.currentTimeMillis(), dueDate = null, status = "unpaid"
                                    )
                                    editorViewModel.saveInvoice(duplicated, data.items.map { it.copy(id = 0, invoiceId = 0) })
                                    Toast.makeText(context, strDuplicated, Toast.LENGTH_SHORT).show()
                                    navController.popBackStack()
                                }
                            },
                            modifier = Modifier.weight(1f).height(52.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, accentBlue.copy(alpha = 0.4f))
                        ) {
                            Icon(Icons.Rounded.ContentCopy, contentDescription = null, tint = accentBlue, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.duplicate_btn), fontWeight = FontWeight.ExtraBold, fontSize = 10.sp, color = accentBlue)
                        }

                        // WHATSAPP
                        OutlinedButton(
                            onClick = {
                                val template  = when (invoice.status.lowercase()) {
                                    InvoiceStatus.PAID.value -> profile?.waTemplatePaid
                                    InvoiceStatus.OVERDUE.value -> profile?.waTemplateOverdue
                                    else      -> profile?.waTemplateUnpaid
                                }
                                val amountStr = CurrencyFormatter.formatWithRate(grand, currency)
                                val message   = formatWhatsAppMessage(template ?: "", invoice.customerName, invoice.invoiceNumber, amountStr)
                                val phone     = invoice.customerWhatsapp ?: invoice.customerPhone ?: ""
                                val target    = formatPhoneForWhatsApp(phone)
                                
                                scope.launch {
                                    PdfShareHelper.shareInvoicePdf(
                                        context = context,
                                        invoiceData = data,
                                        profile = profile,
                                        currency = currency,
                                        message = message,
                                        targetPhone = target
                                    ) { errorMsg ->
                                        Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f).height(52.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF25D366).copy(alpha = 0.4f))
                        ) {
                            Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = null, tint = Color(0xFF25D366), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.whatsapp_btn), fontWeight = FontWeight.ExtraBold, fontSize = 10.sp, color = Color(0xFF25D366))
                        }
                    }

                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        } ?: run {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF00D1FF))
            }
        }
    }
}

@Composable
fun SummaryRow(label: String, value: String, valueColor: Color = Color.White) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = valueColor)
    }
}
