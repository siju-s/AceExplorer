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

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import com.siju.acexplorer.AceApplication
import com.siju.acexplorer.R
import com.siju.acexplorer.ads.AdsView
import com.siju.acexplorer.home.model.HomeModelImpl
import com.siju.acexplorer.home.viewmodel.HomeViewModel
import com.siju.acexplorer.home.viewmodel.HomeViewModelFactory
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.main.viewmodel.MainViewModel
import com.siju.acexplorer.permission.PermissionHelper
import com.siju.acexplorer.storage.view.FileListFragment
import kotlinx.android.synthetic.main.home_categories.*
import kotlinx.android.synthetic.main.home_storage.*
import kotlinx.android.synthetic.main.homescreen.*

private const val TAG = "HomeScreenFragment"

//TODO Add Navigation to this class
class HomeScreenFragment : Fragment() {

    private lateinit var mainViewModel: MainViewModel
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var categoryAdapter: HomeLibAdapter
    private lateinit var storageAdapter: HomeStorageAdapter
    private lateinit var adView : AdsView

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.homescreen, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        setupViewModels()
        adView = AdsView(container)

        initList()
        initObservers()
    }

    private fun setupViewModels() {
        val activity = requireNotNull(activity)
        mainViewModel = ViewModelProviders.of(activity).get(MainViewModel::class.java)
        val viewModelFactory = HomeViewModelFactory(HomeModelImpl(AceApplication.appContext))
        homeViewModel = ViewModelProviders.of(this, viewModelFactory).get(HomeViewModel::class.java)
    }

    private fun initList() {
        setupCategoriesList()
        setupStorageList()
    }

    private fun initObservers() {
        mainViewModel.premiumLiveData.observe(viewLifecycleOwner, Observer {
            Log.e(TAG, "Premium state:$it")
            it?.apply {
                if (it.entitled) {
                    hideAds()
                }
                else {
                    showAds()
                }
            }
        })

        homeViewModel.categories.observe(viewLifecycleOwner, Observer {categoryList ->
            Log.e(TAG, "categories: ${categoryList.size}")
            categoryAdapter.submitList(categoryList)
            homeViewModel.fetchCount(categoryList)
        })

        homeViewModel.storage.observe(viewLifecycleOwner, Observer {
            storageAdapter.submitList(it)
        })

        mainViewModel.permissionStatus.observe(viewLifecycleOwner, Observer { permissionStatus ->
            when (permissionStatus) {
                is PermissionHelper.PermissionState.Granted -> homeViewModel.loadData()
                else -> {
                }
            }
        })

        homeViewModel.categoryData.observe(viewLifecycleOwner, Observer {
            Log.e(TAG, "categorydata: ${it.first}, ${it.second}")
            categoryAdapter.notifyItemChanged(it.first, it.second)
        })

//        lifecycle.addObserver(adView)
    }

    private fun showAds() {
        adView.showAds()
    }

    private fun hideAds() {
        adView.hideAds()
    }

    private fun setupCategoriesList() {
        Log.e(TAG, "setupCategoriesList")
        categoryList.isNestedScrollingEnabled = true
        setCategoryLayoutManager()
        categoryAdapter = HomeLibAdapter {
           loadList(null.toString(), it.category)
        }
//        setItemTouchHelper()
        categoryList.adapter = categoryAdapter
    }

    private fun setupStorageList() {
        Log.e(TAG, "setupStorageList")
        storageList.isNestedScrollingEnabled = true
        storageAdapter = HomeStorageAdapter { storageItem ->
            loadList(storageItem.path, storageItem.category)
        }
        storageList.adapter = storageAdapter
    }

//    private fun setItemTouchHelper() {
//        val callback = SimpleItemTouchHelperCallback(categoryAdapter)
//        val itemTouchHelper = ItemTouchHelper(callback)
//        itemTouchHelper.attachToRecyclerView(categoryList)
//    }

    private fun setCategoryLayoutManager() {
        val gridColumns = homeViewModel.getCategoryGridColumns()
        Log.e(TAG, "gridColumns$gridColumns")
        val gridLayoutManager = GridLayoutManager(context, gridColumns)
        categoryList.layoutManager = gridLayoutManager
    }

    private fun loadList(path: String, category: Category) {
        val transaction = activity?.supportFragmentManager?.beginTransaction()
        transaction?.apply {
            replace(R.id.main_container, FileListFragment.newInstance(path, category))
            addToBackStack(null)
            commit()
        }
    }

    companion object {

        fun newInstance(): HomeScreenFragment {
            return HomeScreenFragment()
        }
    }

}
