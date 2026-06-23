package com.example.zio_ecommercd.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zio_ecommercd.ui.theme.BrandPrimary
import com.example.zio_ecommercd.ui.theme.BrandSecondary
import com.example.zio_ecommercd.ui.theme.ErrorRed

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onPrivacyPolicyClick: () -> Unit = {},
    onTermsClick: () -> Unit = {},
    onHelpClick: () -> Unit = {},
    isDarkMode: Boolean = false,
    onDarkModeToggle: (Boolean) -> Unit = {},
    onChangePassword: (String, (Result<Unit>) -> Unit) -> Unit = { _, callback -> callback(Result.failure(Exception("Not implemented"))) },
    notificationsEnabled: Boolean = true,
    onNotificationsToggle: (Boolean) -> Unit = {}
) {
    var showPasswordDialog by remember { mutableStateOf(false) }
    var passwordResult by remember { mutableStateOf<String?>(null) }
    var passwordSuccess by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().background(androidx.compose.material3.MaterialTheme.colorScheme.background)) {
        // Premium Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(androidx.compose.material3.MaterialTheme.colorScheme.secondary)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(androidx.compose.material3.MaterialTheme.colorScheme.surface.copy(alpha = 0.1f)).clickable(onClick = onBack),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = androidx.compose.material3.MaterialTheme.colorScheme.onSecondary)
                }
                Text(text = "Settings", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = androidx.compose.material3.MaterialTheme.colorScheme.onSecondary)
                Spacer(modifier = Modifier.width(40.dp))
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = "PREFERENCES", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp, modifier = Modifier.padding(start = 4.dp))

            SettingsToggleItem(icon = Icons.Default.Notifications, title = "Notifications", subtitle = "Receive push notifications", checked = notificationsEnabled, onCheckedChange = onNotificationsToggle)
            SettingsToggleItem(icon = Icons.Default.DarkMode, title = "Dark Mode", subtitle = if (isDarkMode) "Currently using dark theme" else "Currently using light theme", checked = isDarkMode, onCheckedChange = onDarkModeToggle)

            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "ACCOUNT", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp, modifier = Modifier.padding(start = 4.dp))
            SettingsClickItem(icon = Icons.Default.Lock, title = "Change Password", subtitle = "Update your account password", onClick = { showPasswordDialog = true })

            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "ABOUT", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp, modifier = Modifier.padding(start = 4.dp))
            SettingsClickItem(icon = Icons.Default.PrivacyTip, title = "Privacy Policy", subtitle = "Read our privacy policy", onClick = onPrivacyPolicyClick)
            SettingsClickItem(icon = Icons.Default.Shield, title = "Terms of Service", subtitle = "Read terms and conditions", onClick = onTermsClick)
            SettingsClickItem(icon = Icons.AutoMirrored.Filled.HelpOutline, title = "Help & Support", subtitle = "Get help with the app", onClick = onHelpClick)
            SettingsClickItem(icon = Icons.Default.Info, title = "App Info", subtitle = "Version 1.0.0", onClick = {})
            
            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    if (showPasswordDialog) {
        ChangePasswordDialog(
            onDismiss = { showPasswordDialog = false; passwordResult = null; passwordSuccess = false },
            onSubmit = { newPassword -> onChangePassword(newPassword) { result -> if (result.isSuccess) { passwordSuccess = true; passwordResult = "Password changed successfully!" } else { passwordResult = result.exceptionOrNull()?.message ?: "Failed to change password. Please re-login and try again." } } },
            resultMessage = passwordResult,
            isSuccess = passwordSuccess
        )
    }
}

@Composable
private fun ChangePasswordDialog(onDismiss: () -> Unit, onSubmit: (String) -> Unit, resultMessage: String?, isSuccess: Boolean) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showCurrentPassword by remember { mutableStateOf(false) }
    var showNewPassword by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = { Text(text = "Change Password", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = androidx.compose.material3.MaterialTheme.colorScheme.secondary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (isSuccess && resultMessage != null) { Text(text = resultMessage, color = BrandPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold) } 
                else {
                    OutlinedTextField(value = currentPassword, onValueChange = { currentPassword = it; validationError = null }, label = { Text("Current Password") }, singleLine = true, visualTransformation = if (showCurrentPassword) VisualTransformation.None else PasswordVisualTransformation(), trailingIcon = { IconButton(onClick = { showCurrentPassword = !showCurrentPassword }) { Icon(imageVector = if (showCurrentPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(20.dp)) } }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandPrimary, cursorColor = BrandPrimary), modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = newPassword, onValueChange = { newPassword = it; validationError = null }, label = { Text("New Password") }, singleLine = true, visualTransformation = if (showNewPassword) VisualTransformation.None else PasswordVisualTransformation(), trailingIcon = { IconButton(onClick = { showNewPassword = !showNewPassword }) { Icon(imageVector = if (showNewPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(20.dp)) } }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandPrimary, cursorColor = BrandPrimary), modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = confirmPassword, onValueChange = { confirmPassword = it; validationError = null }, label = { Text("Confirm New Password") }, singleLine = true, visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandPrimary, cursorColor = BrandPrimary), modifier = Modifier.fillMaxWidth())
                    if (validationError != null) { Text(text = validationError!!, color = ErrorRed, fontSize = 12.sp) }
                    if (resultMessage != null && !isSuccess) { Text(text = resultMessage, color = ErrorRed, fontSize = 12.sp) }
                }
            }
        },
        confirmButton = {
            if (isSuccess) { TextButton(onClick = onDismiss) { Text("Done", color = BrandPrimary, fontWeight = FontWeight.Bold) } } 
            else {
                TextButton(onClick = { when { currentPassword.isEmpty() -> validationError = "Enter current password"; newPassword.length < 6 -> validationError = "Password must be at least 6 characters"; newPassword != confirmPassword -> validationError = "Passwords don't match"; else -> onSubmit(newPassword) } }) { Text("Update", color = BrandPrimary, fontWeight = FontWeight.Bold) }
            }
        },
        dismissButton = { if (!isSuccess) { TextButton(onClick = onDismiss) { Text("Cancel", color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant) } } }
    )
}

@Composable
private fun SettingsToggleItem(icon: ImageVector, title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(androidx.compose.material3.MaterialTheme.colorScheme.surface)
            .clickable { onCheckedChange(!checked) }
            .padding(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(androidx.compose.material3.MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
                Icon(imageVector = icon, contentDescription = null, tint = androidx.compose.material3.MaterialTheme.colorScheme.secondary, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface)
                Text(text = subtitle, fontSize = 12.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = BrandPrimary, uncheckedThumbColor = Color.LightGray, uncheckedTrackColor = Color.LightGray.copy(alpha = 0.5f)))
        }
    }
}

@Composable
private fun SettingsClickItem(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(androidx.compose.material3.MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(androidx.compose.material3.MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
                Icon(imageVector = icon, contentDescription = null, tint = androidx.compose.material3.MaterialTheme.colorScheme.secondary, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface)
                Text(text = subtitle, fontSize = 12.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
        }
    }
}




