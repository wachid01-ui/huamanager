package com.huamanager.modem.router.app

import android.app.Application
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HuaManagerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                MobileAds.initialize(this@HuaManagerApp) {}
            } catch (e: Exception) {
                // ignore
            }
        }
    }
}
