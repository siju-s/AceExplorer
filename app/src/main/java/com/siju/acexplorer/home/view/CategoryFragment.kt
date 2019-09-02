package com.siju.acexplorer.home.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.siju.acexplorer.R
import com.siju.acexplorer.extensions.inflateLayout
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.storage.view.FileListFragment
import com.siju.acexplorer.storage.view.KEY_CATEGORY
import com.siju.acexplorer.storage.view.KEY_PATH
import kotlinx.android.synthetic.main.toolbar.*

class CategoryFragment : Fragment() {

    private lateinit var pagerAdapter: CategoryPagerAdapter
    private lateinit var viewPager: ViewPager

    companion object {
        fun newInstance(path: String?, category: Category): CategoryFragment {
            val bundle = Bundle()
            bundle.apply {
                putString(KEY_PATH, path)
                putSerializable(KEY_CATEGORY, category)
            }
            val categoryFragment = CategoryFragment()
            categoryFragment.arguments = bundle
            return categoryFragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflateLayout(R.layout.category_pager, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupUI(view)
    }

    private fun setupUI(view: View) {
        setupToolbar()
        viewPager = view.findViewById(R.id.categoryPager)
        val tabLayout = view.findViewById<TabLayout>(R.id.categoryTabs)
        tabLayout.setupWithViewPager(viewPager)
        pagerAdapter = CategoryPagerAdapter(childFragmentManager)
        setupAdapter()
    }

    private fun setupToolbar() {
        toolbar.title = resources.getString(R.string.app_name)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
    }

    private fun setupAdapter() {
        val args = arguments
        args?.let {
            val path = args.getString(KEY_PATH)
            val category = args.getSerializable(KEY_CATEGORY) as Category
            createFragment(path, category)
            viewPager.adapter = pagerAdapter
        }
    }


    private fun createFragment(path: String?, category: Category) {
        val allCategory = when (category) {
            Category.GENERIC_IMAGES -> Category.IMAGES_ALL
            Category.GENERIC_VIDEOS -> Category.VIDEO_ALL
            Category.GENERIC_MUSIC -> Category.ALL_TRACKS
            Category.RECENT -> Category.RECENT_ALL
            Category.LARGE_FILES -> Category.LARGE_FILES_ALL
            else -> null
        }
        var fragment1 = FileListFragment.newInstance(path, category, false)
        allCategory?.let {
            fragment1 = FileListFragment.newInstance(path, allCategory, false)
        }
        val fragment2 = FileListFragment.newInstance(path, category, false)
        context?.let { context ->
            pagerAdapter.addFragment(fragment1, context.getString(R.string.category_all))
            pagerAdapter.addFragment(fragment2, getTitle(context, category))
        }
    }

    private fun getTitle(context: Context, category: Category): String {
        return when (category) {
            Category.GENERIC_IMAGES -> context.getString(R.string.categories_folder)
            Category.GENERIC_VIDEOS -> context.getString(R.string.categories_folder)
            Category.GENERIC_MUSIC, Category.RECENT, Category.LARGE_FILES -> context.getString(R.string.categories)
            else -> "null"
        }
    }

    fun onBackPressed() : Boolean {
        val fragment = pagerAdapter.getItem(viewPager.currentItem)
        if (fragment is FileListFragment) {
            return fragment.onBackPressed()
        }
        return true
    }


}