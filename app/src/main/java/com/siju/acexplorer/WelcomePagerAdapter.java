package com.siju.acexplorer;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

class WelcomePagerAdapter extends PagerAdapter {

    private final Context mContext;
    private final int[] mResources;
    private final String[] mHeaderText;
    private final String[] mText;
    private final int[] bgColors;


    WelcomePagerAdapter(Context mContext, int[] mResources, String[] headerText, String[] text, int[] colors) {
        this.mContext = mContext;
        this.mResources = mResources;
        mHeaderText = headerText;
        mText = text;
        bgColors = colors;

    }

    @Override
    public int getCount() {
        return mResources.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View itemView = LayoutInflater.from(mContext).inflate(R.layout.intro_pager_item, container, false);

        ImageView imageView = (ImageView) itemView.findViewById(R.id.imageIntro);
        TextView textHeader = (TextView) itemView.findViewById(R.id.textIntroHeader);
        TextView textContent = (TextView) itemView.findViewById(R.id.textIntro);
        itemView.setBackgroundColor(bgColors[position]);
        textHeader.setText(mHeaderText[position]);
        textContent.setText(mText[position]);

        Log.d("TAG", "Adapter=" + position);
        Glide.with(mContext).load(mResources[position])
                .asGif()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(imageView);

        container.addView(itemView);

        return itemView;
    }


    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((LinearLayout) object);
    }
}