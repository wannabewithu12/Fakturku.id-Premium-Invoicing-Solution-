package com.fakturkuid.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.content.Context
import com.fakturkuid.app.R
import com.fakturkuid.app.data.entity.BackupMetadata
import com.fakturkuid.app.data.manager.BackupResult
import com.fakturkuid.app.data.manager.CloudBackupManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class BackupViewModel(
    private val context: Context,
    private val backupManager: CloudBackupManager,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _history = MutableStateFlow<List<BackupMetadata>>(emptyList())
    val history: StateFlow<List<BackupMetadata>> = _history

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    init {
        ensureAuthAndLoadHistory()
    }

    private fun ensureAuthAndLoadHistory() {
        viewModelScope.launch {
            if (auth.currentUser == null) {
                try {
                    auth.signInAnonymously().await()
                } catch (e: Exception) {
                    _message.value = context.getString(R.string.msg_auth_failed, e.message ?: "")
                }
            }
            loadHistory()
        }
    }

    fun loadHistory() {
        viewModelScope.launch {
            _isLoading.value = true
            _history.value = backupManager.getBackupHistory()
            _isLoading.value = false
        }
    }

    fun performBackup() {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = backupManager.performBackup()) {
                is BackupResult.Success -> {
                    _message.value = context.getString(R.string.msg_backup_success)
                    loadHistory()
                }
                is BackupResult.Failure -> {
                    _message.value = context.getString(R.string.msg_backup_failed, result.error)
                }
            }
            _isLoading.value = false
        }
    }

    fun restoreFromCloud(fileName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = backupManager.restoreFromCloud(fileName)) {
                is BackupResult.Success -> {
                    _message.value = context.getString(R.string.msg_restore_success, result.invoiceCount)
                }
                is BackupResult.Failure -> {
                    _message.value = context.getString(R.string.msg_restore_failed, result.error)
                }
            }
            _isLoading.value = false
        }
    }

    fun deleteBackup(fileName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            if (backupManager.deleteBackup(fileName)) {
                _message.value = context.getString(R.string.msg_delete_backup_success)
                loadHistory()
            } else {
                _message.value = context.getString(R.string.msg_delete_backup_failed)
            }
            _isLoading.value = false
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}
