package com.siju.acexplorer.ui.peekandpop

import android.util.Log
import android.view.View
import android.view.View.GONE
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.siju.acexplorer.R
import com.siju.acexplorer.analytics.Analytics
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.main.model.groups.Category.*
import com.siju.acexplorer.main.model.groups.CategoryHelper.isPeekPopCategory
import com.siju.acexplorer.storage.view.INVALID_POS
import com.siju.acexplorer.ui.autoplay.AutoPlayContainer
import com.siju.acexplorer.ui.autoplay.AutoPlayView.PeekPopVideoCallback
import com.siju.acexplorer.utils.ThumbnailUtils.displayThumb


private const val TAG = "PeekPopUiView"
class PeekPopUiView(val activity: AppCompatActivity, fileListView: RecyclerView) : PeekPopView {

    private val peekAndPop = PeekAndPop.Builder(activity).peekLayout(R.layout.peek_pop)
            .parentViewGroupToDisallowTouchEvents(fileListView).build()
    private var peekPopCallback : PeekPopView.PeekPopCallback? = null
    private var peekPos = INVALID_POS
    private var fileList = arrayListOf<FileInfo>()
    private val context = fileListView.context

    override fun setPeekPopCallback(peekPopCallback: PeekPopView.PeekPopCallback?) {
        this.peekPopCallback = peekPopCallback
    }

    override fun setFileList(list: ArrayList<FileInfo>) {
        this.fileList = list
    }

    override fun getFileData(): java.util.ArrayList<FileInfo> {
        return fileList
    }

    override fun initPeekPopListener() {
        peekAndPop.setOnGeneralActionListener(object : PeekAndPop.OnGeneralActionListener {
            override fun onPeek(longClickView: View, position: Int) {
                Log.d(TAG, "onPeek() called with: longClickView = [$longClickView], position = [$position]")
                loadPeekView(PeekPopView.PeekButton.GENERIC, position, true)
            }

            override fun onPop(longClickView: View, position: Int) {
                peekPos = INVALID_POS
                stopAutoPlayVid()
            }
        })

        peekAndPop.setOnClickListener(object : PeekAndPop.OnClickListener {
            override fun onClick(view: View, position: Int, canShowPeek: Boolean) {
                if (!canShowPeek) {
                    val pos = if (peekAndPop.isNextPrevIcon(view)) peekPos else position
                    peekPopCallback?.onItemClick(view, fileList[pos], pos)
                    return
                }
                if (peekPos == INVALID_POS) {
                    return
                }
                    peekPopCallback?.onItemClick(view, fileList[peekPos], peekPos)
            }

            override fun canShowPeek(): Boolean {
                peekPopCallback ?: return false
                return peekPopCallback!!.canShowPeek()
            }
        })
    }

    override fun addClickView(view: View, pos: Int, category: Category) {
//        Log.d(TAG, "addClickView:pos:$pos, category:$category, this:$this")
        peekAndPop.addClickView(view, pos, category)
    }

    override fun loadPeekView(peekButton: PeekPopView.PeekButton, position: Int, firstRun: Boolean) {
        Log.d(TAG, "loadPeekView: pos:" + position + "list size:" + fileList.size)
        if (position >= fileList.size || position == INVALID_POS) {
            return
        }
        var category: Category? = fileList[position].category
        val view = peekAndPop.getPeekView()
        Log.d(TAG, "loadPeekView: category:$category")
        var pos = position
        if (!isPeekPopCategory(category)) {
            pos = handlePeekDataPos(view, position, peekButton)
            if (pos == position) {
                return
            }
        }

        this.peekPos = pos
        category = fileList[pos].category
        changePeekButtonsState(pos, view)
        val thumb: ImageView = view.findViewById(R.id.imagePeekView)
        val autoPlayView: AutoPlayContainer = view.findViewById(R.id.autoPlayView)
        val shareButton = view.findViewById<ImageButton>(R.id.imageButtonShare)
        if (isPeekPopCategory(category)) {
            shareButton.visibility = View.VISIBLE
        } else {
            shareButton.visibility = GONE
        }
        if (firstRun) {
            Analytics.logger.enterPeekMode()
            autoPlayView.init()
        }
        val customVideoView = autoPlayView.customVideoView
        val volume: ImageView = view.findViewById(R.id.imageVolume)
        val fileNameText = view.findViewById<TextView>(R.id.textFileName)
        val fileInfo: FileInfo = fileList[pos]
        val isPlaying = customVideoView.isPlaying
        when (category) {
            VIDEO, VIDEO_ALL, FOLDER_VIDEOS, AUDIO -> {
                fileNameText.visibility = View.VISIBLE
                fileNameText.text = fileInfo.fileName
                if (AUDIO == category) {
                    autoPlayView.visibility = GONE
                    thumb.visibility = View.VISIBLE
                } else {
                    thumb.visibility = GONE
                    autoPlayView.visibility = View.VISIBLE
                }
                customVideoView.isLooping = true
                customVideoView.setDataSource(fileInfo.filePath)
                customVideoView.setListener(peekPopVideoCallback)
                customVideoView.setVideoMode(AUDIO != category)
                val initialized = customVideoView.init()
                volume.visibility = View.VISIBLE
                volume.setImageResource(if (customVideoView.isMuted) R.drawable.ic_volume_off else R.drawable.ic_volume_on)
                if (initialized) {
                    customVideoView.stopPlayer()
                }
                customVideoView.playNext()
            }
            IMAGE, RECENT_IMAGES, FOLDER_IMAGES, GENERIC_IMAGES -> {
                if (isPlaying) {
                    customVideoView.stopPlayer()
                }
                fileNameText.visibility = GONE
                autoPlayView.visibility = GONE
                volume.visibility = GONE
                thumb.visibility = View.VISIBLE
            }
            else -> {
                if (isPlaying) {
                    customVideoView.stopPlayer()
                }
                fileNameText.visibility = View.VISIBLE
                fileNameText.text = fileInfo.fileName
                autoPlayView.visibility = GONE
                volume.visibility = GONE
                thumb.visibility = View.VISIBLE
            }
        }
        volume.setOnClickListener {
            if (customVideoView.isMuted) {
                customVideoView.unmutePlayer()
                volume.setImageResource(R.drawable.ic_volume_on)
                volume.contentDescription = context.getString(R.string.unmute)
            } else {
                customVideoView.mutePlayer()
                volume.setImageResource(R.drawable.ic_volume_off)
                volume.contentDescription = context.getString(R.string.mute)
            }
        }
        displayThumb(context, fileInfo, fileInfo.category, thumb, null)
    }

    private fun handlePeekDataPos(view: View, position: Int, peekButton: PeekPopView.PeekButton): Int {
        return when (peekButton) {
            PeekPopView.PeekButton.NEXT -> {
                val pos = getNextPeekPos(position)
                if (pos == position) {
                   disablePeekButton(view.findViewById<View>(R.id.buttonNext))
                }
                pos
            }
            PeekPopView.PeekButton.PREVIOUS -> {
                val pos = getPreviousPeekPos(position)
                if (pos == position) {
                    disablePeekButton(view.findViewById<View>(R.id.buttonPrev))
                }
                pos
            }
            else -> position
        }
    }

    private fun getNextPeekPos(position: Int): Int {
        val pos = position + 1
        return if (pos < fileList.size && isPeekPopCategory(fileList[pos].category)) {
            pos
        } else {
            position
        }
    }

    private fun getPreviousPeekPos(position: Int): Int {
        val pos = position - 1
        return if (pos >= 0 && isPeekPopCategory(fileList[pos].category)) {
            pos
        } else {
            position
        }
    }

    private fun disablePeekButton(view: View) {
        view.alpha = 0.3f
        view.isEnabled = false
    }

    private fun enablePeekButton(view: View) {
        view.alpha = 1f
        view.isEnabled = true
    }

    private fun changePeekButtonsState(position: Int, view: View) {
        val prevButton = view.findViewById<View>(R.id.buttonPrev)
        if (position == 0) {
            disablePeekButton(prevButton)
        } else {
            enablePeekButton(prevButton)
        }
        val nextButton = view.findViewById<View>(R.id.buttonNext)
        if (position == fileList.size - 1) {
            disablePeekButton(nextButton)
        } else {
            enablePeekButton(nextButton)
        }
    }

    override fun isPeekMode() = peekAndPop.getPeekView().isShown

    override fun endPeekMode() {
        stopAutoPlayVid()
    }

    override fun pausePeekMode() {
        if (isPeekMode()) {
            stopAutoPlayVid()
        }
    }

    private val peekPopVideoCallback = object : PeekPopVideoCallback {

        override fun onSurfaceDestroyed() {
            peekAndPop.resetViews()
        }
    }

    override fun stopAutoPlayVid() {
        val view = peekAndPop.getPeekView()
        peekAndPop.resetViews()
        val autoPlayView: AutoPlayContainer = view.findViewById(R.id.autoPlayView)
        autoPlayView.customVideoView.clearAll()
        autoPlayView.cleanup()
    }
}