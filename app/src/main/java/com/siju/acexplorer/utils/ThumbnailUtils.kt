package com.siju.acexplorer.utils

import android.content.ContentUris
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.siju.acexplorer.R
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.FileConstants
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.main.model.helper.AppUtils
import java.io.File
import java.util.*

object ThumbnailUtils {
    private val AUDIO_URI = Uri.parse("content://media/external/audio/albumart")
    @JvmStatic
    fun displayThumb(context: Context, fileInfo: FileInfo, category: Category?, imageIcon: ImageView, imageThumbIcon: ImageView?, uri : Uri? = null) {
        val filePath = fileInfo.filePath
        val fileName = fileInfo.fileName
        val isDirectory = fileInfo.isDirectory
        when (category) {
            Category.FILES, Category.DOWNLOADS, Category.COMPRESSED, Category.FAVORITES, Category.PDF, Category.APPS, Category.LARGE_FILES, Category.ZIP_VIEWER, Category.RECENT_APPS -> {
                if (isDirectory) {
                    imageIcon.setImageResource(R.drawable.ic_folder)
                    if (imageThumbIcon != null) {
                        val apkIcon = AppUtils.getAppIconForFolder(context, fileName) // TODO: 10/01/18 It should be package name and not filename
                        if (apkIcon != null) {
                            imageThumbIcon.visibility = View.VISIBLE
                            imageThumbIcon.setImageDrawable(apkIcon)
                        } else {
                            imageThumbIcon.visibility = View.GONE
                            imageThumbIcon.setImageDrawable(null)
                        }
                    }
                } else {
                    hideThumb(imageThumbIcon)
                    imageIcon.setImageDrawable(null)
                    val extension = fileInfo.extension
                    if (extension != null) {
                        changeFileIcon(context, imageIcon, extension.toLowerCase(Locale.ROOT), filePath)
                    } else {
                        imageIcon.setImageResource(R.drawable.ic_doc_white)
                    }
                }
                setThumbHiddenFilter(imageIcon, fileName)
            }
            Category.AUDIO, Category.RECENT_AUDIO -> {
                hideThumb(imageThumbIcon)
                displayAudioAlbumArt(context, fileInfo.bucketId, imageIcon, filePath)
                setThumbHiddenFilter(imageIcon, fileName)
            }
            Category.VIDEO, Category.GENERIC_VIDEOS, Category.FOLDER_VIDEOS, Category.RECENT_VIDEOS, Category.VIDEO_ALL -> {
                hideThumb(imageThumbIcon)
                displayVideoThumb(context, imageIcon, filePath)
                setThumbHiddenFilter(imageIcon, fileName)
            }
            Category.IMAGE, Category.GENERIC_IMAGES, Category.FOLDER_IMAGES, Category.RECENT_IMAGES, Category.IMAGES_ALL -> {
                hideThumb(imageThumbIcon)
                displayImageThumb(context, imageIcon, filePath, uri)
                setThumbHiddenFilter(imageIcon, fileName)
            }
            Category.DOCS, Category.RECENT_DOCS -> {
                hideThumb(imageThumbIcon)
                var extension = fileInfo.extension
                extension = extension?.toLowerCase(Locale.ROOT)
                changeFileIcon(context, imageIcon, extension, null)
                setThumbHiddenFilter(imageIcon, fileName)
            }
            Category.APP_MANAGER -> {
                loadAppIcon(context, imageIcon, fileInfo.filePath)
                setThumbHiddenFilter(imageIcon, fileName)
            }
            else -> imageIcon.setImageResource(R.drawable.ic_folder)
        }
    }

    private fun hideThumb(imageThumbIcon: ImageView?) {
        if (imageThumbIcon != null) {
            imageThumbIcon.visibility = View.GONE
            imageThumbIcon.setImageDrawable(null)
        }
    }

    private fun setThumbHiddenFilter(imageIcon: ImageView, fileName: String?) {
        if (fileName?.startsWith(".") == true) {
            imageIcon.setColorFilter(Color.argb(200, 255, 255, 255))
        } else {
            imageIcon.clearColorFilter()
        }
    }

    private fun displayVideoThumb(context: Context, imageIcon: ImageView, path: String?) {
        if (path == null) {
            imageIcon.setImageResource(R.drawable.ic_movie)
            return
        }
        val videoUri = Uri.fromFile(File(path))
        val options = RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.ic_movie)
        Glide.with(context).load(videoUri)
                .apply(options)
                .into(imageIcon)
    }

    private fun displayImageThumb(context: Context, imageIcon: ImageView, path: String?, uri: Uri?) {
        val imageUri  = if (path == null && uri == null) {
            imageIcon.setImageResource(R.drawable.ic_image_default)
            return
        }
        else if (path == null) {
            uri
        }
        else {
            Uri.fromFile(File(path))
        }
        val options = RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.ic_image_default)
        Glide.with(context).load(imageUri).transition(DrawableTransitionOptions.withCrossFade(2))
                .apply(options)
                .into(imageIcon)
    }

    private fun displayAudioAlbumArt(context: Context, bucketId: Long, imageIcon: ImageView,
                                     path: String?) {
        if (bucketId != -1L) {
            val uri = ContentUris.withAppendedId(AUDIO_URI, bucketId)
            val options = RequestOptions()
                    .centerCrop()
                    .placeholder(R.drawable.ic_music_default)
            Glide.with(context).load(uri).apply(options)
                    .into(imageIcon)
        } else {
            imageIcon.setImageResource(R.drawable.ic_music_default)
            val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            val projection = arrayOf(MediaStore.Audio.Media.ALBUM_ID)
            val selection = MediaStore.Audio.Media.DATA + " = ?"
            val selectionArgs = arrayOf(path)
            val cursor = context.contentResolver.query(uri, projection, selection,
                    selectionArgs,
                    null)
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    Glide.with(context).clear(imageIcon)
                    val albumIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                    val albumId = cursor.getLong(albumIdIndex)
                    val newUri = ContentUris.withAppendedId(AUDIO_URI, albumId)
                    val options = RequestOptions()
                            .centerCrop()
                            .placeholder(R.drawable.ic_music_default)
                            .error(R.drawable.ic_music_default)
                            .fallback(R.drawable.ic_music_default)
                    Glide.with(context).load(newUri)
                            .apply(options)
                            .into(object : CustomTarget<Drawable?>() {
                                override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable?>?) {
                                    imageIcon.setImageDrawable(resource)
                                }

                                override fun onLoadCleared(placeholder: Drawable?) {
                                    imageIcon.setImageResource(0)
                                }

                                override fun onLoadFailed(errorDrawable: Drawable?) {
                                    imageIcon.setImageDrawable(errorDrawable)
                                }
                            })
                }
                cursor.close()
            } else {
                imageIcon.setImageResource(R.drawable.ic_music_default)
            }
        }
    }

    private fun loadAppIcon(context: Context, imageIcon: ImageView, name: String?) {
        val options = RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.ic_apk_green)
                .diskCacheStrategy(DiskCacheStrategy.NONE) // cannot disk cache
        // ApplicationInfo, nor Drawables
        Glide.with(context)
                .`as`(Drawable::class.java)
                .apply(options.dontAnimate().dontTransform().priority(Priority.LOW))
                .load(name)
                .into(imageIcon)
    }

    private fun changeFileIcon(context: Context, imageIcon: ImageView, extension: String?, path: String?) {
        when (extension) {
            FileConstants.APK_EXTENSION -> loadAppIcon(context, imageIcon, path)
            FileConstants.EXT_DOC, FileConstants.EXT_DOCX -> imageIcon.setImageResource(R.drawable.ic_doc)
            FileConstants.EXT_XLS, FileConstants.EXT_XLXS, FileConstants.EXT_CSV -> imageIcon.setImageResource(R.drawable.ic_xls)
            FileConstants.EXT_PPT, FileConstants.EXT_PPTX -> imageIcon.setImageResource(R.drawable.ic_ppt)
            FileConstants.EXT_PDF -> imageIcon.setImageResource(R.drawable.ic_pdf)
            FileConstants.EXT_TEXT -> imageIcon.setImageResource(R.drawable.ic_txt)
            FileConstants.EXT_HTML -> imageIcon.setImageResource(R.drawable.ic_html)
            FileConstants.EXT_ZIP -> imageIcon.setImageResource(R.drawable.ic_file_zip)
            else -> imageIcon.setImageResource(R.drawable.ic_doc_white)
        }
    }
}