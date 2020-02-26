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
package com.siju.acexplorer.welcome

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.siju.acexplorer.R
import com.siju.acexplorer.main.AceActivity
import com.siju.acexplorer.utils.PrefManager

class WelcomeActivity : AppCompatActivity(), View.OnClickListener, OnPageChangeListener {
    private lateinit var viewPager: ViewPager
    private lateinit var dotsLayout: LinearLayout
    private lateinit var skipButton: Button
    private lateinit var nextButton: Button

    private val resIds = intArrayOf(
            R.raw.library,
            R.raw.peekpop,
            R.raw.dragdrop,
            R.raw.dualpane,
            R.raw.theme
    )
    private lateinit var prefManager: PrefManager
    private lateinit var adapter: WelcomePagerAdapter
    private val dots = arrayListOf<ImageView>()
    private var dotsCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefManager = PrefManager(this)

        if (prefManager.isFirstTimeLaunch) {
            setContentView(R.layout.activity_welcome)
            initializeViews()
        }
        else {
            launchHomeScreen()
        }
    }

    private fun initializeViews() {
        viewPager = findViewById(R.id.view_pager)
        dotsLayout = findViewById(R.id.layoutDots)
        skipButton = findViewById(R.id.buttonSkip)
        nextButton = findViewById(R.id.buttonNext)
        nextButton.setOnClickListener(this)
        skipButton.setOnClickListener(this)
        val headerText = arrayOf(getString(R.string.slide_1_title),
                getString(R.string.slide_2_title),
                getString(R.string.slide_3_title),
                getString(R.string.dual_pane_intro_title),
                getString(R.string.slide_4_title))
        val text = arrayOf(getString(R.string.slide_1_desc),
                getString(R.string.slide_2_desc),
                getString(R.string.slide_3_desc),
                getString(R.string.dual_pane_intro_subtitle),
                getString(R.string.slide_4_desc))
        val bgColors = intArrayOf(ContextCompat.getColor(this, R.color.bg_screen1),
                ContextCompat.getColor(this, R.color.bg_screen2),
                ContextCompat.getColor(this, R.color.bg_screen3),
                ContextCompat.getColor(this, R.color.bg_screen4),
                ContextCompat.getColor(this, R.color.bg_screen5))
        adapter = WelcomePagerAdapter(this, resIds, headerText, text, bgColors)
        viewPager.adapter = adapter
        viewPager.currentItem = 0
        viewPager.addOnPageChangeListener(this)
        addBottomDots()
    }

    private fun addBottomDots() {
        dotsCount = adapter.count
        for (i in 0 until dotsCount) {
            val element = ImageView(this)
            dots.add(element)
            dots[i].setImageDrawable(ContextCompat.getDrawable(this, R.drawable.intro_unselecteditem))
            val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(4, 0, 4, 0)
            dotsLayout.addView(dots[i], params)
        }
        dots[0].setImageDrawable(ContextCompat.getDrawable(this, R.drawable.intro_selecteditem))
    }

    private fun launchHomeScreen() {
        prefManager.setFirstTimeLaunch()
        startActivity(Intent(this@WelcomeActivity, AceActivity::class.java))
        finish()
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.buttonNext -> {
                onNextClicked()
            }
            R.id.buttonSkip -> launchHomeScreen()
        }
    }

    private fun onNextClicked() {
        val current = viewPager.currentItem
        if (current + 1 < dotsCount) {
            viewPager.currentItem = current + 1
        } else {
            launchHomeScreen()
        }
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

    override fun onPageSelected(position: Int) {
        for (i in 0 until dotsCount) {
            dots[i].setImageDrawable(ContextCompat.getDrawable(this@WelcomeActivity, R.drawable.intro_unselecteditem))
        }
        dots[position].setImageDrawable(ContextCompat.getDrawable(this@WelcomeActivity, R.drawable.intro_selecteditem))
        if (position + 1 == dotsCount) {
            nextButton.text = getString(R.string.start)
            skipButton.visibility = View.GONE
        } else {
            nextButton.text = getString(R.string.next)
            skipButton.visibility = View.VISIBLE
        }
    }

    override fun onPageScrollStateChanged(state: Int) {}
}