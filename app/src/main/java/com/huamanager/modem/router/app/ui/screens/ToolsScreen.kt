package com.huamanager.modem.router.app.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhoneForwarded
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.huamanager.modem.router.app.ui.i18n.AppLanguage
import com.huamanager.modem.router.app.ui.i18n.LocalAppStrings
import com.huamanager.modem.router.app.ui.theme.AccentPrimary
import com.huamanager.modem.router.app.ui.theme.AppThemeMode
import com.huamanager.modem.router.app.ui.theme.CardBackground
import com.huamanager.modem.router.app.ui.theme.CardBorder
import com.huamanager.modem.router.app.ui.theme.DarkBackground
import com.huamanager.modem.router.app.ui.theme.SignalExcellent
import com.huamanager.modem.router.app.ui.theme.SignalPoor
import com.huamanager.modem.router.app.ui.theme.TextMuted
import com.huamanager.modem.router.app.ui.theme.TextPrimary
import com.huamanager.modem.router.app.ui.theme.TextSecondary
import com.huamanager.modem.router.app.ui.viewmodel.HuaweiViewModel

@Composable
fun ToolsScreen(
    viewModel: HuaweiViewModel,
    modifier: Modifier = Modifier
) {
    val S = LocalAppStrings.current
    val routerIp by viewModel.routerIp.collectAsState()
    val routerUsername by viewModel.routerUsername.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val loginError by viewModel.loginError.collectAsState()
    val deviceInfo by viewModel.deviceInfo.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val ussdResponse by viewModel.ussdResponse.collectAsState()
    val savedPassword by viewModel.savedPassword.collectAsState()
    val appTheme by viewModel.appTheme.collectAsState()
    val appLanguage by viewModel.appLanguage.collectAsState()

    var inputIp by remember(routerIp) { mutableStateOf(routerIp) }
    var inputUsername by remember(routerUsername) { mutableStateOf(routerUsername) }
    var password by remember(savedPassword) { mutableStateOf(savedPassword) }
    var passwordVisible by remember { mutableStateOf(false) }
    var ussdCode by remember { mutableStateOf("*888#") }
    var showRebootConfirm by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section: Router Connection & Login
        item {
            Text(
                text = S.connectionAuthTitle,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CardBackground)
                    .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Gateway IP input
                    OutlinedTextField(
                        value = inputIp,
                        onValueChange = { inputIp = it },
                        label = { Text(S.gatewayIpLabel) },
                        leadingIcon = {
                            Icon(Icons.Default.Router, contentDescription = null, tint = AccentPrimary)
                        },
                        trailingIcon = {
                            if (inputIp != routerIp) {
                                TextButton(onClick = { viewModel.setRouterIp(inputIp) }) {
                                    Text(S.save, color = AccentPrimary, fontWeight = FontWeight.Bold)
                                }
                            }
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = AccentPrimary,
                            unfocusedBorderColor = CardBorder,
                            focusedLabelColor = AccentPrimary,
                            unfocusedLabelColor = TextMuted
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Admin Username
                    OutlinedTextField(
                        value = inputUsername,
                        onValueChange = { inputUsername = it },
                        label = { Text(S.usernameLabel) },
                        leadingIcon = {
                            Icon(Icons.Default.AccountCircle, contentDescription = null, tint = AccentPrimary)
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = AccentPrimary,
                            unfocusedBorderColor = CardBorder,
                            focusedLabelColor = AccentPrimary,
                            unfocusedLabelColor = TextMuted
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Admin Password
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text(S.passwordLabel) },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = AccentPrimary)
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (passwordVisible) S.hidePasswordDesc else S.showPasswordDesc,
                                    tint = AccentPrimary
                                )
                            }
                        },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (password.isNotBlank() && !isRefreshing) {
                                    viewModel.login(password = password.trim(), username = inputUsername.trim())
                                }
                            }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = AccentPrimary,
                            unfocusedBorderColor = CardBorder,
                            focusedLabelColor = AccentPrimary,
                            unfocusedLabelColor = TextMuted
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Error Message Display if any
                    loginError?.let { err ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(SignalPoor.copy(alpha = 0.12f))
                                .border(1.dp, SignalPoor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = SignalPoor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = err,
                                color = SignalPoor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isLoggedIn) Icons.Default.CheckCircle else Icons.Default.Info,
                                contentDescription = null,
                                tint = if (isLoggedIn) SignalExcellent else TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isLoggedIn) S.statusLoggedIn else S.statusNotLoggedIn,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isLoggedIn) SignalExcellent else TextMuted
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (isLoggedIn) {
                                Button(
                                    onClick = { viewModel.logout() },
                                    enabled = !isRefreshing,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = SignalPoor.copy(alpha = 0.15f),
                                        contentColor = SignalPoor
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, SignalPoor.copy(alpha = 0.5f))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ExitToApp,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = S.logoutBtn,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Button(
                                onClick = { viewModel.login(password = password.trim(), username = inputUsername.trim()) },
                                enabled = password.isNotBlank() && !isRefreshing,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AccentPrimary,
                                    contentColor = DarkBackground
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = if (isRefreshing) S.processing else if (isLoggedIn) S.reloginBtn else S.loginBtn,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section: Petunjuk & Tips
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkBackground)
                    .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = AccentPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = S.tipsTitle,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentPrimary
                        )
                    }
                    Text(
                        text = "${S.tip1}\n${S.tip2}\n${S.tip3}\n${S.tip4}\n${S.tip5}",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Section: Ganti Tema Aplikasi
        item {
            Text(
                text = S.themeSectionTitle,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CardBackground)
                    .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = AccentPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = S.themeSectionSubtitle,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "${S.activeThemePrefix} ${appTheme.title}",
                                fontSize = 12.sp,
                                color = AccentPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    AppThemeMode.values().forEach { mode ->
                        val isSelected = appTheme == mode
                        val previewColors = mode.toAppColors()

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) AccentPrimary.copy(alpha = 0.12f)
                                    else CardBorder.copy(alpha = 0.25f)
                                )
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) AccentPrimary else CardBorder,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { viewModel.setAppTheme(mode) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                // Theme palette preview circles
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(previewColors.background)
                                        .border(1.dp, previewColors.cardBorder, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(previewColors.accentPrimary)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = mode.title,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) AccentPrimary else TextPrimary
                                        )
                                        if (mode == AppThemeMode.LIGHT_CLEAN) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(AccentPrimary.copy(alpha = 0.2f))
                                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                                            ) {
                                                Text(
                                                    text = "Light",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = AccentPrimary
                                                )
                                            }
                                        } else if (mode == AppThemeMode.AMOLED_BLACK) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(AccentPrimary.copy(alpha = 0.2f))
                                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                                            ) {
                                                Text(
                                                    text = "OLED",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = AccentPrimary
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = mode.description,
                                        fontSize = 11.sp,
                                        color = TextSecondary,
                                        lineHeight = 15.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            RadioButton(
                                selected = isSelected,
                                onClick = { viewModel.setAppTheme(mode) },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = AccentPrimary,
                                    unselectedColor = TextMuted
                                )
                            )
                        }
                    }
                }
            }
        }

        // Section: Bahasa Aplikasi / Language
        item {
            Text(
                text = S.languageSectionTitle,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CardBackground)
                    .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = null,
                            tint = AccentPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = S.languageSectionSubtitle,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "${S.activeLanguagePrefix} ${appLanguage.title} ${appLanguage.flagEmoji}",
                                fontSize = 12.sp,
                                color = AccentPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    AppLanguage.values().forEach { lang ->
                        val isSelected = appLanguage == lang

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) AccentPrimary.copy(alpha = 0.12f)
                                    else CardBorder.copy(alpha = 0.25f)
                                )
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) AccentPrimary else CardBorder,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { viewModel.setAppLanguage(lang) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = lang.flagEmoji,
                                    fontSize = 24.sp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = lang.title,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) AccentPrimary else TextPrimary
                                    )
                                    Text(
                                        text = lang.nativeName,
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            RadioButton(
                                selected = isSelected,
                                onClick = { viewModel.setAppLanguage(lang) },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = AccentPrimary,
                                    unselectedColor = TextMuted
                                )
                            )
                        }
                    }
                }
            }
        }

        // Section: Router Actions
        item {
            Text(
                text = S.modemControlsTitle,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Reboot button
                Button(
                    onClick = { showRebootConfirm = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SignalPoor.copy(alpha = 0.15f),
                        contentColor = SignalPoor
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.RestartAlt,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(S.rebootBtn, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                // Toggle Mobile Data
                Button(
                    onClick = { viewModel.toggleMobileData(true) },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CardBackground,
                        contentColor = TextPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.SignalCellularAlt,
                        contentDescription = null,
                        tint = AccentPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(S.mobileDataBtn, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        // Section: USSD Tool
        item {
            Text(
                text = S.ussdTitle,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CardBackground)
                    .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = ussdCode,
                            onValueChange = { ussdCode = it },
                            label = { Text(S.ussdCodeLabel) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = AccentPrimary,
                                unfocusedBorderColor = CardBorder,
                                focusedLabelColor = AccentPrimary,
                                unfocusedLabelColor = TextMuted
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        Button(
                            onClick = { viewModel.sendUssd(ussdCode) },
                            enabled = ussdCode.isNotBlank() && !isRefreshing,
                            modifier = Modifier.height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AccentPrimary,
                                contentColor = DarkBackground
                            )
                        ) {
                            Icon(Icons.Default.PhoneForwarded, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(S.ussdCallBtn, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (ussdResponse != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkBackground)
                                .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = S.ussdResultTitle,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AccentPrimary
                                    )
                                    TextButton(onClick = { viewModel.clearUssd() }) {
                                        Text(S.close, fontSize = 11.sp, color = TextMuted)
                                    }
                                }
                                Text(
                                    text = ussdResponse ?: "",
                                    fontSize = 13.sp,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section: Device Information
        item {
            Text(
                text = S.deviceInfoTitle,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CardBackground)
                    .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DeviceInfoRow(S.deviceInfoModel, deviceInfo.deviceName)
                    DeviceInfoRow(S.deviceInfoFirmware, deviceInfo.softwareVersion)
                    DeviceInfoRow(S.deviceInfoWebUi, deviceInfo.webUIVersion)
                    DeviceInfoRow(S.deviceInfoHardware, deviceInfo.hardwareVersion)
                    DeviceInfoRow(S.deviceInfoImei, deviceInfo.imei)
                    DeviceInfoRow(S.deviceInfoImsi, deviceInfo.imsi)
                    DeviceInfoRow(S.deviceInfoMac, deviceInfo.macAddress1)
                }
            }
        }
    }

    if (showRebootConfirm) {
        AlertDialog(
            onDismissRequest = { showRebootConfirm = false },
            containerColor = CardBackground,
            title = { Text(S.rebootConfirmTitle, color = TextPrimary) },
            text = {
                Text(
                    S.rebootConfirmMsg,
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.rebootRouter { showRebootConfirm = false }
                    }
                ) {
                    Text(S.confirm, color = SignalPoor, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRebootConfirm = false }) {
                    Text(S.cancel, color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun DeviceInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, color = TextMuted)
        Text(
            text = value.ifEmpty { "-" },
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
        )
    }
}
