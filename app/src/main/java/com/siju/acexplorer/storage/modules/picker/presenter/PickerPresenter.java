package com.siju.acexplorer.storage.modules.picker.presenter;


public interface PickerPresenter {
    void loadData(String path, boolean isRingtonePicker);

    void getStoragesList();

    void saveLastRingtoneDir(String currentPath);

    String getLastSavedRingtoneDir();
}
