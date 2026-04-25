package com.fakturkuid.app.data.repository

import com.fakturkuid.app.data.dao.InvoiceDao
import com.fakturkuid.app.data.entity.Invoice
import com.fakturkuid.app.data.entity.InvoiceItem
import com.fakturkuid.app.data.entity.InvoiceWithItems
import com.fakturkuid.app.widget.FakturkuWidget
import androidx.glance.appwidget.updateAll
import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class InvoiceRepository(
    private val dao: InvoiceDao,
    private val context: Context
) {
    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    private fun triggerWidgetUpdate() {
        repositoryScope.launch {
            try {
                FakturkuWidget().updateAll(context)
            } catch (e: Exception) {
                // Ignore widget update errors in background
            }
        }
    }
    
    fun getAllInvoices(): Flow<List<Invoice>> {
        return dao.getAllInvoices()
    }

    fun getAllInvoicesWithItems(): Flow<List<InvoiceWithItems>> {
        return dao.getAllInvoicesWithItems()
    }
    
    fun getInvoiceWithItems(id: Long): Flow<InvoiceWithItems?> {
        return dao.getInvoiceWithItems(id)
    }
    
    suspend fun getInvoiceWithItemsSync(id: Long): InvoiceWithItems? {
        return dao.getInvoiceWithItemsSync(id)
    }
    
    suspend fun saveInvoice(invoice: Invoice, items: List<InvoiceItem>) {
        dao.saveInvoiceWithItems(invoice, items)
        triggerWidgetUpdate()
    }

    suspend fun restoreInvoice(invoice: Invoice, items: List<InvoiceItem>) {
        dao.restoreInvoiceWithItems(invoice, items)
        triggerWidgetUpdate()
    }
    
    suspend fun deleteInvoice(invoice: Invoice) {
        dao.deleteInvoice(invoice)
        triggerWidgetUpdate()
    }

    suspend fun updateInvoiceStatus(invoiceId: Long, status: String) {
        dao.updateInvoiceStatus(invoiceId, status)
        triggerWidgetUpdate()
    }

    fun getIncome(startDate: Long, endDate: Long): Flow<Double?> {
        return dao.getIncome(startDate, endDate)
    }

    fun getInvoiceCountByStatus(status: String): Flow<Int> {
        return dao.getInvoiceCountByStatus(status)
    }

    fun getOverdueCount(today: Long): Flow<Int> {
        return dao.getOverdueCount(today)
    }

    suspend fun getInvoiceCountInPeriod(startDate: Long, endDate: Long): Int {
        return dao.getInvoiceCountInPeriod(startDate, endDate)
    }

    suspend fun getInvoicesWithItemsInPeriodSync(startDate: Long, endDate: Long): List<InvoiceWithItems> {
        return dao.getInvoicesWithItemsInPeriodSync(startDate, endDate)
    }

    fun getPaidInvoicesWithItemsSince(startDate: Long): Flow<List<InvoiceWithItems>> {
        return dao.getPaidInvoicesWithItemsSince(startDate)
    }

    fun getInvoicesByCustomer(customerId: Long): Flow<List<InvoiceWithItems>> {
        return dao.getInvoicesByCustomer(customerId)
    }

    fun getTotalPaidByCustomer(customerId: Long): Flow<Double> {
        return dao.getTotalPaidByCustomer(customerId)
    }

    fun getInvoiceCountByCustomer(customerId: Long): Flow<Int> {
        return dao.getInvoiceCountByCustomer(customerId)
    }

    suspend fun getAllInvoicesSync(): List<InvoiceWithItems> {
        return dao.getAllInvoicesWithItemsSync()
    }

    suspend fun deleteAllInvoices() {
        dao.deleteAllInvoices()
        triggerWidgetUpdate()
    }

    suspend fun getIncomeSync(startDate: Long, endDate: Long): Double? {
        return dao.getIncomeSync(startDate, endDate)
    }

    suspend fun getInvoiceCountByStatusSync(status: String): Int {
        return dao.getInvoiceCountByStatusSync(status)
    }
}
