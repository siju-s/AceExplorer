package com.siju.acexplorer.trash;


import android.util.Log;

import com.siju.acexplorer.AceApplication;
import com.siju.acexplorer.trash.database.DatabaseHelper;

import java.util.List;

public class TrashDbHelper {

    private static final String TAG               = "TrashDbHelper";
    private static TrashDbHelper trashDbHelper = new TrashDbHelper();
    private DatabaseHelper databaseHelper;

    private TrashDbHelper() {
        databaseHelper = new DatabaseHelper(AceApplication.getAppContext());
    }

    public static  TrashDbHelper getInstance() {
        return trashDbHelper;
    }

    public void addTrashData(TrashModel trashModel) {
        Log.d(TAG, "addTrashData: dest:"+trashModel.getDestination() + "source:"+trashModel.getSource());
        databaseHelper.addTrashData(trashModel);
    }

    public void addBulkTrashData(List<TrashModel> trashModel) {
        databaseHelper.addBulkTrashData(trashModel);
    }

    public TrashModel readTrashData(String destination) {
        return databaseHelper.readTrashData(destination);
    }

    public List<TrashModel> readMultipleTrashData(String[] destination) {
        return databaseHelper.readMultipleTrashData(destination);
    }

    public int deleteTrashData(String destination) {
        return databaseHelper.deleteTrashData(destination);
    }

}
