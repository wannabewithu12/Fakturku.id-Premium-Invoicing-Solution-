package com.fakturkuid.app.ui.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBackIos
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.navigation.NavController
import com.fakturkuid.app.R
import com.fakturkuid.app.ui.NavRoutes
import com.fakturkuid.app.ui.components.StatusBadge
import com.fakturkuid.app.ui.viewmodel.CustomerViewModel
import com.fakturkuid.app.utils.CurrencyFormatter
import com.fakturkuid.app.utils.DateFormatter
import com.fakturkuid.app.utils.formatPhoneForWhatsApp
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDetailScreen(
    navController: NavController,
    customerId: Long,
    currency: String,
    viewModel: CustomerViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()
    val accentBlue  = if (isDark) Color(0xFF00D1FF) else Color(0xFF0085FF)
    val accentGreen = Color(0xFF2DD4BF)

    val allCustomers by viewModel.allCustomers.collectAsState()
    val customer = remember(allCustomers, customerId) { allCustomers.find { it.id == customerId } }

    val invoices      by viewModel.getInvoicesByCustomer(customerId).collectAsState(initial = emptyList())
    val totalPaid     by viewModel.getTotalPaidByCustomer(customerId).collectAsState(initial = 0.0)
    val invoiceCount  by viewModel.getInvoiceCountByCustomer(customerId).collectAsState(initial = 0)

    val hasUnpaid = invoices.any { it.invoice.status != "paid" }
    
    var showEditSheet by remember { mutableStateOf(false) }

    if (showEditSheet && customer != null) {
        var editName by remember { mutableStateOf(customer.name) }
        var editMemberNumber by remember { mutableStateOf(customer.memberNumber ?: "") }
        var editWhatsapp by remember { mutableStateOf(customer.whatsapp ?: "") }
        var editEmail by remember { mutableStateOf(customer.email ?: "") }
        var isMember by remember { mutableStateOf(customer.isMember) }
        var status by remember { mutableStateOf(customer.status) }

        ModalBottomSheet(
            onDismissRequest = { showEditSheet = false },
            containerColor = Color(0xFF1E293B),
            tonalElevation = 0.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 48.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    stringResource(R.string.edit_customer),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )

                OutlinedTextField(
                    value = editName,
                    onValueChange = { editName = it },
                    label = { Text(stringResource(R.string.customer_name)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentBlue,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                OutlinedTextField(
                    value = editMemberNumber,
                    onValueChange = { editMemberNumber = it },
                    label = { Text(stringResource(R.string.member_number)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentBlue,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                OutlinedTextField(
                    value = editWhatsapp,
                    onValueChange = { editWhatsapp = it },
                    label = { Text(stringResource(R.string.whatsapp_number)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF25D366),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                OutlinedTextField(
                    value = editEmail,
                    onValueChange = { editEmail = it },
                    label = { Text(stringResource(R.string.email_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentBlue,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(stringResource(R.string.customer_type), color = Color.White, fontWeight = FontWeight.Bold)
                        Text(
                            if (isMember) stringResource(R.string.type_member) else stringResource(R.string.type_non_member),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isMember) Color(0xFF00D1FF) else Color.Gray
                        )
                    }
                    Switch(
                        checked = isMember,
                        onCheckedChange = { isMember = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = accentBlue)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(stringResource(R.string.customer_status), color = Color.White, fontWeight = FontWeight.Bold)
                        Text(
                            if (status == "Active") stringResource(R.string.status_active) else stringResource(R.string.status_inactive),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (status == "Active") Color(0xFF2DD4BF) else Color.Gray
                        )
                    }
                    Switch(
                        checked = status == "Active",
                        onCheckedChange = { status = if (it) "Active" else "Inactive" },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF2DD4BF))
                    )
                }

                Button(
                    onClick = {
                        if (editName.isNotBlank()) {
                            viewModel.saveCustomer(
                                customer.copy(
                                    name = editName.trim(),
                                    memberNumber = editMemberNumber.trim().ifBlank { null },
                                    whatsapp = editWhatsapp.trim().ifBlank { null },
                                    email = editEmail.trim().ifBlank { null },
                                    isMember = isMember,
                                    status = status
                                )
                            )
                            showEditSheet = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0085FF)),
                    enabled = editName.isNotBlank()
                ) {
                    Text(stringResource(R.string.save), fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }

    val gradientHeader = if (isDark)
        Brush.verticalGradient(listOf(Color(0xFF0F172A), Color(0xFF1E293B)))
    else
        Brush.verticalGradient(listOf(Color(0xFF0085FF), Color(0xFF00D1FF)))

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(gradientHeader)
                    .padding(top = 16.dp, bottom = 28.dp)
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.align(Alignment.CenterStart).padding(start = 8.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Rounded.ArrowBackIos,
                        contentDescription = stringResource(R.string.back),
                        tint = if (isDark) accentBlue else Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    stringResource(R.string.customer_detail_title).uppercase(),
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 3.sp,
                    color = if (isDark) accentBlue else Color.White
                )
                IconButton(
                    onClick = { showEditSheet = true },
                    modifier = Modifier.align(Alignment.CenterEnd).padding(end = 8.dp)
                ) {
                    Icon(
                        Icons.Rounded.Edit,
                        contentDescription = stringResource(R.string.edit_customer),
                        tint = if (isDark) accentBlue else Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    ) { padding ->
        val c = customer ?: return@Scaffold
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile card
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (isDark) 0.04f else 0.02f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
                ) {
                    Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(56.dp).clip(CircleShape)
                                .background(accentBlue.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                c.name.firstOrNull()?.uppercase() ?: "?",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = accentBlue
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                c.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.Numbers, contentDescription = null, modifier = Modifier.size(12.dp), tint = accentBlue.copy(alpha = 0.5f))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(c.memberNumber ?: "", style = MaterialTheme.typography.bodySmall, color = accentBlue.copy(alpha = 0.8f), fontWeight = FontWeight.Bold)
                                if (c.isMember) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        color = Color(0xFF2DD4BF).copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            stringResource(R.string.type_member).uppercase(),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF2DD4BF),
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 9.sp
                                        )
                                    }
                                }
                            }
                            if (!c.whatsapp.isNullOrBlank()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color(0xFF25D366).copy(alpha = 0.5f))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(c.whatsapp, style = MaterialTheme.typography.bodySmall, color = Color(0xFF25D366).copy(alpha = 0.7f))
                                }
                            }
                        }
                    }
                }
            }

            // Stats row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Total invoices
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        color = accentBlue.copy(alpha = 0.08f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, accentBlue.copy(alpha = 0.15f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(stringResource(R.string.total_invoice_caps), style = MaterialTheme.typography.labelSmall, color = accentBlue, letterSpacing = 1.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("$invoiceCount", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                    // Total paid
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        color = accentGreen.copy(alpha = 0.08f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, accentGreen.copy(alpha = 0.15f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(stringResource(R.string.total_paid_caps), style = MaterialTheme.typography.labelSmall, color = accentGreen, letterSpacing = 1.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                CurrencyFormatter.formatWithRate(totalPaid, currency),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // Action buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { navController.navigate(NavRoutes.CREATE_INVOICE) },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = accentBlue)
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.new_invoice_desc), fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                    }
                    if (hasUnpaid && !c.whatsapp.isNullOrBlank()) {
                        OutlinedButton(
                            onClick = {
                                val phone = formatPhoneForWhatsApp(c.whatsapp)
                                val uri = "https://wa.me/$phone".toUri()
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, uri).apply {
                                    setPackage("com.whatsapp")
                                }
                                try {
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                            },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF25D366).copy(alpha = 0.4f))
                        ) {
                            Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = null, tint = Color(0xFF25D366), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.whatsapp_btn), fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = Color(0xFF25D366))
                        }
                    }
                }
            }

            // Invoice history header
            item {
                Text(
                    stringResource(R.string.invoice_history),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp,
                    color = accentBlue
                )
            }

            // Invoice list
            if (invoices.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(stringResource(R.string.no_invoice_customer), color = Color.Gray.copy(alpha = 0.5f), style = MaterialTheme.typography.bodySmall)
                    }
                }
            } else {
                items(invoices) { data ->
                    val invoice = data.invoice
                    val total = data.calculateTotal()
                    Surface(
                        modifier = Modifier.fillMaxWidth().clickable {
                            navController.navigate(NavRoutes.invoiceDetail(invoice.id))
                        },
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (isDark) 0.03f else 0.02f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(invoice.invoiceNumber, style = MaterialTheme.typography.labelSmall, color = accentBlue, fontWeight = FontWeight.Bold)
                                val locale = androidx.compose.ui.platform.LocalConfiguration.current.locales[0]
                                Text(DateFormatter.formatDate(invoice.issueDate, stringResource(R.string.date_format), locale), style = MaterialTheme.typography.bodySmall, color = Color.Gray.copy(alpha = 0.5f))
                                Text(CurrencyFormatter.formatWithRate(total, currency), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                            }
                            @Suppress("DEPRECATION")
                            StatusBadge(status = invoice.status)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                contentDescription = null,
                                tint = Color.Gray.copy(alpha = 0.3f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}
