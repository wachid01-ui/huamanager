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
import com.huamanager.modem.router.app.ui.ads.AdmobNativeCard
import com.huamanager.modem.router.app.ui.ads.RewardedUnlockDialog
import com.huamanager.modem.router.app.data.model.LteBand
import com.huamanager.modem.router.app.ui.i18n.LocalAppStrings
import com.huamanager.modem.router.app.ui.theme.AccentPrimary
import com.huamanager.modem.router.app.ui.theme.CardBackground
import com.huamanager.modem.router.app.ui.theme.CardBorder
import com.huamanager.modem.router.app.ui.theme.DarkBackground
import com.huamanager.modem.router.app.ui.theme.SignalExcellent
import com.huamanager.modem.router.app.ui.theme.SignalGood
import androidx.compose.material.icons.filled.Warning
import com.huamanager.modem.router.app.ui.theme.SignalPoor
import com.huamanager.modem.router.app.ui.theme.TextMuted
import com.huamanager.modem.router.app.ui.theme.TextPrimary
import com.huamanager.modem.router.app.ui.theme.TextSecondary
import com.huamanager.modem.router.app.ui.viewmodel.HuaViewModel

@Composable
fun BandLockScreen(
    viewModel: HuaViewModel,
    modifier: Modifier = Modifier
) {
    val S = LocalAppStrings.current
    val signal by viewModel.signalInfo.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    val individualBands = remember { LteBand.ALL_BANDS }
    val selectedBands = remember { mutableStateListOf<LteBand>() }

    var showUnlockDialog by remember { mutableStateOf(false) }
    var pendingLockTitle by remember { mutableStateOf("") }
    var pendingLockAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    val handleLock: (String, () -> Unit) -> Unit = { title, action ->
        pendingLockTitle = title
        pendingLockAction = action
        showUnlockDialog = true
    }

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
                            text = S.activeBandLabel,
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (signal.band.isNotEmpty() && signal.band != "-") "Band ${signal.band} (${signal.dlFrequency})" else "Auto",
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
                            text = S.bandLockAuthRequired,
                            color = SignalPoor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        // Section: Carrier Aggregation (4G+)
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = S.carrierAggregationTitle,
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
                            .clickable { handleLock(name) { viewModel.lockBand(hex, name, S.bandLockSuccessMsg, S.bandLockFailedMsg) } }
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
                                text = S.lockBtn,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentPrimary
                            )
                        }
                    }
                }
            }
        }

        item {
            AdmobNativeCard()
        }

        // Section: Custom Multi-Band Selection
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = S.manualBandSelectionTitle,
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
                            text = "${band.name} - ${S.getBandFrequency(band.bandNumber, band.frequency)}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        val desc = S.getBandDescription(band.bandNumber, band.description)
                        if (desc.isNotEmpty()) {
                            Text(
                                text = desc,
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
                    handleLock("${S.customCombinationLabel} ($hex)") {
                        viewModel.lockBand(hex, "${S.customCombinationLabel} ($hex)", S.bandLockSuccessMsg, S.bandLockFailedMsg)
                    }
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
                    text = S.lockSelectedBandsBtn,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }

    if (showUnlockDialog) {
        RewardedUnlockDialog(
            featureTitle = pendingLockTitle,
            onDismiss = { showUnlockDialog = false },
            onUnlocked = { pendingLockAction?.invoke() }
        )
    }
}
