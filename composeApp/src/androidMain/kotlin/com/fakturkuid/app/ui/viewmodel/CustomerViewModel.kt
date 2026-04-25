package com.fakturkuid.app.ui.viewmodel

import android.util.Log

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fakturkuid.app.data.entity.Customer
import com.fakturkuid.app.data.entity.InvoiceWithItems
import com.fakturkuid.app.data.repository.CustomerRepository
import com.fakturkuid.app.data.repository.InvoiceRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(kotlinx.coroutines.FlowPreview::class, kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class CustomerViewModel(
    private val repository: CustomerRepository,
    private val invoiceRepository: InvoiceRepository? = null
) : ViewModel() {

    private val _errorFlow = MutableSharedFlow<String>()
    val errorFlow = _errorFlow.asSharedFlow()

    // Full list for CustomerScreen
    val allCustomers: StateFlow<List<Customer>> = repository.getAllCustomers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Legacy — kept for backward compatibility with CreateInvoiceScreen autocomplete
    private val _customers = MutableStateFlow<List<Customer>>(emptyList())
    val customers: StateFlow<List<Customer>> = allCustomers

    private val _searchQuery = MutableStateFlow("")
    val searchResults: StateFlow<List<Customer>> = _searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            if (query.isBlank()) flowOf(emptyList())
            else repository.searchCustomers(query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun saveCustomer(customer: Customer) {
        viewModelScope.launch {
            try {
                repository.insertCustomer(customer)
            } catch (e: Exception) {
                Log.e("CustomerViewModel", "Error saving customer", e)
                _errorFlow.emit(e.message ?: "Failed to save customer")
            }
        }
    }

    fun deleteCustomer(customer: Customer) {
        viewModelScope.launch {
            try {
                repository.deleteCustomer(customer)
            } catch (e: Exception) {
                Log.e("CustomerViewModel", "Error deleting customer", e)
                _errorFlow.emit(e.message ?: "Failed to delete customer")
            }
        }
    }

    // For CustomerDetailScreen — get invoices by customer
    fun getInvoicesByCustomer(customerId: Long): Flow<List<InvoiceWithItems>> {
        return invoiceRepository?.getInvoicesByCustomer(customerId) ?: flowOf(emptyList())
    }

    fun getTotalPaidByCustomer(customerId: Long): Flow<Double> {
        return invoiceRepository?.getTotalPaidByCustomer(customerId) ?: flowOf(0.0)
    }

    fun getInvoiceCountByCustomer(customerId: Long): Flow<Int> {
        return invoiceRepository?.getInvoiceCountByCustomer(customerId) ?: flowOf(0)
    }

    suspend fun getNextMemberNumber(): String {
        val count = repository.getCustomerCount()
        return (count + 1).toString().padStart(3, '0')
    }
}

