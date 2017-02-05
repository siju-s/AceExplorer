package com.siju.acexplorer.common;

/**
 * Created by SJ on 06-02-2017.
 */

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
    private Listener c;
    private Animator d;
    private boolean e;
    private Runnable f = new Runnable(this) {
        final /* synthetic */ SearchView a;

        {
            this.a = r1;
        }

        public void run() {
            InputMethodManager inputMethodManager = (InputMethodManager) this.a.getContext().getSystemService("input_method");
            if (inputMethodManager != null) {
                inputMethodManager.showSoftInput(this.a.autoCompleteTextView, 0);
            }
        }
    };
    private Runnable g = new Runnable(this) {
        final /* synthetic */ SearchView a;

        {
            this.a = r1;
        }

        public void run() {
            if (this.a.c != null) {
                this.a.c.onQuerySubmit(this.a.autoCompleteTextView.getText());
            }
        }
    };
    private OnClickListener h = new OnClickListener(this) {
        final /* synthetic */ SearchView a;

        {
            this.a = r1;
        }

        public void onClick(View view) {
            switch (view.getId()) {
                case 2131689849:
                    this.a.onSearchClicked();
                    return;
                default:
                    return;
            }
        }
    };
    private TextWatcher i = new TextWatcher(this) {
        final /* synthetic */ SearchView a;

        {
            this.a = r1;
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        }

        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            this.a.onQueryChange(charSequence);
        }

        public void afterTextChanged(Editable editable) {
        }
    };

    public interface Listener {
        void onAnimationProgress(float f, boolean z);

        void onQueryChange(CharSequence charSequence);

        void onQuerySubmit(CharSequence charSequence);

        void onSearchEnabled(boolean z);
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
        this.autoCompleteTextView.setOnEditorActionListener(new OnEditorActionListener(this) {
            final /* synthetic */ SearchView a;

            {
                this.a = r1;
            }

            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i != 6 && i != 3) {
                    return false;
                }
                this.a.removeCallbacks(this.a.g);
                this.a.g.run();
                return true;
            }
        });
        this.searchIcon = (ImageButton) findViewById(R.id.action_search);
        this.searchIcon.setOnClickListener(this.h);
    }

    public AutoCompleteTextView getInput() {
        return this.autoCompleteTextView;
    }

    public void setListener(Listener listener) {
        this.c = listener;
    }

    public void setHint(CharSequence charSequence) {
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder("   ");
        spannableStringBuilder.append(charSequence);
        Drawable drawable = ContextCompat.getDrawable(getContext(),R.drawable.ic_search_white);
        int textSize = (int) (((double) this.autoCompleteTextView.getTextSize()) * 1.25d);
        drawable.setBounds(0, 0, textSize, textSize);
        spannableStringBuilder.setSpan(new ImageSpan(drawable), 1, 2, 33);
        this.autoCompleteTextView.setHint(spannableStringBuilder);
    }

    public boolean isExpanded() {
        return this.e;
    }

    public void enableSearch(boolean z) {
        if (z && !this.e) {
            onSearchClicked();
        } else if (!z && this.e) {
            onCloseClicked();
        }
    }

    private void onSearchClicked() {
        this.e = true;
        if (this.c != null) {
            this.c.onSearchEnabled(true);
        }
        animate(true);
    }

    private void onCloseClicked() {
        this.e = false;
        if (this.c != null) {
            this.c.onSearchEnabled(false);
        }
        animate(false);
        removeCallbacks(this.g);
    }

    private void animate(final boolean z) {
        float f;
        float f2 = 0.0f;
        PropertyValuesHolder[] propertyValuesHolderArr = new PropertyValuesHolder[1];
        Property property = View.X;
        float[] fArr = new float[2];
        fArr[0] = z ? (float) getWidth() : 0.0f;
        if (z) {
            f = 0.0f;
        } else {
            f = (float) getWidth();
        }
        fArr[1] = f;
        propertyValuesHolderArr[0] = PropertyValuesHolder.ofFloat(property, fArr);
        ObjectAnimator ofPropertyValuesHolder = ObjectAnimator.ofPropertyValuesHolder(this.autoCompleteTextView, propertyValuesHolderArr);
        ofPropertyValuesHolder.addListener(new AnimatorListenerAdapter(this) {
            final /* synthetic */ SearchView b;

            public void onAnimationStart(Animator animator) {
                if (z) {
                    this.b.autoCompleteTextView.setVisibility(0);
                }
            }

            public void onAnimationEnd(Animator animator) {
                if (z) {
                    this.b.onExpand();
                    return;
                }
                this.b.autoCompleteTextView.setVisibility(4);
                this.b.onCollapse();
            }
        });
        ofPropertyValuesHolder.addUpdateListener(new AnimatorUpdateListener(this) {
            final /* synthetic */ SearchView b;

            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                if (this.b.c != null) {
                    this.b.c.onAnimationProgress(valueAnimator.getAnimatedFraction(), z);
                }
            }
        });
        ImageButton imageButton = this.searchIcon;
        PropertyValuesHolder[] propertyValuesHolderArr2 = new PropertyValuesHolder[1];
        Property property2 = View.ALPHA;
        float[] fArr2 = new float[1];
        if (!z) {
            f2 = 1.0f;
        }
        fArr2[0] = f2;
        propertyValuesHolderArr2[0] = PropertyValuesHolder.ofFloat(property2, fArr2);
        ObjectAnimator.ofPropertyValuesHolder(imageButton, propertyValuesHolderArr2).addListener(new AnimatorListenerAdapter(this) {
            final /* synthetic */ SearchView b;

            public void onAnimationStart(Animator animator) {
                if (!z) {
                    this.b.searchIcon.setVisibility(0);
                }
            }

            public void onAnimationEnd(Animator animator) {
                if (z) {
                    this.b.searchIcon.setVisibility(4);
                }
            }
        });
        Animator animatorSet = new AnimatorSet();
        animatorSet.setDuration(500);
        animatorSet.setInterpolator(new DecelerateInterpolator(2.0f));
        animatorSet.playTogether(new Animator[]{ofPropertyValuesHolder, r1});
        if (this.d != null) {
            this.d.cancel();
        }
        animatorSet.start();
        this.d = animatorSet;
    }

    private void onExpand() {
        this.autoCompleteTextView.requestFocus();
        setImeVisibility(true);
    }

    private void onCollapse() {
        setImeVisibility(false);
    }

    private void onQueryChange(CharSequence charSequence) {
        removeCallbacks(this.g);
        if (this.e) {
            if (this.c != null) {
                this.c.onQueryChange(charSequence);
            }
            postDelayed(this.g, 800);
        }
    }

    private void setImeVisibility(boolean z) {
        if (z) {
            post(this.f);
            return;
        }
        removeCallbacks(this.f);
        InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService("input_method");
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
        }
    }


}
