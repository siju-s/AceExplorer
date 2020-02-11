package com.siju.acexplorer.home.view

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.siju.acexplorer.home.types.CategoryData
import com.siju.acexplorer.storage.view.FileListFragment

class CategoryPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    private val categoryDataList = arrayListOf<CategoryData>()
    private val fragmentMap = hashMapOf<Int, Fragment>()

    fun addData(categoryData: CategoryData) {
        categoryDataList.add(categoryData)
    }

    fun getFragment(position: Int) : Fragment? {
        return fragmentMap[position]
    }

    fun getTitle(position: Int): String {
        return categoryDataList[position].title
    }

    override fun getItemCount(): Int {
        return categoryDataList.size
    }

    override fun createFragment(position: Int): Fragment {
        val  data = categoryDataList[position]
        val fragment = FileListFragment.newInstance(data.path, data.category, false)
        Log.d("Adapter", "createFragment:pos:$position, frag:$fragment")
        fragment.setCategoryMenuHelper(data.categoryMenuHelper)
        fragmentMap[position] = fragment
        return fragment
    }

    fun clear() {
        fragmentMap.clear()
    }
}