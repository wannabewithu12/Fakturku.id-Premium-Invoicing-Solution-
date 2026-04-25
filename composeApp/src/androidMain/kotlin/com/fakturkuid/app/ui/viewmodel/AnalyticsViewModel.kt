package com.fakturkuid.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fakturkuid.app.data.entity.InvoiceWithItems
import com.fakturkuid.app.data.repository.InvoiceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Holds computed analytics data for the AnalyticsScreen.
 * All heavy computation (grouping, summing) is done here, not in the UI.
 */
class AnalyticsViewModel(private val repository: InvoiceRepository) : ViewModel() {

    // Bar chart: daily revenue for the last 7 days
    // Each entry = Pair(dayLabel "Sel 24", totalRevenue)
    private val _last7DaysRevenue = MutableStateFlow<List<Pair<String, Double>>>(emptyList())
    val last7DaysRevenue: StateFlow<List<Pair<String, Double>>> = _last7DaysRevenue.asStateFlow()

    // Pie chart: status distribution counts
    private val _paidCount   = MutableStateFlow(0)
    private val _unpaidCount = MutableStateFlow(0)
    private val _overdueCount = MutableStateFlow(0)
    val paidCount:   StateFlow<Int> = _paidCount.asStateFlow()
    val unpaidCount: StateFlow<Int> = _unpaidCount.asStateFlow()
    val overdueCount: StateFlow<Int> = _overdueCount.asStateFlow()

    // Summary stats
    private val _totalRevenueMonth = MutableStateFlow(0.0)
    val totalRevenueMonth: StateFlow<Double> = _totalRevenueMonth.asStateFlow()

    private val _avgInvoiceValue = MutableStateFlow(0.0)
    val avgInvoiceValue: StateFlow<Double> = _avgInvoiceValue.asStateFlow()

    private val _bestDay = MutableStateFlow<Pair<String, Double>?>(null)
    val bestDay: StateFlow<Pair<String, Double>?> = _bestDay.asStateFlow()

    init {
        load7DaysRevenue()
        loadStatusCounts()
        loadMonthlyStats()
    }

    private fun load7DaysRevenue() {
        val cal = Calendar.getInstance()
        // Go back 6 days to get 7 days total (today included)
        cal.add(Calendar.DAY_OF_YEAR, -6)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startOf7Days = cal.timeInMillis

        viewModelScope.launch {
            repository.getPaidInvoicesWithItemsSince(startOf7Days).collectLatest { invoices ->
                _last7DaysRevenue.value = computeDailyRevenue(invoices)

                // Best day
                _bestDay.value = _last7DaysRevenue.value.maxByOrNull { it.second }
            }
        }
    }

    private fun computeDailyRevenue(invoices: List<InvoiceWithItems>): List<Pair<String, Double>> {
        val dayFormat = SimpleDateFormat("EEE dd", Locale.getDefault())
        val result = mutableListOf<Pair<String, Double>>()
        val cal = Calendar.getInstance()

        for (i in 6 downTo 0) {
            val targetCal = Calendar.getInstance()
            targetCal.add(Calendar.DAY_OF_YEAR, -i)
            targetCal.set(Calendar.HOUR_OF_DAY, 0); targetCal.set(Calendar.MINUTE, 0); targetCal.set(Calendar.SECOND, 0)
            val dayStart = targetCal.timeInMillis
            targetCal.set(Calendar.HOUR_OF_DAY, 23); targetCal.set(Calendar.MINUTE, 59); targetCal.set(Calendar.SECOND, 59)
            val dayEnd = targetCal.timeInMillis

            val dayRevenue = invoices
                .filter { it.invoice.issueDate in dayStart..dayEnd }
                .sumOf { data -> data.calculateTotal() }
            result.add(dayFormat.format(Date(dayStart)) to dayRevenue)
        }
        return result
    }

    private fun loadStatusCounts() {
        viewModelScope.launch {
            repository.getInvoiceCountByStatus("paid").collectLatest   { _paidCount.value = it }
        }
        viewModelScope.launch {
            repository.getInvoiceCountByStatus("unpaid").collectLatest { _unpaidCount.value = it }
        }
        viewModelScope.launch {
            repository.getOverdueCount(System.currentTimeMillis()).collectLatest { _overdueCount.value = it }
        }
    }

    private fun loadMonthlyStats() {
        val now = Calendar.getInstance()
        val startOfMonth = now.apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0)
        }.timeInMillis
        val endOfMonth = now.apply {
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59)
        }.timeInMillis

        viewModelScope.launch {
            repository.getPaidInvoicesWithItemsSince(startOfMonth).collectLatest { invoices ->
                val total = invoices.sumOf { data -> data.calculateTotal() }
                _totalRevenueMonth.value = total
                _avgInvoiceValue.value = if (invoices.isNotEmpty()) total / invoices.size else 0.0
            }
        }
    }
}
