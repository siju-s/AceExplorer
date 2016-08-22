package com.siju.acexplorer.filesystem.task;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.provider.MediaStore;

import com.siju.acexplorer.filesystem.model.FileInfo;
import com.siju.acexplorer.filesystem.utils.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.ArrayList;


public class FileVerifier extends Thread {
    ArrayList<FileBundle> arrayList = new ArrayList<>();
    private Context mContext;
    boolean rootmode;
    FileVerifierInterface fileVerifierInterface;
    boolean running = true;

    public FileVerifier(Context context, boolean rootmode, FileVerifierInterface fileVerifierInterface) {
        mContext = context;
        this.rootmode = rootmode;
        this.fileVerifierInterface = fileVerifierInterface;
    }

    @Override
    public void run() {
        super.run();
        while (arrayList.size() > 0 && !isInterrupted()) {
            running = true;
            if (arrayList.get(arrayList.size() - 1) != null) {
                FileBundle fileBundle = arrayList.get(arrayList.size() - 1);
                processFile(fileBundle);
                if (arrayList.contains(fileBundle))
                    arrayList.remove(fileBundle);
            }
        }
        running = false;
    }

    public void add(FileBundle fileBundle) {
        arrayList.add(0, fileBundle);
        if (!isAlive()) {
            start();
        }
    }

    public boolean isRunning() {
        return running;
    }

    void stopTask() {
        arrayList.clear();
        interrupt();
    }

    public interface FileVerifierInterface {
        void addFailedFile(FileInfo fileInfo);

        boolean contains(String a);

        boolean containsDirectory(String a);

        void setCopySuccessful(boolean b);
    }

    void processFile(FileBundle fileBundle) {
        FileInfo sourceFile = fileBundle.getFile(), targetFile = fileBundle.getFile2();
        boolean move = fileBundle.isMove();
        if (sourceFile.isDirectory()) {
            if (move) {
                if (!fileVerifierInterface.containsDirectory(sourceFile.getFilePath())) {
                    FileUtils.delete(mContext, rootmode,sourceFile.getFilePath());
                }
            }
            return;
        }

        FileUtils.scanFile(mContext, targetFile.getFilePath());
        if (!checkNonRootFiles(sourceFile, targetFile)) {
            fileVerifierInterface.addFailedFile(sourceFile);
            fileVerifierInterface.setCopySuccessful(false);
        }
      /*  try {
//            targetFile.setLastModified(sourceFile.lastModified());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } */
        if (move) {
            if (!fileVerifierInterface.contains(sourceFile.getFilePath())) {
                FileUtils.delete(mContext, rootmode,sourceFile.getFilePath());

//                if (sourceFile.isLocal())
                    delete(mContext, sourceFile.getFilePath());
            }
        }
    }

    void delete(final Context context, final String file) {
        final String where = MediaStore.MediaColumns.DATA + "=?";
        final String[] selectionArgs = new String[]{
                file
        };
        final ContentResolver contentResolver = context.getContentResolver();
        final Uri filesUri = MediaStore.Files.getContentUri("external");
        // Delete the entry from the media database. This will actually delete media files.
        contentResolver.delete(filesUri, where, selectionArgs);

    }

    boolean checkNonRootFiles(FileInfo hFile1, FileInfo hFile2) {
        File file1 = new File(hFile1.getFilePath());
        File file2 = new File(hFile2.getFilePath());

        long l1 = file1.length(), l2 = file2.length();
        if (file2.exists() && ((l1 != -1 && l2 != -1) ? l1 == l2 : true)) {
            //after basic checks try checksum if possible
            InputStream inputStream = null, inputStream1 = null;
            try {
                inputStream = new FileInputStream(file1);
                inputStream1 = new FileInputStream(file2);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

/*            InputStream inputStream = hFile1.getInputStream();
            InputStream inputStream1 = hFile2.getInputStream();*/
            if (inputStream == null || inputStream1 == null) return true;
            String md5, md5_1;
            try {
                md5 = getMD5Checksum(inputStream);
                md5_1 = getMD5Checksum(inputStream1);
                if (md5 != null && md5_1 != null && md5.length() > 0 && md5_1.length() > 0) {
                    if (md5.equals(md5_1)) return true;
                    else return false;
                } else return true;
            } catch (Exception e) {
                return true;
            }
        }
        return false;
    }

    public String getMD5Checksum(InputStream filename) throws Exception {
        byte[] b = createChecksum(filename);
        String result = "";

        for (int i = 0; i < b.length; i++) {
            result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
        }
        return result;
    }

    public byte[] createChecksum(InputStream fis) throws Exception {
        byte[] buffer = new byte[8192];
        MessageDigest complete = MessageDigest.getInstance("MD5");
        int numRead;

        do {
            numRead = fis.read(buffer);
            if (numRead > 0) {
                complete.update(buffer, 0, numRead);
            }
        } while (numRead != -1);

        fis.close();
        return complete.digest();
    }
}
