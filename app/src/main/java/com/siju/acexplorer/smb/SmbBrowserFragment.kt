/*
 * Copyright (C) 2026 Ace Explorer owned by Siju Sakaria
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

package com.siju.acexplorer.smb

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.siju.acexplorer.R
import com.siju.acexplorer.databinding.DialogSmbConnectBinding
import com.siju.acexplorer.databinding.FragmentSmbBrowserBinding
import com.siju.acexplorer.main.model.helper.ViewHelper
import com.siju.acexplorer.storage.modules.picker.model.PickerModelImpl
import com.siju.acexplorer.storage.modules.picker.types.PickerType
import com.siju.acexplorer.storage.modules.picker.view.PickerFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SmbBrowserFragment : Fragment() {

    private val viewModel: SmbBrowserViewModel by activityViewModels()
    private var _binding: FragmentSmbBrowserBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: SmbFileAdapter
    private lateinit var serverAdapter: SmbServerAdapter
    private lateinit var nearbyServerAdapter: SmbNearbyServerAdapter
    private var selectedEntry: SmbEntry? = null
    private var pendingCopy: SmbEntry? = null
    private var requestedServer: SmbSavedServer? = null
    private var lastShownError: String? = null
    private var openingSavedDrive = false
    private var copyDestinationPaths: ArrayList<String>? = null
    private var lastShownCompletion: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSmbBrowserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requestedServer = savedServerFromArguments()
        copyDestinationPaths = arguments?.getStringArrayList(ARG_COPY_SOURCE_PATHS)
        setupToolbar()
        setupList()
        setupSelectionToolbar()
        observeState()
        parentFragmentManager.setFragmentResultListener(SMB_COPY_REQUEST_KEY, viewLifecycleOwner) { _, result ->
            val destination = result.getString(PickerModelImpl.KEY_PICKER_SELECTED_PATH)
            pendingCopy?.let { entry -> destination?.let { viewModel.copyToDevice(entry, it) } }
            pendingCopy = null
        }
        binding.connectButton.setOnClickListener {
            viewModel.showConnectionPicker()
            showConnectDialog()
        }
        binding.scanNearbyButton.setOnClickListener { viewModel.discoverServers() }
        binding.swipeRefresh.setOnRefreshListener { viewModel.refresh() }
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    navigateUp()
                }
            }
        )
        if (savedInstanceState == null) {
            requestedServer?.let(::connectSavedServer) ?: run {
                if (!viewModel.state.value.connected) {
                    viewModel.showConnectionPicker()
                    viewModel.discoverServers()
                }
            }
        }
    }

    private fun setupToolbar() {
        val toolbar = binding.toolbarContainer.toolbar
        val activity = requireActivity() as AppCompatActivity
        activity.setSupportActionBar(toolbar)
        activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.apply {
            title = requestedServer?.displayName() ?: getString(R.string.smb_title)
            setNavigationOnClickListener {
                if (selectedEntry != null) clearSelection() else navigateUp()
            }
            menu.add(0, MENU_DISCONNECT, 0, R.string.smb_disconnect)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
            setOnMenuItemClickListener { item ->
                if (item.itemId == MENU_DISCONNECT) {
                    showDisconnectDialog()
                    true
                } else {
                    false
                }
            }
        }
    }

    private fun setupList() {
        adapter = SmbFileAdapter(
            onItemClicked = { entry ->
                if (selectedEntry != null) selectEntry(entry)
                else if (entry.isDirectory) viewModel.openDirectory(entry) else viewModel.openFile(entry)
            },
            onItemLongClicked = ::selectEntry
        )
        binding.filesList.layoutManager = LinearLayoutManager(requireContext())
        binding.filesList.adapter = adapter
        serverAdapter = SmbServerAdapter { server ->
            if (server.connectionType == SmbConnectionType.LAN) {
                showLanConnectDialog(server)
            } else {
                showConnectDialog(server)
            }
        }
        binding.savedServersList.layoutManager = LinearLayoutManager(requireContext())
        binding.savedServersList.adapter = serverAdapter
        nearbyServerAdapter = SmbNearbyServerAdapter { server ->
            showLanConnectDialog(SmbSavedServer(host = server.host, connectionType = SmbConnectionType.LAN))
        }
        binding.nearbyServersList.layoutManager = LinearLayoutManager(requireContext())
        binding.nearbyServersList.adapter = nearbyServerAdapter
    }

    private fun setupSelectionToolbar() {
        binding.toolbarBottom.apply {
            inflateMenu(R.menu.action_mode_bottom)
            menu.findItem(R.id.action_edit).isVisible = false
            menu.findItem(R.id.action_cut).isVisible = false
            menu.findItem(R.id.action_delete).isVisible = false
            menu.findItem(R.id.action_delete_fav).isVisible = false
            menu.findItem(R.id.action_share).isVisible = false
            menu.findItem(R.id.action_info).isVisible = false
            menu.findItem(R.id.action_permissions).isVisible = false
            menu.findItem(R.id.action_archive).isVisible = false
            menu.findItem(R.id.action_extract).isVisible = false
            menu.findItem(R.id.action_fav).isVisible = false
            menu.findItem(R.id.action_hide).isVisible = false
            setOnMenuItemClickListener { item ->
                if (item.itemId == R.id.action_copy) {
                    if (copyDestinationPaths == null) {
                        showCopyDestination()
                    } else {
                        viewModel.copyFromDevice(copyDestinationPaths.orEmpty())
                    }
                    true
                } else false
            }
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                viewModel.state.collect(::render)
            }
        }
        viewModel.openedFile.observe(viewLifecycleOwner) { openedFile ->
            openedFile ?: return@observe
            if (openedFile.imageFiles.isNotEmpty()) {
                ViewHelper.openImageFiles(requireContext(), openedFile.imageFiles, openedFile.imageIndex)
            } else {
                ViewHelper.viewFile(requireContext(), openedFile.file.absolutePath, openedFile.file.extension)
            }
            viewModel.consumeOpenedFile()
        }
    }

    private fun render(state: SmbUiState) = with(binding) {
        val connectionStatus = state.transfer?.let { transfer ->
            if (transfer.direction == SmbTransferDirection.UPLOAD) {
                getString(R.string.smb_transfer_uploading, transfer.name, transfer.percent)
            } else {
                getString(R.string.smb_transfer_copying, transfer.name, transfer.percent)
            }
        } ?: state.error ?: if (state.loading) getString(R.string.smb_status_connecting) else getString(R.string.smb_status_connected)
        toolbarContainer.toolbar.title = when {
            state.connected -> state.username.takeIf { it.isNotBlank() }
                ?.let { "$it@${state.host}" }
                ?: state.host
            else -> requestedServer?.displayName() ?: getString(R.string.smb_title)
        }
        toolbarContainer.toolbar.subtitle = state.connectionType?.let { type ->
            val label = getString(
                if (type == SmbConnectionType.LAN) R.string.smb_lan_connection else R.string.smb_server
            )
            "$label · $connectionStatus"
        }
        toolbarContainer.toolbar.menu.findItem(MENU_DISCONNECT)?.isVisible = state.connected
        swipeRefresh.isRefreshing = state.loading && state.connected
        loading.visibility = if (state.loading && !state.connected) View.VISIBLE else View.GONE
        val hasEntries = state.entries.isNotEmpty()
        filesList.visibility = if (state.connected && hasEntries) View.VISIBLE else View.GONE
        offlineContent.visibility = if (state.connected || state.loading) View.GONE else View.VISIBLE
        adapter.submitList(state.entries)
        val hasSavedServers = state.savedServers.isNotEmpty()
        savedServersTitle.visibility = if (hasSavedServers) View.VISIBLE else View.GONE
        savedServersList.visibility = if (hasSavedServers) View.VISIBLE else View.GONE
        serverAdapter.submitList(state.savedServers)
        nearbyServerAdapter.submitList(state.nearbyServers)
        discoveryProgress.visibility = if (state.discoveringServers) View.VISIBLE else View.GONE
        scanNearbyButton.isEnabled = !state.discoveringServers
        scanNearbyButton.setText(
            if (state.hasScannedServers) R.string.smb_rescan_nearby else R.string.smb_scan_nearby
        )
        connectButton.visibility = if (state.connected) View.GONE else View.VISIBLE
        renderBreadcrumb(state)
        if (state.completion != null && copyDestinationPaths != null) {
            copyDestinationPaths = null
        }
        toolbarBottom.visibility = if (
            copyDestinationPaths != null && state.connected && state.shareName.isNotBlank()
        ) {
            toolbarBottom.menu.findItem(R.id.action_copy)?.title = getString(R.string.action_paste)
            View.VISIBLE
        } else if (selectedEntry != null) {
            View.VISIBLE
        } else {
            View.GONE
        }
        if (state.error != null && state.error != lastShownError) {
            lastShownError = state.error
            root.post {
                Toast.makeText(requireContext(), state.error, Toast.LENGTH_LONG).show()
                if (openingSavedDrive) {
                    openingSavedDrive = false
                    findNavController().navigateUp()
                }
            }
        } else if (state.error == null) {
            lastShownError = null
        }
        if (state.completion != null && state.completion != lastShownCompletion) {
            lastShownCompletion = state.completion
            root.post {
                Toast.makeText(requireContext(), state.completion, Toast.LENGTH_SHORT).show()
                viewModel.consumeCompletion()
            }
        } else if (state.completion == null) {
            lastShownCompletion = null
        }
        if (state.connected) openingSavedDrive = false
        emptyText.visibility = if (!state.loading && (
            state.error != null ||
                !(state.connected && hasEntries || hasSavedServers || state.nearbyServers.isNotEmpty())
        )) {
            View.VISIBLE
        } else {
            View.GONE
        }
        emptyText.text = when {
            state.error != null -> state.error
            state.connected -> getString(R.string.smb_empty)
            state.hasScannedServers -> getString(R.string.smb_no_nearby_servers)
            else -> getString(R.string.smb_connect_hint)
        }
    }

    private fun showConnectDialog(server: SmbSavedServer? = null) {
        val dialogBinding = DialogSmbConnectBinding.inflate(layoutInflater)
        dialogBinding.host.editText?.setText(server?.host)
        dialogBinding.share.editText?.setText(server?.shareName)
        dialogBinding.username.editText?.setText(server?.username)
        dialogBinding.rememberPassword.isChecked = server?.encryptedPassword?.isNotEmpty() == true
        dialogBinding.password.editText?.apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            hint = if (server?.let(viewModel::hasUsableSavedPassword) == true) "••••••••" else null
        }
        val dialogBuilder = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.smb_connect)
            .setView(dialogBinding.root)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.smb_connect_action, null)
        if (server != null) {
            dialogBuilder.setNeutralButton(R.string.smb_clear_credentials) { _, _ ->
                viewModel.clearCredentials(server)
            }
        }
        val dialog = dialogBuilder.create()
        dialog.setOnShowListener {
            dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val host = dialogBinding.host.editText?.text?.toString()?.trim().orEmpty()
                val share = dialogBinding.share.editText?.text?.toString()?.trim().orEmpty()
                dialogBinding.host.error = if (host.isBlank()) getString(R.string.smb_host_required) else null
                dialogBinding.share.error = if (share.isBlank()) getString(R.string.smb_share_required) else null
                if (host.isBlank() || share.isBlank()) return@setOnClickListener

                viewModel.connect(
                    host = host,
                    shareName = share,
                    username = dialogBinding.username.editText?.text?.toString().orEmpty(),
                    password = dialogBinding.password.editText?.text?.toString().orEmpty(),
                    rememberPassword = dialogBinding.rememberPassword.isChecked,
                    savedServer = server
                )
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun showLanConnectDialog(server: SmbSavedServer) {
        val dialogBinding = DialogSmbConnectBinding.inflate(layoutInflater)
        dialogBinding.host.visibility = View.GONE
        dialogBinding.share.visibility = View.GONE
        dialogBinding.username.editText?.setText(server.username)
        dialogBinding.rememberPassword.isChecked = server.encryptedPassword.isNotEmpty()
        dialogBinding.password.editText?.apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            hint = if (viewModel.hasUsableSavedPassword(server)) "••••••••" else null
        }
        val dialogBuilder = MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.smb_lan_connect_title, server.host))
            .setView(dialogBinding.root)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.smb_connect_action, null)
        if (server.connectionType == SmbConnectionType.LAN && server.username.isNotEmpty()) {
            dialogBuilder.setNeutralButton(R.string.smb_clear_credentials) { _, _ ->
                viewModel.clearCredentials(server)
            }
        }
        val dialog = dialogBuilder.create()
        dialog.setOnShowListener {
            dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                viewModel.connectLan(
                    host = server.host,
                    username = dialogBinding.username.editText?.text?.toString().orEmpty(),
                    password = dialogBinding.password.editText?.text?.toString().orEmpty(),
                    rememberPassword = dialogBinding.rememberPassword.isChecked,
                    savedServer = server.takeIf { it.encryptedPassword.isNotEmpty() }
                )
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun navigateUp() {
        if (viewModel.navigateUp()) return
        findNavController().navigateUp()
    }

    private fun showDisconnectDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.smb_disconnect)
            .setMessage(getString(R.string.smb_disconnect_confirmation, viewModel.state.value.host))
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.smb_disconnect) { _, _ ->
                viewModel.disconnect()
            }
            .show()
    }

    private fun selectEntry(entry: SmbEntry) {
        selectedEntry = entry
        adapter.selectedPath = entry.path
        binding.toolbarBottom.visibility = View.VISIBLE
    }

    private fun clearSelection() {
        selectedEntry = null
        adapter.selectedPath = null
        binding.toolbarBottom.visibility = View.GONE
    }

    private fun showCopyDestination() {
        val entry = selectedEntry ?: return
        pendingCopy = entry
        clearSelection()
        viewModel.cancelScheduledDisconnect()
        PickerFragment.newInstance(
            pickerType = PickerType.COPY,
            requestKey = SMB_COPY_REQUEST_KEY
        ).show(parentFragmentManager, SMB_COPY_PICKER_TAG)
    }

    private fun renderBreadcrumb(state: SmbUiState) {
        binding.listNavigation.scrollNavigation.visibility = if (state.connected) View.VISIBLE else View.GONE
        if (!state.connected) return
        binding.listNavigation.navButtonsContainer.removeAllViews()
        addBreadcrumbHome()
        if (state.shareName.isBlank()) return
        addBreadcrumbArrow()
        addBreadcrumbPart(state.shareName) { viewModel.openPath("") }
        var path = ""
        state.path.split('\\').filter { it.isNotBlank() }.forEach { part ->
            path = listOf(path, part).filter { it.isNotBlank() }.joinToString("\\")
            val targetPath = path
            addBreadcrumbArrow()
            addBreadcrumbPart(part) { viewModel.openPath(targetPath) }
        }
        binding.listNavigation.scrollNavigation.post {
            binding.listNavigation.scrollNavigation.fullScroll(android.widget.HorizontalScrollView.FOCUS_RIGHT)
        }
    }

    private fun addBreadcrumbHome() {
        val button = layoutInflater.inflate(R.layout.navigation_button, null) as ImageButton
        button.setImageResource(R.drawable.ic_home_white_48)
        button.contentDescription = getString(R.string.smb_title)
        button.setOnClickListener { viewModel.openNetworkRoot() }
        binding.listNavigation.navButtonsContainer.addView(button)
    }

    private fun addBreadcrumbPart(label: String, action: () -> Unit) {
        val button = layoutInflater.inflate(R.layout.material_button, null) as MaterialButton
        button.text = label
        button.setOnClickListener { action() }
        binding.listNavigation.navButtonsContainer.addView(button)
    }

    private fun addBreadcrumbArrow() {
        binding.listNavigation.navButtonsContainer.addView(
            layoutInflater.inflate(R.layout.navigation_arrow, null)
        )
    }

    private fun savedServerFromArguments(): SmbSavedServer? {
        val host = arguments?.getString(ARG_SAVED_HOST).orEmpty()
        val connectionType = arguments?.getString(ARG_SAVED_CONNECTION_TYPE)
            ?.let { runCatching { SmbConnectionType.valueOf(it) }.getOrNull() }
            ?: return null
        val shareName = arguments?.getString(ARG_SAVED_SHARE).orEmpty()
        return SmbServerStore(requireContext()).load().firstOrNull { server ->
            server.host.equals(host, ignoreCase = true) &&
                server.connectionType == connectionType &&
                (connectionType == SmbConnectionType.LAN || server.shareName == shareName)
        }
    }

    private fun connectSavedServer(server: SmbSavedServer) {
        openingSavedDrive = true
        if (viewModel.hasUsableSavedPassword(server)) {
            viewModel.connectSaved(server)
        } else if (server.connectionType == SmbConnectionType.LAN) {
            showLanConnectDialog(server)
        } else {
            showConnectDialog(server)
        }
    }

    private fun SmbSavedServer.displayName(): String = when (connectionType) {
        SmbConnectionType.LAN -> username.takeIf { it.isNotBlank() }?.let { "$it@$host" } ?: host
        SmbConnectionType.MANUAL_SMB -> listOf(host, shareName).filter { it.isNotBlank() }.joinToString("/")
        null -> listOf(host, shareName).filter { it.isNotBlank() }.joinToString("/")
    }

    override fun onStart() {
        super.onStart()
        viewModel.cancelScheduledDisconnect()
    }

    override fun onStop() {
        viewModel.scheduleDisconnect()
        super.onStop()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private companion object {
        const val ARG_SAVED_HOST = "saved_network_host"
        const val ARG_SAVED_SHARE = "saved_network_share"
        const val ARG_SAVED_CONNECTION_TYPE = "saved_network_connection_type"
        const val ARG_COPY_SOURCE_PATHS = "copy_source_paths"
        const val SMB_COPY_REQUEST_KEY = "SmbCopy"
        const val SMB_COPY_PICKER_TAG = "smb_copy_picker"
        const val MENU_DISCONNECT = 1
    }
}
