package com.fakturkuid.app.ui.home

import com.fakturkuid.app.R

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBackIos
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.fakturkuid.app.data.entity.Invoice
import com.fakturkuid.app.ui.components.StatusBadge
import com.fakturkuid.app.ui.viewmodel.InvoiceDashboardViewModel
import com.fakturkuid.app.utils.DateFormatter
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityListScreen(
    navController: NavController,
    viewModel: InvoiceDashboardViewModel = koinViewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val allInvoices by viewModel.filteredInvoices.collectAsState()

    var activeFilter by remember { mutableStateOf("all") }
    val invoices = remember(allInvoices, activeFilter) {
        when (activeFilter) {
            "paid"    -> allInvoices.filter { it.status == "paid" }
            "unpaid"  -> allInvoices.filter { it.status == "unpaid" }
            "overdue" -> allInvoices.filter { it.status == "overdue" }
            else      -> allInvoices
        }
    }

    Scaffold(
        containerColor = Color(0xFF0F172A),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBackIos, contentDescription = stringResource(R.string.back), tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    Text(
                        stringResource(R.string.recent_activity_title).uppercase(),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp,
                        color = Color.White,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.width(48.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    placeholder = { Text(stringResource(R.string.search_hint), color = Color.Gray.copy(alpha = 0.5f)) },
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = Color(0xFF00D1FF)) },
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00D1FF),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                        focusedContainerColor = Color.White.copy(alpha = 0.05f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )

                // Filter Chips
                val filters = listOf(
                    "all"     to stringResource(R.string.filter_all),
                    "paid"    to stringResource(R.string.status_paid),
                    "unpaid"  to stringResource(R.string.status_unpaid),
                    "overdue" to stringResource(R.string.status_overdue)
                )
                androidx.compose.foundation.lazy.LazyRow(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filters) { (key, label) ->
                        val isActive = activeFilter == key
                        val chipColor = when (key) {
                            "paid"    -> Color(0xFF2DD4BF)
                            "unpaid"  -> Color(0xFFFBBF24)
                            "overdue" -> Color(0xFFFB7185)
                            else      -> Color(0xFF00D1FF)
                        }
                        Surface(
                            onClick = { activeFilter = key },
                            shape = androidx.compose.foundation.shape.CircleShape,
                            color = if (isActive) chipColor else Color.White.copy(alpha = 0.05f),
                            border = if (!isActive) androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)) else null
                        ) {
                            Text(
                                label,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Medium,
                                color = if (isActive) Color(0xFF0F172A) else Color.White.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (invoices.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.search_not_found), color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(invoices) { invoice ->
                        ActivityListItem(invoice = invoice) {
                            navController.navigate("invoice_detail/${invoice.id}")
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun ActivityListItem(invoice: Invoice, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 4.dp),
        shape = RoundedCornerShape(20.dp),
        color = Color.White.copy(alpha = 0.03f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    invoice.invoiceNumber,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF00D1FF),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    invoice.customerName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                val locale = androidx.compose.ui.platform.LocalConfiguration.current.locales[0]
                Text(
                    DateFormatter.formatDate(invoice.issueDate, stringResource(R.string.date_format), locale),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                StatusBadge(status = invoice.status)
                Spacer(modifier = Modifier.height(8.dp))
                Icon(
                    Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.1f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
