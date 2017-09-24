/*
 * Copyright (C) 2017 Ace Explorer owned by Siju Sakaria
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.siju.acexplorer.welcome;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.siju.acexplorer.R;
import com.siju.acexplorer.logging.Logger;
import com.siju.acexplorer.utils.PrefManager;
import com.siju.acexplorer.view.AceActivity;

public class WelcomeActivity extends AppCompatActivity implements View.OnClickListener,
        ViewPager.OnPageChangeListener {

    private ViewPager viewPager;
    private LinearLayout dotsLayout;
    private Button btnSkip, btnNext;
    private PrefManager prefManager;
    private int dotsCount;
    private ImageView[] dots;
    private final int[] mImageResources = {
            R.raw.library,
            R.raw.drawer,
            R.raw.dragdrop,
            R.raw.theme
    };

    private WelcomePagerAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefManager = new PrefManager(this);
        Logger.log("WelcomeAct", "CALLED");

        if (!prefManager.isFirstTimeLaunch()) {
            launchHomeScreen();
            return;
        }

        setContentView(R.layout.activity_welcome);
        initializeViews();
    }

    private void initializeViews() {
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        dotsLayout = (LinearLayout) findViewById(R.id.layoutDots);
        btnSkip = (Button) findViewById(R.id.btn_skip);
        btnNext = (Button) findViewById(R.id.btn_next);

        btnNext.setOnClickListener(this);
        btnSkip.setOnClickListener(this);

        String[] headerText = {getString(R.string.slide_1_title),
                getString(R.string.slide_2_title),
                getString(R.string.slide_3_title),
                getString(R.string.slide_4_title)};

        String[] text = {getString(R.string.slide_1_desc),
                getString(R.string.slide_2_desc),
                getString(R.string.slide_3_desc),
                getString(R.string.slide_4_desc)};
        int[] bgColors = {ContextCompat.getColor(this, R.color.bg_screen1),
                ContextCompat.getColor(this, R.color.bg_screen2),
                ContextCompat.getColor(this, R.color.bg_screen3),
                ContextCompat.getColor(this, R.color.bg_screen4)};
        mAdapter = new WelcomePagerAdapter(this, mImageResources, headerText, text, bgColors);
        viewPager.setAdapter(mAdapter);
        viewPager.setCurrentItem(0);
        viewPager.addOnPageChangeListener(this);
        addBottomDots();
    }


    private void addBottomDots() {


        dotsCount = mAdapter.getCount();
        dots = new ImageView[dotsCount];

        for (int i = 0; i < dotsCount; i++) {
            dots[i] = new ImageView(this);
            dots[i].setImageDrawable(ContextCompat.getDrawable(this, R.drawable.intro_unselecteditem));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );

            params.setMargins(4, 0, 4, 0);

            dotsLayout.addView(dots[i], params);
        }

        dots[0].setImageDrawable(ContextCompat.getDrawable(this, R.drawable.intro_selecteditem));
        Logger.log("WelcomeAct", "addBottomDots");

    }

    private void launchHomeScreen() {
        prefManager.setFirstTimeLaunch();
        startActivity(new Intent(WelcomeActivity.this, AceActivity.class));
        finish();
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_next:
                int current = viewPager.getCurrentItem();
                if (current + 1 < dotsCount) {
                    // move to next screen
                    viewPager.setCurrentItem(current + 1);
                } else {
                    launchHomeScreen();
                }
                break;
            case R.id.btn_skip:
                launchHomeScreen();
                break;
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        for (int i = 0; i < dotsCount; i++) {
            dots[i].setImageDrawable(ContextCompat.getDrawable(WelcomeActivity.this, R.drawable
                    .intro_unselecteditem));
        }

        dots[position].setImageDrawable(ContextCompat.getDrawable(WelcomeActivity.this, R.drawable
                .intro_selecteditem));

        if (position + 1 == dotsCount) {
            btnNext.setText(getString(R.string.start));
            btnSkip.setVisibility(View.GONE);
        } else {
            btnNext.setText(getString(R.string.next));
            btnSkip.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }


}