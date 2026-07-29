package com.siju.acexplorer.utils

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.Handler
import android.os.Looper
import android.os.ParcelFileDescriptor
import android.util.LruCache
import android.widget.ImageView
import com.siju.acexplorer.R
import java.io.File
import java.util.concurrent.Executors

object PdfThumbnailLoader {

    private const val THUMBNAIL_WIDTH = 192
    private const val MAX_CACHE_SIZE_KB = 8 * 1024

    private val executor = Executors.newFixedThreadPool(2)
    private val mainHandler = Handler(Looper.getMainLooper())
    private val cache = object : LruCache<String, Bitmap>(MAX_CACHE_SIZE_KB) {
        override fun sizeOf(key: String, value: Bitmap) = value.allocationByteCount / 1024
    }

    fun load(filePath: String?, imageView: ImageView) {
        val file = filePath?.let(::File)
        if (file == null || !file.isFile || !file.canRead()) {
            imageView.setImageResource(R.drawable.ic_pdf)
            return
        }

        val cacheKey = "${file.absolutePath}:${file.lastModified()}:${file.length()}"
        imageView.tag = cacheKey
        synchronized(cache) {
            cache.get(cacheKey)
        }?.let {
            imageView.setImageBitmap(it)
            return
        }

        imageView.setImageResource(R.drawable.ic_pdf)
        executor.execute {
            val thumbnail = renderFirstPage(file)
            if (thumbnail != null) {
                synchronized(cache) {
                    cache.put(cacheKey, thumbnail)
                }
            }
            mainHandler.post {
                if (imageView.tag == cacheKey && thumbnail != null) {
                    imageView.setImageBitmap(thumbnail)
                }
            }
        }
    }

    private fun renderFirstPage(file: File): Bitmap? = runCatching {
        ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { descriptor ->
            PdfRenderer(descriptor).use { renderer ->
                if (renderer.pageCount == 0) return null
                renderer.openPage(0).use { page ->
                    val height = (THUMBNAIL_WIDTH.toFloat() * page.height / page.width)
                        .toInt()
                        .coerceAtLeast(1)
                    Bitmap.createBitmap(THUMBNAIL_WIDTH, height, Bitmap.Config.ARGB_8888).also { bitmap ->
                        bitmap.eraseColor(Color.WHITE)
                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    }
                }
            }
        }
    }.getOrNull()
}
