package com.fakturkuid.app.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.clickable
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.fakturkuid.app.MainActivity
import com.fakturkuid.app.R
import com.fakturkuid.app.data.repository.InvoiceRepository
import com.fakturkuid.app.data.repository.SettingsRepository
import com.fakturkuid.app.utils.CurrencyFormatter
import org.koin.java.KoinJavaComponent.getKoin
import java.util.*

class FakturkuWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        
        // 1. Fetch Real Data from Repositories (Koin)
        val invoiceRepo = getKoin().get<InvoiceRepository>()
        val settingsRepo = getKoin().get<SettingsRepository>()
        
        val language = settingsRepo.getLanguage()
        val currency = settingsRepo.getCurrency()
        
        // Date Range for Today
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val startOfDay = calendar.timeInMillis
        
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val endOfDay = calendar.timeInMillis
        
        val todayIncome = invoiceRepo.getIncomeSync(startOfDay, endOfDay) ?: 0.0
        val paidCount = invoiceRepo.getInvoiceCountByStatusSync("paid")
        val unpaidCount = invoiceRepo.getInvoiceCountByStatusSync("unpaid")
        
        // Localized Context for Widget Strings
        val locale = Locale(language)
        val config = android.content.res.Configuration(context.resources.configuration)
        config.setLocale(locale)
        val localizedContext = context.createConfigurationContext(config)

        // Formatted Values
        val formattedIncome = CurrencyFormatter.formatWithRate(todayIncome, currency)
        
        provideContent {
            GlanceTheme {
                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(Color(0xFF0F172A)) // Dark slate modern background
                        .padding(16.dp)
                        .clickable(actionStartActivity<MainActivity>())
                ) {
                    Column(modifier = GlanceModifier.fillMaxSize()) {
                        
                        // Header
                        Row(
                            modifier = GlanceModifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                localizedContext.getString(R.string.widget_title),
                                style = TextStyle(
                                    color = androidx.glance.unit.ColorProvider(Color(0xFF00D1FF)),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            )
                        }
                        
                        Spacer(GlanceModifier.height(16.dp))
                        
                        // Content
                        Text(
                            localizedContext.getString(R.string.widget_today_income),
                            style = TextStyle(
                                color = androidx.glance.unit.ColorProvider(Color.Gray),
                                fontSize = 10.sp
                            )
                        )
                        Spacer(GlanceModifier.height(4.dp))
                        Text(
                            formattedIncome,
                            style = TextStyle(
                                color = androidx.glance.unit.ColorProvider(Color.White),
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        )
                        
                        Spacer(GlanceModifier.height(16.dp))
                        
                        Row(modifier = GlanceModifier.fillMaxWidth()) {
                            Column(modifier = GlanceModifier.defaultWeight()) {
                                Text(
                                    localizedContext.getString(R.string.widget_paid), 
                                    style = TextStyle(color = androidx.glance.unit.ColorProvider(Color(0xFF2DD4BF)), fontSize = 10.sp)
                                )
                                Text(
                                    localizedContext.getString(R.string.widget_faktur_count, paidCount), 
                                    style = TextStyle(color = androidx.glance.unit.ColorProvider(Color.White), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                )
                            }
                            Column(modifier = GlanceModifier.defaultWeight()) {
                                Text(
                                    localizedContext.getString(R.string.widget_unpaid), 
                                    style = TextStyle(color = androidx.glance.unit.ColorProvider(Color(0xFFFBBF24)), fontSize = 10.sp)
                                )
                                Text(
                                    localizedContext.getString(R.string.widget_faktur_count, unpaidCount), 
                                    style = TextStyle(color = androidx.glance.unit.ColorProvider(Color.White), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
