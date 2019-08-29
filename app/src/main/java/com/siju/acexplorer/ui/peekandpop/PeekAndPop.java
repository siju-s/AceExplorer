package com.siju.acexplorer.ui.peekandpop;

import android.animation.Animator;
import android.content.res.Configuration;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.siju.acexplorer.R;
import com.siju.acexplorer.main.model.groups.Category;
import com.siju.acexplorer.main.model.groups.CategoryHelper;
import com.siju.acexplorer.ui.autoplay.AutoPlayContainer;

import java.util.ArrayList;

public class PeekAndPop {

    private static final int PEEK_VIEW_MARGIN_DP = 12;

    private static final int ANIMATION_PEEK_DURATION = 275;
    private static final int ANIMATION_POP_DURATION  = 250;

    protected Builder             builder;
    private   View                peekView;
    private   ViewGroup           contentView;
    private   ViewGroup           peekLayout;
    private   PeekAnimationHelper peekAnimationHelper;

    private boolean enabled = true;

    private OnGeneralActionListener onGeneralActionListener;
    private OnClickListener         onClickListener;
    protected int     orientation;
    private   float[] peekViewOriginalPosition;
    private   int     peekViewMargin;
    private ImageView         thumbImage;
    private AutoPlayContainer autoPlayView;
    private ImageButton       shareButton;
    private ImageButton       infoButton;
    private ImageButton       previousButton;
    private ImageButton       nextButton;

    public PeekAndPop(Builder builder) {
        this.builder = builder;
        init();
    }

    protected void init() {
        this.onGeneralActionListener = builder.onGeneralActionListener;
//        this.gestureListener = new GestureListener();
//        this.gestureDetector = new GestureDetector(builder.activity, this.gestureListener);
        initialiseGestureListeners();
        this.orientation = builder.activity.getResources().getConfiguration().orientation;
        this.peekViewMargin = DimensionUtil.convertDpToPx(builder.activity.getApplicationContext(), PEEK_VIEW_MARGIN_DP);
        initialisePeekView();
    }

    /**
     * Inflate the peekView, add it to the peekLayout with a shaded/blurred background,
     * bring it to the front and set the peekLayout to have an alpha of 0. Get the peekView's
     * original Y position for use when dragging.
     * <p/>
     * If a flingToActionViewLayoutId is supplied, inflate the flingToActionViewLayoutId.
     */
    private void initialisePeekView() {
        LayoutInflater inflater = LayoutInflater.from(builder.activity);
        contentView = (ViewGroup) builder.activity.findViewById(android.R.id.content).getRootView();

        // Center onPeek view in the onPeek layout and add to the container view group
        peekLayout = (FrameLayout) inflater.inflate(R.layout.peek_background, contentView, false);
        peekView = inflater.inflate(builder.peekLayoutId, peekLayout, false);
        peekView.setId(R.id.peek_view);

        thumbImage = peekView.findViewById(R.id.imagePeekView);
        autoPlayView = peekView.findViewById(R.id.autoPlayView);
        shareButton = peekView.findViewById(R.id.imageButtonShare);
        infoButton = peekView.findViewById(R.id.imageButtonInfo);
        previousButton = peekView.findViewById(R.id.buttonPrev);
        nextButton = peekView.findViewById(R.id.buttonNext);

        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) peekView.getLayoutParams();
        layoutParams.gravity = Gravity.CENTER;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            layoutParams.topMargin = peekViewMargin;
        }

        peekLayout.addView(peekView, layoutParams);
        contentView.addView(peekLayout);

        peekLayout.setVisibility(View.GONE);
        peekLayout.setAlpha(0);
        peekLayout.requestLayout();

        peekAnimationHelper = new PeekAnimationHelper(builder.activity.getApplicationContext(), peekLayout, peekView);

        bringViewsToFront();
        initialiseViewTreeObserver();
        resetViews();
    }

    /**
     * If lollipop or above, use elevation to bring peek views to the front
     */
    private void bringViewsToFront() {
        peekLayout.setElevation(10f);
        peekView.setElevation(10f);
    }

    /**
     * Once the onPeek view has inflated fully, this will also update if the view changes in size change
     */
    private void initialiseViewTreeObserver() {
        peekView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                initialisePeekViewOriginalPosition();
            }
        });
    }

    /**
     * Set an onClick and onTouch listener for each long click view.
     */
    private void initialiseGestureListeners() {
        for (int i = 0; i < builder.longClickViews.size(); i++) {
            initialiseGestureListener(builder.longClickViews.get(i), -1, Category.FILES);
        }
//        gestureDetector.setIsLongpressEnabled(false);
    }

    private void initialiseGestureListener(@NonNull View view, final int position, Category category) {

        PeekAndPopClickListener peekAndPopClickListener = new PeekAndPopClickListener(position, category);
        thumbImage.setOnClickListener(peekAndPopClickListener);
        autoPlayView.setOnClickListener(peekAndPopClickListener);
        infoButton.setOnClickListener(peekAndPopClickListener);
        shareButton.setOnClickListener(peekAndPopClickListener);
        view.setOnClickListener(peekAndPopClickListener);
        previousButton.setOnClickListener(peekAndPopClickListener);
        nextButton.setOnClickListener(peekAndPopClickListener);

        peekLayout.setOnTouchListener(new PeekAndPopOnTouchListener(position));
    }

    /**
     * Check if user has moved or lifted their finger.
     * <p/>
     * If lifted, onPop the view and check if their is a drag to action listener, check
     * if it had been dragged enough and send an event if so.
     * <p/>
     * If moved, check if the user has entered the bounds of the onPeek view.
     * If the user is within the bounds, and is at the edges of the view, then
     * move it appropriately.
     */
    private void handleTouch(@NonNull View view, @NonNull MotionEvent event, int position) {
        if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_CANCEL) {
            pop(view, position);
        }
    }


    /**
     * Initialise the peek view original position to be centred in the middle of the screen.
     */
    private void initialisePeekViewOriginalPosition() {
        peekViewOriginalPosition = new float[2];
        peekViewOriginalPosition[0] = (peekLayout.getWidth() / 2) - (peekView.getWidth() / 2);
        peekViewOriginalPosition[1] = (peekLayout.getHeight() / 2) - (peekView.getHeight() / 2) + peekViewMargin;
    }


    /**
     * Animate the peek view in and send an on peek event
     *
     * @param longClickView the view that was long clicked
     * @param index         the view that long clicked
     */
    private void peek(@NonNull View longClickView, int index) {
        if (onGeneralActionListener != null) {
            onGeneralActionListener.onPeek(longClickView, index);
        }
        peekView.setVisibility(View.VISIBLE);
        peekLayout.setVisibility(View.VISIBLE);
        peekLayout.getBackground().setAlpha(240);

        peekAnimationHelper.animatePeek(ANIMATION_PEEK_DURATION);

        if (builder.parentViewGroup != null) {
            builder.parentViewGroup.requestDisallowInterceptTouchEvent(true);
        }
    }

    /**
     * Animate the peek view in and send a on pop event.
     * Reset all the views and after the peek view has animated out, reset it's position.
     *
     * @param longClickView the view that was long clicked
     * @param index         the view that long clicked
     */
    private void pop(@NonNull View longClickView, int index) {
        if (onGeneralActionListener != null) {
            onGeneralActionListener.onPop(longClickView, index);
        }

        peekAnimationHelper.animatePop(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                Log.d("Peek", "onAnimationEnd: ");
                resetViews();
                animation.cancel();
                peekView.setVisibility(View.GONE);

            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        }, ANIMATION_POP_DURATION);

    }

    /**
     * Reset all views back to their initial values, this done after the onPeek has popped.
     */
    public void resetViews() {
        Log.d("Peek", "resetViews: ");

        peekLayout.setVisibility(View.GONE);
        if (peekViewOriginalPosition != null) {
            peekView.setX(peekViewOriginalPosition[0]);
            peekView.setY(peekViewOriginalPosition[1]);
        }
        peekView.setScaleX(0.85f);
        peekView.setScaleY(0.85f);

    }

    public boolean isPeekLayoutVisible() {
        return peekLayout.getVisibility() == View.VISIBLE;
    }

    public void destroy() {
        resetViews();
        builder = null;
    }


    public void setOnGeneralActionListener(@Nullable OnGeneralActionListener onGeneralActionListener) {
        this.onGeneralActionListener = onGeneralActionListener;
    }


    public void setOnClickListener(@Nullable OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    /**
     * Adds a view to receive long click and touch events
     *  @param view     view to receive events
     * @param position add position of view if in a list, this will be returned in the general action listener
     * @param category
     */
    public void addClickView(@NonNull View view, int position, Category category) {
        initialiseGestureListener(view, position, category);
    }

    public View getPeekView() {
        return peekView;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Builder class used for creating the PeekAndPop view.
     */

    public static class Builder {

        // essentials
        protected final AppCompatActivity activity;
        int peekLayoutId = -1;

        // optional extras
        ViewGroup       parentViewGroup;
        ArrayList<View> longClickViews;

        OnGeneralActionListener onGeneralActionListener;

        public Builder(@NonNull AppCompatActivity activity) {
            this.activity = activity;
            this.longClickViews = new ArrayList<>();
        }

        /**
         * Peek layout resource id, which will be inflated into the onPeek view
         *
         * @param peekLayoutId id of the onPeek layout resource
         * @return
         */
        public Builder peekLayout(@LayoutRes int peekLayoutId) {
            this.peekLayoutId = peekLayoutId;
            return this;
        }


        /**
         * A listener for the onPeek and onPop actions.
         *
         * @param onGeneralActionListener
         * @return
         */
        public Builder onGeneralActionListener(@NonNull OnGeneralActionListener onGeneralActionListener) {
            this.onGeneralActionListener = onGeneralActionListener;
            return this;
        }


        /**
         * If the container view is situated within another view that receives touch events (like a scroll view),
         * the touch events required for the onPeek and onPop will not work correctly so use this method to disallow
         * touch events from the parent view.
         *
         * @param parentViewGroup The parentView that you wish to disallow touch events to (Usually a scroll view, recycler view etc.)
         * @return
         */
        public Builder parentViewGroupToDisallowTouchEvents(@NonNull ViewGroup parentViewGroup) {
            this.parentViewGroup = parentViewGroup;
            return this;
        }


        /**
         * Create the PeekAndPop object
         *
         * @return the PeekAndPop object
         */
        public PeekAndPop build() {
            if (peekLayoutId == -1) {
                throw new IllegalArgumentException("No peekLayoutId specified.");
            }
            return new PeekAndPop(this);
        }
    }

    public boolean isNextPrevIcon(View view) {
        return view.getId() == R.id.buttonNext || view.getId() == R.id.buttonPrev;
    }

    protected class PeekAndPopOnTouchListener implements View.OnTouchListener {

        private int position;

        PeekAndPopOnTouchListener(int position) {
            this.position = position;
        }

        @Override
        public boolean onTouch(final View view, MotionEvent event) {
            if (!enabled) {
                return false;
            }

            handleTouch(view, event, position);
            view.performClick();
            return true;
        }


        public void setPosition(int position) {
            this.position = position;
        }

        public int getPosition() {
            return position;
        }
    }


    protected class PeekAndPopClickListener implements View.OnClickListener {

        private int position;
        private Category category;

        PeekAndPopClickListener(int position, Category category) {
            this.position = position;
            this.category = category;
        }

        @Override
        public void onClick(View v) {
            boolean canShowPeek = onClickListener.canShowPeek() && CategoryHelper.INSTANCE.isPeekPopCategory(category);

            if (!canShowPeek) {
                onClickListener.onClick(v, position, false);
                return;
            }

            if (v.getId() == R.id.imageIcon) {
                peek(v, position);
            }
            else if (v.getId() == R.id.buttonNext || v.getId() == R.id.buttonPrev) {
                onClickListener.onClick(v, position, true);
            }
            else {
                onClickListener.onClick(v, position, true);
                pop(v, position);
            }
        }

        public void setPosition(int position) {
            this.position = position;
        }

        public int getPosition() {
            return position;
        }
    }

//    protected class GestureListener extends GestureDetector.SimpleOnGestureListener {
//
//        private int  position;
//        private View view;
//
//        public void setView(View view) {
//            this.view = view;
//        }
//
//        public void setPosition(int position) {
//            this.position = position;
//        }
//
//        @Override
//        public boolean onDown(MotionEvent e) {
//            return true;
//        }
//
//        @Override
//        public boolean onFling(MotionEvent firstEvent, MotionEvent secondEvent, float velocityX, float velocityY) {
//            return false;
//        }
//
//        private boolean handleFling(float velocityX, float velocityY) {
//            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
//                if (velocityY < -FLING_VELOCITY_THRESHOLD && allowUpwardsFling) {
//                    flingToAction(FLING_UPWARDS, velocityX, velocityY);
//                    return false;
//                } else if (velocityY > FLING_VELOCITY_THRESHOLD && allowDownwardsFling) {
//                    flingToAction(FLING_DOWNWARDS, velocityX, velocityY);
//                    return false;
//                }
//            } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
//                if (velocityX < -FLING_VELOCITY_THRESHOLD && allowUpwardsFling) {
//                    flingToAction(FLING_UPWARDS, velocityX, velocityY);
//                    return false;
//                } else if (velocityX > FLING_VELOCITY_THRESHOLD && allowDownwardsFling) {
//                    flingToAction(FLING_DOWNWARDS, velocityX, velocityY);
//                    return false;
//                }
//            }
//            return true;
//        }
//
//        private void flingToAction(@FlingDirections int direction, float velocityX, float velocityY) {
//            if (animateFling) {
//                if (direction == FLING_UPWARDS) {
//                    peekAnimationHelper.animateExpand(ANIMATION_POP_DURATION, popTime);
//                    peekAnimationHelper.animateFling(velocityX, velocityY, ANIMATION_POP_DURATION, popTime, -FLING_VELOCITY_MAX);
//                } else {
//                    peekAnimationHelper.animateFling(velocityX, velocityY, ANIMATION_POP_DURATION, popTime, FLING_VELOCITY_MAX);
//                }
//            }
//        }
//    }
//
//    public interface OnFlingToActionListener {
//        void onFlingToAction(View longClickView, int position, int direction);
//    }

    public interface OnGeneralActionListener {
        void onPeek(View longClickView, int position);

        void onPop(View longClickView, int position);
    }

    public interface OnClickListener {
        void onClick(View view, int position, boolean canShowPeek);

        boolean canShowPeek();
    }

}
