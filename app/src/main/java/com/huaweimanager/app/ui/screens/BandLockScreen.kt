package com.huaweimanager.app.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.huaweimanager.app.data.model.LteBand
import com.huaweimanager.app.ui.theme.AccentPrimary
import com.huaweimanager.app.ui.theme.CardBackground
import com.huaweimanager.app.ui.theme.CardBorder
import com.huaweimanager.app.ui.theme.DarkBackground
import com.huaweimanager.app.ui.theme.SignalExcellent
import com.huaweimanager.app.ui.theme.SignalGood
import androidx.compose.material.icons.filled.Warning
import com.huaweimanager.app.ui.theme.SignalPoor
import com.huaweimanager.app.ui.theme.TextMuted
import com.huaweimanager.app.ui.theme.TextPrimary
import com.huaweimanager.app.ui.theme.TextSecondary
import com.huaweimanager.app.ui.viewmodel.HuaweiViewModel

@Composable
fun BandLockScreen(
    viewModel: HuaweiViewModel,
    modifier: Modifier = Modifier
) {
    val signal by viewModel.signalInfo.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    val individualBands = remember { LteBand.ALL_BANDS }
    val selectedBands = remember { mutableStateListOf<LteBand>() }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Active Band Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CardBackground)
                    .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Band Aktif Saat Ini",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (signal.band.isNotEmpty() && signal.band != "-") "Band ${signal.band} (${signal.dlFrequency})" else "Otomatis / Belum terdeteksi",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentPrimary
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = AccentPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Warning banner if not logged in
        if (!isLoggedIn) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SignalPoor.copy(alpha = 0.12f))
                        .border(1.dp, SignalPoor.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = SignalPoor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Kunci Band memerlukan autentikasi. Buka tab 'Alat' untuk memasukkan password admin modem dan login terlebih dahulu.",
                            color = SignalPoor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        // Section: Preset Terpopuler di Indonesia
        item {
            Text(
                text = "Preset Cepat (Rekomendasi Indonesia)",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        // Preset 1: Band 3 (1800 MHz - Tercepat)
        item {
            PresetBandItem(
                title = "Band 3 (1800 MHz) - Tercepat & Populer",
                subtitle = "Telkomsel, Indosat, XL, Tri • Paling stabil untuk gaming & streaming",
                badge = "RECOMMENDED",
                onClick = { viewModel.lockBand("4", "Band 3 (1800 MHz)") }
            )
        }

        // Preset 2: Band 40 (2300 MHz - Telkomsel Orbit & Smartfren)
        item {
            PresetBandItem(
                title = "Band 40 (2300 MHz TDD) - Orbit / Smartfren",
                subtitle = "Sangat cocok untuk pengguna Telkomsel Orbit & Smartfren Unlimited",
                badge = "ORBIT",
                onClick = { viewModel.lockBand("8000000000", "Band 40 (2300 MHz)") }
            )
        }

        // Preset 3: Band 1 (2100 MHz)
        item {
            PresetBandItem(
                title = "Band 1 (2100 MHz)",
                subtitle = "Telkomsel, Indosat, XL • Alternatif jika Band 3 padat",
                badge = null,
                onClick = { viewModel.lockBand("1", "Band 1 (2100 MHz)") }
            )
        }

        // Preset 4: Band 8 (900 MHz - Sinyal Kuat)
        item {
            PresetBandItem(
                title = "Band 8 (900 MHz) - Jangkauan Terjauh",
                subtitle = "Sinyal paling tembus tembok / lokasi pinggiran kota",
                badge = "COVERAGE",
                onClick = { viewModel.lockBand("80000000", "Band 8 (900 MHz)") }
            )
        }

        // Preset 5: Auto (Reset semua band)
        item {
            PresetBandItem(
                title = "Mode Auto (Semua Band)",
                subtitle = "Biarkan modem memilih band secara otomatis seperti bawaan pabrik",
                badge = "DEFAULT",
                onClick = { viewModel.lockBand("7FFFFFFFFFFFFFFF", "Mode Auto (Semua Band)") }
            )
        }

        // Section: Carrier Aggregation (4G+)
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Carrier Aggregation (Kombinasi 4G+)",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                LteBand.PRESET_COMBINATIONS.forEach { (name, hex) ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(CardBackground)
                            .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
                            .clickable { viewModel.lockBand(hex, name) }
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = SignalGood,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary
                                )
                            }
                            Text(
                                text = "Kunci",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentPrimary
                            )
                        }
                    }
                }
            }
        }

        // Section: Custom Multi-Band Selection
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Pilih Band Manual / Custom",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        items(individualBands) { band ->
            val isChecked = selectedBands.contains(band)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardBackground)
                    .border(1.dp, if (isChecked) AccentPrimary else CardBorder, RoundedCornerShape(12.dp))
                    .clickable {
                        if (isChecked) {
                            selectedBands.remove(band)
                        } else {
                            selectedBands.add(band)
                        }
                    }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${band.name} - ${band.frequency}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        if (band.description.isNotEmpty()) {
                            Text(
                                text = band.description,
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }

                    Checkbox(
                        checked = isChecked,
                        onCheckedChange = { checked ->
                            if (checked) selectedBands.add(band) else selectedBands.remove(band)
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = AccentPrimary,
                            uncheckedColor = TextMuted,
                            checkmarkColor = DarkBackground
                        )
                    )
                }
            }
        }

        item {
            Button(
                onClick = {
                    val hex = LteBand.calculateCombinedHex(selectedBands)
                    viewModel.lockBand(hex, "Kombinasi Kustom ($hex)")
                },
                enabled = selectedBands.isNotEmpty() && !isRefreshing,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentPrimary,
                    contentColor = DarkBackground
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Terapkan Pilihan Kustom",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
private fun PresetBandItem(
    title: String,
    subtitle: String,
    badge: String?,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardBackground)
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    if (badge != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(AccentPrimary.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = badge,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentPrimary
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onClick,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CardBorder,
                    contentColor = TextPrimary
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Kunci",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
