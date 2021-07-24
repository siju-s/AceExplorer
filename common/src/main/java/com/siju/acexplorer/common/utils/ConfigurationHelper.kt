package com.siju.acexplorer.common.utils

import android.content.res.Configuration
import android.util.Log
import com.siju.acexplorer.common.ViewMode

object ConfigurationHelper {

    fun getHomeGridCols(configuration: Configuration): Int {
        val sw = configuration.smallestScreenWidthDp
        val width = configuration.screenWidthDp
        val orientation = configuration.orientation
        val columns: Int
        when {
            sw >= 720 -> {
                columns = if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    HomeGridColumns.COL_720DP_LAND.value
                } else {
                    HomeGridColumns.COL_720DP.value
                }
            }
            sw >= 600 -> {
                columns = if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    HomeGridColumns.COL_600DP_LAND.value
                } else {
                    HomeGridColumns.COL_600DP.value
                }
            }
            sw < 360 -> {
                columns = if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    HomeGridColumns.COL_320DP_LAND.value
                } else {
                    HomeGridColumns.COL_320DP_PORT.value
                }
            }
            else -> {
                if (width < 500) {
                    columns = HomeGridColumns.COL_PORT.value
                    return columns
                }
                columns = if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    HomeGridColumns.COL_LAND.value
                } else {
                    HomeGridColumns.COL_PORT.value
                }
            }
        }
        return columns
    }

    fun getStorageGridCols(configuration: Configuration, viewMode: ViewMode?): Int {
        val sw = configuration.smallestScreenWidthDp
        val orientation = configuration.orientation
        val width = configuration.screenWidthDp
        return when (viewMode) {
            ViewMode.GALLERY             -> getGalleryColumns(sw, width, orientation)
            ViewMode.LIST, ViewMode.GRID -> getColumns(sw, width, orientation)
            else                         -> getColumns(sw, width, orientation)
        }
    }

    private fun getColumns(sw: Int, width: Int, orientation: Int): Int {
        val columns: Int
        when {
            sw >= 720 -> {
                columns = if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    StorageGridColumns.COL_720DP_LAND.value
                } else {
                    StorageGridColumns.COL_720DP.value
                }
            }
            sw >= 600 -> {
                columns = if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    StorageGridColumns.COL_600DP_LAND.value
                } else {
                    StorageGridColumns.COL_600DP.value
                }
            }
            else -> {
                if (width < 480) {
                    columns = if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                        StorageGridColumns.PORT.value
                    } else {
                        StorageGridColumns.COL_DUAL.value
                    }
                    return columns
                }
                columns = if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    StorageGridColumns.LAND.value
                } else {
                    StorageGridColumns.PORT.value
                }
            }
        }
        return columns
    }

    private fun getGalleryColumns(sw: Int, width: Int, orientation: Int): Int {
        val columns: Int
        when {
            sw >= 720 -> {
                columns = if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    StorageGridColumns.GALLERY_720DP_LAND.value
                } else {
                    StorageGridColumns.GALLERY_720DP_PORT.value
                }
            }
            sw >= 600 -> {
                columns = if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    StorageGridColumns.GALLERY_600DP_LAND.value
                } else {
                    StorageGridColumns.GALLERY_600DP_PORT.value
                }
            }
            else -> {
                if (width < 480) {
                    columns = if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                        StorageGridColumns.GALLERY_PORT.value
                    } else {
                        StorageGridColumns.GALLERY_COL_DUAL.value
                    }
                    return columns
                }
                columns = if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    StorageGridColumns.GALLERY_LAND.value
                } else {
                    StorageGridColumns.GALLERY_PORT.value
                }
            }
        }
        return columns
    }

    fun getStorageDualGridCols(configuration: Configuration, viewMode: ViewMode): Int {
        val sw = configuration.smallestScreenWidthDp
        val columns: Int = if (sw >= 720) {
            if (viewMode === ViewMode.GALLERY) StorageGridColumns.GALLERY_720DP_DUAL.value else StorageGridColumns.COL_720DP_DUAL.value
        } else if (sw >= 600) {
            if (viewMode === ViewMode.GALLERY) StorageGridColumns.GALLERY_COL_600DP_DUAL.value else StorageGridColumns.COL_600DP_DUAL.value
        } else {
            if (viewMode === ViewMode.GALLERY) StorageGridColumns.GALLERY_COL_DUAL.value else StorageGridColumns.COL_DUAL.value
        }
        Log.d("ConfigurationHelper", "getStorageDualGridCols() called with: sw = [$sw] cols:$columns")
        return columns
    }

    internal enum class HomeGridColumns(val value: Int) {
        COL_320DP_PORT(2),
        COL_320DP_LAND(4),
        COL_PORT(3),
        COL_LAND(5),
        COL_600DP(4),
        COL_600DP_LAND(6),
        COL_720DP(5),
        COL_720DP_LAND(6);
    }

    internal enum class StorageGridColumns(val value: Int) {
        PORT(4),
        LAND(5),
        GALLERY_PORT(3),
        GALLERY_LAND(4),
        COL_600DP(4),
        COL_600DP_LAND(5),
        GALLERY_600DP_PORT(3),
        GALLERY_600DP_LAND(4),
        COL_720DP(5),
        COL_720DP_LAND(6),
        GALLERY_720DP_PORT(4),
        GALLERY_720DP_LAND(5),
        COL_DUAL(3),
        GALLERY_COL_DUAL(2),
        COL_600DP_DUAL(3),
        GALLERY_COL_600DP_DUAL(2),
        COL_720DP_DUAL(4),
        GALLERY_720DP_DUAL(3);
    }
}