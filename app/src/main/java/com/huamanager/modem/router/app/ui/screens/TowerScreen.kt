package com.huamanager.modem.router.app.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.huamanager.modem.router.app.ui.ads.AdmobNativeCard
import com.huamanager.modem.router.app.ui.i18n.LocalAppStrings
import com.huamanager.modem.router.app.ui.theme.AccentPrimary
import com.huamanager.modem.router.app.ui.theme.CardBackground
import com.huamanager.modem.router.app.ui.theme.CardBorder
import com.huamanager.modem.router.app.ui.theme.DarkBackground
import com.huamanager.modem.router.app.ui.theme.SignalExcellent
import com.huamanager.modem.router.app.ui.theme.SignalFair
import com.huamanager.modem.router.app.ui.theme.SignalGood
import com.huamanager.modem.router.app.ui.theme.SignalPoor
import com.huamanager.modem.router.app.ui.theme.TextMuted
import com.huamanager.modem.router.app.ui.theme.TextPrimary
import com.huamanager.modem.router.app.ui.theme.TextSecondary
import com.huamanager.modem.router.app.ui.viewmodel.HuaweiViewModel

@Composable
fun TowerScreen(
    viewModel: HuaweiViewModel,
    modifier: Modifier = Modifier
) {
    val S = LocalAppStrings.current
    val isProUser by viewModel.isProUser.collectAsState()
    val signal by viewModel.signalInfo.collectAsState()
    val netStatus by viewModel.networkStatus.collectAsState()
    val isConnected by viewModel.isConnected.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    val enodebId = signal.getCalculatedEnodebId()
    val sectorId = signal.getCalculatedSectorId()
    val eciDec = signal.getEciDecimal()
    val operatorName = if (netStatus.currentNetworkName.isNotEmpty() && netStatus.currentNetworkName != "Terhubung") {
        netStatus.currentNetworkName
    } else {
        signal.getOperatorName()
    }

    // Manual Calculator State
    var calcInputCellId by remember { mutableStateOf("") }
    var calcResultEnb by remember { mutableStateOf<Long?>(null) }
    var calcResultSector by remember { mutableStateOf<Int?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section: Title & Refresh Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = S.towerInfoTitle,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = S.towerSubtitle,
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }

                IconButton(
                    onClick = { viewModel.manualRefresh() },
                    enabled = !isRefreshing
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = S.refresh,
                        tint = AccentPrimary
                    )
                }
            }
        }

        // Section: Main Tower ID Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(CardBackground)
                    .border(1.dp, CardBorder, RoundedCornerShape(18.dp))
                    .padding(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Operator & Status Badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(AccentPrimary.copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(SignalExcellent)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = operatorName,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentPrimary
                            )
                        }

                        Text(
                            text = if (isConnected) S.connected else S.disconnected,
                            fontSize = 11.sp,
                            color = if (isConnected) SignalExcellent else SignalPoor,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Main eNodeB ID Highlight
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(AccentPrimary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CellTower,
                                contentDescription = null,
                                tint = AccentPrimary,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = S.enodebIdLabel,
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                            Text(
                                text = if (enodebId != null) "$enodebId" else if (signal.cellId.isNotEmpty() && signal.cellId != "-") signal.cellId else S.qualitySearching,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary,
                                fontFamily = FontFamily.Monospace
                            )
                            if (sectorId != null) {
                                val physicalSector = signal.getPhysicalSectorNumber()
                                Text(
                                    text = "${S.physicalSectorLabel}: ${S.sectorWord} $physicalSector  •  LCID: $sectorId",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SignalGood
                                )
                            }
                        }
                    }

                    // Parameter Grid (Cell ID, LCID, PCI, Band, TAC)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TowerInfoBadge(
                            label = S.signalMetricCellId,
                            value = if (eciDec != null) "$eciDec" else signal.cellId,
                            subValue = signal.getEciHex(),
                            modifier = Modifier.weight(1f)
                        )
                        TowerInfoBadge(
                            label = "${S.localCellIdLabel} (LCID)",
                            value = if (sectorId != null) "$sectorId" else "-",
                            subValue = "${S.physicalSectorLabel}: ${S.sectorWord} ${signal.getPhysicalSectorNumber()}",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TowerInfoBadge(
                            label = S.activeBandLabel,
                            value = if (signal.band.isNotEmpty() && signal.band != "-") "Band ${signal.band}" else "-",
                            subValue = "EARFCN: ${signal.earfcn}",
                            modifier = Modifier.weight(1f)
                        )
                        TowerInfoBadge(
                            label = S.signalMetricPCI,
                            value = signal.pci,
                            subValue = "${S.frequencyLabel}: ${signal.dlFrequency}",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TowerInfoBadge(
                            label = "TAC / Area Code",
                            value = signal.tac,
                            subValue = "PLMN: ${signal.plmn}",
                            modifier = Modifier.weight(1f)
                        )
                        TowerInfoBadge(
                            label = S.signalMetricRSRP,
                            value = signal.rsrp,
                            subValue = "SINR: ${signal.sinr} | RSRQ: ${signal.rsrq}",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Section: Sector Antenna Direction & Azimuth Guide
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Explore,
                            contentDescription = null,
                            tint = AccentPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = S.antennaAzimuthTitle,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Text(
                        text = S.antennaAzimuthSubtitle,
                        fontSize = 11.sp,
                        color = TextSecondary
                    )

                    // 3 Sector Visual Tiles based on Physical Sector Number
                    val physicalSectorNum = signal.getPhysicalSectorNumber()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SectorTile(
                            sectorTitle = "${S.sectorWord} 1",
                            degrees = "0° – 120°",
                            direction = S.directionNorth,
                            isActive = physicalSectorNum == 1,
                            modifier = Modifier.weight(1f)
                        )
                        SectorTile(
                            sectorTitle = "${S.sectorWord} 2",
                            degrees = "120° – 240°",
                            direction = S.directionEast,
                            isActive = physicalSectorNum == 2,
                            modifier = Modifier.weight(1f)
                        )
                        SectorTile(
                            sectorTitle = "${S.sectorWord} 3",
                            degrees = "240° – 360°",
                            direction = S.directionWest,
                            isActive = physicalSectorNum == 3,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Helpful advice
                    Text(
                        text = "💡 ${S.towerAdviceHeader}: " + signal.getSectorAzimuthDescription(S) +
                                (if (sectorId != null && sectorId > 3) "\nℹ️ ${S.sectorCarrierNote(sectorId, physicalSectorNum)}" else "") +
                                "\n${S.towerAdviceNote}",
                        fontSize = 11.sp,
                        color = TextMuted,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        // Section: Direct Tower Location & Pointing Guide Banner (Di bawah Arah Sektor)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(AccentPrimary.copy(alpha = 0.08f))
                    .border(1.5.dp, AccentPrimary.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(AccentPrimary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.NearMe,
                                contentDescription = null,
                                tint = AccentPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = S.estimatedTowerDirTitle,
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                            Text(
                                text = "${S.towerAdviceIntro} ${signal.getEstimatedTowerBearing(S)}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SignalExcellent
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(CardBackground)
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = SignalGood,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = S.towerAdviceHeader,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentPrimary
                                )
                            }
                            Text(
                                text = "${S.towerAdvicePointing} ${signal.getEstimatedTowerBearing(S)} (${S.sectorWord} ${signal.getPhysicalSectorNumber()}${if (sectorId != null && sectorId > 3) " / LCID $sectorId" else ""}).",
                                fontSize = 11.sp,
                                color = TextPrimary,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }

        if (!isProUser) {
            item {
                AdmobNativeCard()
            }
        }

        // Section: Map Links & Action Tools
        item {
            Text(
                text = S.openCellMapperBtn,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Button: CellMapper
                Button(
                    onClick = {
                        val url = signal.getCellMapperUrl()
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Browser error", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentPrimary,
                        contentColor = DarkBackground
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInBrowser,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = S.openCellMapperBtn,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Button: Google Maps
                    OutlinedButton(
                        onClick = {
                            val query = if (enodebId != null) {
                                "Tower BTS $operatorName eNodeB $enodebId"
                            } else {
                                "Tower BTS $operatorName"
                            }
                            val gmapsUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=" + Uri.encode(query))
                            val intent = Intent(Intent.ACTION_VIEW, gmapsUri)
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Maps error", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = CardBackground,
                            contentColor = TextPrimary
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorder))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = null,
                            tint = SignalGood,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Google Maps", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    // Button: Copy Data
                    OutlinedButton(
                        onClick = {
                            val copyText = buildString {
                                appendLine(S.towerCopyHeader)
                                appendLine("${S.signalMetricOperator.padEnd(14)}: $operatorName (${signal.plmn})")
                                appendLine("eNodeB ID     : ${enodebId ?: "-"}")
                                appendLine("${S.sectorWord} ID     : ${sectorId ?: "-"}")
                                appendLine("Cell ID (ECI) : ${eciDec ?: signal.cellId} (${signal.getEciHex()})")
                                appendLine("PCI           : ${signal.pci}")
                                appendLine("${S.activeBandLabel.padEnd(14)}: Band ${signal.band} (${signal.dlFrequency})")
                                appendLine("EARFCN        : ${signal.earfcn}")
                                appendLine("TAC           : ${signal.tac}")
                                appendLine("RSRP          : ${signal.rsrp}")
                                appendLine("SINR          : ${signal.sinr}")
                                appendLine("RSRQ          : ${signal.rsrq}")
                                appendLine("${S.sectorAntennaLabel.padEnd(14)}: ${signal.getSectorAzimuthDescription(S)}")
                                appendLine("${S.towerCopyTowerLoc.padEnd(14)}: " + String.format(S.towerCopyPosPrefix, signal.getEstimatedTowerBearing(S)))
                                appendLine("${S.towerCopyAimDir.padEnd(14)}: " + String.format(S.towerCopyAimPrefix, signal.getEstimatedTowerBearing(S)))
                                appendLine("CellMapper    : ${signal.getCellMapperUrl()}")
                            }
                            clipboard.setText(AnnotatedString(copyText))
                            Toast.makeText(context, S.copied, Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = CardBackground,
                            contentColor = TextPrimary
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorder))
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = null,
                            tint = AccentPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(S.copy, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // Section: Manual Cell ID / eNodeB Calculator
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = null,
                            tint = AccentPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = S.manualCellIdCalcTitle,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Text(
                        text = S.manualCellIdCalcSubtitle,
                        fontSize = 11.sp,
                        color = TextSecondary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = calcInputCellId,
                            onValueChange = { calcInputCellId = it },
                            placeholder = { Text(S.manualCellIdInputLabel, fontSize = 12.sp) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
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
                            modifier = Modifier.weight(1f)
                        )

                        Button(
                            onClick = {
                                val clean = calcInputCellId.trim()
                                val num = if (clean.startsWith("0x", true) || clean.any { it in 'a'..'f' || it in 'A'..'F' }) {
                                    clean.removePrefix("0x").removePrefix("0X").toLongOrNull(16)
                                } else {
                                    clean.toLongOrNull()
                                }

                                if (num != null && num > 0) {
                                    calcResultEnb = num / 256
                                    calcResultSector = (num % 256).toInt()
                                } else {
                                    calcResultEnb = null
                                    calcResultSector = null
                                    Toast.makeText(context, S.invalidCellIdError, Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AccentPrimary,
                                contentColor = DarkBackground
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(S.calculateBtn, fontWeight = FontWeight.Bold)
                        }
                    }

                    calcResultEnb?.let { enb ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkBackground)
                                .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = S.calcResultTitle, fontSize = 11.sp, color = TextMuted)
                                Text(
                                    text = "${S.calcResultEnodeB} $enb",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SignalExcellent
                                )
                                val lcid = calcResultSector ?: 0
                                val base = when {
                                    lcid >= 200 -> lcid - 200
                                    lcid >= 100 -> lcid - 100
                                    lcid >= 10 -> lcid % 10
                                    else -> lcid
                                }
                                val phys = when (base % 3) {
                                    1 -> 1
                                    2 -> 2
                                    0 -> 3
                                    else -> 1
                                }
                                Text(
                                    text = "${S.physicalSectorLabel}: ${S.sectorWord} $phys  •  LCID: $lcid",
                                    fontSize = 13.sp,
                                    color = SignalGood
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TowerInfoBadge(
    label: String,
    value: String,
    subValue: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(DarkBackground)
            .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Column {
            Text(
                text = label,
                fontSize = 11.sp,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value.ifEmpty { "-" },
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                fontFamily = FontFamily.Monospace
            )
            if (subValue.isNotEmpty() && subValue != "-") {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subValue,
                    fontSize = 10.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
fun SectorTile(
    sectorTitle: String,
    degrees: String,
    direction: String,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isActive) SignalExcellent.copy(alpha = 0.15f) else DarkBackground)
            .border(
                width = if (isActive) 1.5.dp else 1.dp,
                color = if (isActive) SignalExcellent else CardBorder,
                shape = RoundedCornerShape(10.dp)
            )
            .padding(8.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isActive) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = SignalExcellent,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                    text = sectorTitle,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isActive) SignalExcellent else TextPrimary
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = degrees,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isActive) SignalGood else AccentPrimary
            )
            Text(
                text = direction,
                fontSize = 9.sp,
                color = TextMuted,
                lineHeight = 11.sp
            )
        }
    }
}
