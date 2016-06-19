package com.siju.filemanager.filesystem;

/**
 * Created by Siju on 13-06-2016.
 */

public class FileConstants {

    public static final String KEY_PATH = "PATH";
    public static final String KEY_FILENAME = "FILENAME";
    public static final String KEY_DUAL_MODE = "DUAL_MODE";
    public static final String APK_EXTENSION = "apk";

    public enum CATEGORY {
        IMAGE(1),
        VIDEO(2),
        AUDIO(3);

        private int value;

        private CATEGORY(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }


}
