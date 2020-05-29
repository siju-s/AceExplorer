package com.siju.acexplorer.home.view

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.siju.acexplorer.home.types.CategoryData
import com.siju.acexplorer.storage.view.FileListFragment

class CategoryPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    private val categoryDataList = arrayListOf<CategoryData>()
    private val fragmentMap = hashMapOf<Int, Fragment>()

    fun addData(categoryData: CategoryData) {
        categoryDataList.add(categoryData)
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
        Log.d("CategoryAdapter", "createFragment:pos:$position, category:${data.category}, frag:$fragment")
        fragmentMap[position] = fragment
        return fragment
    }

    fun clear() {
        fragmentMap.clear()
        categoryDataList.clear()
    }
}