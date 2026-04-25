package com.fakturkuid.app.ui.analytics

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.fakturkuid.app.R
import com.fakturkuid.app.ui.viewmodel.AnalyticsViewModel
import com.fakturkuid.app.utils.CurrencyFormatter
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    navController: NavController,
    currency: String,
    viewModel: AnalyticsViewModel = koinViewModel()
) {
    val accentBlue  = Color(0xFF00D1FF)
    val accentGreen = Color(0xFF2DD4BF)
    val accentAmber = Color(0xFFFBBF24)
    val accentRed   = Color(0xFFFB7185)

    val last7Days      by viewModel.last7DaysRevenue.collectAsState()
    val paidCount      by viewModel.paidCount.collectAsState()
    val unpaidCount    by viewModel.unpaidCount.collectAsState()
    val overdueCount   by viewModel.overdueCount.collectAsState()
    val monthRevenue   by viewModel.totalRevenueMonth.collectAsState()
    val avgValue       by viewModel.avgInvoiceValue.collectAsState()
    val bestDay        by viewModel.bestDay.collectAsState()

    Scaffold(
        containerColor = Color(0xFF0F172A),
        topBar = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBackIos, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Text(
                    stringResource(R.string.analytics).uppercase(),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.width(48.dp))
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Summary Cards
            item {
                Text(
                    stringResource(R.string.monthly_summary),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    AnalyticsSummaryCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.AutoMirrored.Rounded.TrendingUp,
                        label = stringResource(R.string.monthly_total),
                        value = CurrencyFormatter.formatWithRate(monthRevenue, currency),
                        iconColor = accentGreen
                    )
                    AnalyticsSummaryCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Rounded.Receipt,
                        label = stringResource(R.string.average_transaction),
                        value = CurrencyFormatter.formatWithRate(avgValue, currency),
                        iconColor = accentBlue
                    )
                }
                bestDay?.let { day ->
                    if (day.second > 0) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            color = accentGreen.copy(alpha = 0.08f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, accentGreen.copy(alpha = 0.15f))
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier.size(36.dp).background(accentGreen.copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Rounded.Star, contentDescription = null, tint = accentGreen, modifier = Modifier.size(18.dp))
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(stringResource(R.string.best_day), style = MaterialTheme.typography.labelSmall, color = accentGreen, fontWeight = FontWeight.Bold)
                                    Text(
                                        "${day.first} — ${CurrencyFormatter.formatWithRate(day.second, currency)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Bar Chart: Last 7 Days
            item {
                Text(
                    stringResource(R.string.last_7_days_revenue),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White.copy(alpha = 0.03f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        BarChart(
                            data = last7Days,
                            barColor = accentBlue,
                            currency = currency
                        )
                    }
                }
            }

            // Pie Chart: Invoice Status
            item {
                Text(
                    stringResource(R.string.invoice_status_distribution),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White.copy(alpha = 0.03f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        val total = (paidCount + unpaidCount + overdueCount)
                        DonutChart(
                            paid    = paidCount,
                            unpaid  = unpaidCount,
                            overdue = overdueCount,
                            total   = total
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        // Legend
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatusLegendItem(stringResource(R.string.status_paid),        paidCount,    accentGreen, total)
                            StatusLegendItem(stringResource(R.string.status_unpaid),  unpaidCount,  accentAmber, total)
                            StatusLegendItem(stringResource(R.string.status_overdue),  overdueCount, accentRed,   total)
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }
}

@Composable
fun BarChart(
    data: List<Pair<String, Double>>,
    barColor: Color,
    currency: String
) {
    if (data.isEmpty()) {
        Box(Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.no_data), color = Color.Gray)
        }
        return
    }

    val maxVal = data.maxOf { it.second }.coerceAtLeast(1.0)

    Row(
        modifier = Modifier.fillMaxWidth().height(160.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEach { (label, value) ->
            val fraction = (value / maxVal).toFloat()
            val animFraction by animateFloatAsState(
                targetValue = fraction,
                animationSpec = tween(durationMillis = 800),
                label = "bar_$label"
            )
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                if (value > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height((120 * animFraction).dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(barColor, barColor.copy(alpha = 0.3f))
                                )
                            )
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.1f))
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    label.take(3),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 8.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun DonutChart(
    paid: Int, unpaid: Int, overdue: Int, total: Int
) {
    val safeTotal = if (total <= 0) 1 else total
    val paidPct    = paid.toFloat() / safeTotal
    val unpaidPct  = unpaid.toFloat() / safeTotal
    val overduePct = overdue.toFloat() / safeTotal

    val animPaid    by animateFloatAsState(targetValue = paidPct,    animationSpec = tween(1000), label = "paid")
    val animUnpaid  by animateFloatAsState(targetValue = unpaidPct,  animationSpec = tween(1000), label = "unpaid")
    val animOverdue by animateFloatAsState(targetValue = overduePct, animationSpec = tween(1000), label = "overdue")

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.Canvas(
            modifier = Modifier.size(160.dp)
        ) {
            val strokeWidth = 32.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2
            val topLeft = androidx.compose.ui.geometry.Offset(
                (size.width - radius * 2) / 2,
                (size.height - radius * 2) / 2
            )
            val arcSize = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)

            val gap = 4f
            var startAngle = -90f

            // Paid
            val paidSweep = animPaid * 360f - (if (animPaid > 0) gap else 0f)
            drawArc(
                color = Color(0xFF2DD4BF),
                startAngle = startAngle,
                sweepAngle = paidSweep.coerceAtLeast(0f),
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )
            startAngle += animPaid * 360f

            // Unpaid
            val unpaidSweep = animUnpaid * 360f - (if (animUnpaid > 0) gap else 0f)
            drawArc(
                color = Color(0xFFFBBF24),
                startAngle = startAngle,
                sweepAngle = unpaidSweep.coerceAtLeast(0f),
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )
            startAngle += animUnpaid * 360f

            // Overdue
            val overdueSweep = animOverdue * 360f - (if (animOverdue > 0) gap else 0f)
            drawArc(
                color = Color(0xFFFB7185),
                startAngle = startAngle,
                sweepAngle = overdueSweep.coerceAtLeast(0f),
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "$total",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Text(
                stringResource(R.string.pdf_invoice_label).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 1.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun StatusLegendItem(label: String, count: Int, color: Color, total: Int) {
    val pct = if (total > 0) (count * 100 / total) else 0
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(count.toString(), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold, color = Color.White)
        Text(label, style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = Color.Gray)
        Text("$pct%", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = color, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun AnalyticsSummaryCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    iconColor: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = Color.White.copy(alpha = 0.03f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
            }
            Column {
                Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 9.sp)
                Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold, color = Color.White, maxLines = 1)
            }
        }
    }
}
