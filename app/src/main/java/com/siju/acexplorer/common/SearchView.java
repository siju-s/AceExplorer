package com.siju.acexplorer.common;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.util.Property;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.siju.acexplorer.R;


public class SearchView extends RelativeLayout {
    private ImageButton searchIcon;
    private AutoCompleteTextView autoCompleteTextView;
    private Listener listener;
    private Animator animator;
    private boolean isExpanded;
    private Runnable showInputRunnable = new Runnable() {


        public void run() {
            InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (inputMethodManager != null) {
                inputMethodManager.showSoftInput(autoCompleteTextView, 0);
            }
        }
    };
    private Runnable submitRunnable = new Runnable() {

        public void run() {
            if (listener != null) {
                listener.onQuerySubmit(autoCompleteTextView.getText());
            }
        }
    };
    private OnClickListener onClickListener = new OnClickListener() {

        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.search:
                    onSearchClicked();
            }
        }
    };
    private TextWatcher i = new TextWatcher() {

        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        }

        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            onQueryChange(charSequence);
        }

        public void afterTextChanged(Editable editable) {
        }
    };

    public interface Listener {
        void onAnimationProgress(float f, boolean z);

        void onQueryChange(CharSequence charSequence);

        void onQuerySubmit(CharSequence charSequence);

        void onSearchEnabled(boolean isExpanded);
    }

    public SearchView(Context context) {
        super(context);
        init(null);
    }

    public SearchView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init(attributeSet);
    }

    public SearchView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        init(attributeSet);
    }

    @SuppressLint({"WrongViewCast"})
    private void init(AttributeSet attributeSet) {
        LayoutInflater.from(getContext()).inflate(R.layout.search_view, this);
        this.autoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.input);
        this.autoCompleteTextView.addTextChangedListener(this.i);
        this.autoCompleteTextView.setOnEditorActionListener(new OnEditorActionListener() {

            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {

                if (actionId != EditorInfo.IME_ACTION_DONE && actionId != EditorInfo.IME_ACTION_SEARCH) {
                    return false;
                }
                removeCallbacks(submitRunnable);
                submitRunnable.run();
                return true;
            }
        });
        this.searchIcon = (ImageButton) findViewById(R.id.search);
        this.searchIcon.setOnClickListener(onClickListener);
    }

    public AutoCompleteTextView getInput() {
        return this.autoCompleteTextView;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void setHint(CharSequence charSequence) {
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder("   ");
        spannableStringBuilder.append(charSequence);
        Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_search_white);
        int textSize = (int) (((double) this.autoCompleteTextView.getTextSize()) * 1.25d);
        drawable.setBounds(0, 0, textSize, textSize);
        spannableStringBuilder.setSpan(new ImageSpan(drawable), 1, 2, 33);
        this.autoCompleteTextView.setHint(spannableStringBuilder);
    }

    public boolean isExpanded() {
        return this.isExpanded;
    }

    public void enableSearch(boolean z) {
        if (z && !this.isExpanded) {
            onSearchClicked();
        } else if (!z && this.isExpanded) {
            onCloseClicked();
        }
    }

    private void onSearchClicked() {
        this.isExpanded = true;
        if (this.listener != null) {
            this.listener.onSearchEnabled(true);
        }
        animate(true);
    }

    private void onCloseClicked() {
        this.isExpanded = false;
        if (this.listener != null) {
            this.listener.onSearchEnabled(false);
        }
        animate(false);
        removeCallbacks(this.submitRunnable);
    }

    private void animate(final boolean isExpanded) {
        float f;
        float f2 = 0.0f;
        PropertyValuesHolder[] propertyValuesHolderArr = new PropertyValuesHolder[1];
        Property property = View.X;
        float[] fArr = new float[2];
        fArr[0] = isExpanded ? (float) getWidth() : 0.0f;
        if (isExpanded) {
            f = 0.0f;
        } else {
            f = (float) getWidth();
        }
        fArr[1] = f;
        propertyValuesHolderArr[0] = PropertyValuesHolder.ofFloat(property, fArr);
        ObjectAnimator ofPropertyValuesHolder = ObjectAnimator.ofPropertyValuesHolder(this.autoCompleteTextView, propertyValuesHolderArr);
        ofPropertyValuesHolder.addListener(new AnimatorListenerAdapter() {

            public void onAnimationStart(Animator animator) {
                if (isExpanded) {
                    autoCompleteTextView.setVisibility(VISIBLE);
                }
            }

            public void onAnimationEnd(Animator animator) {
                if (isExpanded) {
                    onExpand();
                    return;
                }
                autoCompleteTextView.setVisibility(INVISIBLE);
                onCollapse();
            }
        });
        ofPropertyValuesHolder.addUpdateListener(new AnimatorUpdateListener() {

            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                if (listener != null) {
                    listener.onAnimationProgress(valueAnimator.getAnimatedFraction(), isExpanded);
                }
            }
        });
        ImageButton imageButton = this.searchIcon;
        PropertyValuesHolder[] propertyValuesHolderArr2 = new PropertyValuesHolder[1];
        Property property2 = View.ALPHA;
        float[] fArr2 = new float[1];
        if (!isExpanded) {
            f2 = 1.0f;
        }
        fArr2[0] = f2;
        propertyValuesHolderArr2[0] = PropertyValuesHolder.ofFloat(property2, fArr2);
        ObjectAnimator ofPropertyValuesHolder2 = ObjectAnimator.ofPropertyValuesHolder(imageButton, propertyValuesHolderArr2);
        ofPropertyValuesHolder2.addListener(new AnimatorListenerAdapter() {

            public void onAnimationStart(Animator animator) {
                if (!isExpanded) {
                    searchIcon.setVisibility(View.VISIBLE);
                }
            }

            public void onAnimationEnd(Animator animator) {
                if (isExpanded) {
                    searchIcon.setVisibility(View.INVISIBLE);
                }
            }
        });
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(500);
        animatorSet.setInterpolator(new DecelerateInterpolator(2.0f));
        animatorSet.playTogether(new Animator[]{ofPropertyValuesHolder, ofPropertyValuesHolder2});
        if (this.animator != null) {
            this.animator.cancel();
        }
        animatorSet.start();
        this.animator = animatorSet;
    }

    private void onExpand() {
        this.autoCompleteTextView.requestFocus();
        setImeVisibility(true);
    }

    private void onCollapse() {
        setImeVisibility(false);
    }

    private void onQueryChange(CharSequence charSequence) {
        removeCallbacks(this.submitRunnable);
        if (this.isExpanded) {
            if (this.listener != null) {
                this.listener.onQueryChange(charSequence);
            }
            postDelayed(this.submitRunnable, 800);
        }
    }

    private void setImeVisibility(boolean z) {
        if (z) {
            post(this.showInputRunnable);
            return;
        }
        removeCallbacks(this.showInputRunnable);
        InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
        }
    }


}
