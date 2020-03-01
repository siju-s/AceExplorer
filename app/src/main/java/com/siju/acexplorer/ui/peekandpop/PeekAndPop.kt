package com.siju.acexplorer.ui.peekandpop

import android.animation.Animator
import android.content.res.Configuration
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import com.siju.acexplorer.R
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.main.model.groups.CategoryHelper.isPeekPopCategory
import com.siju.acexplorer.ui.autoplay.AutoPlayContainer
import com.siju.acexplorer.ui.peekandpop.DimensionUtil.convertDpToPx

private const val PEEK_VIEW_MARGIN_DP = 12
private const val ANIMATION_PEEK_DURATION = 275
private const val ANIMATION_POP_DURATION = 250

open class PeekAndPop(var builder: Builder) {
    private lateinit var peekView: View
    private lateinit var peekLayout: ViewGroup
    private lateinit var thumbImage: ImageView
    private lateinit var autoPlayView: AutoPlayContainer
    private lateinit var shareButton: ImageButton
    private lateinit var infoButton: ImageButton
    private lateinit var previousButton: ImageButton
    private lateinit var nextButton: ImageButton

    private var contentView: ViewGroup?= null
    private var peekViewOriginalPosition : FloatArray? = null
    private var peekAnimationHelper: PeekAnimationHelper? = null
    private var onGeneralActionListener: OnGeneralActionListener? = null
    private var onClickListener: OnClickListener? = null
    private var orientation = 0
    private var peekViewMargin = 0
    var isEnabled = true

    init {
        onGeneralActionListener = builder.onGeneralActionListener
        orientation = builder.activity.resources.configuration.orientation
        peekViewMargin = convertDpToPx(builder.activity.applicationContext, PEEK_VIEW_MARGIN_DP)
        initialisePeekView()
    }

    private fun initialisePeekView() {
        val inflater = LayoutInflater.from(builder.activity)
        contentView = builder.activity.findViewById<View>(android.R.id.content)?.rootView as ViewGroup?
        // Center onPeek view in the onPeek layout and add to the container view group
        peekLayout = inflater.inflate(R.layout.peek_background, contentView, false) as FrameLayout
        peekView = inflater.inflate(builder.peekLayoutId, peekLayout, false)
        peekView.id = R.id.peek_view
        thumbImage = peekView.findViewById(R.id.imagePeekView)
        autoPlayView = peekView.findViewById(R.id.autoPlayView)
        shareButton = peekView.findViewById(R.id.imageButtonShare)
        infoButton = peekView.findViewById(R.id.imageButtonInfo)
        previousButton = peekView.findViewById(R.id.buttonPrev)
        nextButton = peekView.findViewById(R.id.buttonNext)
        val layoutParams = peekView.layoutParams as FrameLayout.LayoutParams
        layoutParams.gravity = Gravity.CENTER
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            layoutParams.topMargin = peekViewMargin
        }
        peekLayout.addView(peekView, layoutParams)
        contentView?.addView(peekLayout)
        peekLayout.visibility = View.GONE
        peekLayout.alpha = 0f
        peekLayout.requestLayout()
        peekAnimationHelper = PeekAnimationHelper(builder.activity.applicationContext, peekLayout, peekView)
        bringViewsToFront()
        initialiseViewTreeObserver()
        resetViews()
    }

    /**
     * If lollipop or above, use elevation to bring peek views to the front
     */
    private fun bringViewsToFront() {
        peekLayout.elevation = 10f
        peekView.elevation = 10f
    }

    fun getPeekView() = peekView

    /**
     * Once the onPeek view has inflated fully, this will also update if the view changes in size change
     */
    private fun initialiseViewTreeObserver() {
        peekView.viewTreeObserver.addOnGlobalLayoutListener { initialisePeekViewOriginalPosition() }
    }

    private fun initialiseGestureListener(view: View, position: Int, category: Category) {
        val peekAndPopClickListener = PeekAndPopClickListener(position, category)
        thumbImage.setOnClickListener(peekAndPopClickListener)
        autoPlayView.setOnClickListener(peekAndPopClickListener)
        infoButton.setOnClickListener(peekAndPopClickListener)
        shareButton.setOnClickListener(peekAndPopClickListener)
        view.setOnClickListener(peekAndPopClickListener)
        previousButton.setOnClickListener(peekAndPopClickListener)
        nextButton.setOnClickListener(peekAndPopClickListener)
        peekLayout.setOnTouchListener(PeekAndPopOnTouchListener(position))
    }

    /**
     * Check if user has moved or lifted their finger.
     *
     *
     * If lifted, onPop the view and check if their is a drag to action listener, check
     * if it had been dragged enough and send an event if so.
     *
     *
     * If moved, check if the user has entered the bounds of the onPeek view.
     * If the user is within the bounds, and is at the edges of the view, then
     * move it appropriately.
     */
    private fun handleTouch(view: View, event: MotionEvent, position: Int) {
        if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_CANCEL) {
            pop(view, position)
        }
    }

    /**
     * Initialise the peek view original position to be centred in the middle of the screen.
     */
    private fun initialisePeekViewOriginalPosition() {
        peekViewOriginalPosition = FloatArray(2)
        peekViewOriginalPosition?.let {
            it[0] = (peekLayout.width / 2 - peekView.width / 2).toFloat()
            it[1] = (peekLayout.height / 2 - peekView.height / 2 + peekViewMargin).toFloat()
        }
    }

    /**
     * Animate the peek view in and send an on peek event
     *
     * @param longClickView the view that was long clicked
     * @param index         the view that long clicked
     */
    private fun peek(longClickView: View, index: Int) {
        onGeneralActionListener?.onPeek(longClickView, index)
        peekView.visibility = View.VISIBLE
        peekLayout.visibility = View.VISIBLE
        peekLayout.background.alpha = 240
        peekAnimationHelper?.animatePeek(ANIMATION_PEEK_DURATION)
        builder.parentViewGroup?.requestDisallowInterceptTouchEvent(true)
    }

    /**
     * Animate the peek view in and send a on pop event.
     * Reset all the views and after the peek view has animated out, reset it's position.
     *
     * @param longClickView the view that was long clicked
     * @param index         the view that long clicked
     */
    private fun pop(longClickView: View, index: Int) {
        if (onGeneralActionListener != null) {
            onGeneralActionListener?.onPop(longClickView, index)
        }
        peekAnimationHelper?.animatePop(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {
                Log.d("Peek", "onAnimationEnd: ")
                resetViews()
                animation.cancel()
                peekView.visibility = View.GONE
            }

            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        }, ANIMATION_POP_DURATION)
    }

    /**
     * Reset all views back to their initial values, this done after the onPeek has popped.
     */
    fun resetViews() {
        Log.d("Peek", "resetViews: ")
        peekLayout.visibility = View.GONE
        peekViewOriginalPosition?.let {
            peekView.x = it[0]
            peekView.y = it[1]
        }
        peekView.scaleX = 0.85f
        peekView.scaleY = 0.85f
    }

    fun setOnGeneralActionListener(onGeneralActionListener: OnGeneralActionListener?) {
        this.onGeneralActionListener = onGeneralActionListener
    }

    fun setOnClickListener(onClickListener: OnClickListener?) {
        this.onClickListener = onClickListener
    }

    /**
     * Adds a view to receive long click and touch events
     * @param view     view to receive events
     * @param position add position of view if in a list, this will be returned in the general action listener
     * @param category
     */
    fun addClickView(view: View, position: Int, category: Category) {
        initialiseGestureListener(view, position, category)
    }

    class Builder(val activity: AppCompatActivity) {
        var peekLayoutId = -1
        // optional extras
        var parentViewGroup: ViewGroup? = null
        var onGeneralActionListener: OnGeneralActionListener? = null
        /**
         * Peek layout resource id, which will be inflated into the onPeek view
         *
         * @param peekLayoutId id of the onPeek layout resource
         * @return
         */
        fun peekLayout(@LayoutRes peekLayoutId: Int): Builder {
            this.peekLayoutId = peekLayoutId
            return this
        }

        /**
         * If the container view is situated within another view that receives touch events (like a scroll view),
         * the touch events required for the onPeek and onPop will not work correctly so use this method to disallow
         * touch events from the parent view.
         *
         * @param parentViewGroup The parentView that you wish to disallow touch events to (Usually a scroll view, recycler view etc.)
         * @return
         */
        fun parentViewGroupToDisallowTouchEvents(parentViewGroup: ViewGroup): Builder {
            this.parentViewGroup = parentViewGroup
            return this
        }

        /**
         * Create the PeekAndPop object
         *
         * @return the PeekAndPop object
         */
        fun build(): PeekAndPop {
            require(peekLayoutId != -1) { "No peekLayoutId specified." }
            return PeekAndPop(this)
        }

    }

    fun isNextPrevIcon(view: View): Boolean {
        return view.id == R.id.buttonNext || view.id == R.id.buttonPrev
    }

    protected inner class PeekAndPopOnTouchListener internal constructor(var position: Int) : View.OnTouchListener {
        override fun onTouch(view: View, event: MotionEvent): Boolean {
            if (!isEnabled) {
                return false
            }
            handleTouch(view, event, position)
            view.performClick()
            return true
        }
    }

    protected inner class PeekAndPopClickListener internal constructor(var position: Int, private val category: Category) : View.OnClickListener {
        override fun onClick(v: View) {
            Log.d("PeekListener", "onClick:$position")
            val canShowPeek = onClickListener?.canShowPeek() == true && isPeekPopCategory(category)
            if (!canShowPeek) {
                onClickListener?.onClick(v, position, false)
                return
            }
            if (v.id == R.id.imageIcon) {
                peek(v, position)
            } else if (v.id == R.id.buttonNext || v.id == R.id.buttonPrev) {
                onClickListener?.onClick(v, position, true)
            } else {
                onClickListener?.onClick(v, position, true)
                pop(v, position)
            }
        }

    }

    interface OnGeneralActionListener {
        fun onPeek(longClickView: View, position: Int)
        fun onPop(longClickView: View, position: Int)
    }

    interface OnClickListener {
        fun onClick(view: View, position: Int, canShowPeek: Boolean)
        fun canShowPeek(): Boolean
    }
}