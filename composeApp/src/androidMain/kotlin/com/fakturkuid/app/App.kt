package com.fakturkuid.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.fakturkuid.app.ui.AppNavigation
import com.fakturkuid.app.ui.theme.FakturkuIdTheme
import com.fakturkuid.app.ui.viewmodel.SettingsViewModel
import org.koin.compose.KoinContext
import org.koin.compose.koinInject

@Composable
fun App() {
    KoinContext {
        val settingsViewModel: SettingsViewModel = koinInject()
        val theme by settingsViewModel.theme.collectAsState()
        
        FakturkuIdTheme(darkTheme = theme == "dark") {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                AppNavigation()
            }
        }
    }
}
