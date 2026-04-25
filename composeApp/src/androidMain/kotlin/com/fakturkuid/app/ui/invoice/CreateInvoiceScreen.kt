package com.fakturkuid.app.ui.invoice

import com.fakturkuid.app.R

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.fakturkuid.app.data.entity.Invoice
import com.fakturkuid.app.data.entity.InvoiceItem
import com.fakturkuid.app.data.entity.Customer
import com.fakturkuid.app.ui.viewmodel.InvoiceEditorViewModel
import com.fakturkuid.app.ui.viewmodel.CustomerViewModel
import com.fakturkuid.app.utils.CurrencyFormatter
import androidx.compose.ui.window.PopupProperties
import org.koin.androidx.compose.koinViewModel
import java.util.*
import java.text.SimpleDateFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateInvoiceScreen(
    navController: NavController,
    invoiceId: Long? = null,
    viewModel: InvoiceEditorViewModel = koinViewModel(),
    customerViewModel: CustomerViewModel = koinViewModel(),
    settingsViewModel: com.fakturkuid.app.ui.viewmodel.SettingsViewModel = koinViewModel()
) {
    val isEditMode = invoiceId != null && invoiceId != 0L
    val currency by settingsViewModel.currency.collectAsState()
    val generatingText = stringResource(R.string.generating_invoice_number)
    var invoiceNumber by remember { mutableStateOf(generatingText) }
    var customerName by remember { mutableStateOf("") }
    var customerId by remember { mutableStateOf<Long?>(null) }
    var customerAddress by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var customerWhatsapp by remember { mutableStateOf("") }
    var customerEmail by remember { mutableStateOf("") }
    var customerMemberNumber by remember { mutableStateOf("") }
    
    var diskonValue by remember { mutableStateOf("0") }
    var diskonType by remember { mutableStateOf("nominal") } // "nominal" or "percentage"
    var pajakValue by remember { mutableStateOf("0") }
    var dueDate by remember { mutableStateOf<Long?>(null) }
    
    var notes by remember { mutableStateOf("") }
    var items by remember { mutableStateOf(listOf<InvoiceItem>()) }

    val customerSuggestions by customerViewModel.searchResults.collectAsState()
    var showSuggestions by remember { mutableStateOf(false) }

    LaunchedEffect(invoiceId) {
        if (isEditMode) {
            val existingData = viewModel.getInvoiceWithItemsSync(invoiceId!!)
            existingData?.let { data ->
                invoiceNumber = data.invoice.invoiceNumber
                customerName = data.invoice.customerName
                customerId = data.invoice.customerId
                customerAddress = data.invoice.customerAddress ?: ""
                customerPhone = data.invoice.customerPhone ?: ""
                customerWhatsapp = data.invoice.customerWhatsapp ?: ""
                customerEmail = data.invoice.customerEmail ?: ""
                customerMemberNumber = data.invoice.customerMemberNumber ?: ""
                diskonValue = data.invoice.diskon.toString()
                diskonType = data.invoice.diskonType
                pajakValue = data.invoice.pajak.toString()
                dueDate = data.invoice.dueDate
                notes = data.invoice.notes ?: ""
                items = data.items
            }
        } else {
            val now = Calendar.getInstance()
            // Reset to start of today for daily numbering
            val start = now.apply { 
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }.timeInMillis
            val count = viewModel.getInvoiceCountInPeriod(start, System.currentTimeMillis())
            invoiceNumber = com.fakturkuid.app.utils.InvoiceNumberGenerator.generate(count)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (isEditMode) "EDIT INVOICE" else stringResource(R.string.new_invoice_title), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, letterSpacing = 2.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBackIos, contentDescription = stringResource(R.string.back), modifier = Modifier.size(20.dp))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                item {
                    Column {
                        Text(
                            stringResource(R.string.customer_identity), 
                            style = MaterialTheme.typography.labelSmall, 
                            fontWeight = FontWeight.Bold, 
                            color = if (androidx.compose.foundation.isSystemInDarkTheme()) Color(0xFF00D1FF) else Color(0xFF0EA5E9)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Box {
                            OutlinedTextField(
                                value = customerName,
                                onValueChange = { 
                                    customerName = it
                                    customerViewModel.onSearchQueryChange(it)
                                    showSuggestions = it.isNotBlank()
                                },
                                label = { Text(stringResource(R.string.customer_name)) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                    focusedBorderColor = Color(0xFF00D1FF).copy(alpha = 0.5f)
                                )
                            )
                            
                            if (showSuggestions && customerSuggestions.isNotEmpty()) {
                                DropdownMenu(
                                    expanded = showSuggestions,
                                    onDismissRequest = { showSuggestions = false },
                                    properties = PopupProperties(focusable = false),
                                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                                ) {
                                    customerSuggestions.forEach { customer ->
                                        DropdownMenuItem(
                                            text = { 
                                                Column {
                                                    Text(customer.name, fontWeight = FontWeight.Bold)
                                                    if (!customer.phone.isNullOrBlank()) Text(customer.phone, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                                }
                                            },
                                            onClick = {
                                                customerName = customer.name
                                                customerId = customer.id
                                                customerAddress = customer.address ?: ""
                                                customerPhone = customer.phone ?: ""
                                                customerWhatsapp = customer.whatsapp ?: ""
                                                customerEmail = customer.email ?: ""
                                                customerMemberNumber = customer.memberNumber ?: ""
                                                
                                                // --- Membership Discount Logic ---
                                                if (customer.isMember) {
                                                    diskonValue = "5"
                                                    diskonType = "percentage"
                                                }
                                                
                                                showSuggestions = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = customerWhatsapp,
                            onValueChange = { customerWhatsapp = it },
                            label = { Text(stringResource(R.string.whatsapp_number)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                focusedBorderColor = Color(0xFF00D1FF).copy(alpha = 0.5f)
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = invoiceNumber,
                            onValueChange = { invoiceNumber = it },
                            label = { Text(stringResource(R.string.invoice_number)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            readOnly = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                focusedBorderColor = Color(0xFF00D1FF).copy(alpha = 0.5f)
                            )
                        )
                        
                    }
                }

                item {
                    Column {
                        Text(
                            stringResource(R.string.invoice_deadline), 
                            style = MaterialTheme.typography.labelSmall, 
                            fontWeight = FontWeight.Bold, 
                            color = if (androidx.compose.foundation.isSystemInDarkTheme()) Color(0xFF00D1FF) else Color(0xFF0EA5E9)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (androidx.compose.foundation.isSystemInDarkTheme()) 0.03f else 0.02f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                // --- Deadline Picker ---
                                val context = androidx.compose.ui.platform.LocalContext.current
                                val calendar = Calendar.getInstance()
                                if (dueDate != null) calendar.timeInMillis = dueDate!!
                                
                                val timePickerDialog = android.app.TimePickerDialog(
                                    context,
                                    { _, hourOfDay, minute ->
                                        val selected = Calendar.getInstance()
                                        if (dueDate != null) selected.timeInMillis = dueDate!!
                                        selected.set(Calendar.HOUR_OF_DAY, hourOfDay)
                                        selected.set(Calendar.MINUTE, minute)
                                        selected.set(Calendar.SECOND, 0)
                                        dueDate = selected.timeInMillis
                                    },
                                    calendar.get(Calendar.HOUR_OF_DAY),
                                    calendar.get(Calendar.MINUTE),
                                    true
                                )

                                val datePickerDialog = android.app.DatePickerDialog(
                                    context,
                                    { _, year, month, dayOfMonth ->
                                        val selected = Calendar.getInstance()
                                        selected.set(year, month, dayOfMonth)
                                        dueDate = selected.timeInMillis
                                        timePickerDialog.show()
                                    },
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH),
                                    calendar.get(Calendar.DAY_OF_MONTH)
                                )

                                OutlinedTextField(
                                    value = if (dueDate == null) "" else SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(dueDate!!)),
                                    onValueChange = { },
                                    label = { Text(stringResource(R.string.select_deadline)) },
                                    shape = RoundedCornerShape(16.dp),
                                    readOnly = true,
                                    trailingIcon = {
                                        if (dueDate != null) {
                                            IconButton(onClick = { dueDate = null }) {
                                                Icon(Icons.Rounded.Close, contentDescription = "Clear")
                                            }
                                        } else {
                                            IconButton(onClick = { datePickerDialog.show() }) {
                                                Icon(Icons.Rounded.CalendarToday, contentDescription = "Select Date")
                                            }
                                        }
                                    },
                                    placeholder = { Text(stringResource(R.string.select_deadline), color = Color.Gray.copy(alpha = 0.5f)) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                        focusedBorderColor = Color(0xFF00D1FF).copy(alpha = 0.5f)
                                    ),
                                    modifier = Modifier.fillMaxWidth().pointerInput(Unit) {
                                        detectTapGestures { datePickerDialog.show() }
                                    }
                                )
                            }
                        }
                    }
                }

                item {
                    Text(
                        stringResource(R.string.transaction_items), 
                        style = MaterialTheme.typography.labelSmall, 
                        fontWeight = FontWeight.Bold, 
                        color = if (androidx.compose.foundation.isSystemInDarkTheme()) Color(0xFF00D1FF) else Color(0xFF0EA5E9)
                    )
                }

                itemsIndexed(items) { index, item ->
                    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (isDark) 0.03f else 0.02f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.description, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                Text("${item.quantity} x ${CurrencyFormatter.formatWithRate(item.unitPrice, currency)}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                            IconButton(onClick = {
                                val updated = items.toMutableList()
                                updated.removeAt(index)
                                items = updated
                            }) {
                                Icon(Icons.Rounded.RemoveCircleOutline, tint = Color(0xFFFB7185).copy(alpha = 0.6f), contentDescription = null)
                            }
                        }
                    }
                }

                item {
                    Column {
                        Text(
                            stringResource(R.string.add_item), 
                            style = MaterialTheme.typography.labelSmall, 
                            fontWeight = FontWeight.Bold, 
                            color = if (androidx.compose.foundation.isSystemInDarkTheme()) Color(0xFF00D1FF) else Color(0xFF0EA5E9)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (androidx.compose.foundation.isSystemInDarkTheme()) 0.02f else 0.01f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                        ) {
                            var draftDesc by remember { mutableStateOf("") }
                            var draftQty by remember { mutableStateOf("1") }
                            var draftPrice by remember { mutableStateOf("") }

                            Column(modifier = Modifier.padding(20.dp)) {
                                OutlinedTextField(
                                    value = draftDesc,
                                    onValueChange = { draftDesc = it },
                                    placeholder = { Text(stringResource(R.string.item_description_hint), color = Color.Gray.copy(alpha = 0.5f)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    OutlinedTextField(
                                        value = draftQty,
                                        onValueChange = { draftQty = it },
                                        label = { Text(stringResource(R.string.quantity_short)) },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    OutlinedTextField(
                                        value = draftPrice,
                                        onValueChange = { draftPrice = it },
                                        label = { Text(stringResource(R.string.unit_price)) },
                                        modifier = Modifier.weight(2f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                }
                                Button(
                                    onClick = {
                                        val q = draftQty.toIntOrNull() ?: 1
                                        val p = draftPrice.toDoubleOrNull() ?: 0.0
                                        if (draftDesc.isNotBlank() && q > 0 && p >= 0.0) {
                                            items = items + InvoiceItem(invoiceId = invoiceId ?: 0L, description = draftDesc, quantity = q, unitPrice = p)
                                            draftDesc = ""; draftQty = "1"; draftPrice = ""
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().padding(top = 20.dp).height(50.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00D1FF).copy(alpha = 0.1f), contentColor = Color(0xFF00D1FF))
                                ) {
                                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(stringResource(R.string.save_to_list), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                        Text(
                            stringResource(R.string.summary_and_additional), 
                            style = MaterialTheme.typography.labelSmall, 
                            fontWeight = FontWeight.Bold, 
                            color = if (androidx.compose.foundation.isSystemInDarkTheme()) Color(0xFF00D1FF) else Color(0xFF0EA5E9)
                        )
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = diskonValue,
                                onValueChange = { diskonValue = it },
                                label = { Text(stringResource(R.string.discount)) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Surface(
                                onClick = { diskonType = if (diskonType == "nominal") "percentage" else "nominal" },
                                modifier = Modifier.height(56.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (androidx.compose.foundation.isSystemInDarkTheme()) 0.05f else 0.02f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                            ) {
                                Box(Modifier.fillMaxHeight().padding(horizontal = 20.dp), contentAlignment = Alignment.Center) {
                                    val symbol = when(currency) {
                                        "en" -> "$"
                                        "ja" -> "¥"
                                        "zh" -> "¥"
                                        else -> "Rp"
                                    }
                                    Text(if (diskonType == "nominal") symbol else "%", fontWeight = FontWeight.ExtraBold, color = if (androidx.compose.foundation.isSystemInDarkTheme()) Color(0xFF00D1FF) else Color(0xFF0085FF))
                                }
                            }
                        }

                        OutlinedTextField(
                            value = pajakValue,
                            onValueChange = { pajakValue = it },
                            label = { Text(stringResource(R.string.tax_percentage)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text(stringResource(R.string.additional_notes_optional)) },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                item {
                    val subtotal = items.sumOf { it.total }
                    val diskon = if (diskonType == "percentage") {
                        subtotal * (diskonValue.toDoubleOrNull() ?: 0.0) / 100
                    } else {
                        diskonValue.toDoubleOrNull() ?: 0.0
                    }
                    val pajak = (subtotal - diskon) * (pajakValue.toDoubleOrNull() ?: 0.0) / 100
                    val grandTotal = subtotal - diskon + pajak

                    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = if (isDark) Color(0xFF0085FF).copy(alpha = 0.05f) else Color(0xFF0085FF).copy(alpha = 0.02f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0085FF).copy(alpha = if (isDark) 0.1f else 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(stringResource(R.string.subtotal), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                Text(CurrencyFormatter.formatWithRate(subtotal, currency), color = MaterialTheme.colorScheme.onSurface)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(stringResource(R.string.discount_amount), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                Text("- ${CurrencyFormatter.formatWithRate(diskon, currency)}", color = Color(0xFFFB7185))
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(stringResource(R.string.tax_amount), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                Text(CurrencyFormatter.formatWithRate(pajak, currency), color = MaterialTheme.colorScheme.onSurface)
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            // Whitespace separation instead of divider
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(stringResource(R.string.grand_total), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, color = if (isDark) Color(0xFF00D1FF) else Color(0xFF0085FF))
                                Text(CurrencyFormatter.formatWithRate(grandTotal, currency), fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(120.dp))
                }
            }

            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            val scale by animateFloatAsState(if (isPressed) 0.95f else 1f, label = "ButtonScale")

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 24.dp,
                color = MaterialTheme.colorScheme.background
            ) {
                Button(
                    onClick = {
                        if (customerId == null && customerName.isNotBlank()) {
                            customerViewModel.saveCustomer(Customer(name = customerName, address = customerAddress, phone = customerPhone))
                        }

                        val newInvoice = Invoice(
                            id = invoiceId ?: 0L,
                            invoiceNumber = invoiceNumber,
                            customerName = customerName,
                            customerAddress = customerAddress,
                            customerPhone = customerPhone,
                            customerWhatsapp = customerWhatsapp,
                            customerEmail = customerEmail,
                            customerMemberNumber = customerMemberNumber,
                            issueDate = System.currentTimeMillis(),
                            dueDate = dueDate,
                            notes = notes,
                            footer = null,
                            diskon = diskonValue.toDoubleOrNull() ?: 0.0,
                            diskonType = diskonType,
                            pajak = pajakValue.toDoubleOrNull() ?: 0.0,
                            status = "unpaid",
                            customerId = customerId
                        )
                        viewModel.saveInvoice(newInvoice, items)
                        navController.popBackStack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .height(64.dp)
                        .scale(scale),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0085FF)),
                    interactionSource = interactionSource,
                    enabled = customerName.isNotBlank() && items.isNotEmpty() && (diskonValue.toDoubleOrNull() ?: -1.0) >= 0.0 && (pajakValue.toDoubleOrNull() ?: -1.0) >= 0.0
                ) {
                    Text(if (isEditMode) "UPDATE INVOICE" else stringResource(R.string.publish_invoice_btn), fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                }
            }
        }
    }
}
