package com.siju.acexplorer.appmanager.view

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.siju.acexplorer.appmanager.R
import com.siju.acexplorer.appmanager.databinding.AppsGalleryItemBinding
import com.siju.acexplorer.appmanager.databinding.AppsGridItemBinding
import com.siju.acexplorer.appmanager.databinding.AppsListItemBinding
import com.siju.acexplorer.appmanager.types.AppInfo
import com.siju.acexplorer.appmanager.viewmodel.AppMgrViewModel
import com.siju.acexplorer.common.ViewMode
import com.siju.acexplorer.common.utils.DateUtils
import java.util.*
import kotlin.collections.ArrayList

class AppMgrAdapter(private val viewModel : AppMgrViewModel,
                    private var viewMode: ViewMode,
                    private val clickListener: (AppInfo, Int) -> Unit,
                    private val longClickListener: (AppInfo, Int, View) -> Unit) :
    ListAdapter<AppInfo, AppMgrAdapter.AppViewHolder>(ItemCallback())
{
    private var unfilteredList = arrayListOf<AppInfo>()
    private var selectionMode = false

    fun onDataLoaded(list : ArrayList<AppInfo>) {
        unfilteredList = list
        submitList(list)
    }

    fun setViewMode(viewMode: ViewMode) {
        this.viewMode = viewMode
    }

    fun setSelectionMode(selectionMode : Boolean) {
        this.selectionMode = selectionMode
    }

    fun getViewMode() =viewMode

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val binding = when(viewMode) {
            ViewMode.LIST -> AppsListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            ViewMode.GRID -> AppsGridItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            ViewMode.GALLERY -> AppsGalleryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            else -> AppsListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        }
        return AppViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        holder.bind(getItem(position), viewModel.isSelected(position), clickListener, longClickListener)
    }

    class AppViewHolder(val binding : ViewBinding) : RecyclerView.ViewHolder(binding.root) {

        private val icon = binding.root.findViewById<ImageView>(R.id.imageIcon)
        private val selectIcon = binding.root.findViewById<ImageView>(R.id.imageSelection)
        private val appNameText = binding.root.findViewById<TextView>(R.id.textAppName)
        private val sizeText = binding.root.findViewById<TextView>(R.id.textSize)
        private val packageNameText : TextView? = binding.root.findViewById(R.id.textPackageName)
        private val dateText: TextView? = binding.root.findViewById(R.id.textDate)

        fun bind(
            appInfo: AppInfo,
            selected: Boolean,
            clickListener: (AppInfo, Int) -> Unit,
            longClickListener: (AppInfo, Int, View) -> Unit
        ) {
            onSelection(selected, selectIcon)
            appNameText.text = appInfo.name
            packageNameText?.text = appInfo.packageName
            val context = itemView.context
            sizeText.text = Formatter.formatFileSize(context, appInfo.size)
            dateText?.text = DateUtils.convertDate(appInfo.updatedDate)
            loadAppIcon(context, icon, appInfo.packageName)

            itemView.setOnClickListener {
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    clickListener(appInfo, pos)
                }
            }
            itemView.setOnLongClickListener {
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    longClickListener(appInfo, pos, it)
                }
                true
            }
        }

        private fun loadAppIcon(context: Context, imageIcon: ImageView, name: String?) {
            val options = RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.ic_apk_green)
                .diskCacheStrategy(DiskCacheStrategy.NONE) // cannot disk cache
            Glide.with(context)
                .`as`(Drawable::class.java)
                .apply(options.dontAnimate().dontTransform().priority(Priority.LOW))
                .load(name)
                .into(imageIcon)
        }

        private fun onSelection(selected: Boolean, imageSelection : ImageView) {
            val color = ContextCompat.getColor(itemView.context, R.color.actionModeItemSelected)
            when {
                selected               -> {
                    itemView.setBackgroundColor(color)
                    imageSelection.visibility = View.VISIBLE
                    imageSelection.isSelected = true

                }
                else                   -> {
                    itemView.setBackgroundColor(Color.TRANSPARENT)
                    imageSelection.isSelected = false
                    imageSelection.visibility = View.GONE
                }
            }
        }
    }

    fun filter(text: String) {
        if (text.isEmpty()) {
           submitList(unfilteredList)
        } else {
            addSearchResults(text)
        }
    }

    private fun addSearchResults(query: String) {
        var text = query
        val result: ArrayList<AppInfo> = ArrayList()
        text = text.lowercase(Locale.getDefault())

        result.addAll(unfilteredList.filter {
            it.name.contains(text) || it.packageName.contains(text)
        })
        submitList(result)
    }


    class ItemCallback : DiffUtil.ItemCallback<AppInfo>() {
       override fun areItemsTheSame(oldItem: AppInfo, newItem: AppInfo) = oldItem.packageName == newItem.packageName

       override fun areContentsTheSame(oldItem: AppInfo, newItem: AppInfo): Boolean = oldItem == newItem

   }
}