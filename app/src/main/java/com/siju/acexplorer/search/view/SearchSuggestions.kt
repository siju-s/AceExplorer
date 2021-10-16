package com.siju.acexplorer.search.view

import android.util.Log
import android.view.View
import com.google.android.material.chip.Chip
import com.siju.acexplorer.databinding.SearchMainBinding
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

class SearchSuggestions(val binding: SearchMainBinding, private val fragment: SearchFragment, private val fileListViewModel: FileListViewModel) {

    private val chipRecent = binding.searchContainer.chipRecent
    private val chipAudio = binding.searchContainer.chipAudio
    private val chipVideos = binding.searchContainer.chipVideos
    private val chipImages = binding.searchContainer.chipImages
    private val chipDocuments = binding.searchContainer.chipDocuments
    private val folderChipGroup = binding.searchContainer.commonSearchChipGroup
    private val categoryChipGroup = binding.searchContainer.categoriesChipGroup

    private var chipCamera: Chip? = null
    private var chipScreenshot: Chip? = null
    private var chipWhatsapp: Chip? = null
    private var chipTelegram: Chip? = null
    private var clearAll = false

    private val checkedChipList = arrayListOf<Chip?>()

    init {
        initCategoryChip()
        addCommonSearchItems()
        initChipListener()
    }

    private fun initCategoryChip() {
        chipRecent.tag = Category.RECENT
        chipAudio.tag = Category.AUDIO
        chipVideos.tag = Category.VIDEO_ALL
        chipImages.tag = Category.IMAGES_ALL
        chipDocuments.tag = Category.DOCS
    }

    private fun addCommonSearchItems() {
        chipCamera = addDirectory(DIRECTORY_CAMERA, SearchUtils.getCameraDirectory())
        chipScreenshot = addDirectory(DIRECTORY_SCREENSHOTS, SearchUtils.getScreenshotDirectory())
        chipWhatsapp = addDirectory(DIRECTORY_WHATSAPP, SearchUtils.getWhatsappDirectory())
        chipTelegram = addDirectory(DIRECTORY_TELEGRAM, SearchUtils.getTelegramDirectory())
    }

    private fun addDirectory(name: String, path: String?): Chip? {
        path ?: return null
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
        clearAllCheckedItems()
    }

    fun showChipGroup() {
        folderChipGroup.visibility = View.VISIBLE
        categoryChipGroup.visibility = View.VISIBLE
        chipRecent.visibility = View.VISIBLE
    }

    private fun createChip(name: String, path: String?): Chip {
        val chip = Chip(binding.root.context)
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
        chipRecent.setOnCheckedChangeListener { _, _ ->
            if (!clearAll) {
                onChipSelected(chipRecent.id)
            }
        }

        categoryChipGroup.setOnCheckedChangeListener { _, checkedChipId ->
            Log.d(TAG, "categoryChipGroup, pos:$checkedChipId, count :${categoryChipGroup.childCount}, $checkedChipId")
            if (!clearAll) {
                if (isDocAndCamOrScreenChecked()) {
                    folderChipGroup.clearCheck()
                    checkedChipList.remove(chipCamera)
                    checkedChipList.remove(chipScreenshot)
                }
                onChipGroupCheckedListener(checkedChipId)
            }
        }

        folderChipGroup.setOnCheckedChangeListener { _, checkedChipId ->
            Log.d(TAG, "Folder chip group, pos:$checkedChipId, count :${folderChipGroup.childCount}, chipCamera:${chipCamera?.id}")
            if (!clearAll) {
                if (isDocAndCamOrScreenChecked()) {
                    chipDocuments.isChecked = false
                    checkedChipList.remove(chipDocuments)
                }
                onChipGroupCheckedListener(checkedChipId)
            }
        }
    }

    private fun isDocAndCamOrScreenChecked() = chipDocuments.isChecked && isCameraOrScreenshotSelected()

    private fun isCameraOrScreenshotSelected() : Boolean =
       chipCamera?.isChecked == true || chipScreenshot?.isChecked == true

    private fun onChipGroupCheckedListener(checkedChipId: Int) {
        if (checkedChipId == -1) {
            if (isNoneChecked()) {
                fileListViewModel.clearBackStack()
                fragment.setEmptyList()
                checkedChipList.clear()
            } else {
                onChipSelected(checkedChipId)
            }
        } else {
            onChipSelected(checkedChipId)
        }
    }

    private fun handleSelectedChipId(checkedChipId: Int) {
        val chip = when (checkedChipId) {
            chipRecent.id -> chipRecent
            chipImages.id -> chipImages
            chipVideos.id -> chipVideos
            chipAudio.id -> chipAudio
            chipDocuments.id -> chipDocuments
            chipCamera?.id -> chipCamera
            chipScreenshot?.id -> chipScreenshot
            chipWhatsapp?.id -> chipWhatsapp
            chipTelegram?.id -> chipTelegram
            else -> { null}
        }
                ?: return
        if (checkedChipList.contains(chip)) {
            checkedChipList.remove(chip)
        } else {
            checkedChipList.add(chip)
        }
        Log.d(TAG, "handleSelectedChipId:checkedChipList:${checkedChipList.size}, chip:$chip")

    }

    private fun onChipSelected(checkedChipId: Int) {
        handleSelectedChipId(checkedChipId)
        fragment.onSearchSuggestionClicked()

        val chipInfo = if (chipRecent.isChecked) {
            onRecentItemChecked()
        } else {
            onRecentItemUnchecked()
        }
        if (chipInfo == null) {
            fragment.setEmptyList()
            fileListViewModel.clearBackStack()
            checkedChipList.clear()
        } else {
            fragment.onChipDataLoaded(chipInfo.category)
            fileListViewModel.clearBackStack()
            fileListViewModel.loadData(chipInfo.path, chipInfo.category)
        }
    }

    private fun onRecentItemChecked(): ChipInfo {
        return when {
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
    }

    private fun onRecentItemUnchecked(): ChipInfo? {
        return when {
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
    }

    private fun getRecentFolderCategory(): Category {
        return when {
            chipImages.isChecked -> Category.RECENT_IMAGES_FOLDER
            chipVideos.isChecked -> Category.RECENT_VIDEOS_FOLDER
            chipAudio.isChecked -> Category.RECENT_AUDIO_FOLDER
            chipDocuments.isChecked -> Category.RECENT_DOC_FOLDER
            else -> {
                throw IllegalArgumentException("We Shouldn't be here")
            }
        }
    }

    private fun getRecentCategory(): Category {
        return when {
            chipImages.isChecked -> Category.SEARCH_RECENT_IMAGES
            chipVideos.isChecked -> Category.SEARCH_RECENT_VIDEOS
            chipAudio.isChecked -> Category.SEARCH_RECENT_AUDIO
            chipDocuments.isChecked -> Category.SEARCH_RECENT_DOCS
            else -> {
                throw IllegalArgumentException("We Shouldn't be here")
            }
        }
    }

    private fun getFolderCategory(): Category {
        return when {
            chipImages.isChecked -> Category.SEARCH_FOLDER_IMAGES
            chipVideos.isChecked -> Category.SEARCH_FOLDER_VIDEOS
            chipAudio.isChecked -> Category.SEARCH_FOLDER_AUDIO
            chipDocuments.isChecked -> Category.SEARCH_FOLDER_DOCS
            else -> {
                throw IllegalArgumentException("We Shouldn't be here")
            }
        }
    }

    private fun getSelectedCategory(): Category {
        return when {
            chipImages.isChecked -> Category.IMAGES_ALL
            chipVideos.isChecked -> Category.VIDEO_ALL
            chipAudio.isChecked -> Category.AUDIO
            chipDocuments.isChecked -> Category.DOCS
            else -> {
                throw IllegalArgumentException("We Shouldn't be here")
            }
        }
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
        return when {
            isAnyCategoryChecked() -> {
                when {
                    chipImages.isChecked -> {
                        SearchUtils.getWhatsappImagesDirectory()
                    }
                    chipVideos.isChecked -> {
                        SearchUtils.getWhatsappVideosDirectory()
                    }
                    chipAudio.isChecked -> {
                        SearchUtils.getWhatsappAudioDirectory()
                    }
                    else -> {
                        SearchUtils.getWhatsappDocDirectory()
                    }
                }
            }
            else -> {
                chipWhatsapp?.tag as String?
            }
        }
    }

    private fun getTelegramMediaPath(): String? {
        return when {
            isAnyCategoryChecked() -> {
                when {
                    chipImages.isChecked -> {
                        SearchUtils.getTelegramImagesDirectory()
                    }
                    chipVideos.isChecked -> {
                        SearchUtils.getTelegramVideosDirectory()
                    }
                    chipAudio.isChecked -> {
                        SearchUtils.getTelegramAudioDirectory()
                    }
                    else -> {
                        SearchUtils.getTelegramDocsDirectory()
                    }
                }
            }
            else -> {
                chipTelegram?.tag as String?
            }
        }
    }

    fun isNoneChecked(): Boolean {
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

    fun clearAllCheckedItems() {
        if (isNoneChecked()) {
            return
        }
        clearAll = true
        categoryChipGroup.clearCheck()
        folderChipGroup.clearCheck()
        chipRecent.isChecked = false
        fileListViewModel.clearBackStack()
        fragment.setEmptyList()
        checkedChipList.clear()
        clearAll = false
    }
}