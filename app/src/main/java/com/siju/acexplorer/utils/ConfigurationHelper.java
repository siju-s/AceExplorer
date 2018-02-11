package com.siju.acexplorer.utils;

import android.content.res.Configuration;

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
        COL_PORT(4),
        COL_LAND(5),
        COL_600DP(4),
        COL_600DP_LAND(5),
        COL_720DP(5),
        COL_720DP_LAND(6),

        COL_DUAL(3),
        COL_600DP_DUAL(3),
        COL_720DP_DUAL(4);

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
            if (sw <=320 && width < 400) {
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

    public static int getStorageGridCols(Configuration configuration) {
        int sw = configuration.smallestScreenWidthDp;
        int orientation = configuration.orientation;
        int width = configuration.screenWidthDp;

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
            if (sw <=320 && width < 400) {
                if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                    columns = StorageGridColumns.COL_DUAL.getValue();
                } else {
                    columns = StorageGridColumns.COL_PORT.getValue();
                }
                return columns;
            }
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                columns = StorageGridColumns.COL_LAND.getValue();
            } else {
                columns = StorageGridColumns.COL_PORT.getValue();
            }
        }
        return columns;
    }

    public static int getStorageDualGridCols(Configuration configuration) {
        int sw = configuration.smallestScreenWidthDp;
        int columns;
        if (sw >= 720) {
            columns = StorageGridColumns.COL_720DP_DUAL.getValue();
        } else if (sw >= 600) {
           columns = StorageGridColumns.COL_600DP_DUAL.getValue();
        } else {
           columns = StorageGridColumns.COL_DUAL.getValue();
        }
        return columns;
    }

}
