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

import com.siju.filemanager.filesystem.FileConstants;
import com.siju.filemanager.filesystem.FileListFragment;

import java.util.List;

/**
 * Parent fragment for Music player
 */
public class TabsFragment extends Fragment {

    ViewPager mViewPager;
    View rootView;

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
        TabsPagerAdapter adapter;
        TabLayout tabLayout;
        mViewPager = (ViewPager) rootView.findViewById(R.id.pager);
        tabLayout = (TabLayout) rootView.findViewById(R.id.tabs);
        adapter = new TabsPagerAdapter(getChildFragmentManager(), getActivity());
        String internalStorage = getResources().getString(R.string.nav_menu_internal_storage);
        FileListFragment fileListFragment = new FileListFragment();
        Bundle bundle = getArguments();
        String path = bundle.getString(FileConstants.KEY_PATH);
        fileListFragment.setArguments(bundle);
        adapter.addFragment(fileListFragment, internalStorage);
        mViewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(mViewPager);
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