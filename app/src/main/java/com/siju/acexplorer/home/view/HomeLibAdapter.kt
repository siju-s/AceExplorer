package com.siju.acexplorer.home.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.siju.acexplorer.R
import com.siju.acexplorer.home.types.HomeLibraryInfo
import com.siju.acexplorer.main.model.groups.CategoryHelper.getCategoryName

private const val TAG = "HomeLibAdapter"
class HomeLibAdapter internal constructor(private val clickListener: (HomeLibraryInfo) -> Unit) :
        ListAdapter<HomeLibraryInfo, HomeLibAdapter.ViewHolder>(CategoryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val item = getItem(position)
//        Log.e(TAG, "onBindViewHolder $position")
        viewHolder.bind(item, clickListener)
    }

    class ViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private var imageLibrary: ImageView = itemView.findViewById(R.id.imageLibrary)
        private var textLibraryName: TextView = itemView.findViewById(R.id.textLibrary)
        private var textCount: TextView = itemView.findViewById(R.id.textCount)
        private var imageDone: ImageView = itemView.findViewById(R.id.imageDone)

        fun bind(item: HomeLibraryInfo, clickListener: (HomeLibraryInfo) -> Unit) {
            imageLibrary.setImageResource(item.resourceId)
            val name = getCategoryName(itemView.context, item.category)
            textLibraryName.text = name
            textCount.text = item.count.toString()
            itemView.tag = item.category
            itemView.setOnClickListener { clickListener(item) }
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.library_item,
                        parent, false)
                return ViewHolder(view)
            }
        }
    }

    class CategoryDiffCallback : DiffUtil.ItemCallback<HomeLibraryInfo>() {
        override fun areItemsTheSame(oldItem: HomeLibraryInfo, newItem: HomeLibraryInfo) = oldItem.category == newItem.category

        override fun areContentsTheSame(oldItem: HomeLibraryInfo, newItem: HomeLibraryInfo): Boolean = oldItem == newItem

    }


}