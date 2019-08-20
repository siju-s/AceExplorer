package com.siju.acexplorer.search.view

import android.app.Activity
import android.app.SearchManager
import android.content.Context.SEARCH_SERVICE
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.siju.acexplorer.AceApplication
import com.siju.acexplorer.R
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.extensions.inflateLayout
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.main.model.helper.UriHelper
import com.siju.acexplorer.main.model.helper.ViewHelper
import com.siju.acexplorer.main.view.dialog.DialogHelper
import com.siju.acexplorer.search.model.SearchModelImpl
import com.siju.acexplorer.search.viewmodel.SearchViewModel
import com.siju.acexplorer.search.viewmodel.SearchViewModelFactory
import com.siju.acexplorer.storage.model.StorageModelImpl
import com.siju.acexplorer.storage.model.ViewMode
import com.siju.acexplorer.storage.modules.zipviewer.view.ZipViewerFragment
import com.siju.acexplorer.storage.view.FileListAdapter
import com.siju.acexplorer.storage.view.FileListHelper
import com.siju.acexplorer.storage.viewmodel.FileListViewModel
import com.siju.acexplorer.storage.viewmodel.FileListViewModelFactory
import com.siju.acexplorer.utils.InstallHelper
import com.siju.acexplorer.utils.ScrollInfo
import kotlinx.android.synthetic.main.toolbar.*
import java.util.*

private const val DELAY_SCROLL_UPDATE_MS = 100L

class SearchFragment private constructor() : Fragment(), SearchView.OnQueryTextListener, FileListHelper {

    companion object {
        fun newInstance() = SearchFragment()
    }

    private lateinit var searchViewModel: SearchViewModel
    private lateinit var fileListViewModel: FileListViewModel
    private lateinit var searchView: SearchView
    private lateinit var filesList: RecyclerView
    private lateinit var adapter: SearchAdapter
    private var fileListAdapter: FileListAdapter? = null

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
        initializeViews(view)
    }

    private fun setupToolbar() {
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
    }

    private fun initializeViews(view: View) {
        filesList = view.findViewById(R.id.recyclerViewFileList)
        filesList.layoutManager = LinearLayoutManager(context)
        adapter = SearchAdapter {
            handleItemClick(it.first, it.second)
        }
        filesList.adapter = adapter
        fileListAdapter = FileListAdapter(ViewMode.LIST, {
            handleItemClick(it.first, it.second)
        },
                { _, _, _ ->
                }
        )
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
                    adapter.addHeaderAndSubmitList(it)
                }
            }
        })

        fileListViewModel.fileData.observe(viewLifecycleOwner, Observer {
            it?.apply {
                if (::filesList.isInitialized) {
                    fileListAdapter?.submitList(it)
                }
            }
        })

        fileListViewModel.directoryClicked.observe(viewLifecycleOwner, Observer {
            it?.apply {
                filesList.adapter = fileListAdapter
                fileListViewModel.saveScrollInfo(getScrollInfo())
            }
        })

        fileListViewModel.scrollInfo.observe(viewLifecycleOwner, Observer {
            it?.apply {
                scrollToPosition(it)
            }
        })

        fileListViewModel.openZipViewerEvent.observe(viewLifecycleOwner, Observer {
            it?.apply {
                val zipViewer = ZipViewerFragment(this@SearchFragment, it.first,
                        it.second)
                fileListViewModel.setZipViewer(zipViewer)
            }
        })

        fileListViewModel.viewFileEvent.observe(viewLifecycleOwner, Observer {
            viewFile(it.first, it.second)
        })
        fileListViewModel.installAppEvent.observe(viewLifecycleOwner, Observer {
            val canInstall = it.first
            if (canInstall) {
                InstallHelper.openInstallScreen(context, it.second)
            }
            else {
                InstallHelper.requestUnknownAppsInstallPermission(this)
            }
        })
    }

    private fun viewFile(path: String, extension: String?) {
        val context = context
        context?.let {
            when (extension?.toLowerCase(Locale.ROOT)) {
                null -> {
                    val uri = UriHelper.createContentUri(context, path)
                    uri?.let {
                        DialogHelper.openWith(it, context)
                    }
                }
                ViewHelper.EXT_APK -> ViewHelper.viewApkFile(context, path,
                        fileListViewModel.apkDialogListener)
                else -> ViewHelper.viewFile(context, path, extension)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        Log.e(SearchFragment::class.java.simpleName, "onCreateOptionsMenu")
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
        Log.e(SearchFragment::class.java.simpleName, "onQueryTextSubmit : $query")
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        Log.e(SearchFragment::class.java.simpleName, "onQueryTextChange : $newText")
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

    private fun getScrollInfo(): ScrollInfo {
        val view = filesList.getChildAt(0)
        val offset = view?.top ?: 0
        val layoutManager = filesList.layoutManager as LinearLayoutManager
        val position = layoutManager.findFirstVisibleItemPosition()
        return ScrollInfo(position, offset)
    }

    //TODO Find way to get right delay time (probably after list drawn)
    private fun scrollToPosition(scrollInfo: ScrollInfo) {
        filesList.postDelayed({
            val layoutManager = filesList.layoutManager as LinearLayoutManager
            scrollListView(scrollInfo, layoutManager)

        }, DELAY_SCROLL_UPDATE_MS)
    }

    private fun scrollListView(scrollInfo: ScrollInfo,
                               layoutManager: LinearLayoutManager) {
        if (shouldScrollToTop(scrollInfo)) {
            scrollListToTop(layoutManager)
        } else {
            layoutManager.scrollToPositionWithOffset(scrollInfo.position,
                    scrollInfo.offset)
        }
    }

    private fun shouldScrollToTop(
            scrollInfo: ScrollInfo) = scrollInfo.position == 0 && scrollInfo.offset == 0

    private fun scrollListToTop(layoutManager: LinearLayoutManager) {
        layoutManager.smoothScrollToPosition(filesList, null, 0)
    }

    fun onBackPressed(): Boolean {
        val result = fileListViewModel.onBackPress()
        if (!result && fileListViewModel.hasNoBackStackEntry()) {
            filesList.adapter = adapter
            searchViewModel.search(null, searchView.query.toString())
            return false
        }
        return result
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        when (requestCode) {
            InstallHelper.UNKNOWN_APPS_INSTALL_REQUEST -> {
                if (resultCode == Activity.RESULT_OK) {
                    InstallHelper.openInstallScreen(context, fileListViewModel.apkPath)
                    fileListViewModel.apkPath = null
                }
            }
        }
    }


}