package com.siju.acexplorer.trash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.siju.acexplorer.common.theme.MyApplicationTheme
import com.siju.acexplorer.common.theme.Theme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TrashFragment : Fragment() {

    private val viewModel: TrashViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MyApplicationTheme(appTheme = Theme.getTheme(requireContext())) {
                    val entries by viewModel.entries.observeAsState(emptyList())
                    val totalSize by viewModel.totalSize.observeAsState(0L)

                    TrashScreen(
                        entries = entries,
                        totalSize = totalSize,
                        retentionDays = viewModel.retentionDays,
                        onNavigateBack = { findNavController().navigateUp() },
                        onRestore = viewModel::restore,
                        onDeleteForever = viewModel::deleteForever,
                        onEmptyAll = viewModel::emptyAll
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.purgeExpired()
    }
}
