package com.huaweimanager.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.huaweimanager.app.data.model.SignalQualityLevel
import com.huaweimanager.app.ui.theme.CardBorder
import com.huaweimanager.app.ui.theme.SignalExcellent
import com.huaweimanager.app.ui.theme.SignalFair
import com.huaweimanager.app.ui.theme.SignalGood
import com.huaweimanager.app.ui.theme.SignalInactive
import com.huaweimanager.app.ui.theme.SignalPoor
import com.huaweimanager.app.ui.theme.TextMuted
import com.huaweimanager.app.ui.theme.TextPrimary

@Composable
fun SignalGauge(
    rsrpValue: Int?,
    quality: SignalQualityLevel,
    modifier: Modifier = Modifier
) {
    // Normalizing RSRP: -130dBm (0%) to -65dBm (100%)
    val targetProgress = if (rsrpValue != null) {
        val clamped = rsrpValue.coerceIn(-130, -65)
        (clamped - (-130)).toFloat() / ((-65) - (-130))
    } else {
        0f
    }

    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 600),
        label = "signal_gauge"
    )

    val color = when (quality) {
        SignalQualityLevel.EXCELLENT -> SignalExcellent
        SignalQualityLevel.GOOD -> SignalGood
        SignalQualityLevel.FAIR -> SignalFair
        SignalQualityLevel.POOR -> SignalPoor
        SignalQualityLevel.UNKNOWN -> SignalInactive
    }

    val trackColor = CardBorder

    val qualityLabel = when (quality) {
        SignalQualityLevel.EXCELLENT -> "SANGAT BAIK"
        SignalQualityLevel.GOOD -> "BAIK"
        SignalQualityLevel.FAIR -> "CUKUP"
        SignalQualityLevel.POOR -> "LEMAH / BURUK"
        SignalQualityLevel.UNKNOWN -> "MENCARI..."
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(190.dp)
    ) {
        Canvas(modifier = Modifier.size(175.dp)) {
            val strokeWidth = 14.dp.toPx()
            // Background arc (240 degrees)
            drawArc(
                color = trackColor,
                startAngle = 150f,
                sweepAngle = 240f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Progress arc
            if (animatedProgress > 0f) {
                drawArc(
                    color = color,
                    startAngle = 150f,
                    sweepAngle = 240f * animatedProgress,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "RSRP",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (rsrpValue != null) "$rsrpValue" else "--",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "dBm",
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = qualityLabel,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}
