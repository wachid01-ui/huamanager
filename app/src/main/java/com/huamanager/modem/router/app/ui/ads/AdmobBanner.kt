package com.huamanager.modem.router.app.ui.ads

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.startapp.sdk.ads.banner.Banner

@Composable
fun StartAppBanner(
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        factory = { context ->
            Banner(context)
        }
    )
}

// Alias agar kompatibel dengan pemanggilan AdmobBanner yang sudah ada
@Composable
fun AdmobBanner(
    modifier: Modifier = Modifier,
    adUnitId: String = ""
) {
    StartAppBanner(modifier = modifier)
}
