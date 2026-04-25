package com.fakturkuid.app.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.fakturkuid.app.data.repository.BusinessProfileRepository
import com.fakturkuid.app.data.repository.InvoiceRepository
import com.fakturkuid.app.data.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val invoiceRepository: InvoiceRepository,
    private val profileRepository: BusinessProfileRepository
) : ViewModel() {

    // Business Profile
    val profile = profileRepository.getProfile()

    // Theme
    private val _theme = MutableStateFlow(settingsRepository.getTheme())
    val theme: StateFlow<String> = _theme.asStateFlow()

    // Language
    private val _language = MutableStateFlow(settingsRepository.getLanguage())
    val language: StateFlow<String> = _language.asStateFlow()

    // Currency
    private val _currency = MutableStateFlow(settingsRepository.getCurrency())
    val currency: StateFlow<String> = _currency.asStateFlow()

    // App Lock
    private val _isAppLockEnabled = MutableStateFlow(settingsRepository.isAppLockEnabled())
    val isAppLockEnabled: StateFlow<Boolean> = _isAppLockEnabled.asStateFlow()

    // Onboarding (One-Time Permission Flow)
    private val _onboardingDone = MutableStateFlow(settingsRepository.isOnboardingDone())
    val onboardingDone: StateFlow<Boolean> = _onboardingDone.asStateFlow()

    fun setTheme(newTheme: String) {
        settingsRepository.setTheme(newTheme)
        _theme.value = newTheme
    }

    fun setLanguage(newLang: String) {
        settingsRepository.setLanguage(newLang)
        _language.value = newLang
    }

    fun setCurrency(newCurr: String) {
        settingsRepository.setCurrency(newCurr)
        _currency.value = newCurr
    }

    fun setAppLockEnabled(enabled: Boolean) {
        settingsRepository.setAppLockEnabled(enabled)
        _isAppLockEnabled.value = enabled
    }

    fun setOnboardingDone() {
        settingsRepository.setOnboardingDone(true)
        _onboardingDone.value = true
    }

    fun clearAppData(context: Context) {
        settingsRepository.clearCache(context)
    }

    suspend fun getInvoicesForReport(start: Long, end: Long) =
        invoiceRepository.getInvoicesWithItemsInPeriodSync(start, end)
}
