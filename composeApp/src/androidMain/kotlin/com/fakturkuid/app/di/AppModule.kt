package com.fakturkuid.app.di

import org.koin.dsl.module
import com.fakturkuid.app.data.database.AppDatabase
import com.fakturkuid.app.data.database.getRoomDatabase
import com.fakturkuid.app.data.repository.BusinessProfileRepository
import com.fakturkuid.app.data.repository.InvoiceRepository
import com.fakturkuid.app.data.repository.CustomerRepository
import com.fakturkuid.app.data.repository.SettingsRepository
import com.fakturkuid.app.ui.viewmodel.BusinessProfileViewModel
import com.fakturkuid.app.ui.viewmodel.InvoiceDashboardViewModel
import com.fakturkuid.app.ui.viewmodel.InvoiceEditorViewModel
import com.fakturkuid.app.ui.viewmodel.AnalyticsViewModel
import com.fakturkuid.app.ui.viewmodel.CustomerViewModel
import com.fakturkuid.app.ui.viewmodel.SettingsViewModel
import com.fakturkuid.app.ui.viewmodel.BackupViewModel
import com.fakturkuid.app.data.manager.CloudBackupManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.storage.ktx.storage
import com.google.firebase.ktx.Firebase

val appModule = module {
    // DAOs
    single { get<AppDatabase>().businessProfileDao() }
    single { get<AppDatabase>().invoiceDao() }
    single { get<AppDatabase>().customerDao() }
    
    // Repositories
    single { BusinessProfileRepository(get()) }
    single { InvoiceRepository(get(), get()) }
    single { CustomerRepository(get()) }
    single { SettingsRepository(get()) }

    // Firebase
    single { Firebase.firestore }
    single { Firebase.storage }
    single { Firebase.auth }

    // Managers
    single { CloudBackupManager(get(), get(), get(), get(), get(), get()) }
    
    // ViewModels
    factory { BusinessProfileViewModel(get()) }
    factory { InvoiceDashboardViewModel(get()) }
    factory { InvoiceEditorViewModel(get()) }
    factory { AnalyticsViewModel(get()) }
    factory { CustomerViewModel(get(), get()) }
    factory { SettingsViewModel(get(), get(), get()) }
    factory { BackupViewModel(get(), get(), get()) }
}
