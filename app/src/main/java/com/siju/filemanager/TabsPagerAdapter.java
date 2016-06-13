/**
 *
 */
package com.siju.filemanager;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * @author siju
 */
public class TabsPagerAdapter extends FragmentStatePagerAdapter {

    private Context context;
    private final List<Fragment> mFragmentList = new ArrayList<>();
    private final List<String> mFragmentTitleList = new ArrayList<>();

    public TabsPagerAdapter(FragmentManager fm, Context c) {
        super(fm);
        context = c;
    }

    public void addFragment(Fragment fragment, String title) {
        mFragmentList.add(fragment);
        mFragmentTitleList.add(title);
    }

    @Override
    public Fragment getItem(int position) {

        return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
        // get item count - equal to number of tabs
        return mFragmentList.size();
    }

    @Override
    public void destroyItem(View collection, int position, Object o) {
        View view = (View) o;
        ((ViewPager) collection).removeView(view);
        view = null;
    }

    // public Fragment getActiveFragment(ViewPager container, int position) {
    // String name = makeFragmentName(container.getId(), position);
    // return mFragmentManager.findFragmentByTag(name);
    // }
    //
    // private static String makeFragmentName(int viewId, int index) {
    // return "android:switcher:" + viewId + ":" + index;
    // }

    @Override
    public CharSequence getPageTitle(int position) {
        return mFragmentTitleList.get(position);
//        if (position == 0)
//            return context.getResources().getString(R.string.tabFolders);
//        else if (position == 1)
//            return context.getResources().getString(R.string.tabPlaylists)
//                    .toUpperCase(Locale.getDefault());
//
//        else if (position == 2)
//            return context.getResources().getString(R.string.songs)
//                    .toUpperCase(Locale.getDefault());
//        else if (position == 3)
//            return context.getResources().getString(R.string.tabAlbums);
//
//        else if (position == 4)
//            return context.getResources().getString(R.string.tabArtists);
//        else
//            return context.getResources().getString(R.string.songs)
//                    .toUpperCase(Locale.getDefault());

    }

}