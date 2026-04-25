package com.fakturkuid.app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.fakturkuid.app.data.entity.Invoice
import com.fakturkuid.app.data.entity.InvoiceItem
import com.fakturkuid.app.data.entity.InvoiceWithItems
import kotlinx.coroutines.flow.Flow

@Dao
interface InvoiceDao {
    @Query("SELECT * FROM invoices ORDER BY issueDate DESC")
    fun getAllInvoices(): Flow<List<Invoice>>

    @Transaction
    @Query("SELECT * FROM invoices ORDER BY issueDate DESC")
    fun getAllInvoicesWithItems(): Flow<List<InvoiceWithItems>>

    @Transaction
    @Query("SELECT * FROM invoices WHERE id = :invoiceId")
    fun getInvoiceWithItems(invoiceId: Long): Flow<InvoiceWithItems?>
    
    @Transaction
    @Query("SELECT * FROM invoices WHERE id = :invoiceId")
    suspend fun getInvoiceWithItemsSync(invoiceId: Long): InvoiceWithItems?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: Invoice): Long

    @Update
    suspend fun updateInvoice(invoice: Invoice)

    @Delete
    suspend fun deleteInvoice(invoice: Invoice)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoiceItems(items: List<InvoiceItem>)

    @Query("DELETE FROM invoice_items WHERE invoiceId = :invoiceId")
    suspend fun deleteInvoiceItemsByInvoiceId(invoiceId: Long)
    
    @Transaction
    suspend fun saveInvoiceWithItems(invoice: Invoice, items: List<InvoiceItem>) {
        if (invoice.id == 0L) {
            val nextId = insertInvoice(invoice)
            insertInvoiceItems(items.map { it.copy(invoiceId = nextId) })
        } else {
            updateInvoice(invoice)
            deleteInvoiceItemsByInvoiceId(invoice.id)
            insertInvoiceItems(items.map { it.copy(invoiceId = invoice.id) })
        }
    }

    @Transaction
    suspend fun restoreInvoiceWithItems(invoice: Invoice, items: List<InvoiceItem>) {
        // Force insert even if ID is present
        insertInvoice(invoice)
        insertInvoiceItems(items)
    }

    @Query("""
        SELECT SUM(
            (sub.total - CASE WHEN i.diskonType = 'percentage' THEN (sub.total * i.diskon / 100.0) ELSE i.diskon END) 
            * (1.0 + (i.pajak / 100.0))
        )
        FROM invoices i
        JOIN (SELECT invoiceId, SUM(quantity * unitPrice) AS total FROM invoice_items GROUP BY invoiceId) sub
        ON i.id = sub.invoiceId
        WHERE i.status = 'paid' AND i.issueDate >= :startDate AND i.issueDate <= :endDate
    """)
    fun getIncome(startDate: Long, endDate: Long): Flow<Double?>

    @Query("SELECT COUNT(*) FROM invoices WHERE status = :status")
    fun getInvoiceCountByStatus(status: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM invoices WHERE dueDate < :today AND status = 'unpaid'")
    fun getOverdueCount(today: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM invoices WHERE issueDate >= :startDate AND issueDate <= :endDate")
    suspend fun getInvoiceCountInPeriod(startDate: Long, endDate: Long): Int

    @Query("UPDATE invoices SET status = :status WHERE id = :invoiceId")
    suspend fun updateInvoiceStatus(invoiceId: Long, status: String)

    @Transaction
    @Query("SELECT * FROM invoices WHERE issueDate >= :startDate AND issueDate <= :endDate ORDER BY issueDate DESC")
    suspend fun getInvoicesWithItemsInPeriodSync(startDate: Long, endDate: Long): List<InvoiceWithItems>

    // Analytics: get all paid invoices with items for chart computation
    @Transaction
    @Query("SELECT * FROM invoices WHERE status = 'paid' AND issueDate >= :startDate ORDER BY issueDate ASC")
    fun getPaidInvoicesWithItemsSince(startDate: Long): Flow<List<InvoiceWithItems>>

    // Customer Management: get invoices belonging to a specific customer
    @Transaction
    @Query("SELECT * FROM invoices WHERE customerId = :customerId ORDER BY issueDate DESC")
    fun getInvoicesByCustomer(customerId: Long): Flow<List<InvoiceWithItems>>

    // Customer Management: total paid amount for a customer
    @Query("""
        SELECT COALESCE(SUM(
            (sub.total - CASE WHEN i.diskonType = 'percentage' THEN (sub.total * i.diskon / 100.0) ELSE i.diskon END) 
            * (1.0 + (i.pajak / 100.0))
        ), 0.0)
        FROM invoices i
        JOIN (SELECT invoiceId, SUM(quantity * unitPrice) AS total FROM invoice_items GROUP BY invoiceId) sub
        ON i.id = sub.invoiceId
        WHERE i.customerId = :customerId AND i.status = 'paid'
    """)
    fun getTotalPaidByCustomer(customerId: Long): Flow<Double>

    // Customer Management: count of invoices for a customer
    @Query("SELECT COUNT(*) FROM invoices WHERE customerId = :customerId")
    fun getInvoiceCountByCustomer(customerId: Long): Flow<Int>

    @Transaction
    @Query("SELECT * FROM invoices ORDER BY issueDate DESC")
    suspend fun getAllInvoicesWithItemsSync(): List<InvoiceWithItems>

    @Query("DELETE FROM invoices")
    suspend fun deleteAllInvoices()

    @Query("""
        SELECT SUM(
            (sub.total - CASE WHEN i.diskonType = 'percentage' THEN (sub.total * i.diskon / 100.0) ELSE i.diskon END) 
            * (1.0 + (i.pajak / 100.0))
        )
        FROM invoices i
        JOIN (SELECT invoiceId, SUM(quantity * unitPrice) AS total FROM invoice_items GROUP BY invoiceId) sub
        ON i.id = sub.invoiceId
        WHERE i.status = 'paid' AND i.issueDate >= :startDate AND i.issueDate <= :endDate
    """)
    suspend fun getIncomeSync(startDate: Long, endDate: Long): Double?

    @Query("SELECT COUNT(*) FROM invoices WHERE status = :status")
    suspend fun getInvoiceCountByStatusSync(status: String): Int
}
