package com.siju.acexplorer.storage.view

import com.siju.acexplorer.AceApplication
import com.siju.acexplorer.logging.Logger
import com.siju.acexplorer.main.model.StorageUtils
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.main.model.groups.CategoryHelper
import com.siju.acexplorer.main.model.groups.StorageFetcher
import com.siju.acexplorer.storage.viewmodel.FileListViewModel
import java.io.File

private const val TAG = "Navigation"
private const val SEPARATOR = "/"

class Navigation(private val viewModel: FileListViewModel) {
    private val externalSDPaths = StorageFetcher(AceApplication.appContext).externalSDList
    private var currentDir: String? = null
    private var initialDir = StorageUtils.internalStorage
    private var isCurrentDirRoot: Boolean = false

    fun createNavigationForCategory(category: Category) {
        if (CategoryHelper.checkIfLibraryCategory(category)) {
            viewModel.addHomeButton()
            addTitle(category)
        }
    }

    private fun addTitle(category: Category) {
        if (shouldShowLibSpecificTitle(category)) {
            viewModel.addGenericTitle(category)
        }
        else if (CategoryHelper.checkIfLibraryCategory(category)) {
            viewModel.addLibraryTitle(category)
        }
    }

    fun setInitialDir(currentDir: String?, category: Category) {
        if (CategoryHelper.checkIfLibraryCategory(category) || currentDir == null) {
            return
        }
        when {
            currentDir.contains(StorageUtils.internalStorage) -> {
                initialDir = StorageUtils.internalStorage
                isCurrentDirRoot = false
            }
            externalSDPaths.size > 0                          -> {
                for (path in externalSDPaths) {
                    if (currentDir.contains(path)) {
                        initialDir = path
                        isCurrentDirRoot = false
                        return
                    }
                }
                initialDir = File.separator
            }
            else                                              -> initialDir = File.separator
        }
        Logger.log(TAG, "initializeStartingDirectory--startingdir=$initialDir")
    }


    fun setNavDirectory(path: String?, category: Category) {
//        Log.e(TAG, "setNavDirectory:path$path, category:$category")
        if (CategoryHelper.checkIfLibraryCategory(category) || path == null) {
            return
        }
        val parts = path.split(SEPARATOR.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        currentDir = path
        viewModel.addHomeButton()

        // If root dir , parts will be 0
        if (parts.isEmpty()) {
            setRootNavDirectory()
        }
        else {
            setupNavigationDirectory(parts)
        }
    }

    private fun setRootNavDirectory() {
        isCurrentDirRoot = true
        initialDir = File.separator
        setNavDir(File.separator, File.separator) // Add Root button
    }

    private fun setupNavigationDirectory(parts: Array<String>) {
        var dir: String
        var count = 0
        val stringBuilder = StringBuilder()
        for (i in 1 until parts.size) {
            dir = stringBuilder.append(File.separator).append(parts[i]).toString()

            if (!dir.contains(initialDir)) {
                continue
            }
            /*Count check so that ROOT is added only once in Navigation
                  Handles the scenario :
                  1. When Fav item is a root child and if we click on any folder in that fav item
                     multiple ROOT blocks are not added to Navigation view*/
            if (isCurrentDirRoot && count == 0) {
                setNavDir(File.separator, File.separator)
            }

            count++
            setNavDir(dir, parts[i])
        }
    }

    private fun setNavDir(path: String, dirName: String) {
        when {
            StorageUtils.internalStorage == path -> {
                isCurrentDirRoot = false
                createNavButtonStorage(StorageUtils.StorageType.INTERNAL, path)
            }
            File.separator == path               -> createNavButtonStorage(
                    StorageUtils.StorageType.ROOT, path)
            externalSDPaths.contains(path)       -> {
                isCurrentDirRoot = false
                createNavButtonStorage(StorageUtils.StorageType.EXTERNAL, path)
            }
            else                                 -> {
                createNavButtonStorageParts(path, dirName)
            }
        }
    }

    private fun createNavButtonStorage(storageType: StorageUtils.StorageType, dir: String) {
        viewModel.createNavButtonStorage(storageType, dir)
    }

    private fun createNavButtonStorageParts(path: String, dirName: String) {
        viewModel.createNavButtonStorageParts(path, dirName)
    }

    private fun shouldShowLibSpecificTitle(category: Category) =
            category == Category.GENERIC_MUSIC || category == Category.GENERIC_VIDEOS ||
                    category == Category.GENERIC_IMAGES || Category.RECENT == category

    fun shouldLoadDir(dir: String?) = currentDir != dir

    fun addLibSpecificNavigation(category: Category, bucketName: String?) {
        when {
            CategoryHelper.checkIfAnyMusicCategory(category) -> createMusicCategoryNavigation(
                    category, bucketName)
            CategoryHelper.isRecentCategory(category) || CategoryHelper.isRecentGenericCategory(
                    category)                                -> createRecentCategoryNavigation(
                    category)
            category == Category.FOLDER_VIDEOS || CategoryHelper.isGenericVideosCategory(
                    category)                                -> createVideosNavigation(
                    category, bucketName)
            category == Category.FOLDER_IMAGES || CategoryHelper.isGenericImagesCategory(
                    category)                                -> createImageCategoryNavigation(
                    category, bucketName)
        }
    }

    private fun createImageCategoryNavigation(category: Category,
                                              bucketName: String?) {
        createNavigationForCategory(Category.GENERIC_IMAGES)
        if (category == Category.FOLDER_IMAGES) {
            createLibraryTitleNavigation(category, bucketName)
        }
    }

    private fun createLibraryTitleNavigation(category: Category, bucketName: String?) {
        viewModel.createLibraryTitleNavigation(category, bucketName)
    }

    private fun createVideosNavigation(category: Category,
                                       bucketName: String?) {
        createNavigationForCategory(Category.GENERIC_VIDEOS)
        if (category == Category.FOLDER_VIDEOS) {
            createLibraryTitleNavigation(category, bucketName)
        }
    }

    private fun createRecentCategoryNavigation(
            category: Category) {
        createNavigationForCategory(Category.RECENT)
        when (category) {
            Category.RECENT_IMAGES, Category.RECENT_VIDEOS, Category.RECENT_AUDIO, Category.RECENT_DOCS, Category.RECENT_APPS -> {
                createLibraryTitleNavigation(category, null)
            }
            else                                                                                                              -> {
            }
        }
    }

    private fun createMusicCategoryNavigation(category: Category, bucketName: String?) {
        createNavigationForCategory(Category.GENERIC_MUSIC)
        when (category) {
            Category.ALBUM_DETAIL, Category.ALBUMS   -> {
                createLibraryTitleNavigation(Category.ALBUMS, null)
                bucketName?.let {
                    createLibraryTitleNavigation(category, bucketName)
                }
            }
            Category.ARTIST_DETAIL, Category.ARTISTS -> {
                createLibraryTitleNavigation(Category.ARTISTS, null)
                bucketName?.let {
                    createLibraryTitleNavigation(category, bucketName)
                }
            }
            Category.GENRE_DETAIL, Category.GENRES   -> {
                createLibraryTitleNavigation(Category.GENRES, null)
                bucketName?.let {
                    createLibraryTitleNavigation(category, bucketName)
                }
            }
            Category.ALL_TRACKS, Category.PODCASTS   -> {
                createLibraryTitleNavigation(category, null)
            }
            else                                     -> {
            }
        }
    }

}