package com.siju.acexplorer.home.view

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.siju.acexplorer.R
import com.siju.acexplorer.extensions.inflateLayout
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.main.model.groups.CategoryHelper
import com.siju.acexplorer.search.helper.SearchUtils
import com.siju.acexplorer.storage.view.FileListFragment
import com.siju.acexplorer.storage.view.KEY_CATEGORY
import com.siju.acexplorer.storage.view.KEY_PATH
import kotlinx.android.synthetic.main.toolbar.*
import java.util.*

class CategoryFragment : Fragment(), CategoryMenuHelper, Toolbar.OnMenuItemClickListener {

    private lateinit var pagerAdapter: CategoryPagerAdapter
    private lateinit var viewPager   : ViewPager

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

    override fun getCategoryView() = view

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupUI(view)
    }

    private fun setupUI(view: View) {
        setupToolbar()
        viewPager = view.findViewById(R.id.categoryPager)
        viewPager.addOnPageChangeListener(pageChangeListener)
        val tabLayout = view.findViewById<TabLayout>(R.id.categoryTabs)
        tabLayout.setupWithViewPager(viewPager)
        pagerAdapter = CategoryPagerAdapter(childFragmentManager)
        setupAdapter()
    }

    private fun setupToolbar() {
        toolbar.inflateMenu(R.menu.filelist_base)
        toolbar.setOnMenuItemClickListener(this)
    }

    private fun setToolbarTitle(title : String) {
        toolbar.title = title
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

    override fun onMenuItemClick(item: MenuItem): Boolean {
        val fragment = pagerAdapter.getItem(viewPager.currentItem)
        if (fragment is FileListFragment) {
            fragment.onMenuItemClick(item)
            return true
        }
        return false
    }

    private val pageChangeListener = object : ViewPager.OnPageChangeListener {
        override fun onPageScrollStateChanged(state: Int) {

        }

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        }

        override fun onPageSelected(position: Int) {
            Log.e(this.javaClass.simpleName, "onPageSelected:$position")
            if (position == -1) {
                return
            }
            val fragment = pagerAdapter.getItem(position)
            if (fragment is FileListFragment) {
                fragment.refreshDataOnSettingChange()
            }
        }
    }


    private fun createFragment(path: String?, category: Category) {
        Log.e(this.javaClass.simpleName, "category:$category")
        setToolbarTitle(CategoryHelper.getCategoryName(context, category).toUpperCase(Locale.getDefault()))
        if (category == Category.WHATSAPP || category == Category.TELEGRAM) {
            addFolderCategoryFragments(path, category)
        } else if (category == Category.DOCS) {
            addDocCategoryFragments(path, category)
        } else {
            addGenericCategoryFragments(path, category)
        }
    }

    private fun addGenericCategoryFragments(path: String?, category: Category) {
        val allCategory = when (category) {
            Category.GENERIC_IMAGES -> Category.IMAGES_ALL
            Category.GENERIC_VIDEOS -> Category.VIDEO_ALL
            Category.GENERIC_MUSIC -> Category.ALL_TRACKS
            Category.RECENT -> Category.RECENT_ALL
            Category.LARGE_FILES -> Category.LARGE_FILES_ALL
            Category.CAMERA_GENERIC -> Category.CAMERA
            else -> null
        }

        var fragment1 = FileListFragment.newInstance(path, category, false)
        allCategory?.let {
            fragment1 = FileListFragment.newInstance(path, allCategory, false)
        }
        fragment1.setCategoryMenuHelper(this)

        val fragment2 = FileListFragment.newInstance(path, category, false)
        fragment2.setCategoryMenuHelper(this)

        context?.let { context ->
            pagerAdapter.addFragment(fragment1, context.getString(R.string.category_all))
            pagerAdapter.addFragment(fragment2, getTitle(context, category))
        }
    }

    private fun getTitle(context: Context, category: Category): String {
        return when (category) {
            Category.GENERIC_IMAGES -> context.getString(R.string.categories_folder)
            Category.GENERIC_VIDEOS -> context.getString(R.string.categories_folder)
            Category.GENERIC_MUSIC, Category.RECENT, Category.LARGE_FILES, Category.CAMERA_GENERIC -> context.getString(R.string.categories)
            Category.SEARCH_FOLDER_IMAGES -> context.getString(R.string.image)
            Category.SEARCH_FOLDER_VIDEOS -> context.getString(R.string.nav_menu_video)
            Category.SEARCH_FOLDER_AUDIO -> context.getString(R.string.audio)
            Category.SEARCH_FOLDER_DOCS -> context.getString(R.string.home_docs)
            Category.PDF -> context.getString(R.string.pdf)
            Category.DOCS_OTHER -> context.getString(R.string.other)
            else -> "null"
        }
    }

    private fun addFolderCategoryFragments(path: String?, category: Category) {
        val fragment1 = FileListFragment.newInstance(path, category, false)
        val fragment2 = FileListFragment.newInstance(getSubDirImagePath(path, Category.SEARCH_FOLDER_IMAGES), Category.SEARCH_FOLDER_IMAGES, false)
        val fragment3 = FileListFragment.newInstance(getSubDirImagePath(path, Category.SEARCH_FOLDER_VIDEOS), Category.SEARCH_FOLDER_VIDEOS, false)
        val fragment4 = FileListFragment.newInstance(getSubDirImagePath(path, Category.SEARCH_FOLDER_AUDIO), Category.SEARCH_FOLDER_AUDIO, false)
        val fragment5 = FileListFragment.newInstance(getSubDirImagePath(path, Category.SEARCH_FOLDER_DOCS), Category.SEARCH_FOLDER_DOCS, false)

        fragment1.setCategoryMenuHelper(this)
        fragment2.setCategoryMenuHelper(this)
        fragment3.setCategoryMenuHelper(this)
        fragment4.setCategoryMenuHelper(this)
        fragment5.setCategoryMenuHelper(this)

        context?.let { context ->
            pagerAdapter.addFragment(fragment1, context.getString(R.string.category_all))
            pagerAdapter.addFragment(fragment2, getTitle(context, Category.SEARCH_FOLDER_IMAGES))
            pagerAdapter.addFragment(fragment3, getTitle(context, Category.SEARCH_FOLDER_VIDEOS))
            pagerAdapter.addFragment(fragment4, getTitle(context, Category.SEARCH_FOLDER_AUDIO))
            pagerAdapter.addFragment(fragment5, getTitle(context, Category.SEARCH_FOLDER_DOCS))
        }
    }

    private fun addDocCategoryFragments(path: String?, category: Category) {
        val fragment1 = FileListFragment.newInstance(path, category, false)
        val fragment2 = FileListFragment.newInstance(path, Category.PDF, false)
        val fragment3 = FileListFragment.newInstance(path, Category.DOCS_OTHER, false)

        fragment1.setCategoryMenuHelper(this)
        fragment2.setCategoryMenuHelper(this)
        fragment3.setCategoryMenuHelper(this)

        context?.let { context ->
            pagerAdapter.addFragment(fragment1, context.getString(R.string.category_all))
            pagerAdapter.addFragment(fragment2, getTitle(context, Category.PDF))
            pagerAdapter.addFragment(fragment3, getTitle(context, Category.DOCS_OTHER))
        }
    }

    private fun getSubDirImagePath(path: String?, category: Category): String? {
        if (path == null) {
            return null
        }
        when (path) {
            SearchUtils.getWhatsappDirectory() -> {
                return when (category) {
                    Category.SEARCH_FOLDER_IMAGES -> SearchUtils.getWhatsappImagesDirectory()
                    Category.SEARCH_FOLDER_VIDEOS -> SearchUtils.getWhatsappVideosDirectory()
                    Category.SEARCH_FOLDER_AUDIO -> SearchUtils.getWhatsappAudioDirectory()
                    Category.SEARCH_FOLDER_DOCS -> SearchUtils.getWhatsappDocDirectory()
                    else -> path
                }
            }
            SearchUtils.getTelegramDirectory() -> {
                return when (category) {
                    Category.SEARCH_FOLDER_IMAGES -> SearchUtils.getTelegramImagesDirectory()
                    Category.SEARCH_FOLDER_VIDEOS -> SearchUtils.getTelegramVideosDirectory()
                    Category.SEARCH_FOLDER_AUDIO -> SearchUtils.getTelegramAudioDirectory()
                    Category.SEARCH_FOLDER_DOCS -> SearchUtils.getTelegramDocsDirectory()
                    else -> path
                }
            }
        }
        return path
    }

    fun onBackPressed(): Boolean {
        val fragment = pagerAdapter.getItem(viewPager.currentItem)
        if (fragment is FileListFragment) {
            return fragment.onBackPressed()
        }
        return true
    }


}