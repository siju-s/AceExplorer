package com.siju.acexplorer.ui.autoplay

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.ImageView

class AutoPlayContainer : FrameLayout {
    lateinit var customVideoView: AutoPlayView
    private set

    constructor(context: Context?) : super(context!!) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context!!, attrs, defStyleAttr) {
        init()
    }

    fun init() {
        customVideoView = AutoPlayView(context)
        val image = ImageView(context)
        image.scaleType = ImageView.ScaleType.CENTER_CROP
        this.addView(customVideoView)
        this.addView(image)
    }

    fun cleanup() {
        removeAllViews()
    }
}