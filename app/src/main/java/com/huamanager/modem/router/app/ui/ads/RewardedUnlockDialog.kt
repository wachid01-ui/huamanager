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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import com.huamanager.modem.router.app.ui.theme.TextMuted
import com.huamanager.modem.router.app.ui.theme.TextPrimary
import com.huamanager.modem.router.app.ui.theme.TextSecondary

@Composable
fun RewardedUnlockDialog(
    featureTitle: String,
    onDismiss: () -> Unit,
    onUnlocked: () -> Unit
) {
    val context = LocalContext.current
    val S = LocalAppStrings.current
    val activity = context as? Activity
    var isAdLoading by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = {
        if (!isAdLoading) onDismiss()
    }) {
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
                            isAdLoading = true
                            AdsManager.loadAndShowRewardedAd(
                                activity = activity,
                                onRewardEarned = {
                                    isAdLoading = false
                                    onUnlocked()
                                    onDismiss()
                                },
                                onDismissed = {
                                    isAdLoading = false
                                    onDismiss()
                                },
                                onFailed = { errorMsg ->
                                    isAdLoading = false
                                    Toast.makeText(
                                        context,
                                        "Gagal memuat video: $errorMsg.\nPastikan HP/modem terhubung ke internet.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            )
                        } else {
                            onUnlocked()
                            onDismiss()
                        }
                    },
                    enabled = !isAdLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentPrimary,
                        contentColor = DarkBackground,
                        disabledContainerColor = AccentPrimary.copy(alpha = 0.5f),
                        disabledContentColor = DarkBackground.copy(alpha = 0.5f)
                    )
                ) {
                    if (isAdLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = DarkBackground,
                            strokeWidth = 2.5.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Menyiapkan Video...",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    } else {
                        Text(
                            text = "🎬 ${S.watchVideoBtn}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                // Button 2: Cancel
                TextButton(
                    onClick = {
                        if (!isAdLoading) onDismiss()
                    },
                    enabled = !isAdLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = S.cancel,
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
