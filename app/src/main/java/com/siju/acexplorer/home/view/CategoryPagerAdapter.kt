package com.siju.acexplorer.home.view

import android.util.Log
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.siju.acexplorer.home.types.CategoryData
import com.siju.acexplorer.storage.view.FileListFragment

class CategoryPagerAdapter(fragmentManager: FragmentManager) : FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private val categoryDataList = arrayListOf<CategoryData>()
    private val fragmentMap = hashMapOf<Int, Fragment>()

    override fun getItem(position: Int): Fragment {
        val  data = categoryDataList[position]
        val fragment = FileListFragment.newInstance(data.path, data.category, false)
        Log.e("Adapter", "getITem:pos:$position, frag:$fragment")
        fragment.setCategoryMenuHelper(data.categoryMenuHelper)
        return fragment
    }

    fun addData(categoryData: CategoryData) {
        categoryDataList.add(categoryData)
    }

    /**
     * If you want to only show icons, return null from this method.
     * @param position
     * @return
     */
    override fun getPageTitle(position: Int): CharSequence? {
        return categoryDataList[position].title
    }

    override fun getCount(): Int {
        return categoryDataList.size
    }

    fun getFragment(position: Int) : Fragment? {
        return fragmentMap[position]
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val fragment =  super.instantiateItem(container, position)
        Log.e("Adapter", "instantiateItem:$position, fragment:$fragment")
        fragmentMap[position] = fragment as Fragment
        return fragment
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        super.destroyItem(container, position, `object`)
        Log.e("Adapter", "destroyItem:$position")
        fragmentMap.remove(position)
    }

}