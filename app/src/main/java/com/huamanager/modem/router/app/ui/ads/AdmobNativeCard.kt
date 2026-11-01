package com.huamanager.modem.router.app.ui.ads

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView

@Composable
fun AdmobNativeCard(
    modifier: Modifier = Modifier,
    adUnitId: String = AdsManager.NATIVE_AD_UNIT_ID
) {
    val context = LocalContext.current
    var nativeAdState by remember { mutableStateOf<NativeAd?>(null) }

    DisposableEffect(adUnitId) {
        val adLoader = AdLoader.Builder(context, adUnitId)
            .forNativeAd { ad ->
                nativeAdState?.destroy()
                nativeAdState = ad
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    // silently handle failure
                }
            })
            .build()

        adLoader.loadAd(AdRequest.Builder().build())

        onDispose {
            nativeAdState?.destroy()
            nativeAdState = null
        }
    }

    val currentAd = nativeAdState ?: return

    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(vertical = 4.dp),
        factory = { ctx ->
            NativeAdView(ctx).apply {
                // Outer Container: Dark Card
                val rootLayout = LinearLayout(ctx).apply {
                    orientation = LinearLayout.VERTICAL
                    val bg = GradientDrawable().apply {
                        setColor(Color.parseColor("#1E222B")) // CardBackground
                        cornerRadius = 36f
                        setStroke(2, Color.parseColor("#2A303C")) // CardBorder
                    }
                    background = bg
                    val pad = (14 * ctx.resources.displayMetrics.density).toInt()
                    setPadding(pad, pad, pad, pad)
                }

                // Row: Ad Badge + Headline
                val topRow = LinearLayout(ctx).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                }

                val adBadge = TextView(ctx).apply {
                    text = "AD"
                    textSize = 9f
                    setTextColor(Color.parseColor("#00E5FF"))
                    val badgeBg = GradientDrawable().apply {
                        setColor(Color.parseColor("#2000E5FF"))
                        cornerRadius = 10f
                    }
                    background = badgeBg
                    val bp = (4 * ctx.resources.displayMetrics.density).toInt()
                    val bph = (6 * ctx.resources.displayMetrics.density).toInt()
                    setPadding(bph, bp, bph, bp)
                }

                val headlineView = TextView(ctx).apply {
                    textSize = 13f
                    setTextColor(Color.WHITE)
                    setTypeface(null, android.graphics.Typeface.BOLD)
                    val lp = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                    lp.marginStart = (8 * ctx.resources.displayMetrics.density).toInt()
                    layoutParams = lp
                }

                topRow.addView(adBadge)
                topRow.addView(headlineView)
                rootLayout.addView(topRow)

                // Middle Row: Icon + Body Text + CTA Button
                val contentRow = LinearLayout(ctx).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                    val lp = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    lp.topMargin = (8 * ctx.resources.displayMetrics.density).toInt()
                    layoutParams = lp
                }

                val iconView = ImageView(ctx).apply {
                    val size = (36 * ctx.resources.displayMetrics.density).toInt()
                    layoutParams = LinearLayout.LayoutParams(size, size).apply {
                        marginEnd = (10 * ctx.resources.displayMetrics.density).toInt()
                    }
                    scaleType = ImageView.ScaleType.FIT_CENTER
                }

                val bodyView = TextView(ctx).apply {
                    textSize = 11f
                    setTextColor(Color.parseColor("#A0A6B2")) // TextSecondary
                    maxLines = 2
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                }

                val ctaView = Button(ctx).apply {
                    textSize = 11f
                    setTextColor(Color.parseColor("#0F1115")) // DarkBackground
                    setTypeface(null, android.graphics.Typeface.BOLD)
                    val ctaBg = GradientDrawable().apply {
                        setColor(Color.parseColor("#00E5FF")) // AccentPrimary
                        cornerRadius = 20f
                    }
                    background = ctaBg
                    val lp = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        (34 * ctx.resources.displayMetrics.density).toInt()
                    ).apply {
                        marginStart = (10 * ctx.resources.displayMetrics.density).toInt()
                    }
                    layoutParams = lp
                }

                contentRow.addView(iconView)
                contentRow.addView(bodyView)
                contentRow.addView(ctaView)
                rootLayout.addView(contentRow)

                addView(rootLayout)

                // Register views with NativeAdView
                this.headlineView = headlineView
                this.bodyView = bodyView
                this.callToActionView = ctaView
                this.iconView = iconView
            }
        },
        update = { nativeAdView ->
            (nativeAdView.headlineView as? TextView)?.text = currentAd.headline

            if (currentAd.body != null) {
                (nativeAdView.bodyView as? TextView)?.apply {
                    text = currentAd.body
                    visibility = View.VISIBLE
                }
            } else {
                nativeAdView.bodyView?.visibility = View.GONE
            }

            if (currentAd.callToAction != null) {
                (nativeAdView.callToActionView as? Button)?.apply {
                    text = currentAd.callToAction
                    visibility = View.VISIBLE
                }
            } else {
                nativeAdView.callToActionView?.visibility = View.GONE
            }

            if (currentAd.icon != null) {
                (nativeAdView.iconView as? ImageView)?.apply {
                    setImageDrawable(currentAd.icon?.drawable)
                    visibility = View.VISIBLE
                }
            } else {
                nativeAdView.iconView?.visibility = View.GONE
            }

            nativeAdView.setNativeAd(currentAd)
        }
    )
}
