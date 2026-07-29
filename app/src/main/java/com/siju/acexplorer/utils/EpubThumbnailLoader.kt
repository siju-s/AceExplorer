package com.siju.acexplorer.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.util.LruCache
import android.util.Xml
import android.widget.ImageView
import com.siju.acexplorer.R
import org.xmlpull.v1.XmlPullParser
import java.io.File
import java.io.InputStream
import java.util.concurrent.Executors
import java.util.zip.ZipFile

object EpubThumbnailLoader {

    private const val THUMBNAIL_WIDTH = 192
    private const val MAX_CACHE_SIZE_KB = 8 * 1024
    private const val CONTAINER_PATH = "META-INF/container.xml"

    private val executor = Executors.newFixedThreadPool(2)
    private val mainHandler = Handler(Looper.getMainLooper())
    private val cache = object : LruCache<String, Bitmap>(MAX_CACHE_SIZE_KB) {
        override fun sizeOf(key: String, value: Bitmap) = value.allocationByteCount / 1024
    }

    fun load(filePath: String?, imageView: ImageView) {
        val file = filePath?.let(::File)
        if (file == null || !file.isFile || !file.canRead()) {
            imageView.setImageResource(R.drawable.ic_doc_white)
            return
        }

        val cacheKey = "epub:${file.absolutePath}:${file.lastModified()}:${file.length()}"
        imageView.tag = cacheKey
        synchronized(cache) {
            cache.get(cacheKey)
        }?.let {
            imageView.setImageBitmap(it)
            return
        }

        imageView.setImageResource(R.drawable.ic_doc_white)
        executor.execute {
            val thumbnail = loadCover(file)
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

    private fun loadCover(file: File): Bitmap? = runCatching {
        ZipFile(file).use { zip ->
            val packagePath = zip.openEntry(CONTAINER_PATH)?.use(::findPackagePath) ?: return null
            val coverPath = zip.openEntry(packagePath)?.use { findCoverPath(it, packagePath) } ?: return null
            decodeThumbnail(zip, coverPath)
        }
    }.getOrNull()

    private fun ZipFile.openEntry(path: String): InputStream? = getEntry(path)?.let(::getInputStream)

    private fun findPackagePath(input: InputStream): String? {
        val parser = Xml.newPullParser().apply { setInput(input, null) }
        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            if (parser.eventType == XmlPullParser.START_TAG && parser.name == "rootfile") {
                return parser.getAttributeValue(null, "full-path")
            }
        }
        return null
    }

    private fun findCoverPath(input: InputStream, packagePath: String): String? {
        val parser = Xml.newPullParser().apply { setInput(input, null) }
        val manifest = mutableMapOf<String, ManifestItem>()
        var coverId: String? = null
        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            if (parser.eventType != XmlPullParser.START_TAG) continue
            when (parser.name) {
                "meta" -> if (parser.getAttributeValue(null, "name") == "cover") {
                    coverId = parser.getAttributeValue(null, "content")
                }
                "item" -> {
                    val id = parser.getAttributeValue(null, "id") ?: continue
                    val href = parser.getAttributeValue(null, "href") ?: continue
                    manifest[id] = ManifestItem(
                        href,
                        parser.getAttributeValue(null, "properties")?.contains("cover-image") == true
                    )
                }
            }
        }
        val cover = coverId?.let(manifest::get) ?: manifest.values.firstOrNull { it.isCover }
        return cover?.let { resolvePath(packagePath, it.href) }
    }

    private fun resolvePath(packagePath: String, href: String): String {
        val directory = packagePath.substringBeforeLast('/', "")
        return if (directory.isEmpty()) href else "$directory/$href"
    }

    private fun decodeThumbnail(zip: ZipFile, path: String): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        val coverEntry = zip.getEntry(path) ?: return null
        zip.getInputStream(coverEntry).use { BitmapFactory.decodeStream(it, null, bounds) }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
        val options = BitmapFactory.Options().apply {
            inSampleSize = calculateSampleSize(bounds.outWidth, THUMBNAIL_WIDTH)
        }
        return zip.openEntry(path)?.use { BitmapFactory.decodeStream(it, null, options) }
    }

    private fun calculateSampleSize(width: Int, targetWidth: Int): Int {
        var sampleSize = 1
        while (width / (sampleSize * 2) >= targetWidth) sampleSize *= 2
        return sampleSize
    }

    private data class ManifestItem(val href: String, val isCover: Boolean)
}
