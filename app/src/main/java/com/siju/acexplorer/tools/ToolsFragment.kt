package com.siju.acexplorer.tools

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.siju.acexplorer.R
import com.siju.acexplorer.main.model.groups.Category

class ToolsFragment : Fragment() {

    private lateinit var toolsList: RecyclerView

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
            toolsList.adapter = Adapter(context) {
                val actions = ToolsFragmentDirections.actionNavigationToolsToAppMgr()
                findNavController().navigate(actions)
            }
        }

    }

    private fun setupToolbar(view: View) {
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = view.context.getString(R.string.tab_tools)
    }

    private class Adapter(context: Context, private val clickListener: (ToolsInfo) -> Unit) : RecyclerView.Adapter<Adapter.Holder>() {
        private val list = arrayListOf<ToolsInfo>()

        init {
            list.add(ToolsInfo(Category.APP_MANAGER, com.siju.acexplorer.common.R.drawable.ic_app_manager, context.getString(com.siju.acexplorer.common.R.string.app_manager)))
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

            fun bind(data: ToolsInfo) {
                icon.setImageResource(data.icon)
                text.text = data.text
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