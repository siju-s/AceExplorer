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

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.siju.acexplorer.R;

class WelcomePagerAdapter extends PagerAdapter {

    private final Context  context;
    private final int[]    resources;
    private final String[] headerText;
    private final String[] text;
    private final int[]    bgColors;


    WelcomePagerAdapter(Context context, int[] resources, String[] headerText, String[] text,
                        int[] colors) {
        this.context = context;
        this.resources = resources;
        this.headerText = headerText;
        this.text = text;
        bgColors = colors;

    }

    @Override
    public int getCount() {
        return resources.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.intro_pager_item,
                                                             container, false);

        ImageView imageView = itemView.findViewById(R.id.imageIntro);
        TextView textHeader = itemView.findViewById(R.id.textIntroHeader);
        TextView textContent = itemView.findViewById(R.id.textIntro);
        itemView.setBackgroundColor(bgColors[position]);
        textHeader.setText(headerText[position]);
        textContent.setText(text[position]);

        Glide.with(context).load(resources[position])
                .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.AUTOMATIC))
                .into(imageView);

        container.addView(itemView);

        return itemView;
    }


    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((LinearLayout) object);
    }
}
