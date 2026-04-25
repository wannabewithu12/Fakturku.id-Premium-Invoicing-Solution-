package com.fakturkuid.app.di

import android.content.Context
import com.fakturkuid.app.data.database.getDatabaseBuilder
import com.fakturkuid.app.data.database.getRoomDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val platformModule = module {
    single { getRoomDatabase(getDatabaseBuilder(androidContext())) }
}
