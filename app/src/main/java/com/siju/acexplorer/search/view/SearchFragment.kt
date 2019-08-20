package com.siju.acexplorer.search.view

import android.app.SearchManager
import android.content.Context.SEARCH_SERVICE
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.siju.acexplorer.AceApplication
import com.siju.acexplorer.R
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.extensions.inflateLayout
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.search.model.SearchModelImpl
import com.siju.acexplorer.search.viewmodel.SearchViewModel
import com.siju.acexplorer.search.viewmodel.SearchViewModelFactory
import com.siju.acexplorer.storage.model.StorageModelImpl
import com.siju.acexplorer.storage.model.ViewMode
import com.siju.acexplorer.storage.modules.zipviewer.view.ZipViewerFragment
import com.siju.acexplorer.storage.view.FileListHelper
import com.siju.acexplorer.storage.view.FilesList
import com.siju.acexplorer.storage.viewmodel.FileListViewModel
import com.siju.acexplorer.storage.viewmodel.FileListViewModelFactory
import kotlinx.android.synthetic.main.toolbar.*
import java.util.*


class SearchFragment private constructor(): Fragment(), SearchView.OnQueryTextListener, FileListHelper {

    companion object {
        fun newInstance() = SearchFragment()
    }

    private lateinit var searchViewModel: SearchViewModel
    private lateinit var fileListViewModel: FileListViewModel
    private lateinit var searchView: SearchView
    private lateinit var filesList: FilesList

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflateLayout(R.layout.search_main, container)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)

        view?.let {
            setupUI(it)
            setupViewModel()
            initObservers()
        }
    }

    private fun setupUI(view: View) {
        setupToolbar()
        filesList = FilesList(this, view, ViewMode.LIST)
    }

    private fun setupToolbar() {
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
    }

    private fun setupViewModel() {
        val viewModelFactory = SearchViewModelFactory(SearchModelImpl(AceApplication.appContext))
        searchViewModel = ViewModelProviders.of(this, viewModelFactory).get(SearchViewModel::class.java)
        val fileListViewModelFactory = FileListViewModelFactory(StorageModelImpl(AceApplication.appContext), true)
        fileListViewModel = ViewModelProviders.of(this, fileListViewModelFactory)
                .get(FileListViewModel::class.java)
        fileListViewModel.setCategory(Category.FILES)
    }

    private fun initObservers() {
        searchViewModel.searchResult.observe(viewLifecycleOwner, Observer {
            it?.apply {
                if (::filesList.isInitialized) {
                    Log.e("SearchFragment", " Search result:${it.size}")
                    filesList.onDataLoaded(it)
                }
            }
        })

        fileListViewModel.fileData.observe(viewLifecycleOwner, Observer {
            it?.apply {
                if (::filesList.isInitialized) {
                    filesList.onDataLoaded(it)
                }
            }
        })

        fileListViewModel.directoryClicked.observe(viewLifecycleOwner, Observer {
            it?.apply {
                fileListViewModel.saveScrollInfo(filesList.getScrollInfo())
            }
        })

        fileListViewModel.scrollInfo.observe(viewLifecycleOwner, Observer {
            it?.apply {
                filesList.scrollToPosition(it)
            }
        })

        fileListViewModel.openZipViewerEvent.observe(viewLifecycleOwner, Observer {
            it?.apply {
                val zipViewer = ZipViewerFragment(this@SearchFragment, it.first,
                        it.second)
                fileListViewModel.setZipViewer(zipViewer)
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        Log.e(SearchFragment.javaClass.simpleName, "onCreateOptionsMenu")
        inflater.inflate(R.menu.search, menu)
        searchView = menu.findItem(R.id.action_search).actionView as SearchView
        setupSearchView()
    }

    private fun setupSearchView() {
        searchView.setIconifiedByDefault(false)
        searchView.clearFocus()
        searchView.maxWidth = Integer.MAX_VALUE
        searchView.imeOptions = EditorInfo.IME_FLAG_NO_EXTRACT_UI
        searchView.imeOptions = EditorInfo.IME_ACTION_SEARCH
        searchView.setOnQueryTextListener(this)
        val searchManager = context?.getSystemService(
                SEARCH_SERVICE) as SearchManager
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(activity!!.componentName))
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        Log.e(SearchFragment.javaClass.simpleName, "onQueryTextSubmit : $query")
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        Log.e(SearchFragment.javaClass.simpleName, "onQueryTextChange : $newText")
        searchViewModel.search(null, newText)
        return true
    }

    override fun handleItemClick(fileInfo: FileInfo, position: Int) {
        fileListViewModel.handleItemClick(fileInfo, position)
    }

    override fun handleLongItemClick(fileInfo: FileInfo, second: Int) {
    }

    override fun isDualModeEnabled() = false

    override fun isDragNotStarted() = true

    override fun onUpEvent() {
    }

    override fun onMoveEvent() {
    }

    override fun endActionMode() {
    }

    override fun getCategory() = Category.FILES

    override fun onDragDropEvent(pos: Int, data: ArrayList<FileInfo>) {
    }

    fun onBackPressed() : Boolean {
        val result = fileListViewModel.onBackPress()
        if (!result && fileListViewModel.hasNoBackStackEntry()) {
            searchViewModel.search(null, searchView.query.toString())
            return false
        }
        return result
    }


}