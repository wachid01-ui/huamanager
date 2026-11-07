package com.huamanager.modem.router.app.ui.ads

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.startapp.sdk.adsbase.Ad
import com.startapp.sdk.adsbase.StartAppAd
import com.startapp.sdk.adsbase.adlisteners.AdDisplayListener
import com.startapp.sdk.adsbase.adlisteners.AdEventListener
import com.startapp.sdk.adsbase.adlisteners.VideoListener

object AdsManager {
    private const val TAG = "AdsManager"

    // Start.io Production App ID
    const val STARTAPP_APP_ID = "210266488"

    // Ubah ke false jika aplikasi sudah siap dipakai pengguna umum (iklan nyata/live)
    var IS_TEST_MODE = true

    private var startAppAd: StartAppAd? = null
    private var isAdLoading = false

    private val pendingLoadedCallbacks = mutableListOf<() -> Unit>()
    private val pendingFailedCallbacks = mutableListOf<(String) -> Unit>()

    fun loadRewardedAd(
        context: Context,
        onLoaded: (() -> Unit)? = null,
        onFailed: ((String) -> Unit)? = null
    ) {
        if (startAppAd?.isReady == true) {
            onLoaded?.invoke()
            return
        }

        if (onLoaded != null) pendingLoadedCallbacks.add(onLoaded)
        if (onFailed != null) pendingFailedCallbacks.add(onFailed)

        if (isAdLoading) {
            Log.d(TAG, "Start.io ad is already loading in background, queued listener.")
            return
        }

        isAdLoading = true
        Log.d(TAG, "Requesting new RewardedAd from Start.io...")
        val ad = StartAppAd(context.applicationContext)
        ad.loadAd(StartAppAd.AdMode.REWARDED_VIDEO, object : AdEventListener {
            override fun onReceiveAd(receivedAd: Ad) {
                startAppAd = ad
                isAdLoading = false
                Log.d(TAG, "Start.io RewardedAd successfully loaded and ready to display")
                val loaded = ArrayList(pendingLoadedCallbacks)
                pendingLoadedCallbacks.clear()
                pendingFailedCallbacks.clear()
                loaded.forEach { it.invoke() }
            }

            override fun onFailedToReceiveAd(failedAd: Ad?) {
                startAppAd = null
                isAdLoading = false
                val errorMsg = failedAd?.errorMessage ?: "Gagal memuat iklan Start.io"
                Log.e(TAG, "Failed to load Start.io RewardedAd: $errorMsg")
                val failed = ArrayList(pendingFailedCallbacks)
                pendingLoadedCallbacks.clear()
                pendingFailedCallbacks.clear()
                failed.forEach { it.invoke(errorMsg) }
            }
        })
    }

    fun isRewardedAdReady(): Boolean = startAppAd?.isReady == true

    fun showRewardedAd(
        activity: Activity,
        onRewardEarned: () -> Unit,
        onDismissed: (() -> Unit)? = null,
        onFailed: ((String) -> Unit)? = null
    ) {
        val ad = startAppAd
        if (ad != null && ad.isReady) {
            var earnedReward = false

            ad.setVideoListener(object : VideoListener {
                override fun onVideoCompleted() {
                    Log.d(TAG, "User completed watching Start.io rewarded video")
                    earnedReward = true
                    onRewardEarned()
                }
            })

            ad.showAd(object : AdDisplayListener {
                override fun adDisplayed(displayedAd: Ad?) {
                    Log.d(TAG, "Start.io ad displayed full screen")
                }

                override fun adHidden(hiddenAd: Ad?) {
                    Log.d(TAG, "Start.io ad closed by user")
                    startAppAd = null
                    // Preload iklan berikutnya di background
                    loadRewardedAd(activity.applicationContext)
                    onDismissed?.invoke()
                }

                override fun adClicked(clickedAd: Ad?) {
                    Log.d(TAG, "Start.io ad clicked")
                }

                override fun adNotDisplayed(notDisplayedAd: Ad?) {
                    Log.e(TAG, "Start.io ad not displayed")
                    startAppAd = null
                    loadRewardedAd(activity.applicationContext)
                    onFailed?.invoke("Iklan tidak dapat ditampilkan.")
                }
            })
        } else {
            onFailed?.invoke("Iklan video belum siap di memori.")
        }
    }

    fun loadAndShowRewardedAd(
        activity: Activity,
        onRewardEarned: () -> Unit,
        onDismissed: (() -> Unit)? = null,
        onFailed: ((String) -> Unit)? = null
    ) {
        if (isRewardedAdReady()) {
            showRewardedAd(
                activity = activity,
                onRewardEarned = onRewardEarned,
                onDismissed = onDismissed,
                onFailed = onFailed
            )
            return
        }

        var isCompleted = false
        val timeoutHandler = Handler(Looper.getMainLooper())
        val timeoutRunnable = Runnable {
            if (!isCompleted) {
                isCompleted = true
                Log.w(TAG, "loadAndShowRewardedAd timed out after 12s")
                onFailed?.invoke("Waktu memuat video habis. Silakan periksa koneksi internet.")
            }
        }
        timeoutHandler.postDelayed(timeoutRunnable, 12000L)

        loadRewardedAd(
            context = activity,
            onLoaded = {
                timeoutHandler.removeCallbacks(timeoutRunnable)
                if (!isCompleted && !activity.isFinishing && !activity.isDestroyed) {
                    isCompleted = true
                    showRewardedAd(
                        activity = activity,
                        onRewardEarned = onRewardEarned,
                        onDismissed = onDismissed,
                        onFailed = onFailed
                    )
                }
            },
            onFailed = { errorMsg ->
                timeoutHandler.removeCallbacks(timeoutRunnable)
                if (!isCompleted) {
                    isCompleted = true
                    onFailed?.invoke(errorMsg)
                }
            }
        )
    }
}
