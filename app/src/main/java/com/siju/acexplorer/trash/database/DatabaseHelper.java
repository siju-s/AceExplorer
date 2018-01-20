package com.siju.acexplorer.trash.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.siju.acexplorer.trash.TrashModel;

import java.util.ArrayList;
import java.util.List;


public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME    = "ace.db";
    private static final int    DATABASE_VERSION = 1;
    private static final String TRASH_TABLE_NAME = "Trash";
    private final        String text             = " TEXT";
    private static final String KEY_ID           = "id";
    private static final String KEY_DESTINATION  = "dest";
    private static final String KEY_SOURCE       = "source";


    private final String CREATE_TRASH = "CREATE TABLE IF NOT EXISTS " + TRASH_TABLE_NAME + " (" +
            KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            KEY_DESTINATION + " TEXT," +
            KEY_SOURCE + " TEXT" + ");";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TRASH);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TRASH_TABLE_NAME);
        onCreate(db);
    }


    public void addTrashData(TrashModel trashModel) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_DESTINATION, trashModel.getDestination());
        values.put(KEY_SOURCE, trashModel.getSource());

        // Inserting Row
        db.replace(TRASH_TABLE_NAME, null, values);
        db.close();
    }


    public void addBulkTrashData(List<TrashModel> trashModel) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            for (TrashModel trashModel1 : trashModel) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(KEY_DESTINATION, trashModel1.getDestination());
                contentValues.put(KEY_SOURCE, trashModel1.getSource());
                db.replace(TRASH_TABLE_NAME, null, contentValues);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public TrashModel readTrashData(String destination) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TRASH_TABLE_NAME, null, KEY_DESTINATION + "=?",
                                 new String[]{destination}, null, null, null, null);
        if (cursor == null) {
            return null;
        }
        TrashModel trashModel = null;
        if (cursor.moveToFirst()) {

            trashModel = new TrashModel(cursor.getString(1),
                                        cursor.getString(2));
        }
        cursor.close();
        return trashModel;
    }

    public List<TrashModel> readMultipleTrashData(String[] destination) {
        SQLiteDatabase db = this.getReadableDatabase();

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < destination.length; i++) {
            stringBuilder.append("'");
            stringBuilder.append(destination[i]);
            stringBuilder.append("'");
            if (i < destination.length - 1) {
                stringBuilder.append(",");
            }
        }
        Log.d("DbHelper", "readMultipleTrashData: " + stringBuilder.toString());

        Cursor cursor = db.query(TRASH_TABLE_NAME, null, KEY_DESTINATION + " IN " + "(" + stringBuilder.toString()
                + ")", null, null, null, null, null);
        if (cursor == null) {
            return null;
        }
        List<TrashModel> trashModel = new ArrayList<>();
        if (cursor.moveToFirst()) {

            do {
                TrashModel trash = new TrashModel(cursor.getString(1),
                                                  cursor.getString(2));
                trashModel.add(trash);

            } while (cursor.moveToNext());
        }
        cursor.close();
        return trashModel;
    }


    public int deleteTrashData(String destination) {
        SQLiteDatabase db = this.getWritableDatabase();
        int deleted = db.delete(TRASH_TABLE_NAME, KEY_DESTINATION + " = ?",
                                new String[]{destination});
        db.close();
        return deleted;
    }


    public void deleteBulkTrashData(String[] destination) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TRASH_TABLE_NAME, KEY_DESTINATION + " IN",
                  destination);
        db.close();
    }


}
