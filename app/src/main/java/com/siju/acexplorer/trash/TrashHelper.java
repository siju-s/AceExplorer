package com.siju.acexplorer.trash;


import android.content.Context;
import android.util.JsonReader;
import android.util.JsonWriter;
import android.util.Log;

import com.siju.acexplorer.AceApplication;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;

public class TrashHelper {

    private static final String TRASH_DIR       = "Trash";
    private static final String TRASH_INFO_FILE = "TrashInfo.json";


    public static String getTrashDir(Context context) {
        File filesDir = new File(context.getFilesDir(), TRASH_DIR);
        Log.d("TrashHelper", "getTrashDir: canwrite:" + filesDir.canWrite());

        return filesDir.getAbsolutePath();
    }

    private static String getTrashInfoFile(Context context) {
        return getTrashDir(context) + "/" + TRASH_INFO_FILE;
    }

    public static void createTrashDir(Context context) {
        File filesDir = context.getFilesDir();
        File trashDir;
        if (filesDir.canWrite()) {
            trashDir = new File(filesDir, TRASH_DIR);
            if (!trashDir.exists()) {
                boolean isCreated = trashDir.mkdirs();
                if (isCreated) {
                    File trashInfo = new File(trashDir, TRASH_INFO_FILE);
                    try {
                        isCreated = trashInfo.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                Log.d("TrashHelper", "createTrashDir: " + isCreated + " canwrite:" + trashDir.canWrite());
            }
        }
    }

    public static boolean isTrashDir(Context context, String dir) {
        return dir.contains(getTrashDir(context));
    }


    public static void storeFileInfo(List<String> destinationPath, List<String> sourcePath) {
        OutputStream out;
        try {
            out = new FileOutputStream(getTrashInfoFile(AceApplication.getAppContext()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }
        JsonWriter writer;
        try {
            writer = new JsonWriter(new OutputStreamWriter(out, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return;
        }
        try {
            writer.beginArray();
            writer.beginObject();
            for (int i = 0; i < destinationPath.size(); i++) {
                String value = new File(sourcePath.get(i)).getParent();
                writer.name(destinationPath.get(i)).value(value);
            }
            writer.endArray();
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void readFileInfo(List<String> destinationPath) {
        HashMap<String, String> pathPair = new HashMap<>();
        InputStream inputStream;
        try {
            inputStream = new FileInputStream(getTrashInfoFile(AceApplication.getAppContext()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }
        JsonReader reader;

            reader = new JsonReader(new InputStreamReader(inputStream));

//
//        try {
//            reader.beginArray();
//            reader.beginObject();
//            while(reader.hasNext()) {
//                String name = reader.nextName();
//                if (destinationPath.c)
//            }
//            for (int i = 0; i < destinationPath.size(); i++) {
//                String value = new File(sourcePath.get(i)).getParent();
//                writer.name(destinationPath.get(i)).value(value);
//            }
//            writer.endArray();
//            writer.close();

//        } catch (IOException e) {
//            e.printStackTrace();
//        }

    }






}
