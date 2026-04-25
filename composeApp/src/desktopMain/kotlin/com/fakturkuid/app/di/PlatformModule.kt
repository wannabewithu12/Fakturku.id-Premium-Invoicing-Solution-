package com.fakturkuid.app.di

import com.fakturkuid.app.data.database.getDatabaseBuilder
import com.fakturkuid.app.data.database.getRoomDatabase
import org.koin.dsl.module

val platformModule = module {
    single { getRoomDatabase(getDatabaseBuilder()) }
}
