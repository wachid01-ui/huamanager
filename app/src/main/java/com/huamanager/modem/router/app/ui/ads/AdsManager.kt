package com.huamanager.modem.router.app.ui.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

object AdsManager {
    private const val TAG = "AdsManager"

    // Google AdMob Official Test Ad Unit IDs
    const val BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
    const val NATIVE_AD_UNIT_ID = "ca-app-pub-3940256099942544/2247696110"
    const val REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"

    private var rewardedAd: RewardedAd? = null
    private var isAdLoading = false

    fun loadRewardedAd(
        context: Context,
        onLoaded: (() -> Unit)? = null,
        onFailed: ((String) -> Unit)? = null
    ) {
        if (rewardedAd != null) {
            onLoaded?.invoke()
            return
        }
        if (isAdLoading) return

        isAdLoading = true
        Log.d(TAG, "Requesting new RewardedAd from Google AdMob...")
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            context,
            REWARDED_AD_UNIT_ID,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isAdLoading = false
                    Log.d(TAG, "RewardedAd successfully loaded and ready to display")
                    onLoaded?.invoke()
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                    isAdLoading = false
                    val errorMsg = "${error.message} (code: ${error.code})"
                    Log.e(TAG, "Failed to load RewardedAd: $errorMsg")
                    onFailed?.invoke(errorMsg)
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
                    // Preload next ad for future use
                    loadRewardedAd(activity)

                    if (earnedReward) {
                        onRewardEarned()
                    }
                    onDismissed?.invoke()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.e(TAG, "RewardedAd failed to show: ${adError.message}")
                    rewardedAd = null
                    loadRewardedAd(activity)
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
        } else {
            loadRewardedAd(
                context = activity,
                onLoaded = {
                    showRewardedAd(
                        activity = activity,
                        onRewardEarned = onRewardEarned,
                        onDismissed = onDismissed,
                        onFailed = onFailed
                    )
                },
                onFailed = { errorMsg ->
                    onFailed?.invoke(errorMsg)
                }
            )
        }
    }
}
