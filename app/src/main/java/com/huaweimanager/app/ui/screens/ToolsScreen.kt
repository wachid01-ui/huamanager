package com.huaweimanager.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneForwarded
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.huaweimanager.app.ui.theme.AccentPrimary
import com.huaweimanager.app.ui.theme.CardBackground
import com.huaweimanager.app.ui.theme.CardBorder
import com.huaweimanager.app.ui.theme.DarkBackground
import com.huaweimanager.app.ui.theme.SignalExcellent
import com.huaweimanager.app.ui.theme.SignalPoor
import com.huaweimanager.app.ui.theme.TextMuted
import com.huaweimanager.app.ui.theme.TextPrimary
import com.huaweimanager.app.ui.theme.TextSecondary
import com.huaweimanager.app.ui.viewmodel.HuaweiViewModel

@Composable
fun ToolsScreen(
    viewModel: HuaweiViewModel,
    modifier: Modifier = Modifier
) {
    val routerIp by viewModel.routerIp.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val deviceInfo by viewModel.deviceInfo.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val ussdResponse by viewModel.ussdResponse.collectAsState()

    var inputIp by remember(routerIp) { mutableStateOf(routerIp) }
    var password by remember { mutableStateOf("") }
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
                text = "Koneksi & Autentikasi Modem",
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
                        label = { Text("IP Gateway Modem (Default: 192.168.8.1)") },
                        leadingIcon = {
                            Icon(Icons.Default.Router, contentDescription = null, tint = AccentPrimary)
                        },
                        trailingIcon = {
                            if (inputIp != routerIp) {
                                TextButton(onClick = { viewModel.setRouterIp(inputIp) }) {
                                    Text("Simpan", color = AccentPrimary, fontWeight = FontWeight.Bold)
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

                    // Admin Password
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password Admin WebUI Modem") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = AccentPrimary)
                        },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
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

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isLoggedIn) "Status: Sudah Login" else "Status: Belum Login",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isLoggedIn) SignalExcellent else TextMuted
                        )

                        Button(
                            onClick = { viewModel.login(password) {} },
                            enabled = password.isNotBlank() && !isRefreshing,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AccentPrimary,
                                contentColor = DarkBackground
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Login", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Section: Router Actions
        item {
            Text(
                text = "Kontrol Modem",
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
                    Text("Reboot Modem", fontWeight = FontWeight.Bold, fontSize = 13.sp)
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
                    Text("Aktifkan Data", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        // Section: USSD Tool
        item {
            Text(
                text = "Dial USSD (Cek Pulsa / Kuota)",
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
                            label = { Text("Kode USSD (contoh: *888# atau *363#)") },
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
                            Text("Panggil", fontWeight = FontWeight.Bold)
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
                                        text = "Hasil USSD:",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AccentPrimary
                                    )
                                    TextButton(onClick = { viewModel.clearUssd() }) {
                                        Text("Tutup", fontSize = 11.sp, color = TextMuted)
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
                text = "Informasi Perangkat",
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
                    DeviceInfoRow("Tipe Modem", deviceInfo.deviceName)
                    DeviceInfoRow("Versi Firmware", deviceInfo.softwareVersion)
                    DeviceInfoRow("Versi WebUI", deviceInfo.webUIVersion)
                    DeviceInfoRow("Versi Hardware", deviceInfo.hardwareVersion)
                    DeviceInfoRow("IMEI", deviceInfo.imei)
                    DeviceInfoRow("IMSI", deviceInfo.imsi)
                    DeviceInfoRow("MAC Address", deviceInfo.macAddress1)
                }
            }
        }
    }

    if (showRebootConfirm) {
        AlertDialog(
            onDismissRequest = { showRebootConfirm = false },
            containerColor = CardBackground,
            title = { Text("Reboot Modem Huawei?", color = TextPrimary) },
            text = {
                Text(
                    "Modem akan dimatikan dan dinyalakan ulang. Koneksi internet WiFi akan terputus sementara selama 1-2 menit.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.rebootRouter { showRebootConfirm = false }
                    }
                ) {
                    Text("Reboot Sekarang", color = SignalPoor, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRebootConfirm = false }) {
                    Text("Batal", color = TextSecondary)
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
