package com.fakturkuid.app.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fakturkuid.app.data.entity.Invoice
import com.fakturkuid.app.data.entity.InvoiceItem
import com.fakturkuid.app.data.entity.InvoiceWithItems
import com.fakturkuid.app.data.repository.InvoiceRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class InvoiceEditorViewModel(private val repository: InvoiceRepository) : ViewModel() {

    private val _errorFlow = MutableSharedFlow<String>()
    val errorFlow = _errorFlow.asSharedFlow()

    fun saveInvoice(invoice: Invoice, items: List<InvoiceItem>) {
        viewModelScope.launch {
            try {
                repository.saveInvoice(invoice, items)
            } catch (e: Exception) {
                Log.e("InvoiceEditorViewModel", "Error saving invoice", e)
                _errorFlow.emit(e.message ?: "Failed to save invoice")
            }
        }
    }

    fun deleteInvoice(invoice: Invoice) {
        viewModelScope.launch {
            try {
                repository.deleteInvoice(invoice)
            } catch (e: Exception) {
                Log.e("InvoiceEditorViewModel", "Error deleting invoice", e)
                _errorFlow.emit(e.message ?: "Failed to delete invoice")
            }
        }
    }

    fun updateInvoiceStatus(invoiceId: Long, status: String) {
        viewModelScope.launch {
            try {
                repository.updateInvoiceStatus(invoiceId, status)
            } catch (e: Exception) {
                Log.e("InvoiceEditorViewModel", "Error updating invoice status", e)
                _errorFlow.emit(e.message ?: "Failed to update status")
            }
        }
    }

    fun duplicateInvoice(invoiceId: Long) {
        viewModelScope.launch {
            try {
                repository.getInvoiceWithItemsSync(invoiceId)?.let { data ->
                    val newInvoice = data.invoice.copy(
                        id = 0,
                        invoiceNumber = "COPY-${data.invoice.invoiceNumber}",
                        issueDate = System.currentTimeMillis()
                    )
                    repository.saveInvoice(newInvoice, data.items.map { it.copy(id = 0, invoiceId = 0) })
                }
            } catch (e: Exception) {
                Log.e("InvoiceEditorViewModel", "Error duplicating invoice", e)
                _errorFlow.emit(e.message ?: "Failed to duplicate invoice")
            }
        }
    }

    suspend fun getInvoiceCountInPeriod(start: Long, end: Long): Int {
        return repository.getInvoiceCountInPeriod(start, end)
    }

    suspend fun getInvoiceWithItemsSync(id: Long): InvoiceWithItems? {
        return repository.getInvoiceWithItemsSync(id)
    }
}
