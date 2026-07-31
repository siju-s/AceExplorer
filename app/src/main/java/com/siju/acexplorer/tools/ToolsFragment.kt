package com.siju.acexplorer.tools

import android.content.Context
import android.os.Bundle
import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.siju.acexplorer.R
import com.siju.acexplorer.main.model.groups.Category
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ToolsFragment : Fragment() {

    private val viewModel: ToolsViewModel by viewModels()

    private var toolsList: RecyclerView? = null
    private var adapter: Adapter? = null

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.tools_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolsList = view.findViewById(R.id.toolsListView)
        setupToolbar(view)

        context?.let { context ->
            adapter = Adapter(context, ::onToolClicked)
            toolsList?.adapter = adapter
        }

        viewModel.trashSize.observe(viewLifecycleOwner) { trashSize ->
            adapter?.setTrashSize(Formatter.formatFileSize(requireContext(), trashSize))
        }
    }

    private fun onToolClicked(toolsInfo: ToolsInfo) {
        when (toolsInfo.category) {
            Category.TRASH -> findNavController().navigate(
                    ToolsFragmentDirections.actionNavigationToolsToTrash())

            else -> findNavController().navigate(
                    ToolsFragmentDirections.actionNavigationToolsToAppMgr())
        }
    }

    private fun setupToolbar(view: View) {
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = view.context.getString(R.string.tab_tools)
    }

    override fun onDestroyView() {
        toolsList?.adapter = null
        toolsList = null
        adapter = null
        super.onDestroyView()
    }

    private class Adapter(context: Context, private val clickListener: (ToolsInfo) -> Unit) : RecyclerView.Adapter<Adapter.Holder>() {
        private val list = arrayListOf<ToolsInfo>()

        init {
            list.add(ToolsInfo(Category.APP_MANAGER, com.siju.acexplorer.common.R.drawable.ic_app_manager, context.getString(com.siju.acexplorer.common.R.string.app_manager)))
            list.add(ToolsInfo(Category.TRASH, R.drawable.ic_recycle_bin, context.getString(R.string.trash_title)))
        }

        /** Refreshes only the Recycle Bin row, whose subtitle tracks how much space it holds. */
        fun setTrashSize(formattedSize: String) {
            val position = list.indexOfFirst { toolsInfo -> toolsInfo.category == Category.TRASH }
            if (position == -1) {
                return
            }
            val existing = list[position]
            list[position] = ToolsInfo(existing.category, existing.icon, existing.text, formattedSize)
            notifyItemChanged(position)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
            return Holder.from(parent, clickListener)
        }

        override fun getItemCount() = list.size

        override fun onBindViewHolder(holder: Holder, position: Int) {
            holder.bind(list[position])
        }

        class Holder(val view: View, private val clickListener: (ToolsInfo) -> Unit) : RecyclerView.ViewHolder(view) {
            private val icon: ImageView = view.findViewById(R.id.toolImage)
            private val text: TextView = view.findViewById(R.id.textToolName)
            private val subtitle: TextView = view.findViewById(R.id.textToolSubtitle)

            fun bind(data: ToolsInfo) {
                icon.setImageResource(data.icon)
                text.text = data.text
                subtitle.text = data.subtitle
                subtitle.isVisible = data.subtitle != null
                view.setOnClickListener {
                    clickListener(data)
                }
            }

            companion object {
                fun from(parent: ViewGroup, clickListener: (ToolsInfo) -> Unit): Holder {
                    val view = LayoutInflater.from(parent.context).inflate(R.layout.tools_list_item, parent, false)
                    return Holder(view, clickListener)
                }
            }
        }

    }
}
