package com.huamanager.modem.router.app.ui.ads

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.huamanager.modem.router.app.ui.i18n.LocalAppStrings
import com.huamanager.modem.router.app.ui.theme.AccentPrimary
import com.huamanager.modem.router.app.ui.theme.CardBackground
import com.huamanager.modem.router.app.ui.theme.CardBorder
import com.huamanager.modem.router.app.ui.theme.DarkBackground
import com.huamanager.modem.router.app.ui.theme.SignalExcellent
import com.huamanager.modem.router.app.ui.theme.TextMuted
import com.huamanager.modem.router.app.ui.theme.TextPrimary
import com.huamanager.modem.router.app.ui.theme.TextSecondary

@Composable
fun RewardedUnlockDialog(
    featureTitle: String,
    onDismiss: () -> Unit,
    onUnlocked: () -> Unit,
    onUpgradeProClick: () -> Unit
) {
    val context = LocalContext.current
    val S = LocalAppStrings.current
    val activity = context as? Activity

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(CardBackground)
                .border(1.5.dp, CardBorder, RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Icon Header
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(AccentPrimary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LockOpen,
                        contentDescription = null,
                        tint = AccentPrimary,
                        modifier = Modifier.size(30.dp)
                    )
                }

                // Title & Subtitle
                Text(
                    text = "${S.rewardUnlockTitle}: $featureTitle",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = S.rewardUnlockDesc,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Button 1: Watch Ad (Free)
                Button(
                    onClick = {
                        if (activity != null) {
                            if (AdsManager.isRewardedAdReady()) {
                                AdsManager.showRewardedAd(
                                    activity = activity,
                                    onRewardEarned = {
                                        onUnlocked()
                                        onDismiss()
                                    },
                                    onDismissed = {
                                        onDismiss()
                                    }
                                )
                            } else {
                                Toast.makeText(context, S.adNotReady, Toast.LENGTH_SHORT).show()
                                AdsManager.loadRewardedAd(context)
                                // Gracefully unlock if ad network failed
                                onUnlocked()
                                onDismiss()
                            }
                        } else {
                            onUnlocked()
                            onDismiss()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentPrimary,
                        contentColor = DarkBackground
                    )
                ) {
                    Text(
                        text = "🎬 ${S.watchVideoBtn}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                // Button 2: Upgrade to PRO
                Button(
                    onClick = {
                        onDismiss()
                        onUpgradeProClick()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SignalExcellent.copy(alpha = 0.2f),
                        contentColor = SignalExcellent
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = S.proUpgradeBtn,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                // Button 3: Cancel
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = S.cancel,
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
