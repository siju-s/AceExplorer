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

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.siju.acexplorer.AceApplication
import com.siju.acexplorer.R
import com.siju.acexplorer.home.edit.model.CategoryEditModelImpl
import com.siju.acexplorer.home.edit.viewmodel.CategoryEditViewModel
import com.siju.acexplorer.home.edit.viewmodel.CategoryEditViewModelFactory
import com.siju.acexplorer.storage.view.custom.helper.SimpleItemTouchHelperCallback
import kotlinx.android.synthetic.main.category_edit.*
import kotlinx.android.synthetic.main.toolbar.*

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
        adapter = CategoryEditAdapter(this)
        categoryList.setHasFixedSize(true)
        categoryList.adapter = adapter

        val callback = SimpleItemTouchHelperCallback(adapter)
        itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(categoryList)
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
                adapter.submitData(it)
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
            R.id.action_ok                        -> {
                categoryEditViewModel.saveCategories(adapter.getCheckedCategories())
                fragmentManager?.popBackStack()
            }

            R.id.action_cancel, android.R.id.home -> fragmentManager?.popBackStack()
        }
        return true
    }

    companion object {
        fun newInstance() = CategoryEditFragment()
    }
}

