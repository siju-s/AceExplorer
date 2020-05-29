package com.siju.acexplorer.extensions

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.viewpager2.widget.ViewPager2

fun ViewPager2.findCurrentFragment(fragmentManager: FragmentManager): Fragment? {
    return fragmentManager.findFragmentByTag("f$currentItem")
}
