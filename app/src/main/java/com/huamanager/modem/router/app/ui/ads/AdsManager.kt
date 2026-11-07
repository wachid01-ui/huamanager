package com.huamanager.modem.router.app.ui.ads

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

object AdsManager {
    private const val TAG = "AdsManager"

    // Google AdMob Production Ad Unit IDs
    const val BANNER_AD_UNIT_ID = "ca-app-pub-7986150590993691/7348826102"
    const val NATIVE_AD_UNIT_ID = "ca-app-pub-7986150590993691/2096499428"
    const val REWARDED_AD_UNIT_ID = "ca-app-pub-7986150590993691/5065268538"

    private var rewardedAd: RewardedAd? = null
    private var isAdLoading = false

    private val pendingLoadedCallbacks = mutableListOf<() -> Unit>()
    private val pendingFailedCallbacks = mutableListOf<(String) -> Unit>()

    fun loadRewardedAd(
        context: Context,
        onLoaded: (() -> Unit)? = null,
        onFailed: ((String) -> Unit)? = null
    ) {
        if (rewardedAd != null) {
            onLoaded?.invoke()
            return
        }

        if (onLoaded != null) pendingLoadedCallbacks.add(onLoaded)
        if (onFailed != null) pendingFailedCallbacks.add(onFailed)

        if (isAdLoading) {
            Log.d(TAG, "RewardedAd is already loading in background, queued listener.")
            return
        }

        isAdLoading = true
        Log.d(TAG, "Requesting new RewardedAd from Google AdMob...")
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            context.applicationContext,
            REWARDED_AD_UNIT_ID,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isAdLoading = false
                    Log.d(TAG, "RewardedAd successfully loaded and ready to display")
                    val loaded = ArrayList(pendingLoadedCallbacks)
                    pendingLoadedCallbacks.clear()
                    pendingFailedCallbacks.clear()
                    loaded.forEach { it.invoke() }
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                    isAdLoading = false
                    val errorMsg = "${error.message} (code: ${error.code})"
                    Log.e(TAG, "Failed to load RewardedAd: $errorMsg")
                    val failed = ArrayList(pendingFailedCallbacks)
                    pendingLoadedCallbacks.clear()
                    pendingFailedCallbacks.clear()
                    failed.forEach { it.invoke(errorMsg) }
                }
            }
        )
    }

    fun isRewardedAdReady(): Boolean = rewardedAd != null

    fun showRewardedAd(
        activity: Activity,
        onRewardEarned: () -> Unit,
        onDismissed: (() -> Unit)? = null,
        onFailed: ((String) -> Unit)? = null
    ) {
        val ad = rewardedAd
        if (ad != null) {
            var earnedReward = false

            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "RewardedAd opened and displayed full screen")
                }

                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "RewardedAd closed by user")
                    rewardedAd = null
                    // Immediately preload the next ad in background so subsequent requests are instant
                    loadRewardedAd(activity.applicationContext)

                    if (earnedReward) {
                        onRewardEarned()
                    }
                    onDismissed?.invoke()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.e(TAG, "RewardedAd failed to show: ${adError.message}")
                    rewardedAd = null
                    loadRewardedAd(activity.applicationContext)
                    onFailed?.invoke(adError.message)
                }
            }

            ad.show(activity) { rewardItem ->
                Log.d(TAG, "User earned reward: ${rewardItem.amount} ${rewardItem.type}")
                earnedReward = true
            }
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
                onFailed?.invoke("Waktu memuat video habis (koneksi lambat). Silakan periksa koneksi internet.")
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
