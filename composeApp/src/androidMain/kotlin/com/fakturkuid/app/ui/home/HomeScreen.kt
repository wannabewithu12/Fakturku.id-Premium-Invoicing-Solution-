package com.fakturkuid.app.ui.home

import com.fakturkuid.app.R

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.basicMarquee
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.fakturkuid.app.data.entity.Invoice
import com.fakturkuid.app.data.entity.InvoiceWithItems
import com.fakturkuid.app.data.entity.InvoiceStatus
import com.fakturkuid.app.ui.NavRoutes
import com.fakturkuid.app.ui.components.AutoSizeText
import com.fakturkuid.app.ui.components.StatusBadge
import com.fakturkuid.app.ui.components.StatusColor
import com.fakturkuid.app.ui.viewmodel.BusinessProfileViewModel
import com.fakturkuid.app.ui.viewmodel.InvoiceDashboardViewModel
import com.fakturkuid.app.ui.viewmodel.InvoiceEditorViewModel
import com.fakturkuid.app.ui.viewmodel.SettingsViewModel
import com.fakturkuid.app.utils.CurrencyFormatter
import com.fakturkuid.app.utils.DateFormatter
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: InvoiceDashboardViewModel = koinViewModel(),
    editorViewModel: InvoiceEditorViewModel = koinViewModel(),
    profileViewModel: BusinessProfileViewModel = koinViewModel(),
    settingsViewModel: SettingsViewModel = koinViewModel()
) {
    val invoicesWithItems by viewModel.invoicesWithItems.collectAsState()
    val profile by profileViewModel.profile.collectAsState()
    val currency by settingsViewModel.currency.collectAsState()

    val isDark = androidx.compose.foundation.isSystemInDarkTheme()

    Scaffold(
        containerColor = Color.Transparent, // We will use a gradient Box
        floatingActionButton = {
            if (invoicesWithItems.isNotEmpty()) {
                FloatingActionButton(
                    onClick = { navController.navigate(NavRoutes.CREATE_INVOICE) },
                    containerColor = Color(0xFF0085FF),
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.padding(bottom = 16.dp, end = 8.dp)
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = stringResource(R.string.create_new_invoice_btn), modifier = Modifier.size(28.dp))
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(
                0.0f to Color(0xFF0A0F1D),
                0.32f to Color(0xFF0A0F1D), // Moved up to sit right between Paid pill and Action Cards
                1.0f to Color(0xFF02040A)
            )
        )) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
        ) {
            // Header Section
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            androidx.compose.foundation.Image(
                                painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_launcher_premium),
                                contentDescription = "App Icon",
                                modifier = Modifier.size(18.dp).clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.app_name).uppercase(), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = Color(0xFF00D1FF).copy(alpha = 0.8f), fontSize = 10.sp, letterSpacing = 1.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            profile?.businessName?.takeIf { it.isNotBlank() } ?: stringResource(R.string.default_company_name),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                    
                    Surface(
                        onClick = { navController.navigate(NavRoutes.SETTINGS) },
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.05f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
                    ) {
                        if (profile?.logoBlob?.isNotEmpty() == true) {
                            coil.compose.AsyncImage(
                                model = profile?.logoBlob,
                                contentDescription = null,
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                modifier = Modifier.size(48.dp).clip(CircleShape)
                            )
                        } else if (!profile?.logoUri.isNullOrEmpty()) {
                             coil.compose.AsyncImage(
                                model = java.io.File(profile?.logoUri ?: ""),
                                contentDescription = null,
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                modifier = Modifier.size(48.dp).clip(CircleShape)
                            )
                        } else {
                            Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                                Icon(Icons.Rounded.Business, contentDescription = null, tint = Color(0xFF00D1FF), modifier = Modifier.size(24.dp))
                            }
                        }
                    }
                }
            }

            // Stats Cards
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .height(IntrinsicSize.Min), // Forces both cards to match the taller one
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    DashboardStatCard(
                        icon = Icons.Rounded.AccountBalanceWallet,
                        label = stringResource(R.string.daily_income),
                        amount = invoicesWithItems.filter { DateFormatter.isToday(it.invoice.issueDate) && it.invoice.status == InvoiceStatus.PAID.value }.sumOf { data ->
                            data.calculateTotal()
                        },
                        currency = currency,
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        color = Color(0xFF2DD4BF)
                    )
                    DashboardStatCard(
                        icon = Icons.Rounded.AutoGraph,
                        label = stringResource(R.string.monthly_total),
                        amount = invoicesWithItems.filter { DateFormatter.isThisMonth(it.invoice.issueDate) && it.invoice.status == InvoiceStatus.PAID.value }.sumOf { data ->
                            data.calculateTotal()
                        },
                        currency = currency,
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        color = Color(0xFF00D1FF)
                    )
                }
            }

            // Status Summary Pill
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xFF161F33), // Richer dark navy
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).height(IntrinsicSize.Min),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatusMiniStat(label = stringResource(R.string.status_paid), count = invoicesWithItems.count { it.invoice.status == InvoiceStatus.PAID.value }, color = Color(0xFF2DD4BF), modifier = Modifier.weight(1f))
                        StatusMiniStat(label = stringResource(R.string.status_unpaid), count = invoicesWithItems.count { it.invoice.status == InvoiceStatus.UNPAID.value }, color = Color(0xFFFBBF24), modifier = Modifier.weight(1f))
                        StatusMiniStat(label = stringResource(R.string.status_overdue), count = invoicesWithItems.count { it.invoice.status == InvoiceStatus.OVERDUE.value }, color = Color(0xFFFB7185), modifier = Modifier.weight(1f))
                    }
                }
            }

            // Main Actions (2 Columns)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .height(IntrinsicSize.Min), // Align heights
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    MainActionCard(
                        icon = Icons.Rounded.InsertChartOutlined,
                        title = stringResource(R.string.analytics),
                        subTitle = stringResource(R.string.analytics_desc),
                        onClick = { navController.navigate(NavRoutes.ANALYTICS) },
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        color = Color(0xFF0085FF) // Blue Accent
                    )
                    MainActionCard(
                        icon = Icons.Rounded.PeopleOutline,
                        title = stringResource(R.string.customers),
                        subTitle = stringResource(R.string.customers_desc),
                        onClick = { navController.navigate(NavRoutes.CUSTOMERS) },
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        color = Color(0xFF2DD4BF) // Green Accent
                    )
                }
            }

            // Recent Activity Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.recent_activity), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.Gray, letterSpacing = 2.sp)
                    Text(
                        stringResource(R.string.see_all), 
                        modifier = Modifier.clickable { navController.navigate(NavRoutes.SEE_ALL) },
                        style = MaterialTheme.typography.labelSmall, 
                        fontWeight = FontWeight.Bold, 
                        color = Color(0xFF00D1FF)
                    )
                }
            }

            if (invoicesWithItems.isEmpty()) {
                item {
                    EmptyStatePremium(onCreateClick = { navController.navigate(NavRoutes.CREATE_INVOICE) })
                }
            } else {
                itemsIndexed(invoicesWithItems.take(5)) { index, data ->
                    InvoiceListItem(
                        invoiceData = data,
                        currency = currency,
                        onClick = { navController.navigate(NavRoutes.invoiceDetail(data.invoice.id)) },
                        onDelete = { editorViewModel.deleteInvoice(data.invoice) },
                        isDark = true,
                        index = index
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(120.dp)) }
        }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun DashboardStatCard(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, amount: Double, currency: String, modifier: Modifier, color: Color) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF161F33), // Richer dark navy
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(20.dp).fillMaxHeight()) {
            Box(
                modifier = Modifier.size(48.dp).background(color.copy(alpha = 0.15f), RoundedCornerShape(14.dp)), // Proportional scaling
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(28.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color(0xFF8E98A8), fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.weight(1f)) // Push amount to bottom for alignment
            AutoSizeText(
                text = CurrencyFormatter.formatWithRate(amount, currency),
                style = MaterialTheme.typography.headlineMedium, // Changed back to headlineMedium since it will auto-size
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                maxLines = 1
            )
        }
    }
}

@Composable
fun StatusMiniStat(label: String, count: Int, color: Color, modifier: Modifier = Modifier) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Text(count.toString(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = color) // Number takes the color
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) // Label is gray
    }
}

@Composable
fun MainActionCard(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subTitle: String, onClick: () -> Unit, modifier: Modifier, color: Color) {
    // Tentukan background yang 'selaras' (matching tint) dengan warna accent
    val cardBgColor = when (color) {
        Color(0xFF0085FF) -> Color(0xFF101B30) // Deep Blue Navy untuk Analytics
        Color(0xFF2DD4BF) -> Color(0xFF0B2220) // Deep Green Navy untuk Customers
        else -> Color(0xFF161F33)
    }
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = cardBgColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(40.dp).background(color.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold, color = color, fontSize = 13.sp)
                Text(subTitle, style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 9.sp, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
fun EmptyStatePremium(onCreateClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp, bottom = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(Color(0xFF0F172A), CircleShape), // Very dark circle
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.ReceiptLong, // Highly professional B2B icon
                contentDescription = null,
                tint = Color(0xFF253B55), // Very muted professional slate
                modifier = Modifier.size(60.dp) 
            )
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            stringResource(R.string.no_activity_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            stringResource(R.string.no_activity_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onCreateClick,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(56.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0085FF))
        ) {
            Text(stringResource(R.string.create_new_invoice_btn), fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp, color = Color(0xFF0B1222))
        }
    }
}

@Composable
fun InvoiceListItem(invoiceData: InvoiceWithItems, currency: String, onClick: () -> Unit, onDelete: () -> Unit = {}, isDark: Boolean, index: Int) {
    val invoice = invoiceData.invoice
    val grandTotal = invoiceData.calculateTotal()

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = androidx.compose.animation.core.tween(300, delayMillis = index * 50)) +
                expandVertically(animationSpec = androidx.compose.animation.core.tween(300, delayMillis = index * 50))
    ) {
        Surface(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 6.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color.White.copy(alpha = 0.03f),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(StatusColor.get(invoice.status).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.Bolt,
                        contentDescription = null,
                        tint = StatusColor.get(invoice.status),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                // Invoice info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        invoice.invoiceNumber,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF00D1FF),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        invoice.customerName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    val locale = java.util.Locale.getDefault()
                    val pattern = stringResource(R.string.date_format)
                    Text(
                        DateFormatter.formatDate(invoice.issueDate, pattern, locale),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
                // Actions
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.Rounded.Delete,
                        contentDescription = "Delete",
                        tint = Color.White.copy(alpha = 0.3f),
                        modifier = Modifier.size(18.dp)
                    )
                }
                Icon(
                    Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.2f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
