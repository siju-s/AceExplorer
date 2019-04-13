package com.siju.acexplorer.ads;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.siju.acexplorer.AceApplication;
import com.siju.acexplorer.R;
import com.siju.acexplorer.main.model.FileConstants;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import static com.siju.acexplorer.main.model.FileConstants.ADS;

public class AdsView {

    private Context   context;
    private ViewGroup view;
    private AdView    adView;

    public AdsView(ViewGroup viewGroup) {
        this.context = viewGroup.getContext();
        this.view = viewGroup;
    }

    public void showAds() {
        LinearLayout adviewLayout = getAdContainer();
        if (adView == null) {
            createAd();
            loadAd();
            addView(adviewLayout);
        } else {
            ((LinearLayout) adView.getParent()).removeAllViews();
            addView(adviewLayout);
            if (!adView.isLoading()) {
                loadAd();
            }
        }
        loadAd();
    }

    private LinearLayout getAdContainer() {
        return view.findViewById(R.id.adviewLayout);
    }

    private void createAd() {
        adView = new AdView(AceApplication.getAppContext());
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId(getContext().getResources().getString(R.string.banner_ad_unit_id));
    }

    private void loadAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    private void addView(LinearLayout adviewLayout) {
        adviewLayout.addView(adView);
    }

    public void hideAds() {
        LinearLayout adviewLayout = getAdContainer();
        if (adviewLayout.getChildCount() != 0) {
            adviewLayout.removeView(adView);
        }
    }

    public void pauseAds() {
        if (adView != null) {
            adView.pause();
        }
    }

    public void resumeAds() {
        if (adView != null) {
            adView.resume();
        }
    }

    public void destroyAds() {
        if (adView != null) {
            adView.destroy();
        }
    }

    public Context getContext() {
        return context;
    }

    private BroadcastReceiver adsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                return;
            }
            if (ADS.equals(intent.getAction())) {
                boolean isPremium = intent.getBooleanExtra(FileConstants.KEY_PREMIUM, false);
                if (isPremium) {
                    onPremiumVersion();
                } else {
                    onFreeVersion();
                }
            }
        }
    };

    private void onFreeVersion() {
        if (adResultListener != null) {
            adResultListener.onFreeVersion();
        }
    }

    private void onPremiumVersion() {
        if (adResultListener != null) {
            adResultListener.onPremiumVersion();
        }
    }

    private AdResultListener adResultListener;

    public void setAdResultListener(AdResultListener adResultListener) {
        this.adResultListener = adResultListener;
    }

    public void register() {
        IntentFilter intentFilter = new IntentFilter(ADS);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(adsReceiver, intentFilter);
    }

    public void unregister() {
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(adsReceiver);
    }

    public interface AdResultListener {

        void onFreeVersion();

        void onPremiumVersion();
    }
}
