package com.fakturkuid.app

import android.os.Bundle
import android.content.res.Configuration
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Icon
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.fakturkuid.app.data.database.AppDatabase
import com.fakturkuid.app.data.repository.BusinessProfileRepository
import com.fakturkuid.app.data.repository.SettingsRepository
import com.fakturkuid.app.ui.AppNavigation
import com.fakturkuid.app.ui.theme.FakturkuIdTheme
import com.fakturkuid.app.ui.viewmodel.SettingsViewModel
import com.fakturkuid.app.utils.BiometricHelper
import org.koin.androidx.compose.koinViewModel
import java.util.Locale

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Silent startup sync (No activity recreation here)
        val repo = SettingsRepository(this)
        val initialTheme = repo.getTheme()  
        
        // Apply system base ONLY if strictly necessary for the very first frame
        val initialMode = if (initialTheme == "dark") AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        if (AppCompatDelegate.getDefaultNightMode() != initialMode) {
            AppCompatDelegate.setDefaultNightMode(initialMode)
        }

        setContent {
            val settingsViewModel: SettingsViewModel = koinViewModel()
            val theme by settingsViewModel.theme.collectAsState()
            val language by settingsViewModel.language.collectAsState()
            val currency by settingsViewModel.currency.collectAsState()
            val isAppLockEnabled by settingsViewModel.isAppLockEnabled.collectAsState()
            
            // The Activity Context is required for direct resource injection
            val context = LocalContext.current

            // Logo Self-Healing: Recreate logo file from DB blob if missing
            LaunchedEffect(Unit) {
                val db = org.koin.java.KoinJavaComponent.getKoin().get<AppDatabase>()
                val profileRepo = BusinessProfileRepository(db.businessProfileDao())
                profileRepo.syncLogoFromFile(context)
            }
            
            // SIDE EFFECT: The Spontaneous Engine
            // This forces the Activity Resources and System Locale to change INSTANTLY under the finger
            LaunchedEffect(language) {
                val locale = Locale(language)
                Locale.setDefault(locale)
                
                val config = Configuration(context.resources.configuration)
                config.setLocale(locale)
                config.setLayoutDirection(locale)
                
                // Nuclear Update: Direct resource injection for immediate stringResource() reaction
                @Suppress("DEPRECATION")
                context.resources.updateConfiguration(config, context.resources.displayMetrics)
                
                // Also trigger Activity recreation if needed, but Compose should handle it with the 'key'
            }

            // The 'key' forces Compose to re-read all strings from the updated resources
            key(language, theme, currency) {
                val configuration = remember(language, theme) {
                    val config = Configuration(context.resources.configuration)
                    val locale = Locale(language)
                    config.setLocale(locale)
                    
                    val uiMode = if (theme == "dark") {
                        Configuration.UI_MODE_NIGHT_YES
                    } else {
                        Configuration.UI_MODE_NIGHT_NO
                    }
                    config.uiMode = (config.uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or uiMode
                    config
                }

                CompositionLocalProvider(
                    LocalConfiguration provides configuration
                ) {
                    FakturkuIdTheme(darkTheme = theme == "dark") {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            var isAuthenticated by remember { mutableStateOf(false) }
                            var authTrigger by remember { mutableStateOf(0) }

                            // Lifecycle observer to re-lock the app when it goes to background (ON_STOP)
                            // and trigger authentication when it comes back to foreground (ON_RESUME)
                            val lifecycleOwner = LocalLifecycleOwner.current
                            DisposableEffect(lifecycleOwner, isAppLockEnabled) {
                                val observer = LifecycleEventObserver { _, event ->
                                    if (event == Lifecycle.Event.ON_STOP && isAppLockEnabled) {
                                        isAuthenticated = false
                                    } else if (event == Lifecycle.Event.ON_RESUME && isAppLockEnabled && !isAuthenticated) {
                                        authTrigger++ // Force re-trigger when returning to foreground
                                    }
                                }
                                lifecycleOwner.lifecycle.addObserver(observer)
                                onDispose {
                                    lifecycleOwner.lifecycle.removeObserver(observer)
                                }
                            }

                            LaunchedEffect(isAppLockEnabled, isAuthenticated, authTrigger) {
                                if (isAppLockEnabled && !isAuthenticated) {
                                    BiometricHelper.authenticate(
                                        activity = this@MainActivity,
                                        title = context.getString(R.string.app_lock),
                                        subtitle = context.getString(R.string.app_lock_desc),
                                        onSuccess = { isAuthenticated = true },
                                        onError = { /* Stay locked */ }
                                    )
                                } else if (!isAppLockEnabled) {
                                    isAuthenticated = true
                                }
                            }

                            Box {
                                AppNavigation()

                                if (isAppLockEnabled && !isAuthenticated) {
                                    // Lock Screen Overlay - Solid background to protect privacy
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(MaterialTheme.colorScheme.background)
                                            .clickable { authTrigger++ },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(
                                                Icons.Rounded.Lock, 
                                                contentDescription = "Locked", 
                                                modifier = Modifier.size(64.dp), 
                                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                                            )
                                            Spacer(modifier = Modifier.height(16.dp))
                                            androidx.compose.material3.TextButton(onClick = { 
                                                authTrigger++ 
                                            }) {
                                                androidx.compose.material3.Text(
                                                    context.getString(R.string.app_lock),
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
