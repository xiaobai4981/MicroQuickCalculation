package io.github.subhamtyagi.quickcalculation.ads;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.initialization.AdapterStatus;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

import java.util.Map;

public final class AdManager {
    private static final String TAG = "AdMobManager";

    private static volatile AdManager sInstance;
    public static AdManager getInstance() {
        if (sInstance == null) {
            synchronized (AdManager.class) {
                if (sInstance == null) sInstance = new AdManager();
            }
        }
        return sInstance;
    }
    private AdManager() {}

    private Context appContext;
    private boolean initialized = false;

    private InterstitialAd interstitialAd;
    private RewardedAd rewardedAd;

    /** 在 Application.onCreate() 或首个 Activity.onCreate() 调用一次 */
    public void init(Context context) {
        if (initialized) return;
        this.appContext = context.getApplicationContext();

        // 按你原逻辑：后台线程初始化，初始化成功后预加载两种广告
        new Thread(() -> MobileAds.initialize(appContext, (InitializationStatus status) -> {
            for (Map.Entry<String, AdapterStatus> entry : status.getAdapterStatusMap().entrySet()) {
                if (entry.getValue().getInitializationState() == AdapterStatus.State.READY) {
                    loadInterstitialAd();
                    loadRewardedAd();
                }
            }
            initialized = true;
        })).start();
    }

    // ================== 插屏 ==================
    public void loadInterstitialAd() {
        if (appContext == null) return;
        InterstitialAd.load(
                appContext,
                AdUnitId.INTERSTITIAL_ADUNITID,
                new AdRequest.Builder().build(),
                new InterstitialAdLoadCallback() {
                    @Override public void onAdLoaded(@NonNull InterstitialAd ad) {
                        interstitialAd = ad;
                    }
                    @Override public void onAdFailedToLoad(@NonNull LoadAdError e) {
                        Log.d(TAG, "Interstitial load fail: " + e.getMessage());
                        interstitialAd = null;
                    }
                }
        );
    }

    /** 仅在可用时展示；不可用则仅执行 afterAdClosed（不触发加载） */
    public void showInterstitialAd(Activity activity, @Nullable Runnable afterAdClosed) {
        if (interstitialAd == null) {
            if (afterAdClosed != null) activity.runOnUiThread(afterAdClosed);
            return;
        }
        interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override public void onAdDismissedFullScreenContent() {
                interstitialAd = null;
                loadInterstitialAd(); // 播放结束后再预取
                if (afterAdClosed != null) activity.runOnUiThread(afterAdClosed);
            }
            @Override public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                interstitialAd = null;
                loadInterstitialAd();
                if (afterAdClosed != null) activity.runOnUiThread(afterAdClosed);
            }
        });
        activity.runOnUiThread(() -> interstitialAd.show(activity));
    }

    public void showInterstitialAd(Activity activity) { showInterstitialAd(activity, null); }
    public boolean isInterstitialReady() { return interstitialAd != null; }

    // ================== 激励 ==================
    public void loadRewardedAd() {
        if (appContext == null) return;
        RewardedAd.load(
                appContext,
                AdUnitId.REWRDVIDEO_ADUNITID,
                new AdRequest.Builder().build(),
                new RewardedAdLoadCallback() {
                    @Override public void onAdLoaded(@NonNull RewardedAd ad) {
                        rewardedAd = ad;
                    }
                    @Override public void onAdFailedToLoad(@NonNull LoadAdError e) {
                        Log.d(TAG, "Rewarded load fail: " + e.getMessage());
                        rewardedAd = null;
                    }
                }
        );
    }

    /** 仅在可用时展示；不可用则仅执行 afterAdClosed（不触发加载） */
    public void showRewardedAd(Activity activity,
                               @Nullable OnUserEarnedRewardListener rewardListener,
                               @Nullable Runnable afterAdClosed) {
        if (rewardedAd == null) {
            if (afterAdClosed != null) activity.runOnUiThread(afterAdClosed);
            return;
        }
        rewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override public void onAdDismissedFullScreenContent() {
                rewardedAd = null;
                loadRewardedAd(); // 播放结束后再预取
                if (afterAdClosed != null) activity.runOnUiThread(afterAdClosed);
            }
            @Override public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                rewardedAd = null;
                loadRewardedAd();
                if (afterAdClosed != null) activity.runOnUiThread(afterAdClosed);
            }
        });
        activity.runOnUiThread(() ->
                rewardedAd.show(activity, reward -> {
                    if (rewardListener != null) rewardListener.onUserEarnedReward(reward);
                })
        );
    }

    public void showRewardedAd(Activity activity, @Nullable Runnable afterAdClosed) {
        showRewardedAd(activity, null, afterAdClosed);
    }
    public void showRewardedAd(Activity activity) { showRewardedAd(activity, null, null); }
    public boolean isRewardedReady() { return rewardedAd != null; }

    // ============== 概率触发（保持你的简洁形态） ==============
    /** 简洁版：50% 触发；激励40% / 插屏60%（不带回调、不做可用性兜底） */
    public void maybeShowAd(Activity activity) {
        if (Math.random() < 0.5) {
            if (Math.random() < 0.4) {
                showRewardedAdConfirmDialog(activity);
            } else {
                activity.runOnUiThread(() -> showInterstitialAd(activity));
            }
        }
    }
    private void showRewardedAdConfirmDialog(Activity activity) {
        if (rewardedAd == null) {
            Log.d(TAG, "RewardedAd not ready, skip dialog");
            return;
        }

        activity.runOnUiThread(() -> {
            new android.app.AlertDialog.Builder(activity)
                    .setTitle("WATCH AD TO SUPPORT US?")
                    .setMessage("Would you？")
                    .setCancelable(true)
                    .setPositiveButton("YES", (dialog, which) -> {
                        dialog.dismiss();
                        showRewardedAd(activity);
                    })
                    .setNegativeButton("NO", (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .show();
        });
    }


    /** 带回调：若选中的广告不可用，则直接执行 afterAdClosed（不尝试切换、也不触发加载） */
    public void maybeShowAd(Activity activity, @Nullable Runnable afterAdClosed) {
        if (Math.random() < 0.5) {
            boolean pickRewarded = Math.random() < 0.4;
            if (pickRewarded) {
                activity.runOnUiThread(() -> showRewardedAd(activity, afterAdClosed));
            } else {
                activity.runOnUiThread(() -> showInterstitialAd(activity, afterAdClosed));
            }
        } else {
            if (afterAdClosed != null) activity.runOnUiThread(afterAdClosed);
        }
    }
}
