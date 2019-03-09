package com.siju.acexplorer.main.model.data;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.webkit.MimeTypeMap;

import com.siju.acexplorer.main.model.FileConstants;
import com.siju.acexplorer.common.types.FileInfo;
import com.siju.acexplorer.main.model.HiddenFileHelper;
import com.siju.acexplorer.main.model.groups.Category;
import com.siju.acexplorer.main.model.helper.FileUtils;

import java.io.File;
import java.util.ArrayList;

import static com.siju.acexplorer.main.model.HiddenFileHelper.constructionNoHiddenFilesArgs;
import static com.siju.acexplorer.main.model.helper.FileUtils.getCategoryFromExtension;
import static com.siju.acexplorer.main.model.helper.SortHelper.sortFiles;

class DocumentDataFetcher {

    private static final int LARGE_FILES_MIN_SIZE_MB = 104857600; //100 MB

    static ArrayList<FileInfo> fetchDocumentsByCategory(Context context, Category category,
                                                        boolean showOnlyCount, int sortMode, boolean showHidden)
    {
        Uri uri = MediaStore.Files.getContentUri("external");
        String selection = "";
        String[] selectionArgs = null;

        switch (category) {
            case DOCS:
                selection = constructSelectionForDocs() ;
                if (!showHidden) {
                    selection = selection + " AND " + constructionNoHiddenFilesArgs();
                }
                break;
            case COMPRESSED:
                selection = getMediaTypeNone() + " AND " + constructSelectionForZip();
                if (!showHidden) {
                    selection = selection + " AND " + constructionNoHiddenFilesArgs();
                }
                break;
            case PDF:
                String pdf1 = MimeTypeMap.getSingleton().getMimeTypeFromExtension(FileConstants
                                                                                          .EXT_PDF);
                if (!showHidden) {
                    selection = constructionNoHiddenFilesArgs() + " AND ";
                }
                selection += getMediaTypeNone() + " AND " + MediaStore.Files.FileColumns.MIME_TYPE + " =?";
                selectionArgs = new String[]{pdf1};
                break;
            case LARGE_FILES:
                if (!showHidden) {
                    selection = constructionNoHiddenFilesArgs() + " AND ";
                }
                selection += MediaStore.Files.FileColumns.SIZE + " >?";
                selectionArgs = new String[]{String.valueOf(LARGE_FILES_MIN_SIZE_MB)};
                break;
        }
        Cursor cursor = context.getContentResolver().query(uri, null, selection, selectionArgs,
                                                           null);

        return getDataFromCursor(cursor, category, showOnlyCount, sortMode, showHidden);
    }

    static String constructSelectionForDocs() {
        String doc = MimeTypeMap.getSingleton().getMimeTypeFromExtension(FileConstants.EXT_DOC);
        String docx = MimeTypeMap.getSingleton().getMimeTypeFromExtension(FileConstants.EXT_DOCX);
        String txt = MimeTypeMap.getSingleton().getMimeTypeFromExtension(FileConstants.EXT_TEXT);
        String html = MimeTypeMap.getSingleton().getMimeTypeFromExtension(FileConstants.EXT_HTML);
        String pdf = MimeTypeMap.getSingleton().getMimeTypeFromExtension(FileConstants.EXT_PDF);
        String xls = MimeTypeMap.getSingleton().getMimeTypeFromExtension(FileConstants.EXT_XLS);
        String xlxs = MimeTypeMap.getSingleton().getMimeTypeFromExtension(FileConstants.EXT_XLXS);
        String ppt = MimeTypeMap.getSingleton().getMimeTypeFromExtension(FileConstants.EXT_PPT);
        String pptx = MimeTypeMap.getSingleton().getMimeTypeFromExtension(FileConstants.EXT_PPTX);

        return MediaStore.Files.FileColumns.MIME_TYPE + " IN " + "("
               + "'" + doc + "'" + ","
               + "'" + docx + "'" + ","
               + "'" + txt + "'" + ","
               + "'" + html + "'" + ","
               + "'" + pdf + "'" + ","
               + "'" + xls + "'" + ","
               + "'" + xlxs + "'" + ","
               + "'" + ppt + "'" + ","
               + "'" + pptx + "'" + " )";
    }

    static String constructSelectionForZip() {
        String zip = MimeTypeMap.getSingleton().getMimeTypeFromExtension(FileConstants.EXT_ZIP);
        String tar = MimeTypeMap.getSingleton().getMimeTypeFromExtension(FileConstants.EXT_TAR);
        String tgz = MimeTypeMap.getSingleton().getMimeTypeFromExtension(FileConstants.EXT_TGZ);
        String rar = MimeTypeMap.getSingleton().getMimeTypeFromExtension(FileConstants.EXT_RAR);

        return MediaStore.Files.FileColumns.MIME_TYPE + " IN " + "("
               + "'" + zip + "'" + ","
               + "'" + tar + "'" + ","
               + "'" + tgz + "'" + ","
               + "'" + rar + "'" + ")";
    }

    private static String getMediaTypeNone() {
        return MediaStore.Files.FileColumns.MEDIA_TYPE + " = " + MediaStore.Files.FileColumns.MEDIA_TYPE_NONE;
    }

    private static ArrayList<FileInfo> getDataFromCursor(Cursor cursor, Category category,
                                                         boolean showOnlyCount, int sortMode,
                                                         boolean showHidden)
    {
        ArrayList<FileInfo> fileInfoList = new ArrayList<>();
        if (cursor == null) {
            return fileInfoList;
        }
        if (cursor.moveToFirst()) {
            if (showOnlyCount) {
                fileInfoList.add(new FileInfo(category, cursor.getCount()));
                cursor.close();
                return fileInfoList;
            }
            int titleIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.TITLE);
            int sizeIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE);
            int dateIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED);
            int fileIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID);
            int pathIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA);
            do {
                String path = cursor.getString(pathIndex);
                File file = new File(path);
                if (HiddenFileHelper.shouldSkipHiddenFiles(file, showHidden)) {
                    continue;
                }
                String fileName = cursor.getString(titleIndex);
                String extension = FileUtils.getExtension(path);
                String nameWithExt = FileUtils.constructFileNameWithExtension(fileName, extension);
                long size = cursor.getLong(sizeIndex);
                long date = cursor.getLong(dateIndex);
                long fileId = cursor.getLong(fileIdIndex);
                fileInfoList.add(new FileInfo(getCategoryFromExtension(extension), fileId, nameWithExt, path, date, size, extension));
            } while (cursor.moveToNext());
        }
        cursor.close();
        if (isLargeFilesCategory(category)) {
            sortMode = 5; // (Size desc) TODO 29-Aug-2018 Replace sort mode with enums
        }
        sortFiles(fileInfoList, sortMode);

        return fileInfoList;
    }

    private static boolean isLargeFilesCategory(Category category) {
        return Category.LARGE_FILES.equals(category);
    }
}
