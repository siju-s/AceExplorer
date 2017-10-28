package com.siju.acexplorer.storage.modules.picker.presenter;

/**
 * Created by sj on 20/09/17.
 */

public interface PickerPresenter {
    void loadData(String path, boolean isRingtonePicker);

    void getStoragesList();

    void saveLastRingtoneDir(String currentPath);

    String getLastSavedRingtoneDir();
}
