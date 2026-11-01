package com.huamanager.modem.router.app

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.huamanager.modem.router.app.ui.ads.AdsManager

class HuaManagerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            MobileAds.initialize(this) {
                AdsManager.loadRewardedAd(this)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
