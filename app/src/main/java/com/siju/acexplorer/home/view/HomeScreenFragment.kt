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

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.os.storage.StorageManager
import android.os.storage.StorageVolume
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.siju.acexplorer.R
import com.siju.acexplorer.databinding.HomescreenBinding
import com.siju.acexplorer.home.viewmodel.HomeViewModel
import com.siju.acexplorer.main.helper.UpdateChecker
import com.siju.acexplorer.main.model.StorageItem
import com.siju.acexplorer.main.model.StorageUtils
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.main.viewmodel.MainViewModel
import com.siju.acexplorer.permission.PermissionHelper
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

private const val TAG = "HomeScreenFragment"

@AndroidEntryPoint
class HomeScreenFragment : Fragment() {

    private val mainViewModel: MainViewModel by activityViewModels()
    private val homeViewModel: HomeViewModel by viewModels()
    private lateinit var categoryAdapter: HomeLibAdapter
    private lateinit var storageAdapter: HomeStorageAdapter

    private var searchItem: MenuItem? = null
    private var _binding: HomescreenBinding? = null
    private val binding get() = _binding!!

    private val storageManager by lazy {
        requireContext().getSystemService(Context.STORAGE_SERVICE) as StorageManager
    }

    // Refreshes the storage list when a removable drive (USB OTG / SD card) changes
    // state - mounted or unmounted - so it appears/disappears without leaving the screen.
    // StorageVolumeCallback (API 30+) is reliable across OEMs, unlike the legacy
    // ACTION_MEDIA_* broadcasts whose data scheme some devices do not populate.
    private val storageVolumeCallback = object : StorageManager.StorageVolumeCallback() {
        override fun onStateChanged(volume: StorageVolume) {
            Log.d(TAG, "storage volume state changed: ${volume.state}")
            homeViewModel.refreshStorageList()
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?): View {
        _binding = HomescreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(true)

        setupToolbar()
        initList()
        checkIfFilePicker()
        initObservers()
    }

    private fun setupToolbar() {
        val toolbar = binding.toolbarContainer.toolbar
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
        mainViewModel.navigateToRecent.observe(viewLifecycleOwner, { navigateToRecent ->
            if (navigateToRecent == true) {
                navigateToRecent()
            }
        })

        homeViewModel.categories.observe(viewLifecycleOwner, { categoryList ->
            Log.d(TAG, "categories: ${categoryList.size}")
            categoryAdapter.submitList(categoryList)
            homeViewModel.fetchCount(categoryList)
        })

        homeViewModel.storage.observe(viewLifecycleOwner, {
            it?.apply {
                mainViewModel.setStorageList(it)
                storageAdapter.submitList(buildStorageRows(it))
            }
        })

        mainViewModel.permissionStatus.observe(viewLifecycleOwner, { permissionStatus ->
            Log.d(TAG, "initObservers: permstatus:$permissionStatus")
            when (permissionStatus) {
                is PermissionHelper.PermissionState.Granted -> homeViewModel.loadData()
                else -> {}
            }
        })

        homeViewModel.categoryData.observe(viewLifecycleOwner, {
            categoryAdapter.notifyItemChanged(it.first, it.second)
            categoryAdapter.notifyDataSetChanged()
        })

        homeViewModel.categoryClickEvent.observe(viewLifecycleOwner) {
            it?.apply {
                loadCategory(first, second)
                homeViewModel.setCategoryClickEvent(null)
            }
        }
    }

    private fun checkIfFilePicker() {
        if (mainViewModel.isFilePicker()) {
            onFilePicker()
        }
    }

    private fun onFilePicker() {
        hideSearch()
    }

    private fun navigateToRecent() {
        loadCategory(null, Category.RECENT)
    }

    private fun hideSearch() {
        searchItem?.isVisible = false
    }

    private fun setupCategoriesList() {
        categoryAdapter = HomeLibAdapter {
            homeViewModel.onCategoryClick(it.category)
        }
        setupCategoryAdapter()
    }

    private fun setupStorageList() {
        val storageList = binding.storage.storageList
        storageAdapter = HomeStorageAdapter { storageItem ->
            onStorageClicked(storageItem)
        }
        storageList.adapter = storageAdapter
        val layoutManager = storageList.layoutManager as GridLayoutManager
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            // Section headers span the full width; storage cards take a single column.
            override fun getSpanSize(position: Int): Int {
                return if (storageAdapter.isHeader(position)) layoutManager.spanCount else 1
            }
        }
    }

    private fun buildStorageRows(items: List<StorageItem>): List<StorageRow> {
        val rows = ArrayList<StorageRow>()
        val internal = items.filter { it.storageType == StorageUtils.StorageType.INTERNAL }
        val external = items.filter { it.storageType != StorageUtils.StorageType.INTERNAL }
        if (internal.isNotEmpty()) {
            rows.add(StorageRow.Header(getString(R.string.storage_section_internal)))
            internal.forEach { rows.add(StorageRow.Item(it)) }
        }
        if (external.isNotEmpty()) {
            rows.add(StorageRow.Header(getString(R.string.storage_section_external)))
            external.forEach { rows.add(StorageRow.Item(it)) }
        }
        return rows
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

    private fun onStorageClicked(storageItem: StorageItem) {
        // A removable drive may have been unmounted since the list was drawn; opening its
        // dead path would crash the file list. Verify it is still readable first.
        val file = File(storageItem.path)
        if (!file.exists() || !file.canRead()) {
            Toast.makeText(requireContext(), R.string.drive_unavailable, Toast.LENGTH_SHORT).show()
            homeViewModel.refreshStorageList()
            return
        }
        loadList(storageItem.path, storageItem.category)
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
        mainViewModel.navigateToSearch()
    }

    override fun onStart() {
        super.onStart()
        storageManager.registerStorageVolumeCallback(
                ContextCompat.getMainExecutor(requireContext()), storageVolumeCallback)
        // A drive may have been mounted/unmounted while this screen was in the background.
        homeViewModel.refreshStorageList()
    }

    override fun onStop() {
        super.onStop()
        storageManager.unregisterStorageVolumeCallback(storageVolumeCallback)
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
        _binding = null
    }
}
