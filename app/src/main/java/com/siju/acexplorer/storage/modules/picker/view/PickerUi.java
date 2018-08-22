package com.siju.acexplorer.storage.modules.picker.view;

import com.siju.acexplorer.common.types.FileInfo;

import java.util.ArrayList;
import java.util.List;


public interface PickerUi {

    void onDataLoaded(ArrayList<FileInfo> data);

    void setListener(Listener listener);

    void onStoragesFetched(List<String> storagesList);

    interface Listener {

    }
}
