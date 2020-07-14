/*
 * Copyright (C) 2017 Ace Explorer owned by Siju Sakaria
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.siju.acexplorer.home.view

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.siju.acexplorer.R
import com.siju.acexplorer.ads.AdsView
import com.siju.acexplorer.home.viewmodel.HomeViewModel
import com.siju.acexplorer.main.helper.UpdateChecker
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.main.viewmodel.MainViewModel
import com.siju.acexplorer.permission.PermissionHelper
import com.siju.acexplorer.theme.Theme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.home_categories.*
import kotlinx.android.synthetic.main.home_storage.*
import kotlinx.android.synthetic.main.homescreen.*
import kotlinx.android.synthetic.main.toolbar.*

private const val TAG = "HomeScreenFragment"

@AndroidEntryPoint
class HomeScreenFragment : Fragment() {

    private lateinit var mainViewModel: MainViewModel
    private val homeViewModel: HomeViewModel by viewModels()
    private lateinit var categoryAdapter: HomeLibAdapter
    private lateinit var storageAdapter: HomeStorageAdapter
    private lateinit var adView: AdsView

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.homescreen, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        setHasOptionsMenu(true)
        setupViewModels()
        adView = AdsView(container)
        lifecycle.addObserver(adView)

        setupToolbar()
        initList()
        initObservers()
    }

    private fun setupViewModels() {
        val activity = requireNotNull(activity)
        mainViewModel = ViewModelProvider(activity).get(MainViewModel::class.java)
    }

    private fun setupToolbar() {
        toolbar.title = resources.getString(R.string.app_name)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
    }

    private fun initList() {
        setupCategoriesList()
        setupStorageList()
        editButton.setOnClickListener {
            showCategoryEditScreen()
        }
    }

    private fun showCategoryEditScreen() {
       val action = HomeScreenFragmentDirections.actionNavigationHomeToCategoryEdit()
        findNavController().navigate(action)
    }

    private fun initObservers() {
        mainViewModel.premiumLiveData.observe(viewLifecycleOwner, Observer {
            Log.d(TAG, "Premium state:$it")
            it?.apply {
                if (it.entitled) {
                    hideAds()
                } else {
                    showAds()
                }
            }
        })

        homeViewModel.categories.observe(viewLifecycleOwner, Observer { categoryList ->
            Log.d(TAG, "categories: ${categoryList.size}")
            categoryAdapter.submitList(categoryList)
            homeViewModel.fetchCount(categoryList)
        })

        homeViewModel.storage.observe(viewLifecycleOwner, Observer {
            it?.apply {
                mainViewModel.setStorageList(it)
                storageAdapter.submitList(it)
            }
        })

        mainViewModel.permissionStatus.observe(viewLifecycleOwner, Observer { permissionStatus ->
            when (permissionStatus) {
                is PermissionHelper.PermissionState.Granted -> homeViewModel.loadData()
            }
        })

        homeViewModel.categoryData.observe(viewLifecycleOwner, Observer {
            Log.d(TAG, "categorydata: ${it.first}, ${it.second}")
            categoryAdapter.notifyItemChanged(it.first, it.second)
            categoryAdapter.notifyDataSetChanged()
        })

        homeViewModel.categoryClickEvent.observe(viewLifecycleOwner, Observer {
            it?.apply {
                loadCategory(first, second)
                homeViewModel.setCategoryClickEvent(null)
            }
        })
        mainViewModel.theme.observe(viewLifecycleOwner, Observer {
            it?.apply {
                if (Theme.isDarkColoredTheme(resources, this)) {
                    editButton.setImageResource(R.drawable.ic_rename_white)
                }
                else {
                    editButton.setImageResource(R.drawable.ic_edit)
                }
            }
        })
    }

    private fun showAds() {
        adView.showAds()
    }

    private fun hideAds() {
        adView.hideAds()
    }

    private fun setupCategoriesList() {
        Log.d(TAG, "setupCategoriesList")
        categoryList.isNestedScrollingEnabled = true
        categoryAdapter = HomeLibAdapter {
            homeViewModel.onCategoryClick(it.category)
        }
        setupCategoryAdapter()
    }

    private fun setupStorageList() {
        Log.d(TAG, "setupStorageList")
        storageList.isNestedScrollingEnabled = true
        storageAdapter = HomeStorageAdapter { storageItem ->
            loadList(storageItem.path, storageItem.category)
        }
        storageList.adapter = storageAdapter
    }

    private fun setupCategoryAdapter() {
        val gridColumns = homeViewModel.getCategoryGridColumns()
        Log.d(TAG, "gridColumns$gridColumns")
        val gridLayoutManager = GridLayoutManager(context, gridColumns)
        categoryList.layoutManager = gridLayoutManager
        categoryList.adapter = categoryAdapter
    }

    private fun loadList(path: String?, category: Category) {
        val actions = HomeScreenFragmentDirections.actionNavigationHomeToFileListFragment(path, category, true)
        val navController = findNavController()
        navController.navigate(actions)
    }

    private fun loadCategory(path: String?, category: Category) {
        val action = if (isCategorySplitRequired(category)) {
            HomeScreenFragmentDirections.actionNavigationHomeToCategoryFragment(path, category)
        } else {
            HomeScreenFragmentDirections.actionNavigationHomeToFileListFragment(path, category, true)
        }
        findNavController().navigate(action)
    }

    private fun isCategorySplitRequired(category: Category): Boolean {
        return category == Category.WHATSAPP || category == Category.TELEGRAM ||
                category == Category.GENERIC_MUSIC || category == Category.GENERIC_IMAGES ||
                category == Category.GENERIC_VIDEOS || category == Category.CAMERA_GENERIC ||
                category == Category.RECENT || category == Category.LARGE_FILES ||
                category == Category.DOCS
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.home, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_search -> {
                navigateToSearchScreen()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun navigateToSearchScreen() {
        mainViewModel.navigateToSearch.value = true
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Log.d(TAG, "onConfigurationChanged:$newConfig")
        setupCategoryAdapter()
    }

    fun showUpdateSnackbar(updateChecker: UpdateChecker?) {
        updateChecker?.showUpdateSnackbar(view?.findViewById(R.id.container))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        lifecycle.removeObserver(adView)
    }
}
