package com.fakturkuid.app.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.fakturkuid.app.data.entity.BusinessProfile
import com.fakturkuid.app.data.entity.Invoice
import com.fakturkuid.app.data.entity.InvoiceItem
import com.fakturkuid.app.data.entity.Customer
import com.fakturkuid.app.data.dao.BusinessProfileDao
import com.fakturkuid.app.data.dao.InvoiceDao
import com.fakturkuid.app.data.dao.CustomerDao
import kotlinx.coroutines.Dispatchers

@Database(
    entities = [BusinessProfile::class, Invoice::class, InvoiceItem::class, Customer::class],
    version = 8,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun businessProfileDao(): BusinessProfileDao
    abstract fun invoiceDao(): InvoiceDao
    abstract fun customerDao(): CustomerDao

    companion object {
        val MIGRATION_3_4 = object : androidx.room.migration.Migration(3, 4) {
            override fun migrate(connection: androidx.sqlite.SQLiteConnection) {
                connection.prepare("ALTER TABLE business_profiles ADD COLUMN waTemplatePaid TEXT DEFAULT NULL").apply { 
                    step()
                    close()
                }
                connection.prepare("ALTER TABLE business_profiles ADD COLUMN waTemplateUnpaid TEXT DEFAULT NULL").apply { 
                    step()
                    close()
                }
                connection.prepare("ALTER TABLE business_profiles ADD COLUMN waTemplateOverdue TEXT DEFAULT NULL").apply { 
                    step()
                    close()
                }
            }
        }

        val MIGRATION_5_6 = object : androidx.room.migration.Migration(5, 6) {
            override fun migrate(connection: androidx.sqlite.SQLiteConnection) {
                connection.prepare("ALTER TABLE customers ADD COLUMN memberNumber TEXT DEFAULT NULL").apply {
                    step()
                    close()
                }
            }
        }
        val MIGRATION_6_7 = object : androidx.room.migration.Migration(6, 7) {
            override fun migrate(connection: androidx.sqlite.SQLiteConnection) {
                connection.prepare("ALTER TABLE customers ADD COLUMN isMember INTEGER NOT NULL DEFAULT 0").apply {
                    step()
                    close()
                }
                connection.prepare("ALTER TABLE customers ADD COLUMN status TEXT NOT NULL DEFAULT 'Active'").apply {
                    step()
                    close()
                }
            }
        }
        val MIGRATION_7_8 = object : androidx.room.migration.Migration(7, 8) {
            override fun migrate(connection: androidx.sqlite.SQLiteConnection) {
                connection.prepare("ALTER TABLE invoices ADD COLUMN customerEmail TEXT DEFAULT NULL").apply {
                    step()
                    close()
                }
                connection.prepare("ALTER TABLE invoices ADD COLUMN customerMemberNumber TEXT DEFAULT NULL").apply {
                    step()
                    close()
                }
            }
        }
    }
}

fun getRoomDatabase(
    builder: RoomDatabase.Builder<AppDatabase>
): AppDatabase {
    return builder
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .addMigrations(AppDatabase.MIGRATION_3_4, AppDatabase.MIGRATION_5_6, AppDatabase.MIGRATION_6_7, AppDatabase.MIGRATION_7_8)
        .fallbackToDestructiveMigration(true)
        .build()
}
