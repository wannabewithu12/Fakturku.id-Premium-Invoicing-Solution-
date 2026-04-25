package com.fakturkuid.app.ui.customer

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.fakturkuid.app.R
import com.fakturkuid.app.data.entity.Customer
import com.fakturkuid.app.ui.NavRoutes
import com.fakturkuid.app.ui.components.StatusBadge
import com.fakturkuid.app.ui.viewmodel.CustomerViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerScreen(
    navController: NavController,
    viewModel: CustomerViewModel = koinViewModel()
) {
    val customers by viewModel.allCustomers.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showAddSheet by remember { mutableStateOf(false) }

    val filteredCustomers = remember(customers, searchQuery) {
        if (searchQuery.isBlank()) customers
        else customers.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    // Add Customer BottomSheet
    if (showAddSheet) {
        var newName by remember { mutableStateOf("") }
        var newMemberNumber by remember { mutableStateOf("...") }
        var newWhatsapp by remember { mutableStateOf("") }
        var newEmail by remember { mutableStateOf("") }
        var isMember by remember { mutableStateOf(false) }
        var status by remember { mutableStateOf("Active") }

        LaunchedEffect(Unit) {
            newMemberNumber = viewModel.getNextMemberNumber()
        }

        ModalBottomSheet(
            onDismissRequest = { showAddSheet = false },
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
                    stringResource(R.string.add_customer_btn),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                
                // Name
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text(stringResource(R.string.customer_name)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00D1FF),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color.White.copy(alpha = 0.05f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.03f)
                    ),
                    leadingIcon = { Icon(Icons.Rounded.Person, contentDescription = null, tint = Color(0xFF00D1FF).copy(alpha = 0.6f)) }
                )

                // WhatsApp
                OutlinedTextField(
                    value = newWhatsapp,
                    onValueChange = { newWhatsapp = it },
                    label = { Text(stringResource(R.string.whatsapp_number)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF25D366),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color.White.copy(alpha = 0.05f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.03f)
                    ),
                    leadingIcon = { Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = null, tint = Color(0xFF25D366).copy(alpha = 0.6f)) }
                )

                // Email
                OutlinedTextField(
                    value = newEmail,
                    onValueChange = { newEmail = it },
                    label = { Text(stringResource(R.string.email_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00D1FF),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color.White.copy(alpha = 0.05f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.03f)
                    ),
                    leadingIcon = { Icon(Icons.Rounded.Email, contentDescription = null, tint = Color(0xFF00D1FF).copy(alpha = 0.6f)) }
                )

                // Member Type Toggle
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
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF00D1FF))
                    )
                }

                if (isMember) {
                    Text(
                        stringResource(R.string.member_discount_msg),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF2DD4BF),
                        fontWeight = FontWeight.Bold
                    )
                }

                // Member Number (Read-only)
                OutlinedTextField(
                    value = newMemberNumber,
                    onValueChange = { newMemberNumber = it },
                    label = { Text(stringResource(R.string.member_number)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00D1FF),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color.White.copy(alpha = 0.05f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.03f)
                    ),
                    leadingIcon = { Icon(Icons.Rounded.Numbers, contentDescription = null, tint = Color(0xFF00D1FF).copy(alpha = 0.6f)) }
                )

                Button(
                    onClick = {
                        if (newName.isNotBlank()) {
                            viewModel.saveCustomer(
                                Customer(
                                    name = newName.trim(),
                                    whatsapp = newWhatsapp.trim().ifBlank { null },
                                    email = newEmail.trim().ifBlank { null },
                                    memberNumber = newMemberNumber,
                                    isMember = isMember,
                                    status = status
                                )
                            )
                            showAddSheet = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0085FF)),
                    enabled = newName.isNotBlank()
                ) {
                    Text(stringResource(R.string.save), fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                }
            }
        }
    }

    Scaffold(
        containerColor = Color(0xFF0F172A),
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Rounded.ArrowBackIos,
                        contentDescription = stringResource(R.string.back),
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    stringResource(R.string.customers_title).uppercase(),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 3.sp,
                    color = Color.White,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                IconButton(
                    onClick = { showAddSheet = true },
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Icon(
                        Icons.Rounded.PersonAdd,
                        contentDescription = stringResource(R.string.add_customer_btn),
                        tint = Color(0xFF00D1FF),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        },
        // Removed FAB as requested for a cleaner look. Add function moved to TopBar.
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                placeholder = { Text(stringResource(R.string.search_customer_hint), color = Color.Gray.copy(alpha = 0.5f)) },
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

            Spacer(modifier = Modifier.height(24.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (filteredCustomers.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(stringResource(R.string.no_customer_found), color = Color.Gray.copy(alpha = 0.5f))
                        }
                    }
                } else {
                    items(filteredCustomers) { customer ->
                        CustomerListItem(
                            customer = customer,
                            accentBlue = Color(0xFF00D1FF),
                            onClick = { navController.navigate(NavRoutes.customerDetail(customer.id)) },
                            onDelete = { viewModel.deleteCustomer(customer) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CustomerListItem(
    customer: Customer,
    accentBlue: Color,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White.copy(alpha = 0.03f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar circle with initial
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(accentBlue.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    customer.name.firstOrNull()?.uppercase() ?: "?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = accentBlue
                )
            }
            Spacer(modifier = Modifier.width(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    customer.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        customer.memberNumber ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        color = accentBlue.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Bold
                    )
                    if (customer.isMember) {
                        Surface(
                            color = accentBlue.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                stringResource(R.string.type_member).uppercase(),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = accentBlue,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 8.sp
                            )
                        }
                    }
                    if (!customer.whatsapp.isNullOrBlank()) {
                        Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = null, tint = Color(0xFF25D366), modifier = Modifier.size(10.dp))
                    }
                }
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Rounded.Delete, 
                    contentDescription = stringResource(R.string.delete), 
                    tint = Color(0xFFFB7185).copy(alpha = 0.8f), 
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
        }
    }
}

