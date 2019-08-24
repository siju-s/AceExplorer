package com.siju.acexplorer.search.view

import android.util.Log
import android.view.View
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.siju.acexplorer.R
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.search.helper.SearchUtils
import com.siju.acexplorer.search.types.ChipInfo
import com.siju.acexplorer.storage.viewmodel.FileListViewModel
import java.io.File

private const val DIRECTORY_CAMERA = "Camera"
private const val DIRECTORY_SCREENSHOTS = "Screenshots"
private const val DIRECTORY_WHATSAPP = "Whatsapp"
private const val DIRECTORY_TELEGRAM = "Telegram"

private const val TAG = "SearchSuggestions"

class SearchSuggestions(val view: View, private val fragment: SearchFragment, private val fileListViewModel: FileListViewModel) {

    private lateinit var chipRecent: Chip
    private lateinit var chipAudio: Chip
    private lateinit var chipVideos: Chip
    private lateinit var chipImages: Chip
    private lateinit var chipDocuments: Chip
    private lateinit var folderChipGroup: ChipGroup
    private lateinit var categoryChipGroup: ChipGroup

    private var chipCamera: Chip? = null
    private var chipScreenshot: Chip? = null
    private var chipWhatsapp: Chip? = null
    private var chipTelegram: Chip? = null


    init {
        initializeViews()
        initCategoryChip()
        addCommonSearchItems()
        initChipListener()
    }

    private fun initializeViews() {
        chipRecent = view.findViewById(R.id.chipRecent)
        chipAudio = view.findViewById(R.id.chipAudio)
        chipVideos = view.findViewById(R.id.chipVideos)
        chipImages = view.findViewById(R.id.chipImages)
        chipDocuments = view.findViewById(R.id.chipDocuments)

        categoryChipGroup = view.findViewById(R.id.categoriesChipGroup)
        folderChipGroup = view.findViewById(R.id.commonSearchChipGroup)
    }

    private fun initCategoryChip() {
        chipRecent.tag = Category.RECENT
        chipAudio.tag = Category.AUDIO
        chipVideos.tag = Category.VIDEO
        chipImages.tag = Category.IMAGE
        chipDocuments.tag = Category.DOCS
    }

    private fun addCommonSearchItems() {
        chipCamera = addDirectory(DIRECTORY_CAMERA, SearchUtils.getCameraDirectory())
        chipScreenshot = addDirectory(DIRECTORY_SCREENSHOTS, SearchUtils.getScreenshotDirectory())
        chipWhatsapp = addDirectory(DIRECTORY_WHATSAPP, SearchUtils.getWhatsappDirectory())
        chipTelegram = addDirectory(DIRECTORY_TELEGRAM, SearchUtils.getTelegramDirectory())
    }

    private fun addDirectory(name: String, path: String?): Chip? {
        if (File(path).exists()) {
            val chip = createChip(name, path)
            addChip(chip)
            return chip
        }
        return null
    }

    fun hideSuggestions() {
        folderChipGroup.visibility = View.GONE
        categoryChipGroup.visibility = View.GONE
        chipRecent.visibility = View.GONE
    }

    fun showChipGroup() {
        folderChipGroup.visibility = View.VISIBLE
        categoryChipGroup.visibility = View.VISIBLE
        chipRecent.visibility = View.VISIBLE
    }

    private fun createChip(name: String, path: String?): Chip {
        val chip = Chip(view.context)
        chip.text = name
        chip.isClickable = true
        chip.isCheckable = true
        chip.tag = path
        return chip
    }

    private fun addChip(chip: Chip) {
        folderChipGroup.addView(chip)
    }

    private fun initChipListener() {
        chipRecent.setOnCheckedChangeListener { _, position ->
            onChipSelected()
        }

        categoryChipGroup.setOnCheckedChangeListener { button, checkedChipId ->
            Log.e(TAG, "categoryChipGroup, pos:$checkedChipId, count :${categoryChipGroup.childCount}, ${categoryChipGroup.checkedChipId}")
            onChipGroupCheckedListener(checkedChipId)
        }

        folderChipGroup.setOnCheckedChangeListener { _, checkedChipId ->
            Log.e(TAG, "Folder chip group, pos:$checkedChipId, count :${folderChipGroup.childCount}")
            onChipGroupCheckedListener(checkedChipId)
        }
    }

    private fun onChipGroupCheckedListener(checkedChipId: Int) {
        if (checkedChipId == -1) {
            if (isNoneChecked()) {
                fileListViewModel.clearBackStack()
                fragment.setEmptyList()
            } else {
                onChipSelected()
            }
        }
        else {
            onChipSelected()
        }
    }

    private fun onChipSelected() {
        fileListViewModel.clearBackStack()
        fragment.onSearchSuggestionClicked()

        val chipInfo = if (chipRecent.isChecked) {
            onRecentItemChecked()
        } else {
            onRecentItemUnchecked()
        }

        if (chipInfo == null) {
            fragment.setEmptyList()
        } else {
            fileListViewModel.loadData(chipInfo.path, chipInfo.category)
        }
    }

    private fun onRecentItemChecked(): ChipInfo {
        val chipInfo = when {
            isAnyCategoryChecked() && isAnyFolderItemChecked() -> {
                ChipInfo.createRecentCategoryFolder(getSelectedFolderChipPath(), getRecentFolderCategory())
            }
            isAnyCategoryChecked() -> {
                ChipInfo.createRecentCategory(null, getRecentCategory())
            }
            isAnyFolderItemChecked() -> {
                ChipInfo.createRecentFolder(getSelectedFolderChipPath(), Category.RECENT_FOLDER)
            }
            else -> {
                ChipInfo.createRecent()
            }
        }
        return chipInfo
    }

    private fun onRecentItemUnchecked(): ChipInfo? {
        val chipInfo = when {
            isAnyCategoryChecked() && isAnyFolderItemChecked() -> {
                ChipInfo.createFolderCategory(getSelectedFolderChipPath(), getFolderCategory())
            }
            isAnyCategoryChecked() -> {
                ChipInfo.createCategory(null, getSelectedCategory())
            }
            isAnyFolderItemChecked() -> {
                ChipInfo.createFolder(getSelectedFolderChipPath())
            }
            else -> {
                null
            }
        }
        return chipInfo
    }

    private fun getRecentFolderCategory(): Category {
        val category = when {
            chipImages.isChecked -> Category.RECENT_IMAGES_FOLDER
            chipVideos.isChecked -> Category.RECENT_VIDEOS_FOLDER
            chipAudio.isChecked -> Category.RECENT_AUDIO_FOLDER
            chipDocuments.isChecked -> Category.RECENT_DOC_FOLDER
            else -> {
                throw IllegalArgumentException("We Shouldn't be here")
            }
        }
        return category
    }

    private fun getRecentCategory(): Category {
        val category = when {
            chipImages.isChecked -> Category.RECENT_IMAGES
            chipVideos.isChecked -> Category.RECENT_VIDEOS
            chipAudio.isChecked -> Category.RECENT_AUDIO
            chipDocuments.isChecked -> Category.RECENT_AUDIO
            else -> {
                throw IllegalArgumentException("We Shouldn't be here")
            }
        }
        return category
    }

    private fun getFolderCategory(): Category {
        val category = when {
            chipImages.isChecked -> Category.SEARCH_FOLDER_IMAGES
            chipVideos.isChecked -> Category.SEARCH_FOLDER_VIDEOS
            chipAudio.isChecked -> Category.SEARCH_FOLDER_AUDIO
            chipDocuments.isChecked -> Category.SEARCH_FOLDER_DOCS
            else -> {
                throw IllegalArgumentException("We Shouldn't be here")
            }
        }
        return category
    }

    private fun getSelectedCategory(): Category {
        val category = when {
            chipImages.isChecked -> Category.IMAGE
            chipVideos.isChecked -> Category.VIDEO
            chipAudio.isChecked -> Category.AUDIO
            chipDocuments.isChecked -> Category.DOCS
            else -> {
                throw IllegalArgumentException("We Shouldn't be here")
            }
        }
        return category
    }


    private fun getSelectedFolderChipPath(): String? {
        return when {
            chipCamera?.isChecked == true -> chipCamera?.tag as String
            chipScreenshot?.isChecked == true -> chipScreenshot?.tag as String
            chipWhatsapp?.isChecked == true -> getWhatsappMediaPath()
            else -> getTelegramMediaPath()
        }
    }

    private fun getWhatsappMediaPath(): String? {
        val path = when {
            isAnyCategoryChecked() -> {
                if (chipImages.isChecked) {
                    SearchUtils.getWhatsappImagesDirectory()
                } else if (chipVideos.isChecked) {
                    SearchUtils.getWhatsappVideosDirectory()
                } else if (chipAudio.isChecked) {
                    SearchUtils.getWhatsappAudioDirectory()
                } else {
                    SearchUtils.getWhatsappDocDirectory()
                }
            }
            else -> {
                chipWhatsapp?.tag as String
            }
        }
        return path
    }

    private fun getTelegramMediaPath(): String? {
        val path = when {
            isAnyCategoryChecked() -> {
                if (chipImages.isChecked) {
                    SearchUtils.getTelegramImagesDirectory()
                } else if (chipVideos.isChecked) {
                    SearchUtils.getTelegramVideosDirectory()
                } else if (chipAudio.isChecked) {
                    SearchUtils.getTelegramAudioDirectory()
                } else {
                    SearchUtils.getTelegramDocsDirectory()
                }
            }
            else -> {
                chipTelegram?.tag as String
            }
        }
        return path
    }

    private fun isNoneChecked(): Boolean {
        return !chipRecent.isChecked && !isAnyFolderItemChecked() && !isAnyCategoryChecked()
    }

    private fun isAnyFolderItemChecked(): Boolean {
        return chipCamera?.isChecked == true || chipScreenshot?.isChecked == true || chipWhatsapp?.isChecked == true
                || chipTelegram?.isChecked == true
    }

    private fun isAnyCategoryChecked(): Boolean {
        return chipImages.isChecked || chipVideos.isChecked || chipDocuments.isChecked
                || chipAudio.isChecked
    }
}