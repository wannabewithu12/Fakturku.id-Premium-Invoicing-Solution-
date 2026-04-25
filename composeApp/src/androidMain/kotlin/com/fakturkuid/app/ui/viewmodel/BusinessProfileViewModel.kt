package com.fakturkuid.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fakturkuid.app.data.entity.BusinessProfile
import com.fakturkuid.app.data.repository.BusinessProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class BusinessProfileViewModel(private val repository: BusinessProfileRepository) : ViewModel() {

    private val _profile = MutableStateFlow<BusinessProfile?>(null)
    val profile: StateFlow<BusinessProfile?> = _profile.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            repository.getProfile().collectLatest {
                _profile.value = it
            }
        }
    }

    fun saveProfile(
        name: String,
        address: String,
        phone: String,
        email: String,
        logoUri: String?,
        logoBlob: ByteArray?,
        footer: String?,
        waTemplatePaid: String? = _profile.value?.waTemplatePaid,
        waTemplateUnpaid: String? = _profile.value?.waTemplateUnpaid,
        waTemplateOverdue: String? = _profile.value?.waTemplateOverdue
    ) {
        viewModelScope.launch {
            repository.saveProfile(
                BusinessProfile(
                    businessName = name,
                    address = address,
                    phone = phone,
                    email = email,
                    logoUri = logoUri,
                    logoBlob = logoBlob,
                    defaultFooter = footer,
                    waTemplatePaid = waTemplatePaid,
                    waTemplateUnpaid = waTemplateUnpaid,
                    waTemplateOverdue = waTemplateOverdue
                )
            )
        }
    }

    fun saveWaTemplates(paid: String, unpaid: String, overdue: String) {
        val current = _profile.value ?: return
        viewModelScope.launch {
            repository.saveProfile(
                current.copy(
                    waTemplatePaid = paid,
                    waTemplateUnpaid = unpaid,
                    waTemplateOverdue = overdue
                )
            )
        }
    }
}
