package com.siju.acexplorer.filesystem;


public class FileConstants {

    public static final String PREFS_HIDDEN = "prefHidden";
    public static final String PREFS_DUAL_PANE = "prefDualPane";
    public static final String KEY_DUAL_ENABLED = "dualEnabled";
    public static final String PREFS_HOMESCREEN = "prefHomeScreen";
    public static final String PREFS_RESET = "prefsReset";
    public static final String PREFS_THEME = "prefThemes";

    public static final String CURRENT_THEME = "theme";
    public static final String ROOT_ACCESS = "root";
    public static final String SAF_URI = "saf_uri";

    public static final int THEME_LIGHT = 0;
    public static final int THEME_DARK = 1;
    public static final String OPERATION = "operation";

    public static final int FOLDER_CREATE = 1;
    public static final int FILE_CREATE = 2;
    public static final int RENAME = 3;
    public static final int MOVE = 4;
    public static final int COPY = 5;
    public static final int DELETE = 6;
    public static final int COMPRESS = 7;
    public static final int EXTRACT = 8;

    public static final String IS_OPERATION_SUCCESS = "IS_OPERATION_SUCCESS";
    public static final String RELOAD_LIST = "reload_list";
    public static final String REFRESH = "refresh";
    public static final String KEY_PATH = "PATH";
    public static final String KEY_HOME = "HOME";
    public static final String KEY_PATH_OTHER = "OTHER_PATH";
    public static final String KEY_FOCUS_DUAL = "DUAL_FOCUS";
    public static final String KEY_LIB_SORTLIST = "LIB_LIST";
    public static final String KEY_GRID_COLUMNS = "grid_columns";
    public static final String KEY_POSITION = "position";
    public static final String KEY_OFFSET = "offset";
    public static final String KEY_DUAL_MODE = "DUAL_MODE";
    public static final String APK_EXTENSION = "apk";
    public static final String KEY_CATEGORY = "CATEGORY";
    public static final String KEY_PREMIUM= "PREMIUM";

    public static final String KEY_ZIP = "ZIP";

    public static final String KEY_SORT_MODE = "sort_mode";
    public static final int KEY_SORT_NAME = 0;
    public static final int KEY_LISTVIEW = 0;
    public static final int KEY_GRIDVIEW = 1;


    /********** DOCUMENT EXTENSIONS**************/
    public static final String EXT_TEXT = "txt";
    public static final String EXT_HTML = "html";
    public static final String EXT_PDF = "pdf";
    public static final String EXT_DOC = "doc";
    public static final String EXT_DOCX = "docx";
    public static final String EXT_XLS = "xls";
    public static final String EXT_XLXS = "xlsx";
    public static final String EXT_CSV = "csv";
    public static final String EXT_PPT = "ppt";
    public static final String EXT_PPTX = "pptx";
    public static final String EXT_ZIP = "zip";
    public static final String EXT_TAR = "tar";
    public static final String EXT_TGZ = "tgz";
    public static final String EXT_RAR = "rar";


    public enum CATEGORY {
        FILES(0),
        AUDIO(1),
        VIDEO(2),
        IMAGE(3),
        DOCS(4),
        DOWNLOADS(5),
        ADD(6),
        COMPRESSED(7),
        FAVORITES(8),
        PDF(9),
        APPS(10),
        LARGE_FILES(11),
        ZIP_VIEWER(12),
        GENERIC_LIST(13),
        PICKER(14);

        private final int value;

        CATEGORY(int value) {
            this.value = value;
        }

        public int getValue() {

            return value;
        }
    }

    public enum WRITE_MODES {
        ROOT(0),
        INTERNAL(1),
        EXTERNAL(2);

        private final int value;

        WRITE_MODES(int value) {
            this.value = value;
        }

        public int getValue() {

            return value;
        }
    }
}
