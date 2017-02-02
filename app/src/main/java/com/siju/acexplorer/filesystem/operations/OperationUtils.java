package com.siju.acexplorer.filesystem.operations;

import android.content.Context;

import com.siju.acexplorer.utils.Utils;

import java.io.File;

import static com.siju.acexplorer.filesystem.storage.StorageUtils.isOnExtSdCard;
import static com.siju.acexplorer.filesystem.utils.FileUtils.isFileNonWritable;
import static com.siju.acexplorer.filesystem.utils.FileUtils.isWritable;

public class OperationUtils {

    public static final String ACTION_SAF = "ACTION_SAF";
    public static final String KEY_FILENAME = "filename";
    public static final String KEY_FILEPATH = "filepath";
    public static final String KEY_FILEPATH2 = "filepath2";
    public static final String KEY_OPERATION = "operation";
    public static final String KEY_FILES = "op_files";
    public static final String KEY_POSITION = "pos";
    public static final String KEY_CONFLICT_DATA = "conflict_data";
    public static final String ACTION_OP_REFRESH = "refresh";
    public static final String ACTION_RELOAD_LIST = "reload";
    public static final String ACTION_OP_FAILED = "failed";
    public static final String KEY_RESULT = "result";

    public enum WriteMode {
        ROOT,
        INTERNAL,
        EXTERNAL
    }


    public static WriteMode checkFolder(final String f, Context context) {
        if (f == null) return WriteMode.ROOT;
        File folder = new File(f);
        if (Utils.isAtleastLollipop() && isOnExtSdCard(folder, context)) {
            if (!folder.exists() || !folder.isDirectory()) {
                return WriteMode.ROOT;
            }

            // On Android 5 and above, trigger storage access framework.
            if (isFileNonWritable(folder, context)) {
                return WriteMode.EXTERNAL;
            }
            return WriteMode.INTERNAL;
        } else if (Utils.isKitkat() && isOnExtSdCard(folder, context)) {
            // Assume that Kitkat workaround works
            return WriteMode.INTERNAL;
        } else if (isWritable(new File(folder, "DummyFile"))) {
            return WriteMode.INTERNAL;
        } else {
            return WriteMode.ROOT;
        }
    }

}
