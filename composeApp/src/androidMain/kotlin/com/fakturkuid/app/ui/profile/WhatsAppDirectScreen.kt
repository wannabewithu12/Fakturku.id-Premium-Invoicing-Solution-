package com.fakturkuid.app.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBackIos
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.fakturkuid.app.R
import com.fakturkuid.app.ui.viewmodel.BusinessProfileViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhatsAppDirectScreen(
    navController: NavController,
    viewModel: BusinessProfileViewModel = koinViewModel()
) {
    val profile by viewModel.profile.collectAsState()

    var paidTemplate by remember(profile) { mutableStateOf(profile?.waTemplatePaid ?: "") }
    var unpaidTemplate by remember(profile) { mutableStateOf(profile?.waTemplateUnpaid ?: "") }
    var overdueTemplate by remember(profile) { mutableStateOf(profile?.waTemplateOverdue ?: "") }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.whatsapp_direct_title), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, letterSpacing = 2.sp) },
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
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                stringResource(R.string.wa_template_hint),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            TemplateField(
                label = stringResource(R.string.wa_template_unpaid),
                value = unpaidTemplate,
                onValueChange = { unpaidTemplate = it }
            )

            TemplateField(
                label = stringResource(R.string.wa_template_paid),
                value = paidTemplate,
                onValueChange = { paidTemplate = it }
            )

            TemplateField(
                label = stringResource(R.string.wa_template_overdue),
                value = overdueTemplate,
                onValueChange = { overdueTemplate = it }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    viewModel.saveWaTemplates(paidTemplate, unpaidTemplate, overdueTemplate)
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
            ) {
                Icon(Icons.Rounded.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(12.dp))
                Text(stringResource(R.string.wa_template_save), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun TemplateField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            minLines = 3,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                focusedBorderColor = Color(0xFF25D366).copy(alpha = 0.5f)
            )
        )
    }
}
