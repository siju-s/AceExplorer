package com.siju.acexplorer.ads

import android.content.Context
import android.util.Log
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent

import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.siju.acexplorer.AceApplication
import com.siju.acexplorer.R

class AdsView(private val view: ViewGroup) : LifecycleObserver {

    val context: Context = view.context
    private var adView: AdView? = null

    private val adContainer: LinearLayout
        get() = view.findViewById(R.id.adviewLayout)

    fun showAds() {
        Log.e(this.javaClass.simpleName, "showAds")
        if (adView == null) {
            createAd()
        }
        else {
            adContainer.removeAllViews()
        }
        if (adView?.isLoading == false) {
            loadAd()
        }
        addView(adContainer)
    }

    private fun createAd() {
        adView = AdView(AceApplication.appContext)
        adView?.adSize = AdSize.BANNER
        adView?.adUnitId = context.resources.getString(R.string.banner_ad_unit_id)
    }

    private fun loadAd() {
        val adRequest = AdRequest.Builder().build()
        adView?.loadAd(adRequest)
    }

    private fun addView(adviewLayout: ViewGroup) {
        adviewLayout.addView(adView)
    }

    fun hideAds() {
        Log.e(this.javaClass.simpleName, "hideAds")
        val adviewLayout = adContainer
        if (adviewLayout.childCount != 0) {
            adviewLayout.removeView(adView)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun pauseAds() {
        Log.e("AdView", "pauseAds")
        adView?.pause()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun resumeAds() {
        Log.e("AdView", "resumeAds")
        adView?.resume()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun destroyAds() {
        adView?.destroy()
    }
}
