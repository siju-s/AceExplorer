package com.siju.acexplorer.main

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManagerFactory
import com.kobakei.ratethisapp.RateThisApp

private const val TAG = "ReviewManager"
class ReviewManager(val activity: AppCompatActivity) {

    private val manager = ReviewManagerFactory.create(activity)

    fun checkCanShowReviewDialog() {
        RateThisApp.onCreate(activity)
        if (RateThisApp.shouldShowRateDialog()) {
            showReviewDialog()
        }
    }

    private fun showReviewDialog() {
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val reviewInfo = task.result
                showPlayStoreReviewDialog(activity, reviewInfo)
            } else {
                showCustomRateDialog()
            }
        }
    }

    private fun showPlayStoreReviewDialog(activity : AppCompatActivity, reviewInfo: ReviewInfo) {
        val flow = manager.launchReviewFlow(activity, reviewInfo)
        flow.addOnCompleteListener {
            Log.d(TAG, "showPlayStoreReviewDialog: ")
            // The flow has finished. The API does not indicate whether the user
            // reviewed or not, or even whether the review dialog was shown. Thus, no
            // matter the result, we continue our app flow.
        }
    }

    private fun showCustomRateDialog() {
        RateThisApp.showRateDialog(activity)
    }
}