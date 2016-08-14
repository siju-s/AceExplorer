package com.siju.filemanager.filesystem.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.provider.DocumentFile;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.siju.filemanager.R;
import com.siju.filemanager.common.Logger;
import com.siju.filemanager.filesystem.model.FileInfo;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.Collator;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static android.webkit.MimeTypeMap.getSingleton;


/**
 * Created by Siju on 16-06-2016.
 */

public class FileUtils {

    private static String mCurrentDirectory;
    public static final boolean isDebug = true;
    private static final int BUFFER = 2048; //2 KB
    private static FileComparator comparator = new FileComparator();
    private static int fileCount = 0;
    static final String TAG = "FileUtils";
    private static Activity activity;
    public static final int ACTION_NONE = 0;
    public static final int ACTION_REPLACE = 1;
    public static final int ACTION_SKIP = 2;
    public static final int ACTION_KEEP = 3;
    public static final int ACTION_CANCEL = 4;


    public static File getRootDirectory() {
        return Environment.getRootDirectory();
    }

    public static File getInternalStorage() {
        return Environment.getExternalStorageDirectory();
    }

    public static String getAbsolutePath(File file) {
        if (file == null)
            return null;
        return file.getAbsolutePath();
    }

    public static File getDownloadsDirectory() {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    }

    public static String getSpaceLeft(Context context, File path) {
        String freeSpace = Formatter.formatFileSize(context, path.getFreeSpace());
        return freeSpace;
    }

    public static String getTotalSpace(Context context, File path) {
        String totalSpace = Formatter.formatFileSize(context, path.getTotalSpace());
        return totalSpace;
    }

    public static String getCurrentDirectory() {
        return mCurrentDirectory;
    }

    public static void setCurrentDirectory(String path) {
        mCurrentDirectory = path;
    }


    /**
     * External memory card locations
     * 1. /storage/sdcard1
     * 2. /storage/MicroSD
     * 3. /mnt/extSdCard
     *
     * @return
     */
    public static File getExternalStorage() {

        File internalStorage = getInternalStorage();
        File parent = internalStorage.getParentFile().getParentFile();

        if (parent.exists()) {
            File extSD = new File(parent, "sdcard1");

            if (extSD.exists()) {
                return extSD;
            } else {
                File extSD1 = new File(parent, "MicroSD");
                if (extSD1.exists()) {
                    return extSD1;
                } else {
                    File extSD2 = new File("/mnt/extSdCard");
                    if (extSD2.exists()) {
                        return extSD2;
                    }
                    return null;
                }
            }
        } else {
            return null;
        }

    }

    final static Pattern DIR_SEPARATOR = Pattern.compile("/");

    public static List<String> getStorageDirectories(Context context, boolean permissionGranted) {
        // Final set of paths
        final ArrayList<String> rv = new ArrayList<>();
        // Primary physical SD-CARD (not emulated)
        final String rawExternalStorage = System.getenv("EXTERNAL_STORAGE");
        // All Secondary SD-CARDs (all exclude primary) separated by ":"
        final String rawSecondaryStoragesStr = System.getenv("SECONDARY_STORAGE");
        // Primary emulated SD-CARD
        final String rawEmulatedStorageTarget = System.getenv("EMULATED_STORAGE_TARGET");
        if (TextUtils.isEmpty(rawEmulatedStorageTarget)) {
            // Device has physical external storage; use plain paths.
            if (TextUtils.isEmpty(rawExternalStorage)) {
                // EXTERNAL_STORAGE undefined; falling back to default.
                rv.add("/storage/sdcard0");
            } else {
                rv.add(rawExternalStorage);
            }
        } else {
            // Device has emulated storage; external storage paths should have
            // userId burned into them.
            final String rawUserId;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                rawUserId = "";
            } else {
                final String path = Environment.getExternalStorageDirectory().getAbsolutePath();
                final String[] folders = DIR_SEPARATOR.split(path);
                final String lastFolder = folders[folders.length - 1];
                boolean isDigit = false;
                try {
                    Integer.valueOf(lastFolder);
                    isDigit = true;
                } catch (NumberFormatException ignored) {
                }
                rawUserId = isDigit ? lastFolder : "";
            }
            // /storage/emulated/0[1,2,...]
            if (TextUtils.isEmpty(rawUserId)) {
                rv.add(rawEmulatedStorageTarget);
            } else {
                rv.add(rawEmulatedStorageTarget + File.separator + rawUserId);
            }
        }
        // Add all secondary storages
        if (!TextUtils.isEmpty(rawSecondaryStoragesStr)) {
            // All Secondary SD-CARDs splited into array
            final String[] rawSecondaryStorages = rawSecondaryStoragesStr.split(File.pathSeparator);
            Collections.addAll(rv, rawSecondaryStorages);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && permissionGranted)
            rv.clear();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            String strings[] = getExtSdCardPathsForActivity(context);
            for (String s : strings) {
                File f = new File(s);
                if (!rv.contains(s) && canListFiles(f))
                    rv.add(s);
            }
        }

        File usb = getUsbDrive();
        if (usb != null && !rv.contains(usb.getPath())) rv.add(usb.getPath());
        return rv;
    }


    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String[] getExtSdCardPathsForActivity(Context context) {
        List<String> paths = new ArrayList<String>();
        for (File file : context.getExternalFilesDirs("external")) {
            if (file != null) {
                int index = file.getAbsolutePath().lastIndexOf("/Android/data");
                if (index < 0) {
                    Log.w("AmazeFileUtils", "Unexpected external file dir: " + file.getAbsolutePath());
                } else {
                    String path = file.getAbsolutePath().substring(0, index);
                    try {
                        path = new File(path).getCanonicalPath();
                    } catch (IOException e) {
                        // Keep non-canonical path.
                    }
                    paths.add(path);
                }
            }
        }
        if (paths.isEmpty()) paths.add("/storage/sdcard1");
        return paths.toArray(new String[0]);
    }


    public static boolean canListFiles(File f) {
        try {
            if (f.canRead() && f.isDirectory())
                return true;
            else
                return false;
        } catch (Exception e) {
            return false;
        }
    }


    public static File getUsbDrive() {
        File parent;
        parent = new File("/storage");

        try {
            for (File f : parent.listFiles()) {
                if (f.exists() && f.getName().toLowerCase().contains("usb") && f.canExecute()) {
                    return f;
                }
            }
        } catch (Exception e) {
        }
        parent = new File("/mnt/sdcard/usbStorage");
        if (parent.exists() && parent.canExecute())
            return (parent);
        parent = new File("/mnt/sdcard/usb_storage");
        if (parent.exists() && parent.canExecute())
            return parent;

        return null;
    }


    public static String convertDate(long dateInMs) {
        SimpleDateFormat df2 = new SimpleDateFormat("MMM dd, yyyy hh:mm a");
        String dateText = df2.format(dateInMs);
        return dateText;
    }


    public FileUtils(Activity activity) {
        this.activity = activity;
    }

    /**
     * @param source      the file to be copied
     * @param destination the directory to move the file to
     * @return
     */

    public static int copyToDirectory(Context context, String source, String destination, boolean isMoveOperation, int
            action, PasteUtils.BackGroundOperationsTask.Progress progress) {
        PasteUtils.BackGroundOperationsTask.Progress progressBg = progress;
        Logger.log("TAG", "ACTION==" + action);
        File sourceFile = new File(source);
        File destinationDir = new File(destination);
        byte[] data = new byte[BUFFER];
        int read = 0;
        boolean isMove = isMoveOperation;
        int fileAction = action;

        boolean exists = destinationDir.exists();
        boolean value = destinationDir.canWrite();
        boolean sourceisFile = sourceFile.isFile();
        boolean destIsDir = destinationDir.isDirectory();
        File newFile = null;

        if (destinationDir.canWrite()) {
            String file_name = source.substring(source.lastIndexOf("/"), source.length());
            if (sourceFile.isFile() && destinationDir.isDirectory()) {
                String fileNameWithoutExt = file_name.substring(0, file_name.lastIndexOf("."));
                long size = sourceFile.length();
                Logger.log("TAG", "fileNameWithoutExt==" + fileNameWithoutExt);
                if (fileAction == ACTION_SKIP) {
                    return -1;
                } else if (fileAction == ACTION_KEEP) {
                    String extension = file_name.substring(file_name.lastIndexOf("."), file_name.length());
                    String newName = destination + fileNameWithoutExt + "(2)" + extension;
                    Logger.log("TAG", "newName==" + newName);
                    newFile = new File(newName);
                } else if (fileAction == ACTION_REPLACE) {
                    String destinationDirFile = destinationDir + "/" + file_name;
                    new File(destinationDirFile).delete();
                    newFile = new File(destination + file_name);
                } else {
                    newFile = new File(destination + file_name);
                }

                int progress1 = 0;
                try {

                    BufferedOutputStream outputStream = new BufferedOutputStream(
                            new FileOutputStream(newFile));
                    BufferedInputStream bufferedInputStream = new BufferedInputStream(
                            new FileInputStream(sourceFile));
                    int count = 0;
                    int tempProgress = 0;
                    while ((read = bufferedInputStream.read(data, 0, BUFFER)) != -1) {
                        count++;
                        outputStream.write(data, 0, read);
                        double value1 = (double) BUFFER / size;
                        if (progress != null) {
                            progress1 = (int) (value1 * count * 100);
                            if (tempProgress != progress1) {
                                tempProgress = progress1;
                                progress.publish(tempProgress);
//                            System.out.println("Progress==" + progress1 + "File=" + file_name);
                            }
                        }


                    }
//                    if (progress1 != 100) {
//                        progress.publish(100);
//                    }
                    outputStream.flush();
                    bufferedInputStream.close();
                    outputStream.close();

                } catch (FileNotFoundException e) {
                    Log.e("FileNotFoundException", e.getMessage());
                    return -1;

                } catch (IOException e) {
                    Log.e("IOException", e.getMessage());
                    return -1;

                }

            } else if (sourceFile.isDirectory() && destinationDir.isDirectory()) {
                if (fileAction == ACTION_SKIP) {
                    return -1;
                } else if (fileAction == ACTION_REPLACE) {
                    String destinationDirFile = destinationDir + "/" + file_name;
                    new File(destinationDirFile).delete();
                }

                String files[] = sourceFile.list();
                String dir = destination + file_name;
                int len = files.length;

                if (!new File(dir).mkdir()) {
                    return -1;
                }

                for (int i = 0; i < len; i++) {
                    copyToDirectory(context, source + "/" + files[i], dir, isMove, fileAction, progressBg);
                }

            }
        } else {
            return -1;
        }

        // If move operation
        if (isMove && action != FileUtils.ACTION_REPLACE) {
            // delete the original file
            sourceFile.delete();
        }
        return 0;
    }

    /**
     * View the file in external apps based on Mime Type
     *
     * @param context
     * @param path
     * @param extension
     */
    public static void viewFile(Context context, String path, String extension) {

        Uri uri = Uri.fromFile(new File(path));

        Intent intent = new Intent();
        intent.setAction(android.content.Intent.ACTION_VIEW);
        // To lowercase used since capital extensions like MKV doesn't get recognised
        String ext = extension.toLowerCase();
        String mimeType = getSingleton().getMimeTypeFromExtension(ext);
        Logger.log("TAG", "uri==" + uri + "MIME=" + mimeType);
        intent.setDataAndType(uri, mimeType);
        PackageManager packageManager = context.getPackageManager();

        if (mimeType != null) {
            if (intent.resolveActivity(packageManager) != null) {
//            Intent chooser = Intent.createChooser(intent, context.getString(R.string.msg_open_file));
                context.startActivity(intent);
            } else {
                showMessage(context, context.getString(R.string.msg_error_not_supported));
            }
        } else {
            openWith(uri, context);
        }

    }

    private static void openWith(final Uri uri, final Context context) {
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_openas);
        dialog.setTitle(context.getString(R.string.open_as));
        String[] items = new String[]{context.getString(R.string.text), context.getString(R.string.image),
                context.getString(R.string.video), context.getString(R.string.audio),
                context.getString(R.string.other)};

        ListView listView = (ListView) dialog.findViewById(R.id.listOpenAs);
        ArrayAdapter<String> itemsAdapter =
                new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, items);
        listView.setAdapter(itemsAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);
                switch (position) {
                    case 0:
                        intent.setDataAndType(uri, "text/*");
                        break;
                    case 1:
                        intent.setDataAndType(uri, "image/*");
                        break;
                    case 2:
                        intent.setDataAndType(uri, "video/*");
                        break;
                    case 3:
                        intent.setDataAndType(uri, "audio/*");
                        break;
          /*          case 4:
                        intent = new Intent(c, DbViewer.class);
                        intent.putExtra("path", f.getPath());
                        break;*/
                    case 4:
                        intent.setDataAndType(uri, "*/*");
                        break;
                }
                PackageManager packageManager = context.getPackageManager();
                if (intent.resolveActivity(packageManager) != null) {
//            Intent chooser = Intent.createChooser(intent, context.getString(R.string.msg_open_file));
                    context.startActivity(intent);
                } else {
                    showMessage(context, context.getString(R.string.msg_error_not_supported));
                }
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    /**
     * Validates file name at the time of creation
     * special reserved characters shall not be allowed in the file names
     *
     * @param name the file which needs to be validated
     * @return boolean if the file name is valid or invalid
     */
    public static boolean validateFileName(String name) {

       /* StringBuilder builder = new StringBuilder(file.getPath());
        String newName = builder.substring(builder.lastIndexOf("/") + 1, builder.length());*/
        String newName = name.trim();
        if (newName.contains("/") ||
                newName.length() == 0) {
            return false;
        }
        return true;
    }

    private static void showMessage(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static void shareFiles(Context context, ArrayList<FileInfo> fileInfo, int category) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND_MULTIPLE);
        if (category == 0) {
            intent.setType("*/*");
        } else {
            String extension = fileInfo.get(0).getExtension();
            String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            intent.setType(mimeType);
        }

        ArrayList<Uri> files = new ArrayList<>();

        for (FileInfo info : fileInfo) {
            File file = new File(info.getFilePath());
            Uri uri = Uri.fromFile(file);
            System.out.println("shareuri==" + uri);
            files.add(uri);
        }

        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
        context.startActivity(intent);
    }


    public static ArrayList<FileInfo> sortFiles(ArrayList<FileInfo> files, int sortMode) {

        switch (sortMode) {
            case 0:
                Collections.sort(files, FileUtils.comparatorByName);
                break;
            case 1:
                Collections.sort(files, FileUtils.comparatorByNameDesc);
                break;
            case 2:
                Collections.sort(files, FileUtils.comparatorByType);
                break;
            case 3:
                Collections.sort(files, FileUtils.comparatorByTypeDesc);
                break;
            case 4:
                Collections.sort(files, FileUtils.comparatorBySize);
                break;
            case 5:
                Collections.sort(files, FileUtils.comparatorBySizeDesc);
                break;
            case 6:
                Collections.sort(files, FileUtils.comparatorByDate);
                break;
            case 7:
                Collections.sort(files, FileUtils.comparatorByDateDesc);
                break;

        }
        return files;
    }


    private final static Comparator<? super String> type = new Comparator<String>() {

        public int compare(String arg0, String arg1) {
            String ext = null;
            String ext2 = null;
            int ret;
            File file1 = new File(arg0);
            File file2 = new File(arg1);

            try {
                ext = arg0.substring(arg0.lastIndexOf(".") + 1, arg0.length())
                        .toLowerCase();
                ext2 = arg1.substring(arg1.lastIndexOf(".") + 1, arg1.length())
                        .toLowerCase();

            } catch (IndexOutOfBoundsException e) {
                return 0;
            }
            ret = ext.compareTo(ext2);

            if (ret == 0)
                return arg0.toLowerCase().compareTo(arg1.toLowerCase());
            else {
                if ((file1.isDirectory()) && (!file2.isDirectory()))
                    return -1;
                if ((!file1.isDirectory()) && (file2.isDirectory()))
                    return 1;
                if ((file1.getName().startsWith("."))
                        && (!file2.getName().startsWith(".")))
                    return -1;
                if ((!file1.getName().startsWith("."))
                        && (file2.getName().startsWith(".")))
                    return 1;
            }

            return ret;
        }
    };


    public static Comparator<? super FileInfo> comparatorByName = new Comparator<FileInfo>() {

        public int compare(FileInfo file1, FileInfo file2) {

            if ((file1.isDirectory()) && (!file2.isDirectory()))
                return -1;
            if ((!file1.isDirectory()) && (file2.isDirectory()))
                return 1;
            // here both are folders or both are files : sort alpha
            return file1.getFileName().toLowerCase()
                    .compareTo(file2.getFileName().toLowerCase());
        }

    };

    public static Comparator<? super FileInfo> comparatorByNameDesc = new Comparator<FileInfo>() {

        public int compare(FileInfo file1, FileInfo file2) {

            if ((file1.isDirectory()) && (!file2.isDirectory()))
                return -1;
            if ((!file1.isDirectory()) && (file2.isDirectory()))
                return 1;
            // here both are folders or both are files : sort alpha
            return file2.getFileName().toLowerCase()
                    .compareTo(file1.getFileName().toLowerCase());
        }

    };

/*    private static Comparator<? super FileInfo> comparatorByTypeFiles = new Comparator<FileInfo>() {

        public int compare(FileInfo file1, FileInfo file2) {
            // sort folders first
            if ((file1.isDirectory()) && (!file2.isDirectory()))
                return -1;
            if ((!file1.isDirectory()) && (file2.isDirectory()))
                return 1;

            // here both are folders or both are files : sort alpha
            return file1.getName().toLowerCase()
                    .compareTo(file2.getName().toLowerCase());
        }

    };*/


    public static Comparator<? super FileInfo> comparatorBySize = new Comparator<FileInfo>() {

        public int compare(FileInfo file1, FileInfo file2) {

            if ((file1.isDirectory()) && (!file2.isDirectory())) {
                return -1;
            }
            if ((!file1.isDirectory()) && (file2.isDirectory())) {
                return 1;
            }


            Long first = getSize(new File(file1.getFilePath()));
            Long second = getSize(new File(file2.getFilePath()));

//            Logger.log("SIJU","Size1="+first+" Size2="+second);

            return first.compareTo(second);
        }
    };

    public static Comparator<? super FileInfo> comparatorBySizeDesc = new Comparator<FileInfo>() {

        public int compare(FileInfo file1, FileInfo file2) {

            if ((file1.isDirectory()) && (!file2.isDirectory())) {
                return -1;
            }
            if ((!file1.isDirectory()) && (file2.isDirectory())) {
                return 1;
            }

            Long first = getSize(new File(file1.getFilePath()));
            Long second = getSize(new File(file2.getFilePath()));
            Logger.log("SIJU", "Size1=" + first + " Size2=" + second);


            return second.compareTo(first);
        }
    };

    public static Comparator<? super FileInfo> comparatorByType = new Comparator<FileInfo>() {

        public int compare(FileInfo file1, FileInfo file2) {

            String arg0 = file1.getFileName();
            String arg1 = file2.getFileName();

            final int s1Dot = arg0.lastIndexOf('.');
            final int s2Dot = arg1.lastIndexOf('.');

            if ((s1Dot == -1) == (s2Dot == -1)) { // both or neither

                arg0 = arg0.substring(s1Dot + 1);
                arg1 = arg1.substring(s2Dot + 1);
                return (arg0.toLowerCase()).compareTo((arg1.toLowerCase()));
            } else if (s1Dot == -1) { // only s2 has an extension, so s1 goes
                // first
                return -1;
            } else { // only s1 has an extension, so s1 goes second
                return 1;
            }
        }

    };

    public static Comparator<? super FileInfo> comparatorByTypeDesc = new Comparator<FileInfo>() {

        public int compare(FileInfo file1, FileInfo file2) {

            String arg0 = file2.getFileName();
            String arg1 = file1.getFileName();

            final int s1Dot = arg0.lastIndexOf('.');
            final int s2Dot = arg1.lastIndexOf('.');

            if ((s1Dot == -1) == (s2Dot == -1)) { // both or neither

                arg0 = arg0.substring(s1Dot + 1);
                arg1 = arg1.substring(s2Dot + 1);
                return (arg0.toLowerCase()).compareTo((arg1.toLowerCase()));
            } else if (s1Dot == -1) { // only s2 has an extension, so s1 goes
                // first
                return -1;
            } else { // only s1 has an extension, so s1 goes second
                return 1;
            }
        }

    };


    public static Comparator<? super FileInfo> comparatorByDate = new Comparator<FileInfo>() {

        public int compare(FileInfo file1, FileInfo file2) {

            Long date1 = new File(file1.getFilePath()).lastModified();
            Long date2 = new File(file2.getFilePath()).lastModified();

//            Long date1 = convertStringToLongDate(file1.getFileDate());
//            Long date2 = convertStringToLongDate(file2.getFileDate());
//
//            Long first = file1.getFileDate();
//            Long second = file2.getFileDate();

            return date1.compareTo(date2);
        }
    };

    public static Comparator<? super FileInfo> comparatorByDateDesc = new Comparator<FileInfo>() {

        public int compare(FileInfo file1, FileInfo file2) {

            Long date1 = new File(file1.getFilePath()).lastModified();
            Long date2 = new File(file2.getFilePath()).lastModified();


//            Long date1 = convertStringToLongDate(file1.getFileDate());
//            Long date2 = convertStringToLongDate(file2.getFileDate());
//
//            Long first = file1.getFileDate();
//            Long second = file2.getFileDate();

            return date2.compareTo(date1);
        }
    };

    private static long convertStringToLongDate(String dateModified) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());
            Date date = sdf.parse(dateModified);

            long modDate = date.getTime();
            return modDate;

        } catch (ParseException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static long getFolderSize(File directory) {
        long length = 0;
        try {
            for (File file : directory.listFiles()) {

                if (file.isFile())
                    length += file.length();
                else
                    length += getFolderSize(file);
            }
        } catch (Exception e) {
        }
        return length;
    }

    public static void updateMediaStore(Context context, int category, long id, String
            renamedFilePath) {
        ContentValues values = new ContentValues();
        switch (category) {
            case 1:
                Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
//                newUri = Uri.withAppendedPath(musicUri, "" + id);
//                values.put(MediaStore.Audio.Media.TITLE, title);
                values.put(MediaStore.Audio.Media.DATA, renamedFilePath);
                String audioId = "" + id;
                context.getContentResolver().update(musicUri, values, MediaStore.Audio.Media._ID
                        + "= ?", new String[]{audioId});
                break;

            case 2:
                Uri videoUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
//                newUri = Uri.withAppendedPath(musicUri, "" + id);

//                values.put(MediaStore.Video.Media.TITLE, title);
                values.put(MediaStore.Video.Media.DATA, renamedFilePath);
                String videoId = "" + id;
                context.getContentResolver().update(videoUri, values, MediaStore.Video.Media._ID
                        + "= ?", new String[]{videoId});
                break;

            case 3:
                Uri imageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
//                newUri = Uri.withAppendedPath(musicUri, "" + id);
//                values.put(MediaStore.Images.Media.TITLE, title);

                values.put(MediaStore.Images.Media.DATA, renamedFilePath);
                String imageId = "" + id;
                context.getContentResolver().update(imageUri, values, MediaStore.Images.Media._ID
                        + "= ?", new String[]{imageId});
                break;

            case 4:
                Uri filesUri = MediaStore.Files.getContentUri("external");
                values.put(MediaStore.Files.FileColumns.DATA, renamedFilePath);
                String fileId = "" + id;
                context.getContentResolver().update(filesUri, values, MediaStore.Files.FileColumns._ID
                        + "= ?", new String[]{fileId});
                break;

            default:
                break;
        }
    }

    public static long getSize(File file) {

        long size = 0;
        long len = 0;
        if (file.isFile()) {
            size = file.length();
        } else if (file.isDirectory()) {
//            size = org.apache.commons.io.FileUtils.sizeOfDirectory(file);
            File[] list = file.listFiles();
            if (list != null) {
                size = list.length;
            }
//
//            for (int j = 0; j < len; j++) {
//                if (list[j].isFile()) {
//                    size = size + list[j].length();
//                } else if (list[j].isDirectory()) {
//                    size = size + getSize(list[j]);
//
//                }
//
//            }

        }
        return size;
    }


    /**
     * @param zipName
     * @param toDir
     * @param fromDir
     */
    public void extractZipFilesFromDir(String zipName, String toDir, String fromDir) {
        if (!(toDir.charAt(toDir.length() - 1) == '/'))
            toDir += "/";
        if (!(fromDir.charAt(fromDir.length() - 1) == '/'))
            fromDir += "/";

        String org_path = fromDir + zipName;

        extractZipFiles(org_path, toDir);
    }

    /**
     * @param zip_file
     * @param directory
     */


    public static void extractZipFiles(String zip_file, String directory) {
        byte[] data = new byte[BUFFER];
        String name, path, zipDir;
        ZipEntry entry;
        ZipInputStream zipstream;

        if (!(directory.charAt(directory.length() - 1) == '/'))
            directory += "/";

        if (zip_file.contains("/")) {
            path = zip_file;
            name = path.substring(path.lastIndexOf("/") + 1,
                    path.length() - 4);
            zipDir = directory + name + "/";

        } else {
            path = directory + zip_file;
            name = path.substring(path.lastIndexOf("/") + 1,
                    path.length() - 4);
            zipDir = directory + name + "/";
        }

        new File(zipDir).mkdir();

        try {
            zipstream = new ZipInputStream(new FileInputStream(path));

            while ((entry = zipstream.getNextEntry()) != null) {
                String buildDir = zipDir;
                String[] dirs = entry.getName().split("/");

                if (dirs != null && dirs.length > 0) {
                    for (int i = 0; i < dirs.length - 1; i++) {
                        buildDir += dirs[i] + "/";
                        new File(buildDir).mkdir();
                    }
                }

                int read = 0;
                FileOutputStream out = new FileOutputStream(
                        zipDir + entry.getName());
                while ((read = zipstream.read(data, 0, BUFFER)) != -1)
                    out.write(data, 0, read);

                zipstream.closeEntry();
                out.close();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static OutputStream getOutputStream(@NonNull final File target, Context context, long s) throws Exception {

        OutputStream outStream = null;
        try {
            // First try the normal way
            if (isWritable(target)) {
                // standard way
                outStream = new FileOutputStream(target);
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    // Storage Access Framework
                    DocumentFile targetDocument = getDocumentFile(target, false, context);
                    outStream =
                            context.getContentResolver().openOutputStream(targetDocument.getUri());
                } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
                    // Workaround for Kitkat ext SD card
                    return getOutputStream(context, target.getPath());
                }


            }
        } catch (Exception e) {
            Log.e("AmazeFileUtils",
                    "Error when copying file from " + target.getAbsolutePath(), e);
        }
        return outStream;
    }


    /**
     * Check is a file is writable. Detects write issues on external SD card.
     *
     * @param file The file
     * @return true if the file is writable.
     */
    public static final boolean isWritable(final File file) {
        if (file == null)
            return false;
        boolean isExisting = file.exists();

        try {
            FileOutputStream output = new FileOutputStream(file, true);
            try {
                output.close();
            } catch (IOException e) {
                // do nothing.
            }
        } catch (FileNotFoundException e) {
            return false;
        }
        boolean result = file.canWrite();

        // Ensure that file is not created during this process.
        if (!isExisting) {
            file.delete();
        }

        return result;
    }


    /**
     * Get a DocumentFile corresponding to the given file (for writing on ExtSdCard on Android 5). If the file is not
     * existing, it is created.
     *
     * @param file        The file.
     * @param isDirectory flag indicating if the file should be a directory.
     * @return The DocumentFile
     */
    public static DocumentFile getDocumentFile(final File file, final boolean isDirectory, Context context) {
        String baseFolder = getExtSdCardFolder(file, context);
        boolean originalDirectory = false;
        if (baseFolder == null) {
            return null;
        }

        String relativePath = null;
        try {
            String fullPath = file.getCanonicalPath();
            if (!baseFolder.equals(fullPath))
                relativePath = fullPath.substring(baseFolder.length() + 1);
            else originalDirectory = true;
        } catch (IOException e) {
            return null;
        } catch (Exception f) {
            originalDirectory = true;
            //continue
        }
        String as = PreferenceManager.getDefaultSharedPreferences(context).getString("URI", null);

        Uri treeUri = null;
        if (as != null) treeUri = Uri.parse(as);
        if (treeUri == null) {
            return null;
        }

        // start with root of SD card and then parse through document tree.
        DocumentFile document = DocumentFile.fromTreeUri(context, treeUri);
        if (originalDirectory) return document;
        String[] parts = relativePath.split("\\/");
        for (int i = 0; i < parts.length; i++) {
            DocumentFile nextDocument = document.findFile(parts[i]);

            if (nextDocument == null) {
                if ((i < parts.length - 1) || isDirectory) {
                    nextDocument = document.createDirectory(parts[i]);
                } else {
                    nextDocument = document.createFile("image", parts[i]);
                }
            }
            document = nextDocument;
        }

        return document;
    }

    public static OutputStream getOutputStream(Context context, String str) {
        OutputStream outputStream = null;
        Uri fileUri = getUriFromFile(str, context);
        if (fileUri != null) {
            try {
                outputStream = context.getContentResolver().openOutputStream(fileUri);
            } catch (Throwable th) {
            }
        }
        return outputStream;
    }

    public static Uri getUriFromFile(final String path, Context context) {
        ContentResolver resolver = context.getContentResolver();

        Cursor filecursor = resolver.query(MediaStore.Files.getContentUri("external"),
                new String[]{BaseColumns._ID}, MediaStore.MediaColumns.DATA + " = ?",
                new String[]{path}, MediaStore.MediaColumns.DATE_ADDED + " desc");

        if (filecursor != null) {
            filecursor.moveToFirst();
            if (filecursor.isAfterLast()) {
                filecursor.close();
                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.DATA, path);
                return resolver.insert(MediaStore.Files.getContentUri("external"), values);
            } else {
                int imageId = filecursor.getInt(filecursor.getColumnIndex(BaseColumns._ID));
                Uri uri = MediaStore.Files.getContentUri("external").buildUpon().appendPath(
                        Integer.toString(imageId)).build();
                filecursor.close();
                return uri;
            }
        }
        return null;

    }


    /**
     * Determine the main folder of the external SD card containing the given file.
     *
     * @param file the file.
     * @return The main folder of the external SD card containing this file, if the file is on an SD card. Otherwise,
     * null is returned.
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String getExtSdCardFolder(final File file, Context context) {
        String[] extSdPaths = getExtSdCardPaths(context);
        try {
            for (int i = 0; i < extSdPaths.length; i++) {
                if (file.getCanonicalPath().startsWith(extSdPaths[i])) {
                    return extSdPaths[i];
                }
            }
        } catch (IOException e) {
            return null;
        }
        return null;
    }

    /**
     * Get a list of external SD card paths. (Kitkat or higher.)
     *
     * @return A list of external SD card paths.
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String[] getExtSdCardPaths(Context context) {
        List<String> paths = new ArrayList<String>();
        for (File file : context.getExternalFilesDirs("external")) {
            if (file != null && !file.equals(context.getExternalFilesDir("external"))) {
                int index = file.getAbsolutePath().lastIndexOf("/Android/data");
                if (index < 0) {
                    Log.w("AmazeFileUtils", "Unexpected external file dir: " + file.getAbsolutePath());
                } else {
                    String path = file.getAbsolutePath().substring(0, index);
                    try {
                        path = new File(path).getCanonicalPath();
                    } catch (IOException e) {
                        // Keep non-canonical path.
                    }
                    paths.add(path);
                }
            }
        }
        if (paths.isEmpty()) paths.add("/storage/sdcard1");
        return paths.toArray(new String[0]);
    }


    /**
     * @param path
     */


    public static int createZipFile(String path) {

        File dir = new File(path);

        File parent = dir.getParentFile();
        String filepath = parent.getAbsolutePath();
        String[] list = dir.list();
        String name;
        if (dir.isDirectory()) {
            name = path.substring(path.lastIndexOf("/"), path.length());
        } else {
            name = path.substring(path.lastIndexOf("/"), path.lastIndexOf("."));
        }
        String _path;

        if (!dir.canRead() || !dir.canWrite())
            return -1;

        int len = 1;
        if (list != null) {
            len = list.length;
        }

        if (path.charAt(path.length() - 1) != '/')
            _path = path + "/";
        else
            _path = path;

        try {
            ZipOutputStream zip_out = new ZipOutputStream(
                    new BufferedOutputStream(
                            new FileOutputStream(filepath + name + ".zip"), BUFFER));

            for (int i = 0; i < len; i++) {
                if (list != null) {
                    zip_folder(new File(_path + list[i]), zip_out);
                } else {
                    zip_folder(new File(_path), zip_out);
                }
            }

            zip_out.close();

        } catch (FileNotFoundException e) {
            Log.e("File not found", e.getMessage());
            return -1;

        } catch (IOException e) {
            Log.e("IOException", e.getMessage());
            return -1;
        }

        return 0;
    }


    /**
     * @param filePath
     * @param newName
     * @return
     */
    public static int renameTarget(String filePath, String newName) {
        File src = new File(filePath);
        String ext = "";
        File dest;

        if (src.isFile())
            /*get file extension*/
            ext = filePath.substring(filePath.lastIndexOf("."), filePath.length());

        if (newName.length() < 1)
            return -1;

        String temp = filePath.substring(0, filePath.lastIndexOf("/"));

        dest = new File(temp + "/" + newName);
        if (src.renameTo(dest))
            return 0;
        else
            return -1;
    }

    /**
     * @param path
     * @param name
     * @return
     */
    public static int createDir(String path, String name) {
        int len = path.length();

        if (len < 1 || len < 1)
            return -1;

        if (path.charAt(len - 1) != '/')
            path += "/";

        File newFolder = new File(path + name);
        if (newFolder.exists()) {
            return -2;
        }
        if (newFolder.mkdir())
            return 0;

        return -1;
    }

    public static int createFile(String path, String name) {

        int len = path.length();

        if (len < 1 || len < 1)
            return -1;

        if (path.charAt(len - 1) != '/')
            path += "/";

        File newFile = new File(path + name);
        if (newFile.exists())
            return -2;

        try {
            if (newFile.createNewFile())
                return 0;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

        }

        return -1;


    }

    /**
     * The full path name of the file to delete.
     *
     * @param path name
     * @return
     */
    public static int deleteTarget(String path) {
        File target = new File(path);


        if (target.exists() && target.isFile() && target.canWrite()) {
            target.delete();
            return 0;
        } else if (target.exists() && target.isDirectory() && target.canRead()) {
            String[] file_list = target.list();

            if (file_list != null && file_list.length == 0) {
                target.delete();
                return 0;

            } else if (file_list != null && file_list.length > 0) {

                for (int i = 0; i < file_list.length; i++) {
                    File temp_f = new File(target.getAbsolutePath() + "/" + file_list[i]);

                    if (temp_f.isDirectory())
                        deleteTarget(temp_f.getAbsolutePath());
                    else if (temp_f.isFile())
                        temp_f.delete();
                }
            }
            if (target.exists())
                if (target.delete())
                    return 0;
        }
        return -1;
    }

    public static Uri getContentUriForDelete(Context context, String filePath, int category) {
        Uri uri = null;
        switch (category) {
            case 1:
                uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                break;
            case 2:
                uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                break;
            case 3:
                uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                break;
            case 4:
            case 7:
            case 9:
            case 11:
                uri = MediaStore.Files.getContentUri("external");
                ;
                break;

        }
        File audioFile = new File(filePath);
        Cursor cursor = context.getContentResolver().query(
                uri, null,
                MediaStore.MediaColumns.DATA + "=? ", new String[]{filePath}, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {

                int id = cursor.getInt(cursor.getColumnIndex(BaseColumns._ID));
//                Uri baseUri = Uri.parse("content://media/external/audio/media");
                cursor.close();
                return Uri.withAppendedPath(uri, "" + id);
            }
            cursor.close();
        }
/*        if (audioFile.exists()) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DATA, filePath);
            return context.getContentResolver().insert(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values);
        }*/
        return null;
    }


    /**
     * converts integer from wifi manager to an IP address.
     *
     * @param ip
     * @return
     */
    public static String integerToIPAddress(int ip) {
        String ascii_address = "";
        int[] num = new int[4];

        num[0] = (ip & 0xff000000) >> 24;
        num[1] = (ip & 0x00ff0000) >> 16;
        num[2] = (ip & 0x0000ff00) >> 8;
        num[3] = ip & 0x000000ff;

        ascii_address = num[0] + "." + num[1] + "." + num[2] + "." + num[3];

        return ascii_address;
    }

    /**
     * @param dir
     * @param fileName
     * @return
     */
    public static ArrayList<String> searchInDirectory(String dir, String fileName) {
        ArrayList<String> names = new ArrayList<String>();
        search_file(dir, fileName, names);

        return names;
    }

    public static ArrayList<String> Ultrasearch(String dir, String fileName) {
        ArrayList<String> names = new ArrayList<String>();
        Ultrasearch_file(dir, fileName, names);

        return names;
    }


    /*
     *
     * @param file
     * @param zout
     * @throws IOException
     */
    private static void zip_folder(File file, ZipOutputStream zout) throws ZipException, IOException {
        byte[] data = new byte[BUFFER];
        int read;

        if (file.isFile()) {
            ZipEntry entry = new ZipEntry(file.getName());
            zout.putNextEntry(entry);
            BufferedInputStream instream = new BufferedInputStream(
                    new FileInputStream(file));

            while ((read = instream.read(data, 0, BUFFER)) != -1)
                zout.write(data, 0, read);

            zout.closeEntry();
            instream.close();

        } else if (file.isDirectory()) {
            String[] list = file.list();
            int len = list.length;

            for (int i = 0; i < len; i++)
                zip_folder(new File(file.getPath() + "/" + list[i]), zout);
        }
    }

	/*
     *
	 * @param path
	 */


    // Inspired by org.apache.commons.io.FileUtils.isSymlink()
    private static boolean isSymlink(File file) throws IOException {
        File fileInCanonicalDir = null;
        if (file.getParent() == null) {
            fileInCanonicalDir = file;
        } else {
            File canonicalDir = file.getParentFile().getCanonicalFile();
            fileInCanonicalDir = new File(canonicalDir, file.getName());
        }
        return !fileInCanonicalDir.getCanonicalFile().equals(fileInCanonicalDir.getAbsoluteFile());
    }

    public static Drawable getAppIcon(Context context, String url) {


        Drawable icon;
        String filePath = url;
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageArchiveInfo(filePath, PackageManager
                    .GET_ACTIVITIES);
            ApplicationInfo appInfo = packageInfo.applicationInfo;
            appInfo.sourceDir = filePath;
            appInfo.publicSourceDir = filePath;

            icon = appInfo.loadIcon(context.getPackageManager());
            return icon;
        } catch (Exception e) {

            return null;
        }
    }


    public static Drawable getAppIconForFolder(Context context, String packageName) {

        try {
            Drawable d = context.getPackageManager().getApplicationIcon(packageName);
            return d;
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }

    }


    /*
     * (non-JavaDoc)
     * I dont like this method, it needs to be rewritten. Its hacky in that
     * if you are searching in the root dir (/) then it is not going to be treated
     * as a recursive method so the user dosen't have to sit forever and wait.
     *
     * I will rewrite this ugly method.
     *
     * @param dir		directory to search in
     * @param fileName	filename that is being searched for
     * @param n			ArrayList to populate results
     *
     */
    private static void search_file(String dir, String fileName, ArrayList<String> n) {
        File root_dir = new File(dir);
        String[] list = root_dir.list();

        if (list != null && root_dir.canRead()) {
            int len = list.length;

            for (int i = 0; i < len; i++) {
                File check = new File(dir + "/" + list[i]);
                String name = check.getName();

                if (check.isFile() && name.toLowerCase().
                        contains(fileName.toLowerCase())) {
                    n.add(check.getPath());
                } else if (check.isDirectory()) {
                    if (name.toLowerCase().contains(fileName.toLowerCase())) {
                        n.add(check.getPath());

                    } else if (check.canRead() && !dir.equals("/"))
                        search_file(check.getAbsolutePath(), fileName, n);
                }
            }
        }
    }

    public static int getFileCount(File file) {
        fileCount = 0;
        calculateFileCount(file);
        return fileCount;
    }

    private static void calculateFileCount(File file) {
        if (!file.isDirectory()) {
            fileCount++;
            return;
        }
        if (file.list() == null) {
            return;
        }
        for (String fileName : file.list()) {
            File f = new File(file.getAbsolutePath() + File.separator + fileName);
            calculateFileCount(f);
        }
    }


    public static ArrayList<File> getDuplicates(File file) {
        ArrayList<File> dupfiles = new ArrayList<File>();
        HashMap<Long, ArrayList<String>> lists = new HashMap<Long, ArrayList<String>>();
        Find(file.getAbsolutePath(), lists);
        for (ArrayList<String> list : lists.values()) {

            if (list.size() > 1) {

                for (String filepath : list) {

                    dupfiles.add(new File(filepath));

                }

            }
        }
        return dupfiles;

    }

    public static void Find(String dir, HashMap<Long, ArrayList<String>> lists) {
        File root_dir = new File(dir);
        String[] filelist = root_dir.list();

        if (filelist != null && root_dir.canRead()) {
            int len = filelist.length;

            for (int i = 0; i < len; i++) {
                File check = new File(dir + "/" + filelist[i]);

                if (check.isFile()) {
                    long length = check.length();
                    ArrayList<String> list = lists.get(length);
                    if (list == null) {
                        list = new ArrayList<String>();
                        lists.put(length, list);
                    }
                    list.add(check.getAbsolutePath());
                } else if (check.isDirectory()) {
                    Find(check.getAbsolutePath(), lists);
                }
            }
        }
    }

    public boolean CompareSize(File file1, File file2) {
        if (file1.length() == file2.length())
            return true;
        else
            return false;

    }

    private static void Ultrasearch_file(String dir, String fileName, ArrayList<String> n) {
        File root_dir = new File(dir);
        String[] list = root_dir.list();

        if (list != null && root_dir.canRead()) {
            int len = list.length;

            for (int i = 0; i < len; i++) {
                File check = new File(dir + "/" + list[i]);
                String name = check.getName();

                if (check.isFile() && name.toLowerCase().
                        contains(fileName.toLowerCase())) {
                    n.add(check.getPath());
                } else if (check.isDirectory() && !check.getName().startsWith(".")) {
                    if (name.toLowerCase().contains(fileName.toLowerCase()))
                        n.add(check.getPath());

                }
            }
        }
    }

    /**
     * Whether the URI is a local one.
     *
     * @param uri
     * @return
     */
    public static boolean isLocal(String uri) {
        if (uri != null && !uri.startsWith("http://")) {
            return true;
        }
        return false;
    }

    /**
     * Gets the extension of a file name, like ".png" or ".jpg".
     *
     * @param uri
     * @return Extension including the dot("."); "" if there is no extension;
     * null if uri was null.
     */
    public static String getExtension(String uri) {
        if (uri == null) {
            return null;
        }

        int dot = uri.lastIndexOf(".");
        if (dot >= 0) {
            return uri.substring(dot);
        } else {
            // No extension.
            return "";
        }
    }

//        /**
//         * Returns true if uri is a media uri.
//         *
//         * @param uri
//         * @return
//         */
//        public static boolean isMediaUri(String uri) {
//            if (uri.startsWith(Audio.Media.INTERNAL_CONTENT_URI.toString())
//                    || uri.startsWith(Audio.Media.EXTERNAL_CONTENT_URI.toString())
//                    || uri.startsWith(Video.Media.INTERNAL_CONTENT_URI.toString())
//                    || uri.startsWith(Video.Media.EXTERNAL_CONTENT_URI.toString())) {
//                return true;
//            } else {
//                return false;
//            }
//        }

    /**
     * Convert File into Uri.
     *
     * @param file
     * @return uri
     */
    public static Uri getUri(File file) {
        if (file != null) {
            return Uri.fromFile(file);
        }
        return null;
    }

    /**
     * Convert Uri into File.
     *
     * @param uri
     * @return file
     */
    public static File getFile(Uri uri) {
        if (uri != null) {
            String filepath = uri.getPath();
            if (filepath != null) {
                return new File(filepath);
            }
        }
        return null;
    }

    /**
     * Returns the path only (without file name).
     *
     * @param file
     * @return
     */
    public static File getPathWithoutFilename(File file) {
        if (file != null) {
            if (file.isDirectory()) {
                // no file to be split off. Return everything
                return file;
            } else {
                String filename = file.getName();
                String filepath = file.getAbsolutePath();

                // Construct path without file name.
                String pathwithoutname = filepath.substring(0, filepath.length() - filename.length());
                if (pathwithoutname.endsWith("/")) {
                    pathwithoutname = pathwithoutname.substring(0, pathwithoutname.length() - 1);
                }
                return new File(pathwithoutname);
            }
        }
        return null;
    }

    /**
     * Constructs a file from a path and file name.
     *
     * @param curdir
     * @param file
     * @return
     */


    public static File getFile(String curdir, String file) {
        String separator = "/";
        if (curdir.endsWith("/")) {
            separator = "";
        }
        File clickedFile = new File(curdir + separator
                + file);
        return clickedFile;
    }

    public static File getFile(File curdir, String file) {
        return getFile(curdir.getAbsolutePath(), file);
    }

    public static String formatSize(Context context, long sizeInBytes) {
        return Formatter.formatFileSize(context, sizeInBytes);
    }

 /*       public static String formatDate(Context context, long dateTime) {
            return DateFormat.getDateFormat(context).format(new Date(dateTime));
        }*/


    /**
     * @param f - file which need be checked
     * @return if is archive - returns true othewise
     */
    public static boolean checkIfZipArchive(File f) {
        try {
            new ZipFile(f);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

//        public static String getMD5(String filepath){
//            try {
//                return MD5.asHex(MD5.getHash(new File(filepath)));
//            } catch (IOException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//                return null;
//            }
//        }

    public static String getFastHash(String filepath) {
        MessageDigest md;
        String hash;
        File file = new File(filepath);
        try {
            try {
                md = MessageDigest.getInstance("MD5");

            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException("cannot initialize MD5 hash function", e);
            }
            FileInputStream fin = new FileInputStream(file);
            if (file.length() > 1048576L) {
                byte data[] = new byte[(int) file.length() / 100];
                fin.read(data);
                fin.close();
                hash = new BigInteger(1, md.digest(data)).toString(16);
            } else if (file.length() > 1024L) {
                byte data[] = new byte[(int) file.length() / 10];
                fin.read(data);
                fin.close();
                hash = new BigInteger(1, md.digest(data)).toString(16);
            } else {
                byte data[] = new byte[(int) file.length()];
                fin.read(data);
                fin.close();
                hash = new BigInteger(1, md.digest(data)).toString(16);
            }
        } catch (IOException e) {
            // TODO: handle exception
            throw new RuntimeException("cannot read file " + file.getAbsolutePath(), e);

        }


        return hash;
    }

    public static void sortFile(String[] fileNames) {
        Arrays.sort(fileNames, Collator.getInstance());
    }

    private static class FileComparator implements Comparator<File> {
        private Collator c = Collator.getInstance();

        public int compare(File f1, File f2) {
            if (f1 == f2)
                return 0;

            if (f1.isDirectory() && f2.isFile())
                return -1;
            if (f1.isFile() && f2.isDirectory())
                return 1;

            return c.compare(f1.getName(), f2.getName());
        }
    }


        /*public static Bitmap getBitmap(Context context, File imageFile, int size) {
            if(!imageFile.exists() || imageFile.isDirectory()){
                return BitmapFactory.decodeResource(context.getResources(), R.drawable.image);
            }
            InputStream photoStream = null;
            Bitmap mBitmap = null;
            try {
                photoStream = new FileInputStream(imageFile);
                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inJustDecodeBounds = true;
                opts.inSampleSize = 1;

                mBitmap = BitmapFactory.decodeStream(photoStream, null, opts);
                if (opts.outWidth > opts.outHeight && opts.outWidth > size) {
                    opts.inSampleSize = opts.outWidth / size;
                } else if (opts.outWidth < opts.outHeight && opts.outHeight > size) {
                    opts.inSampleSize = opts.outHeight / size;
                }
                if (opts.inSampleSize < 1) {
                    opts.inSampleSize = 1;
                }
                opts.inJustDecodeBounds = false;
                photoStream.close();
                photoStream = new FileInputStream(imageFile);
                mBitmap = BitmapFactory.decodeStream(photoStream, null, opts);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (photoStream != null) {
                    try {
                        photoStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            if(mBitmap==null){
                return BitmapFactory.decodeResource(context.getResources(), R.drawable.image);
            }
            return mBitmap;
        }
*/


    public synchronized static void printDebug(String str) {
        if (isDebug) {
            System.out.println(str);
        }
    }

      /*  public void getOnClick(String filepath){

            final File file = new File(filepath);
            final String ext = FileUtils.getExtension(filepath);

            if (ext.equalsIgnoreCase(".pdf"))
            {
                Intent pdfIntent = new Intent();
                pdfIntent.setAction(android.content.Intent.ACTION_VIEW);
                pdfIntent.setDataAndType(Uri.fromFile(file), "application/pdf");
                try{
                    activity.startActivity(pdfIntent);
                }catch (Exception e) {
                    // TODO: handle exception
                    final Intent intent = new Intent(activity, PDFViewer.class);
                    intent.putExtra(PdfViewerActivity.EXTRA_PDFFILENAME, filepath);
                    activity.startActivity(intent);

                    //showMessage("couldn't find a PDF viewer");
                }
            }

            else {
                String mimeType = MimeTypes.getMimeType(file.getName());
                Intent myIntent = new Intent();
                myIntent.setAction(android.content.Intent.ACTION_VIEW);
                myIntent.setDataAndType(Uri.fromFile(file), mimeType);
                try {
                    activity.startActivity(myIntent);
                }catch (Exception e) {
                    // TODO: handle exception
                    Toast.makeText(activity, "No application to open  file",
                            Toast.LENGTH_SHORT).show();
                }



            }

        }
*/


}
