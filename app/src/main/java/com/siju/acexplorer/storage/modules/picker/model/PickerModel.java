package com.siju.acexplorer.storage.modules.picker.model;

import java.util.List;

/**
 * Created by sj on 20/09/17.
 */

public interface PickerModel {

    void getStoragesList();

    void setListener(Listener listener);

    void saveLastRingtoneDir(String currentPath);

    String getLastSavedRingtoneDir();


    interface Listener {

        void onStoragesFetched(List<String> storagesList);

    }
}
