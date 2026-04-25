package com.fakturkuid.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.fakturkuid.app.ui.analytics.AnalyticsScreen
import com.fakturkuid.app.ui.customer.CustomerDetailScreen
import com.fakturkuid.app.ui.customer.CustomerScreen
import com.fakturkuid.app.ui.home.HomeScreen
import com.fakturkuid.app.ui.home.ActivityListScreen
import com.fakturkuid.app.ui.invoice.CreateInvoiceScreen
import com.fakturkuid.app.ui.invoice.InvoiceDetailScreen
import com.fakturkuid.app.ui.profile.ProfileScreen
import com.fakturkuid.app.ui.profile.WhatsAppDirectScreen
import com.fakturkuid.app.ui.settings.SettingsScreen
import com.fakturkuid.app.ui.viewmodel.SettingsViewModel
import com.fakturkuid.app.ui.backup.BackupScreen
import org.koin.androidx.compose.koinViewModel

object NavRoutes {
    const val SPLASH          = "splash"
    const val HOME           = "home"
    const val PROFILE        = "profile"
    const val CREATE_INVOICE = "create_invoice"
    const val EDIT_INVOICE   = "edit_invoice/{invoiceId}"
    const val INVOICE_DETAIL = "invoice_detail/{invoiceId}"
    const val SEE_ALL        = "see_all"
    const val SETTINGS       = "settings"
    const val ANALYTICS      = "analytics"
    const val CUSTOMERS      = "customers"
    const val CUSTOMER_DETAIL = "customer_detail/{customerId}"
    const val WHATSAPP_DIRECT = "whatsapp_direct"
    const val BACKUP          = "backup"

    fun invoiceDetail(invoiceId: Long)   = "invoice_detail/$invoiceId"
    fun editInvoice(invoiceId: Long)     = "edit_invoice/$invoiceId"
    fun customerDetail(customerId: Long) = "customer_detail/$customerId"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val settingsViewModel: SettingsViewModel = koinViewModel()
    val currency by settingsViewModel.currency.collectAsState()

    NavHost(navController = navController, startDestination = NavRoutes.SPLASH) {
        composable(NavRoutes.SPLASH) {
            com.fakturkuid.app.ui.splash.SplashScreen(navController = navController)
        }

        composable(NavRoutes.HOME) {
            HomeScreen(navController = navController)
        }

        composable(NavRoutes.PROFILE) {
            ProfileScreen(navController = navController)
        }

        composable(NavRoutes.WHATSAPP_DIRECT) {
            WhatsAppDirectScreen(navController = navController)
        }

        composable(NavRoutes.CREATE_INVOICE) {
            CreateInvoiceScreen(navController = navController, invoiceId = null)
        }

        composable(
            route = NavRoutes.EDIT_INVOICE,
            arguments = listOf(navArgument("invoiceId") { type = NavType.LongType })
        ) { backStackEntry ->
            val invoiceId = backStackEntry.arguments?.getLong("invoiceId") ?: 0L
            CreateInvoiceScreen(navController = navController, invoiceId = invoiceId)
        }

        composable(NavRoutes.SEE_ALL) {
            ActivityListScreen(navController = navController)
        }

        composable(NavRoutes.SETTINGS) {
            SettingsScreen(navController = navController)
        }

        composable(NavRoutes.ANALYTICS) {
            AnalyticsScreen(navController = navController, currency = currency)
        }

        composable(NavRoutes.BACKUP) {
            BackupScreen(navController = navController)
        }

        composable(NavRoutes.CUSTOMERS) {
            CustomerScreen(navController = navController)
        }

        composable(
            route = NavRoutes.CUSTOMER_DETAIL,
            arguments = listOf(navArgument("customerId") { type = NavType.LongType })
        ) { backStackEntry ->
            val customerId = backStackEntry.arguments?.getLong("customerId") ?: 0L
            CustomerDetailScreen(navController = navController, customerId = customerId, currency = currency)
        }

        composable(
            route = NavRoutes.INVOICE_DETAIL,
            arguments = listOf(navArgument("invoiceId") { type = NavType.LongType })
        ) { backStackEntry ->
            val invoiceId = backStackEntry.arguments?.getLong("invoiceId") ?: 0L
            InvoiceDetailScreen(navController = navController, invoiceId = invoiceId)
        }
    }
}
