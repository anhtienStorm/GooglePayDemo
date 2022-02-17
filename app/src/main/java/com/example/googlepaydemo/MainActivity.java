package com.example.googlepaydemo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private AdView adView, adView2;
    private RewardedAd mRewardedAd;
    private AdRequest mAdRequest;
    private Button mLoadAds, mGooglePay;

    private BillingClient mBillingClient;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        adView = findViewById(R.id.adView);
        adView2 = findViewById(R.id.adView2);
        mLoadAds = findViewById(R.id.load_ads);
        mGooglePay = findViewById(R.id.google_pay);

        mAdRequest = new AdRequest.Builder().build();

        adView.loadAd(mAdRequest);
        adView2.loadAd(mAdRequest);

        mLoadAds.setOnClickListener(view -> {
            loadAdsRewarded();
        });

        mBillingClient = BillingClient.newBuilder(this)
                .enablePendingPurchases()
                .setListener((billingResult, list) -> {

                })
                .build();
        connectToGooglePlayBilling();
    }

    private void connectToGooglePlayBilling() {
        mBillingClient.startConnection(
                new BillingClientStateListener() {
                    @Override
                    public void onBillingServiceDisconnected() {

                    }

                    @Override
                    public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                            getProductDetails();
                        }
                    }
                }
        );
    }

    private void getProductDetails() {
        List<String> productIds = new ArrayList<>();
        productIds.add("android.test.purchased");
        SkuDetailsParams getProductDetailsQuery = SkuDetailsParams
                .newBuilder()
                .setSkusList(productIds)
                .setType(BillingClient.SkuType.INAPP)
                .build();
        mBillingClient.querySkuDetailsAsync(
                getProductDetailsQuery,
                (billingResult, list) -> {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK &&
                    list != null) {
                        SkuDetails itemInfo = list.get(0);
                        mGooglePay.setText(itemInfo.getPrice());
                        mGooglePay.setOnClickListener(view -> {
                            mBillingClient.launchBillingFlow(
                                    this,
                                    BillingFlowParams.newBuilder().setSkuDetails(itemInfo).build()
                            );
                        });
                    }
                }
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void loadAdsRewarded() {
        RewardedAd.load(this, "ca-app-pub-3940256099942544/5224354917",
                mAdRequest, new RewardedAdLoadCallback() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error.
                        Log.d("TienNAb", "onAdFailedToLoad: " + loadAdError.getMessage());
                        mRewardedAd = null;
                    }

                    @Override
                    public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                        mRewardedAd = rewardedAd;
                        Log.d("TienNAb", "Ad was loaded.");
                        showAdsRewarded();
                    }
                });
    }

    public void showAdsRewarded() {
        if (mRewardedAd != null) {
            mRewardedAd.show(MainActivity.this, rewardItem -> {
                // Handle the reward.
                Log.d("TienNAb", "The user earned the reward.");
                int rewardAmount = rewardItem.getAmount();
                String rewardType = rewardItem.getType();
            });
        } else {
            Log.d("TienNAb", "The rewarded ad wasn't ready yet.");
        }
    }
}