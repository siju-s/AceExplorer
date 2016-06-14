package com.siju.filemanager;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.siju.filemanager.filesystem.FileConstants;
import com.siju.filemanager.filesystem.FileListFragment;

import java.util.List;

/**
 * Parent fragment for Music player
 */
public class TabsFragment extends Fragment {

    ViewPager mViewPager;
    View rootView;
    TabsPagerAdapter mAdapter;
    String internalStorage;
    TabLayout mTabLayout;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.tabs_swipe, container, false);
        return rootView;

    }
//
//    @Override
//    public void onViewCreated(View view, Bundle savedInstanceState) {
//
//    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        mViewPager = (ViewPager) rootView.findViewById(R.id.pager);
        mTabLayout = (TabLayout) rootView.findViewById(R.id.tabs);
        mAdapter = new TabsPagerAdapter(getChildFragmentManager(), getActivity());
        internalStorage = getResources().getString(R.string.nav_menu_internal_storage);
        FileListFragment fileListFragment = new FileListFragment();
        Bundle bundle = getArguments();
        String path = bundle.getString(FileConstants.KEY_PATH);
        fileListFragment.setArguments(bundle);
        mAdapter.addFragment(fileListFragment, internalStorage);
        mViewPager.setAdapter(mAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
        LinearLayout tabStrip = (LinearLayout) mTabLayout.getChildAt(0);
        for (int i = 0; i < tabStrip.getChildCount(); i++) {
            final int finalI = i;
            tabStrip.getChildAt(i).setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Toast.makeText(getContext(), "Long click --" + finalI, Toast.LENGTH_LONG);
                    return true;
                }
            });
        }
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

                if (state == ViewPager.SCROLL_STATE_DRAGGING) {
                    if (mAdapter.getCount() == 1) {
                        addView();
                    }
                }

            }
        });
    }

    private void addView() {
        FileListFragment fileListFragment = new FileListFragment();
        Bundle bundle = getArguments();
//        String path = bundle.getString(FileConstants.KEY_PATH);
        fileListFragment.setArguments(bundle);
        mAdapter.addFragment(fileListFragment, internalStorage);
        mAdapter.notifyDataSetChanged();
        LinearLayout tabStrip = (LinearLayout) mTabLayout.getChildAt(0);
        for (int i = 0; i < tabStrip.getChildCount(); i++) {
            final int finalI = i;
            tabStrip.getChildAt(i).setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Toast.makeText(getContext(), "Long click --" + finalI, Toast.LENGTH_LONG);
                    return true;
                }
            });
        }
    }

//    @Override
//    public void onActi(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//
//    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(this.getClass().getSimpleName(), "ON ACTIVITY RESULT --REQ CODE=" + requestCode);

        super.onActivityResult(requestCode, resultCode, data);
        List<Fragment> fragments = getChildFragmentManager().getFragments();
        if (fragments != null) {
            for (Fragment fragment : fragments) {
                fragment.onActivityResult(requestCode, resultCode, data);
            }
        }
    }


}