package com.fakturkuid.app.data.repository

import com.fakturkuid.app.data.dao.CustomerDao
import com.fakturkuid.app.data.entity.Customer
import kotlinx.coroutines.flow.Flow

class CustomerRepository(private val dao: CustomerDao) {
    fun getAllCustomers(): Flow<List<Customer>> = dao.getAllCustomers()
    
    suspend fun insertCustomer(customer: Customer): Long = dao.insertCustomer(customer)
    
    suspend fun updateCustomer(customer: Customer) = dao.updateCustomer(customer)
    
    suspend fun deleteCustomer(customer: Customer) = dao.deleteCustomer(customer)
    
    fun searchCustomers(query: String): Flow<List<Customer>> = dao.searchCustomers("%$query%")
    
    suspend fun getCustomerCount(): Int = dao.getCustomerCount()

    suspend fun getAllCustomersSync(): List<Customer> = dao.getAllCustomersSync()

    suspend fun deleteAllCustomers() = dao.deleteAllCustomers()

    suspend fun insertCustomers(customers: List<Customer>) = dao.insertCustomers(customers)
}
