package com.siju.acexplorer.utils;

import android.content.res.Configuration;
import android.util.Log;

import com.siju.acexplorer.storage.model.ViewMode;

import static com.siju.acexplorer.utils.ConfigurationHelper.HomeGridColumns.COL_600DP;
import static com.siju.acexplorer.utils.ConfigurationHelper.HomeGridColumns.COL_600DP_LAND;
import static com.siju.acexplorer.utils.ConfigurationHelper.HomeGridColumns.COL_720DP;
import static com.siju.acexplorer.utils.ConfigurationHelper.HomeGridColumns.COL_720DP_LAND;
import static com.siju.acexplorer.utils.ConfigurationHelper.HomeGridColumns.COL_LAND;
import static com.siju.acexplorer.utils.ConfigurationHelper.HomeGridColumns.COL_PORT;


public class ConfigurationHelper {
    enum HomeGridColumns {
        COL_PORT(3),
        COL_LAND(5),
        COL_600DP(4),
        COL_600DP_LAND(6),
        COL_720DP(5),
        COL_720DP_LAND(6);

        private final int value;

        HomeGridColumns(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

    }

    enum StorageGridColumns {
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

        private final int value;

        StorageGridColumns(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

    }

    public static int getHomeGridCols(Configuration configuration) {
        int sw = configuration.smallestScreenWidthDp;
        int width = configuration.screenWidthDp;
        int orientation = configuration.orientation;
        int columns;
        if (sw >= 720) {
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                columns = COL_720DP_LAND.getValue();
            } else {
                columns = COL_720DP.getValue();
            }
        } else if (sw >= 600) {
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                columns = COL_600DP_LAND.getValue();
            } else {
                columns = COL_600DP.getValue();
            }
        } else {
            if (width < 500) {
                columns = COL_PORT.getValue();
                return columns;
            }
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                columns = COL_LAND.getValue();
            } else {
                columns = COL_PORT.getValue();
            }
        }
        return columns;
    }

    public static int getStorageGridCols(Configuration configuration, ViewMode viewMode) {
        int sw = configuration.smallestScreenWidthDp;
        int orientation = configuration.orientation;
        int width = configuration.screenWidthDp;

        switch (viewMode) {
            case GALLERY:
                return getGalleryColumns(sw, width, orientation);
            case LIST:
            case GRID:
            default:
                return getColumns(sw, width, orientation);
        }
    }

    private static int getColumns(int sw, int width, int orientation) {
        int columns;
        if (sw >= 720) {
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                columns = StorageGridColumns.COL_720DP_LAND.getValue();
            } else {
                columns = StorageGridColumns.COL_720DP.getValue();
            }
        } else if (sw >= 600) {
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                columns = StorageGridColumns.COL_600DP_LAND.getValue();
            } else {
                columns = StorageGridColumns.COL_600DP.getValue();
            }
        } else {
            if (width < 480) {
                if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                    columns = StorageGridColumns.PORT.getValue();
                } else {
                    columns = StorageGridColumns.COL_DUAL.getValue();
                }
                return columns;
            }
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                columns = StorageGridColumns.LAND.getValue();
            } else {
                columns = StorageGridColumns.PORT.getValue();
            }
        }
        return columns;
    }


    private static int getGalleryColumns(int sw, int width, int orientation) {
        int columns;
        if (sw >= 720) {
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                columns = StorageGridColumns.GALLERY_720DP_LAND.getValue();
            } else {
                columns = StorageGridColumns.GALLERY_720DP_PORT.getValue();
            }
        } else if (sw >= 600) {
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                columns = StorageGridColumns.GALLERY_600DP_LAND.getValue();
            } else {
                columns = StorageGridColumns.GALLERY_600DP_PORT.getValue();
            }
        } else {
            if (width < 480) {
                if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                    columns = StorageGridColumns.GALLERY_PORT.getValue();
                } else {
                    columns = StorageGridColumns.GALLERY_COL_DUAL.getValue();
                }
                return columns;
            }
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                columns = StorageGridColumns.GALLERY_LAND.getValue();
            } else {
                columns = StorageGridColumns.GALLERY_PORT.getValue();
            }
        }
        return columns;
    }

    public static int getStorageDualGridCols(Configuration configuration, ViewMode viewMode) {
        int sw = configuration.smallestScreenWidthDp;
        int columns;
        if (sw >= 720) {
            columns = viewMode == ViewMode.GALLERY ? StorageGridColumns.GALLERY_720DP_DUAL.getValue()
                    : StorageGridColumns.COL_720DP_DUAL.getValue();
        } else if (sw >= 600) {
            columns = viewMode == ViewMode.GALLERY ? StorageGridColumns.GALLERY_COL_600DP_DUAL.getValue()
                    : StorageGridColumns.COL_600DP_DUAL.getValue();
        } else {
            columns = viewMode == ViewMode.GALLERY ? StorageGridColumns.GALLERY_COL_DUAL.getValue()
                    : StorageGridColumns.COL_DUAL.getValue();
        }
        Log.e("ConfigurationHelper", "getStorageDualGridCols() called with: sw = [" + sw + "]" + " cols:" + columns);
        return columns;
    }

}
