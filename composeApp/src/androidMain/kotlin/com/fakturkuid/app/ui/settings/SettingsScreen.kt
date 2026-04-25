package com.fakturkuid.app.ui.settings

import com.fakturkuid.app.R
import androidx.compose.ui.res.stringResource

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.fakturkuid.app.data.entity.BusinessProfile
import com.fakturkuid.app.ui.NavRoutes
import com.fakturkuid.app.ui.viewmodel.SettingsViewModel
import com.fakturkuid.app.utils.PdfGenerator
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.io.File
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController
) {
    val activityContext = androidx.compose.ui.platform.LocalContext.current as androidx.activity.ComponentActivity
    val viewModel: SettingsViewModel = koinViewModel(viewModelStoreOwner = activityContext)
    
    val profile by viewModel.profile.collectAsState(initial = null)
    val theme by viewModel.theme.collectAsState()
    val language by viewModel.language.collectAsState()
    val currency by viewModel.currency.collectAsState()
    val isAppLockEnabled by viewModel.isAppLockEnabled.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    fun showSnackbar(message: String) {
        scope.launch {
            snackbarHostState.showSnackbar(message, duration = SnackbarDuration.Short)
        }
    }
    
    var reportTypeToExport by remember { mutableStateOf("") }

    val pdfExportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
        if (uri != null) {
            scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                try {
                    val invoices = viewModel.getInvoicesForReport(0, System.currentTimeMillis())
                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        if (reportTypeToExport == "daily") {
                            PdfGenerator.generateDailyReportPdf(context, outputStream, invoices, profile, currency)
                        } else if (reportTypeToExport == "monthly") {
                            PdfGenerator.generateMonthlySummaryPdf(context, outputStream, invoices, profile, currency)
                        }
                    }
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        showSnackbar(context.getString(R.string.msg_report_pdf_success))
                    }
                } catch (e: Exception) {
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        Toast.makeText(context, context.getString(R.string.msg_report_pdf_failed, e.message), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }





    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.settings_title), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, letterSpacing = 2.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBackIos, contentDescription = stringResource(R.string.back), modifier = Modifier.size(20.dp))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            // --- 1. Identitas Instansi (Top Level) ---
            SettingsSectionHeader(title = stringResource(R.string.company_identity))
            IdentityPremiumCard(
                profile = profile,
                onClick = { navController.navigate(NavRoutes.PROFILE) }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // --- 2. Manajemen Data ---
            SettingsSectionHeader(title = stringResource(R.string.data_management))
            SettingsCard {

                SettingsActionItem(
                    title = stringResource(R.string.cloud_backup_title),
                    subtitle = stringResource(R.string.cloud_backup_desc),
                    icon = Icons.Rounded.CloudSync,
                    iconColor = Color(0xFF00D1FF),
                    onClick = { navController.navigate(NavRoutes.BACKUP) }
                )
                SettingsActionItem(
                    title = stringResource(R.string.whatsapp_direct_menu),
                    subtitle = stringResource(R.string.whatsapp_direct_menu_desc),
                    icon = Icons.Rounded.Message,
                    iconColor = Color(0xFF25D366),
                    onClick = { navController.navigate(NavRoutes.WHATSAPP_DIRECT) }
                )
                SettingsActionItem(
                    title = stringResource(R.string.clear_cache),
                    subtitle = stringResource(R.string.clear_cache_desc),
                    icon = Icons.Rounded.AutoDelete,
                    iconColor = Color(0xFFFB7185),
                    onClick = { 
                        viewModel.clearAppData(context)
                        showSnackbar(context.getString(R.string.msg_optimize_success))
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- 3. Laporan Keuangan ---
            SettingsSectionHeader(title = stringResource(R.string.financial_reports_pdf))
            SettingsCard {
                SettingsActionItem(
                    title = stringResource(R.string.print_daily_detail),
                    subtitle = stringResource(R.string.print_daily_detail_desc),
                    icon = Icons.AutoMirrored.Rounded.ReceiptLong,
                    onClick = {
                        reportTypeToExport = "daily"
                        pdfExportLauncher.launch("Detail_Harian_${System.currentTimeMillis()}.pdf")
                    }
                )
                SettingsActionItem(
                    title = stringResource(R.string.print_monthly_summary),
                    subtitle = stringResource(R.string.print_monthly_summary_desc),
                    icon = Icons.Rounded.Summarize,
                    onClick = {
                        reportTypeToExport = "monthly"
                        pdfExportLauncher.launch("Ringkasan_Bulanan_${System.currentTimeMillis()}.pdf")
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- 4. Sistem & Personalisasi ---
            SettingsSectionHeader(title = stringResource(R.string.system_and_personalization))
            SettingsCard {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFF00D1FF).copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.DarkMode, contentDescription = null, tint = Color(0xFF00D1FF), modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(18.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.dark_mode), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(if (theme == "dark") stringResource(R.string.active) else stringResource(R.string.inactive), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = theme == "dark",
                        onCheckedChange = { viewModel.setTheme(if (it) "dark" else "light") },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF00D1FF))
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFF2DD4BF).copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.Security, contentDescription = null, tint = Color(0xFF2DD4BF), modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(18.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.database_encryption), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(stringResource(R.string.active_via_keystore), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = Color(0xFF2DD4BF), modifier = Modifier.size(24.dp))
                }

                HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFF00D1FF).copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.Fingerprint, contentDescription = null, tint = Color(0xFF00D1FF), modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(18.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.app_lock), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(stringResource(R.string.app_lock_desc), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = isAppLockEnabled,
                        onCheckedChange = { viewModel.setAppLockEnabled(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF00D1FF))
                    )
                }
                
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFF00D1FF).copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.Translate, contentDescription = null, tint = Color(0xFF00D1FF), modifier = Modifier.size(22.dp))
                        }
                        Spacer(modifier = Modifier.width(18.dp))
                        Text(stringResource(R.string.language_selection), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        LanguageBadge("ID", stringResource(R.string.lang_id), active = language == "id") { viewModel.setLanguage("id") }
                        LanguageBadge("EN", stringResource(R.string.lang_en), active = language == "en") { viewModel.setLanguage("en") }
                        LanguageBadge("JA", stringResource(R.string.lang_ja), active = language == "ja") { viewModel.setLanguage("ja") }
                        LanguageBadge("ZH", stringResource(R.string.lang_zh), active = language == "zh") { viewModel.setLanguage("zh") }
                    }
                }
                
                HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFF00D1FF).copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.AttachMoney, contentDescription = null, tint = Color(0xFF00D1FF), modifier = Modifier.size(22.dp))
                        }
                        Spacer(modifier = Modifier.width(18.dp))
                        Text(stringResource(R.string.currency_selection), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        LanguageBadge("Rp", "IDR", active = currency == "id") { viewModel.setCurrency("id") }
                        LanguageBadge("$", "USD", active = currency == "en") { viewModel.setCurrency("en") }
                        LanguageBadge("¥", "JPY", active = currency == "ja") { viewModel.setCurrency("ja") }
                        LanguageBadge("¥", "CNY", active = currency == "zh") { viewModel.setCurrency("zh") }
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
            Text(
                stringResource(R.string.app_version_info),
                modifier = Modifier.align(Alignment.CenterHorizontally),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun IdentityPremiumCard(profile: BusinessProfile?, onClick: () -> Unit) {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (isDark) 0.05f else 0.03f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF00D1FF).copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                if (profile?.logoUri?.isNotEmpty() == true) {
                    AsyncImage(
                        model = File(profile.logoUri),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Rounded.Business, contentDescription = null, tint = Color(0xFF00D1FF), modifier = Modifier.size(32.dp))
                }
            }
            Spacer(modifier = Modifier.width(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    profile?.businessName ?: stringResource(R.string.default_company_name),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    stringResource(R.string.business_identity_logo),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (androidx.compose.foundation.isSystemInDarkTheme()) Color(0xFF00D1FF) else Color(0xFF0EA5E9)
                )
            }
            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
        }
    }
}

@Composable
fun LanguageBadge(label: String, name: String, active: Boolean, onClick: () -> Unit) {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(if (active) Color(0xFF0085FF) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)),
            contentAlignment = Alignment.Center
        ) {
            Text(label, fontWeight = FontWeight.ExtraBold, color = if (active) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 14.sp)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(name, style = MaterialTheme.typography.labelSmall, color = if (active) (if (isDark) Color.White else Color(0xFF0085FF)) else Color.Gray)
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = if (androidx.compose.foundation.isSystemInDarkTheme()) Color(0xFF00D1FF) else Color(0xFF0EA5E9),
        letterSpacing = 1.5.sp,
        modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
    )
}

@Composable
fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (isDark) 0.05f else 0.02f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.fillMaxWidth(), content = content)
    }
}

@Composable
fun SettingsActionItem(title: String, subtitle: String, icon: ImageVector, iconColor: Color = Color(0xFF00D1FF), onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(iconColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.width(18.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
        Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = Color.Gray.copy(alpha = 0.3f), modifier = Modifier.size(18.dp))
    }
}

@Composable
fun ReportButton(title: String, subtitle: String, color: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier.height(110.dp).clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.08f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Rounded.Assessment, contentDescription = null, tint = color)
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = color.copy(alpha = 0.8f))
        }
    }
}
