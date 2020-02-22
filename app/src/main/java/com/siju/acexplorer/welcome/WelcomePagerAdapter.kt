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

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.siju.acexplorer.R

internal class WelcomePagerAdapter(private val context: Context, private val resources: IntArray,
                                   private val headerText: Array<String>,
                                   private val text: Array<String>,
                                   private val bgColors: IntArray) : PagerAdapter()
{
    override fun getCount(): Int {
        return resources.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val itemView = LayoutInflater.from(context).inflate(R.layout.intro_pager_item,
                container, false)
        val imageView = itemView.findViewById<ImageView>(R.id.imageIntro)
        val textHeader = itemView.findViewById<TextView>(R.id.textIntroHeader)
        val textContent = itemView.findViewById<TextView>(R.id.textIntro)
        itemView.setBackgroundColor(bgColors[position])
        textHeader.text = headerText[position]
        textContent.text = text[position]
        Glide.with(context).load(resources[position])
                .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.AUTOMATIC))
                .into(imageView)
        container.addView(itemView)
        return itemView
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as LinearLayout)
    }

}