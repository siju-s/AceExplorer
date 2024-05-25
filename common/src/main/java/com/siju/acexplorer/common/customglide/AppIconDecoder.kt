package com.siju.acexplorer.common.customglide

import android.content.Context
import android.content.pm.ApplicationInfo
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.ResourceDecoder
import com.bumptech.glide.load.resource.drawable.DrawableResource
import com.bumptech.glide.util.Util
import java.io.IOException

internal class AppIconDecoder(private val context: Context) : ResourceDecoder<ApplicationInfo, Drawable> {
    @Throws(IOException::class)
    override fun handles(source: ApplicationInfo, options: Options): Boolean {
        return true
    }

    @Suppress("UNCHECKED_CAST")
    @Throws(IOException::class)
    override fun decode(applicationInfo: ApplicationInfo, width: Int, height: Int, options: Options): DrawableResource<Drawable?> {
        val icon = applicationInfo.loadIcon(context.packageManager)
        return object : DrawableResource<Drawable?>(icon) {
            override fun getResourceClass(): Class<Drawable?> {
                return Drawable::class.java as Class<Drawable?>
            }

            override fun getSize(): Int {
                return if (drawable is BitmapDrawable) {
                    Util.getBitmapByteSize((drawable as BitmapDrawable).bitmap)
                } else {
                    1
                }
            }

            override fun recycle() {}
        }
    }

}