package com.fakturkuid.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fakturkuid.app.data.entity.Invoice
import com.fakturkuid.app.data.entity.InvoiceWithItems
import com.fakturkuid.app.data.repository.InvoiceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class InvoiceDashboardViewModel(private val repository: InvoiceRepository) : ViewModel() {

    private val _invoices = MutableStateFlow<List<Invoice>>(emptyList())
    val invoices: StateFlow<List<Invoice>> = _invoices.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _filteredInvoices = MutableStateFlow<List<Invoice>>(emptyList())
    val filteredInvoices = _filteredInvoices.asStateFlow()

    private val _invoicesWithItems = MutableStateFlow<List<InvoiceWithItems>>(emptyList())
    val invoicesWithItems = _invoicesWithItems.asStateFlow()

    private val _currentInvoice = MutableStateFlow<InvoiceWithItems?>(null)
    val currentInvoice: StateFlow<InvoiceWithItems?> = _currentInvoice.asStateFlow()

    private val _dailyIncome = MutableStateFlow(0.0)
    val dailyIncome: StateFlow<Double> = _dailyIncome.asStateFlow()

    private val _monthlyIncome = MutableStateFlow(0.0)
    val monthlyIncome: StateFlow<Double> = _monthlyIncome.asStateFlow()

    private val _paidCount = MutableStateFlow(0)
    val paidCount: StateFlow<Int> = _paidCount.asStateFlow()

    private val _unpaidCount = MutableStateFlow(0)
    val unpaidCount: StateFlow<Int> = _unpaidCount.asStateFlow()

    private val _overdueCount = MutableStateFlow(0)
    val overdueCount: StateFlow<Int> = _overdueCount.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllInvoices().collectLatest {
                _invoices.value = it
                filterInvoices()
            }
        }
        viewModelScope.launch {
            repository.getAllInvoicesWithItems().collectLatest {
                _invoicesWithItems.value = it
            }
        }
        loadStats()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        filterInvoices()
    }

    private fun filterInvoices() {
        val query = _searchQuery.value.lowercase()
        _filteredInvoices.value = if (query.isEmpty()) {
            _invoices.value
        } else {
            _invoices.value.filter { 
                it.invoiceNumber.lowercase().contains(query) || 
                it.customerName.lowercase().contains(query)
            }
        }
    }

    private fun loadStats() {
        val now = java.util.Calendar.getInstance()
        
        // Daily
        val startOfDay = now.apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
        }.timeInMillis
        val endOfDay = now.apply {
            set(java.util.Calendar.HOUR_OF_DAY, 23)
            set(java.util.Calendar.MINUTE, 59)
            set(java.util.Calendar.SECOND, 59)
        }.timeInMillis

        viewModelScope.launch {
            repository.getIncome(startOfDay, endOfDay).collect {
                _dailyIncome.value = it ?: 0.0
            }
        }

        // Monthly
        now.timeInMillis = System.currentTimeMillis()
        val startOfMonth = now.apply {
            set(java.util.Calendar.DAY_OF_MONTH, 1)
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
        }.timeInMillis
        val endOfMonth = now.apply {
            set(java.util.Calendar.DAY_OF_MONTH, getActualMaximum(java.util.Calendar.DAY_OF_MONTH))
            set(java.util.Calendar.HOUR_OF_DAY, 23)
            set(java.util.Calendar.MINUTE, 59)
            set(java.util.Calendar.SECOND, 59)
        }.timeInMillis

        viewModelScope.launch {
            repository.getIncome(startOfMonth, endOfMonth).collect {
                _monthlyIncome.value = it ?: 0.0
            }
        }

        // Status Counts
        viewModelScope.launch {
            repository.getInvoiceCountByStatus("paid").collect { _paidCount.value = it }
        }
        viewModelScope.launch {
            repository.getInvoiceCountByStatus("unpaid").collect { _unpaidCount.value = it }
        }
        viewModelScope.launch {
            repository.getOverdueCount(System.currentTimeMillis()).collect { _overdueCount.value = it }
        }
    }

    fun loadInvoice(id: Long) {
        viewModelScope.launch {
            repository.getInvoiceWithItems(id).collectLatest {
                _currentInvoice.value = it
            }
        }
    }
}
