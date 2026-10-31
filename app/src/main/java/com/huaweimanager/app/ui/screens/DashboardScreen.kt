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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Router
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.huaweimanager.app.data.model.SignalQualityLevel
import com.huaweimanager.app.ui.components.MetricCard
import com.huaweimanager.app.ui.components.SignalGauge
import com.huaweimanager.app.ui.theme.AccentPrimary
import com.huaweimanager.app.ui.theme.CardBackground
import com.huaweimanager.app.ui.theme.CardBorder
import com.huaweimanager.app.ui.theme.DarkBackground
import com.huaweimanager.app.ui.theme.SignalExcellent
import com.huaweimanager.app.ui.theme.SignalFair
import com.huaweimanager.app.ui.theme.SignalGood
import com.huaweimanager.app.ui.theme.SignalPoor
import com.huaweimanager.app.ui.theme.TextMuted
import com.huaweimanager.app.ui.theme.TextPrimary
import com.huaweimanager.app.ui.theme.TextSecondary
import com.huaweimanager.app.ui.viewmodel.HuaweiViewModel

@Composable
fun DashboardScreen(
    viewModel: HuaweiViewModel,
    modifier: Modifier = Modifier
) {
    val signal by viewModel.signalInfo.collectAsState()
    val netStatus by viewModel.networkStatus.collectAsState()
    val isConnected by viewModel.isConnected.collectAsState()
    val isAutoRefresh by viewModel.isAutoRefresh.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val routerIp by viewModel.routerIp.collectAsState()

    val rsrpVal = signal.parseRsrpValue()
    val sinrVal = signal.parseSinrValue()
    val rsrqVal = signal.parseRsrqValue()

    val sinrColor = when {
        sinrVal == null -> TextMuted
        sinrVal >= 20 -> SignalExcellent
        sinrVal >= 13 -> SignalGood
        sinrVal >= 0 -> SignalFair
        else -> SignalPoor
    }

    val rsrqColor = when {
        rsrqVal == null -> TextMuted
        rsrqVal >= -8 -> SignalExcellent
        rsrqVal >= -12 -> SignalGood
        rsrqVal >= -16 -> SignalFair
        else -> SignalPoor
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Connection Header Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(CardBackground)
                    .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Router,
                                contentDescription = null,
                                tint = AccentPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Modem Gateway",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                                Text(
                                    text = routerIp,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                        }

                        // Status Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isConnected) SignalExcellent.copy(alpha = 0.15f) else SignalPoor.copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (isConnected) SignalExcellent else SignalPoor)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isConnected) "ONLINE" else "OFFLINE",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isConnected) SignalExcellent else SignalPoor
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CellTower,
                                contentDescription = null,
                                tint = SignalGood,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = netStatus.currentNetworkName,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "${netStatus.getNetworkTypeName()} ${if (signal.band.isNotEmpty() && signal.band != "-") "• Band " + signal.band else ""}",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        IconButton(
                            onClick = { viewModel.manualRefresh() }
                        ) {
                            if (isRefreshing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = AccentPrimary
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh",
                                    tint = AccentPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Main Signal Gauge
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(CardBackground)
                    .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
                    .padding(vertical = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                SignalGauge(
                    rsrpValue = rsrpVal,
                    quality = signal.getOverallQuality()
                )
            }
        }

        // 3. Signal Parameters Grid (RSRP, SINR, RSRQ, RSSI)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        label = "SINR (Noise Ratio)",
                        value = signal.sinr.replace("dB", "").trim(),
                        unit = "dB",
                        statusColor = sinrColor,
                        statusText = when {
                            sinrVal == null -> null
                            sinrVal >= 13 -> "Bagus"
                            sinrVal >= 0 -> "Cukup"
                            else -> "Banyak Noise"
                        },
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        label = "RSRQ (Kualitas)",
                        value = signal.rsrq.replace("dB", "").trim(),
                        unit = "dB",
                        statusColor = rsrqColor,
                        statusText = when {
                            rsrqVal == null -> null
                            rsrqVal >= -10 -> "Stabil"
                            else -> "Fluktuatif"
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        label = "RSSI (Daya Total)",
                        value = signal.rssi.replace("dBm", "").trim(),
                        unit = "dBm",
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        label = "LTE BAND",
                        value = if (signal.band.isNotEmpty() && signal.band != "-") "B${signal.band}" else "-",
                        unit = signal.dlFrequency,
                        statusColor = AccentPrimary,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        label = "PCI (Physical Cell ID)",
                        value = signal.pci,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        label = "eNodeB / CELL ID",
                        value = signal.getCalculatedEnodebId()?.let { "eNB $it" } ?: signal.cellId,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 4. Quick Auto-Refresh Switch
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CardBackground)
                    .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Auto Refresh Real-time",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Update sinyal setiap 2.5 detik",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }

                    Switch(
                        checked = isAutoRefresh,
                        onCheckedChange = { viewModel.toggleAutoRefresh() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = DarkBackground,
                            checkedTrackColor = AccentPrimary,
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = DarkBackground
                        )
                    )
                }
            }
        }
    }
}
