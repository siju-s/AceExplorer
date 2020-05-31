package com.siju.acexplorer.imageviewer.view

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.github.chrisbanes.photoview.PhotoView
import com.siju.acexplorer.R

class ImageViewerPagerAdapter(private val context: Context, private val list: ArrayList<Uri?>) : PagerAdapter() {

    override fun isViewFromObject(view: View, any: Any): Boolean {
        return view == any
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val itemView = LayoutInflater.from(context).inflate(R.layout.image_viewer_item,
                container, false)
        val image = itemView.findViewById<PhotoView>(R.id.image)
        Glide.with(context).load(list[position])
                .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.AUTOMATIC))
                .into(image)
        container.addView(itemView)
        return itemView
    }

    override fun destroyItem(container: ViewGroup, position: Int, any: Any) {
        container.removeView(any as RelativeLayout)
    }

    override fun getCount() = list.size

    fun removeItem(currentPos: Int) {
        list.removeAt(currentPos)
        notifyDataSetChanged()
    }

    override fun getItemPosition(any: Any): Int {
        return POSITION_NONE
    }
}