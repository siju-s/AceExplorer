package com.siju.filemanager.filesystem;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.text.format.Formatter;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.siju.filemanager.BaseActivity;
import com.siju.filemanager.R;
import com.siju.filemanager.common.Logger;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

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
                    return null;
                }
            }
        } else {
            return null;
        }

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
            action, BaseActivity.BackGroundOperationsTask.Progress progress) {
        BaseActivity.BackGroundOperationsTask.Progress progressBg = progress;
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
                        progress1 = (int) (value1 * count * 100);
                        if (tempProgress != progress1) {
                            tempProgress = progress1;
                            progress.publish(tempProgress);
//                            System.out.println("Progress==" + progress1 + "File=" + file_name);
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
        if (isMove) {
            // delete the original file
            sourceFile.delete();
        }
        return 0;
    }

    /**
     * View the file in external apps based on Mime Type
     * @param context
     * @param path
     * @param extension
     */
    public static void viewFile(Context context,String path,String extension) {

        Uri uri = Uri.fromFile(new File(path));

        Intent intent = new Intent();
        intent.setAction(android.content.Intent.ACTION_VIEW);
        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        Logger.log("TAG","uri=="+uri+"MIME="+mimeType);
        intent.setDataAndType(uri,mimeType);
        PackageManager packageManager = context.getPackageManager();
        if (intent.resolveActivity(packageManager) != null) {
            Intent chooser = Intent.createChooser(intent,context.getString(R.string.msg_open_file));
            context.startActivity(chooser);
        }
        else {
            showMessage(context,context.getString(R.string.msg_error_not_supported));
        }

    }

    private static void showMessage(Context context,String msg) {
        Toast .makeText(context,msg,Toast.LENGTH_SHORT).show();
    }

    public static void shareFiles(Context context, ArrayList<FileInfo> fileInfo) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND_MULTIPLE);
        intent.setType("*/*");

        ArrayList<Uri> files = new ArrayList<Uri>();

        for (FileInfo info : fileInfo) {
            File file = new File(info.getFilePath());
            Uri uri = Uri.fromFile(file);
            System.out.println("shareuri==" + uri);
            files.add(uri);
        }

        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
        context.startActivity(intent);
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

    /**
     * @param path
     */


    public static int createZipFile(String path) {

        File dir = new File(path);

        File parent = dir.getParentFile();
        String filepath = parent.getAbsolutePath();
        String[] list = dir.list();
        String name = path.substring(path.lastIndexOf("/"), path.length());
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

        if (new File(path + name).mkdir())
            return 0;

        return -1;
    }

    public static int createFile(String path, String name) {

        int len = path.length();

        if (len < 1 || len < 1)
            return -1;

        if (path.charAt(len - 1) != '/')
            path += "/";

        try {
            if (new File(path + name).createNewFile())
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

/*        private static void veiwImage(File file) {
            DisplayMetrics dm = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
            final int minrez = Math.min(dm.widthPixels, dm.heightPixels);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            options.outHeight = options.outWidth = 0;
            options.inSampleSize = 1;
            String path = file.getAbsolutePath();
            BitmapFactory.decodeFile(path, options);
            if (options.outWidth > 0 && options.outHeight > 0) {
                // Now see how much we need to scale it down.
                int widthFactor = (options.outWidth + minrez - 1) / minrez;
                widthFactor = Math.max(widthFactor, (options.outHeight + minrez - 1) / minrez);
                widthFactor = Math.max(widthFactor, 1);
                options.inSampleSize = widthFactor;
                options.inJustDecodeBounds = false;
                ImageView img = new ImageView(activity);
                img.setImageBitmap(BitmapFactory.decodeFile(path, options));
                new AlertDialog.Builder(activity).setTitle("Image Preview").setIcon(R.drawable.image).setView(img)
                .create()
                        .show();
            }
}*/


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
