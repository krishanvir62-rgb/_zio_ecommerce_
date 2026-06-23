package com.example.zio_ecommercd.ui.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.zio_ecommercd.data.model.User
import com.example.zio_ecommercd.ui.theme.BrandAccent
import com.example.zio_ecommercd.ui.theme.BrandPrimary
import com.example.zio_ecommercd.ui.theme.ErrorRed

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    user: User?,
    onLogout: () -> Unit,
    onSettingsClick: () -> Unit = {},
    onOrdersClick: () -> Unit = {},
    onMyListingsClick: () -> Unit = {},
    onPhotoUpload: (Uri) -> Unit = {},
    onUpdatePhone: (String, (Result<Unit>) -> Unit) -> Unit = { _, cb -> cb(Result.failure(Exception("Not implemented"))) },
    isUploadingPhoto: Boolean = false,
    contentPadding: PaddingValues = PaddingValues()
) {
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var showPhoneDialog by remember { mutableStateOf(false) }
    var phoneResult by remember { mutableStateOf<String?>(null) }
    var phoneSuccess by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            onPhotoUpload(it)
        }
    }

    androidx.compose.material3.Scaffold(
        topBar = {
            androidx.compose.material3.TopAppBar(
                title = { Text("My Profile", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = androidx.compose.material3.MaterialTheme.colorScheme.onSecondary) },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = androidx.compose.material3.MaterialTheme.colorScheme.onSecondary)
                    }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.material3.MaterialTheme.colorScheme.secondary
                )
            )
        },
        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize().padding(contentPadding)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Profile Card (Starts immediately below header)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .shadow(4.dp, RoundedCornerShape(24.dp))
                    .clip(RoundedCornerShape(24.dp))
                    .background(androidx.compose.material3.MaterialTheme.colorScheme.surface)
                    .padding(vertical = 32.dp, horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Avatar with upload
                    Box(contentAlignment = Alignment.BottomEnd) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .border(3.dp, BrandPrimary, CircleShape)
                                .background(androidx.compose.material3.MaterialTheme.colorScheme.surface),
                            contentAlignment = Alignment.Center
                        ) {
                            val photoUrl = user?.photoUrl?.takeIf { it.isNotEmpty() }
                            if (photoUrl != null && !isUploadingPhoto) {
                                SubcomposeAsyncImage(
                                    model = photoUrl, contentDescription = "Profile photo",
                                    contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()
                                )
                            } else if (isUploadingPhoto) {
                                CircularProgressIndicator(color = BrandPrimary, modifier = Modifier.size(36.dp), strokeWidth = 3.dp)
                            } else {
                                Icon(Icons.Default.Person, contentDescription = "Profile", tint = Color.Gray, modifier = Modifier.size(44.dp))
                            }
                        }
                        // Camera Edit Button
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .shadow(4.dp, CircleShape)
                                .clip(CircleShape)
                                .background(androidx.compose.material3.MaterialTheme.colorScheme.surface)
                                .border(1.dp, Color.LightGray, CircleShape)
                                .clickable { imagePickerLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = "Upload photo", tint = BrandPrimary, modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(text = user?.name ?: "User", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = user?.email ?: "user@example.com", fontSize = 14.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

        // ── Options List ──
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ProfileInfoCard(icon = Icons.Default.Email, label = "Email Address", value = user?.email ?: "Not set")
            ProfileInfoCard(icon = Icons.Default.Phone, label = "Phone Number", value = user?.phone?.ifEmpty { "Not set" } ?: "Not set")

            ProfileActionCard(icon = Icons.Default.Edit, label = "Edit Phone Number", subtitle = "Update your contact number", onClick = { showPhoneDialog = true; phoneResult = null; phoneSuccess = false })
            ProfileActionCard(icon = Icons.Default.Receipt, label = "My Orders", subtitle = "View order history & status", onClick = onOrdersClick)
            ProfileActionCard(icon = Icons.Default.Store, label = "My Listings", subtitle = "Manage your products", onClick = onMyListingsClick)
            ProfileActionCard(icon = Icons.Default.Settings, label = "App Settings", subtitle = "Preferences, privacy & more", onClick = onSettingsClick)

            Spacer(modifier = Modifier.height(30.dp))

            // Premium Logout Button
            Button(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(2.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface)
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text("Logout Account", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = ErrorRed)
            }
            Spacer(modifier = Modifier.height(100.dp)) // Padding for bottom nav
        }
    }
    }

    if (showPhoneDialog) {
        EditPhoneDialog(
            currentPhone = user?.phone ?: "",
            onDismiss = { showPhoneDialog = false; phoneResult = null; phoneSuccess = false },
            onSubmit = { newPhone ->
                onUpdatePhone(newPhone) { result ->
                    if (result.isSuccess) { phoneSuccess = true; phoneResult = "Phone updated successfully!" } 
                    else { phoneResult = result.exceptionOrNull()?.message ?: "Failed to update phone" }
                }
            },
            resultMessage = phoneResult,
            isSuccess = phoneSuccess
        )
    }
}

@Composable
private fun ProfileInfoCard(icon: ImageVector, label: String, value: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(androidx.compose.material3.MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(androidx.compose.material3.MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = androidx.compose.material3.MaterialTheme.colorScheme.secondary, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = label, fontSize = 12.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
private fun ProfileActionCard(icon: ImageVector, label: String, subtitle: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(androidx.compose.material3.MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(androidx.compose.material3.MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = androidx.compose.material3.MaterialTheme.colorScheme.secondary, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = label, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface)
                Text(text = subtitle, fontSize = 12.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun EditPhoneDialog(currentPhone: String, onDismiss: () -> Unit, onSubmit: (String) -> Unit, resultMessage: String?, isSuccess: Boolean) {
    var phone by remember { mutableStateOf(currentPhone) }
    var validationError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = { Text(text = "Update Phone Number", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = androidx.compose.material3.MaterialTheme.colorScheme.secondary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (isSuccess && resultMessage != null) {
                    Text(text = resultMessage, color = BrandPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                } else {
                    OutlinedTextField(
                        value = phone, onValueChange = { phone = it; validationError = null },
                        label = { Text("Phone Number") }, singleLine = true, shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandPrimary, cursorColor = BrandPrimary),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (validationError != null) { Text(text = validationError!!, color = ErrorRed, fontSize = 12.sp) }
                    if (resultMessage != null && !isSuccess) { Text(text = resultMessage, color = ErrorRed, fontSize = 12.sp) }
                }
            }
        },
        confirmButton = {
            if (isSuccess) { TextButton(onClick = onDismiss) { Text("Done", color = BrandPrimary, fontWeight = FontWeight.Bold) } } 
            else {
                TextButton(onClick = {
                    when { phone.isBlank() -> validationError = "Phone number cannot be empty"; phone.length < 10 -> validationError = "Enter a valid phone number"; else -> onSubmit(phone) }
                }) { Text("Update", color = BrandPrimary, fontWeight = FontWeight.Bold) }
            }
        },
        dismissButton = { if (!isSuccess) { TextButton(onClick = onDismiss) { Text("Cancel", color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant) } } }
    )
}





