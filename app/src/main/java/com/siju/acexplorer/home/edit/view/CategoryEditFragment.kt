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

package com.siju.acexplorer.home.edit.view

import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.siju.acexplorer.AceApplication
import com.siju.acexplorer.R
import com.siju.acexplorer.extensions.showToast
import com.siju.acexplorer.home.edit.model.CategoryEditModelImpl
import com.siju.acexplorer.home.edit.viewmodel.CategoryEditViewModel
import com.siju.acexplorer.home.edit.viewmodel.CategoryEditViewModelFactory
import com.siju.acexplorer.storage.view.custom.helper.SimpleItemTouchHelperCallback
import kotlinx.android.synthetic.main.category_edit.*
import kotlinx.android.synthetic.main.toolbar.*

private const val MIN_LIBRARY_ITEMS = 3
private const val MAX_LIBRARY_ITEMS = 12

class CategoryEditFragment : Fragment(), OnStartDragListener {
    private lateinit var categoryEditViewModel: CategoryEditViewModel
    private lateinit var itemTouchHelper: ItemTouchHelper
    private lateinit var adapter: CategoryEditAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.category_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        setupUI()
    }

    private fun setupUI() {
        setupViewModels()
        setupToolbar()
        setupList()
        initObservers()
        loadData()
    }

    private fun setupToolbar() {
        toolbar.title = resources.getString(R.string.app_name)
        val activity = activity as AppCompatActivity?
        activity?.setSupportActionBar(toolbar)
        activity?.supportActionBar?.setHomeButtonEnabled(true)
        activity?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setupList() {
        setCategoryLayoutManager()
        adapter = CategoryEditAdapter { item, _ ->
            onCategoryEdit(item)
        }
        categoryList.adapter = adapter

        val callback = SimpleItemTouchHelperCallback(adapter)
        itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(categoryList)
    }

    private fun onCategoryEdit(item: CategoryEditModelImpl.DataItem.Content) {
        val checkedCount = adapter.getCheckedItemCount()
        if (item.categoryEdit.checked) {
            onCategoryRemove(checkedCount, item)
        } else {
            onCategoryAdd(checkedCount, item)
        }
    }

    private fun onCategoryAdd(checkedCount: Int, item: CategoryEditModelImpl.DataItem.Content) {
        if (checkedCount + 1 > MAX_LIBRARY_ITEMS) {
            context.showToast(getString(R.string.category_max_min_items_error))
        } else {
            categoryEditViewModel.addCategory(item, checkedCount)
        }
    }

    private fun onCategoryRemove(checkedCount: Int, item: CategoryEditModelImpl.DataItem.Content) {
        if (checkedCount - 1 < MIN_LIBRARY_ITEMS) {
            context.showToast(getString(R.string.category_max_min_items_error))
        } else {
            categoryEditViewModel.removeCategory(item, checkedCount)
        }
    }

    private fun setCategoryLayoutManager() {
        val gridColumns = getGridColumns()
        Log.e("CategoryEdit", "gridCols:$gridColumns")
        val gridLayoutManager = GridLayoutManager(context, gridColumns)
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when (adapter.getItemViewType(position)) {
                    CategoryEditAdapter.EDIT_ITEM_VIEW_TYPE_HEADER -> gridColumns
                    CategoryEditAdapter.EDIT_ITEM_VIEW_TYPE_ITEM -> 1
                    else -> 1
                }
            }
        }
        categoryList.layoutManager = gridLayoutManager
    }

    private fun getGridColumns(): Int {
        val imageSize = resources.getDimensionPixelSize(R.dimen.home_library_width) +
                2 * resources.getDimensionPixelSize(R.dimen.margin_16)
        return Resources.getSystem().displayMetrics.widthPixels / imageSize
    }

    private fun setupViewModels() {
        val viewModelFactory = CategoryEditViewModelFactory(
                CategoryEditModelImpl(AceApplication.appContext))
        categoryEditViewModel = ViewModelProviders.of(this, viewModelFactory)
                .get(CategoryEditViewModel::class.java)
    }

    private fun initObservers() {
        categoryEditViewModel.categories.observe(viewLifecycleOwner, Observer {
            it?.apply {
                Log.e("CategoryEditFragment", "count:${it.size}, itemsEdited:${categoryEditViewModel.itemsEdited.value}")
                if (categoryEditViewModel.itemsEdited.value == true) {
                    categoryEditViewModel.setItemEditComplete()
                    adapter.submitData(it)
                    adapter.notifyDataSetChanged()
                } else {
                    adapter.submitData(it)
                }
            }
        })
    }

    private fun loadData() {
        categoryEditViewModel.fetchCategories()
    }

    override fun onStartDrag(viewHolder: RecyclerView.ViewHolder) {
        itemTouchHelper.startDrag(viewHolder)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.library_sort, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_ok -> {
                categoryEditViewModel.saveCategories(adapter.getCheckedCategories())
                fragmentManager?.popBackStack()
            }

            R.id.action_cancel, android.R.id.home -> fragmentManager?.popBackStack()
        }
        return true
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Log.e(this.javaClass.simpleName, "onConfigurationChanged:${newConfig.orientation}")
        setCategoryLayoutManager()
    }

    companion object {
        fun newInstance() = CategoryEditFragment()
    }
}


