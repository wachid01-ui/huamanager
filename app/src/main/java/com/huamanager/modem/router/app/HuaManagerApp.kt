package com.huamanager.modem.router.app

import android.app.Application
import com.huamanager.modem.router.app.ui.ads.AdsManager
import com.startapp.sdk.adsbase.StartAppSDK

class HuaManagerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            StartAppSDK.init(this, AdsManager.STARTAPP_APP_ID, false)
            StartAppSDK.setTestAdsEnabled(AdsManager.IS_TEST_MODE)
            AdsManager.loadRewardedAd(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
