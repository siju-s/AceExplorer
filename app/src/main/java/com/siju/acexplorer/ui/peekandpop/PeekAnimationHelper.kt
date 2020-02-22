package com.siju.acexplorer.ui.peekandpop

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.res.Configuration
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator

/**
 *
 *
 * Helper class for animating the PeekAndPop views
 */
internal class PeekAnimationHelper(private val context: Context, private val peekLayout: View, private val peekView: View) {
    /**
     * Occurs on on long hold.
     *
     *
     * Animates the peek view to fade in and scale to it's full size.
     * Also fades the peek background layout in.
     */
    fun animatePeek(duration: Int) {
        peekView.alpha = 1f
        val animatorLayoutAlpha = ObjectAnimator.ofFloat(peekLayout, "alpha", 1f)
        animatorLayoutAlpha.interpolator = OvershootInterpolator(1.2f)
        animatorLayoutAlpha.duration = duration.toLong()
        val animatorScaleX = ObjectAnimator.ofFloat(peekView, "scaleX", 1f)
        animatorScaleX.duration = duration.toLong()
        val animatorScaleY = ObjectAnimator.ofFloat(peekView, "scaleY", 1f)
        animatorScaleY.duration = duration.toLong()
        val animatorSet = AnimatorSet()
        animatorSet.interpolator = OvershootInterpolator(1.2f)
        animatorSet.play(animatorScaleX).with(animatorScaleY)
        animatorSet.start()
        animatorLayoutAlpha.start()
    }

    /**
     * Occurs on touch up.
     *
     *
     * Animates the peek view to return to it's original position and shrink.
     * Also animate the peek background layout to fade out.
     */
    fun animatePop(animatorListener: Animator.AnimatorListener?, duration: Int) {
        val animatorLayoutAlpha = ObjectAnimator.ofFloat(peekLayout, "alpha", 0f)
        animatorLayoutAlpha.duration = duration.toLong()
        animatorLayoutAlpha.addListener(animatorListener)
        animatorLayoutAlpha.interpolator = DecelerateInterpolator(1.5f)
        animatorLayoutAlpha.start()
        animateReturn(duration)
    }

    /**
     * Occurs when the peek view is dragged but not flung.
     *
     *
     * Animate the peek view back to it's original position and shrink it.
     */
    private fun animateReturn(duration: Int) {
        val animatorTranslate = if (context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            ObjectAnimator.ofFloat(peekView, "translationY", 0f)
        } else {
            ObjectAnimator.ofFloat(peekView, "translationX", 0f)
        }
        val animatorShrinkY = ObjectAnimator.ofFloat(peekView, "scaleY", 0.75f)
        val animatorShrinkX = ObjectAnimator.ofFloat(peekView, "scaleX", 0.75f)
        animatorShrinkX.interpolator = DecelerateInterpolator()
        animatorShrinkY.interpolator = DecelerateInterpolator()
        animatorTranslate.interpolator = DecelerateInterpolator()
        animatorShrinkX.duration = duration.toLong()
        animatorShrinkY.duration = duration.toLong()
        animatorTranslate.duration = duration.toLong()
        animatorShrinkX.start()
        animatorShrinkY.start()
        animatorTranslate.start()
    }
}