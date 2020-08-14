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
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.siju.acexplorer.R
import com.siju.acexplorer.ads.AdsView
import com.siju.acexplorer.databinding.HomescreenBinding
import com.siju.acexplorer.home.viewmodel.HomeViewModel
import com.siju.acexplorer.main.helper.UpdateChecker
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.main.viewmodel.MainViewModel
import com.siju.acexplorer.permission.PermissionHelper
import dagger.hilt.android.AndroidEntryPoint

private const val TAG = "HomeScreenFragment"

@AndroidEntryPoint
class HomeScreenFragment : Fragment() {

    private val mainViewModel: MainViewModel by activityViewModels()
    private val homeViewModel: HomeViewModel by viewModels()
    private lateinit var categoryAdapter: HomeLibAdapter
    private lateinit var storageAdapter: HomeStorageAdapter
    private lateinit var adView: AdsView

    private var searchItem: MenuItem? = null
    private var _binding: HomescreenBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?): View? {
        _binding = HomescreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        setHasOptionsMenu(true)
        adView = AdsView(binding.container)
        lifecycle.addObserver(adView)

        setupToolbar()
        initList()
        checkIfFilePicker()
        initObservers()
    }

    private fun setupToolbar() {
        val toolbar = binding.appBar.toolbar.toolbar
        toolbar.title = resources.getString(R.string.app_name)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
    }

    private fun initList() {
        setupCategoriesList()
        setupStorageList()
        binding.category.editButton.setOnClickListener {
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

        mainViewModel.navigateToRecent.observe(viewLifecycleOwner, Observer {navigateToRecent ->
            if (navigateToRecent == true) {
                navigateToRecent()
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
            categoryAdapter.notifyItemChanged(it.first, it.second)
            categoryAdapter.notifyDataSetChanged()
        })

        homeViewModel.categoryClickEvent.observe(viewLifecycleOwner, Observer {
            it?.apply {
                loadCategory(first, second)
                homeViewModel.setCategoryClickEvent(null)
            }
        })
    }

    private fun checkIfFilePicker() {
        if (mainViewModel.isFilePicker()) {
            onFilePicker()
        }
    }

    private fun onFilePicker() {
        hideSearch()
        hideAds()
    }

    private fun navigateToRecent() {
        loadCategory(null, Category.RECENT)
    }

    private fun hideSearch() {
        searchItem?.isVisible = false
    }

    private fun showAds() {
        adView.showAds()
    }

    private fun hideAds() {
        adView.hideAds()
    }

    private fun setupCategoriesList() {
        categoryAdapter = HomeLibAdapter(homeViewModel)
        setupCategoryAdapter()
    }

    private fun setupStorageList() {
        val storageList = binding.storage.storageList
        storageAdapter = HomeStorageAdapter { storageItem ->
            loadList(storageItem.path, storageItem.category)
        }
        storageList.adapter = storageAdapter
    }

    private fun setupCategoryAdapter() {
        val gridColumns = homeViewModel.getCategoryGridColumns()
        Log.d(TAG, "gridColumns$gridColumns")
        val gridLayoutManager = GridLayoutManager(context, gridColumns)
        binding.category.categoryList.apply {
            layoutManager = gridLayoutManager
            adapter = categoryAdapter
        }
    }

    private fun loadList(path: String?, category: Category) {
        val actions = HomeScreenFragmentDirections.actionNavigationHomeToFileListFragment(path, category, true)
        val navController = findNavController()
        navController.navigate(actions)
    }

    private fun loadCategory(path: String?, category: Category) {
        Log.d(TAG, "loadCategory() called with: path = $path, category = $category")
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
        searchItem = menu.findItem(R.id.action_search)
        if (mainViewModel.isFilePicker()) {
            hideSearch()
        }
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
        _binding = null
    }
}
