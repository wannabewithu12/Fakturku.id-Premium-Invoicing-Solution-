package com.fakturkuid.app.ui.profile

import com.fakturkuid.app.R

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.fakturkuid.app.ui.NavRoutes
import com.fakturkuid.app.ui.viewmodel.BusinessProfileViewModel
import com.fakturkuid.app.utils.FileUtil
import org.koin.androidx.compose.koinViewModel
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.scale
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: BusinessProfileViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val profile by viewModel.profile.collectAsState()

    var name by remember(profile) { mutableStateOf(profile?.businessName ?: "") }
    var address by remember(profile) { mutableStateOf(profile?.address ?: "") }
    var phone by remember(profile) { mutableStateOf(profile?.phone ?: "") }
    var email by remember(profile) { mutableStateOf(profile?.email ?: "") }
    var footer by remember(profile) { mutableStateOf(profile?.defaultFooter ?: "") }
    var logoPath by remember(profile) { mutableStateOf(profile?.logoUri ?: "") }
    var logoBlob by remember(profile) { mutableStateOf<ByteArray?>(profile?.logoBlob) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val bytes = FileUtil.getBytesFromUri(context, it)
            if (bytes != null) {
                logoBlob = bytes
                val fileName = "logo_${System.currentTimeMillis()}.png"
                logoPath = FileUtil.saveBytesToInternalStorage(context, bytes, fileName) ?: ""
            }
        }
    }

    Scaffold(
        containerColor = Color(0xFF0F172A),
        topBar = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBackIos, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Text(
                    stringResource(R.string.business_profile).uppercase(),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp,
                    color = Color.White,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.width(48.dp))
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            // Logo Section
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    shape = RoundedCornerShape(32.dp),
                    color = Color.White.copy(alpha = 0.05f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                    modifier = Modifier.size(140.dp)
                ) {
                    if (logoBlob?.isNotEmpty() == true) {
                        AsyncImage(
                            model = logoBlob,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else if (logoPath.isNotEmpty()) {
                        AsyncImage(
                            model = File(logoPath),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Rounded.AddAPhoto, contentDescription = null, tint = Color(0xFF00D1FF), modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(stringResource(R.string.add_logo), style = MaterialTheme.typography.labelSmall, color = Color(0xFF00D1FF), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Form Section
            ProfileTextField(
                label = stringResource(R.string.business_name),
                placeholder = stringResource(R.string.business_name_hint),
                value = name,
                onValueChange = { name = it },
                icon = Icons.Rounded.Business
            )
            ProfileTextField(
                label = stringResource(R.string.address),
                placeholder = stringResource(R.string.full_address_hint),
                value = address,
                onValueChange = { address = it },
                icon = Icons.Rounded.LocationOn
            )
            ProfileTextField(
                label = stringResource(R.string.phone_number),
                placeholder = stringResource(R.string.phone_whatsapp_hint),
                value = phone,
                onValueChange = { phone = it },
                icon = Icons.Rounded.Phone
            )
            ProfileTextField(
                label = stringResource(R.string.email),
                placeholder = stringResource(R.string.business_email_hint),
                value = email,
                onValueChange = { email = it },
                icon = Icons.Rounded.Email
            )
            ProfileTextField(
                label = stringResource(R.string.invoice_footer),
                placeholder = stringResource(R.string.invoice_footer_hint),
                value = footer,
                onValueChange = { footer = it },
                icon = Icons.Rounded.EditNote,
                singleLine = false
            )

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = {
                    viewModel.saveProfile(name, address, phone, email, logoPath, logoBlob, footer)
                    navController.popBackStack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0085FF))
            ) {
                Text(stringResource(R.string.save_profile), fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
            }
            
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

@Composable
fun ProfileTextField(
    label: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    singleLine: Boolean = true
) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.Gray, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(icon, contentDescription = null, tint = Color(0xFF00D1FF).copy(alpha = 0.6f), modifier = Modifier.size(20.dp)) },
            placeholder = { Text(placeholder, color = Color.White.copy(alpha = 0.3f)) },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF00D1FF),
                unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                focusedContainerColor = Color.White.copy(alpha = 0.05f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            singleLine = singleLine
        )
    }
}
