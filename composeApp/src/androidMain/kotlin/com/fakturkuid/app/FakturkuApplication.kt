package com.fakturkuid.app

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import com.fakturkuid.app.di.appModule
import com.fakturkuid.app.di.platformModule

import com.fakturkuid.app.utils.SecurityManager

class FakturkuApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Ensure secure keystore initialization on startup
        SecurityManager.getOrCreateDatabasePassphrase(this)
        
        startKoin {
            androidLogger()
            androidContext(this@FakturkuApplication)
            modules(appModule, platformModule)
        }
    }
}
