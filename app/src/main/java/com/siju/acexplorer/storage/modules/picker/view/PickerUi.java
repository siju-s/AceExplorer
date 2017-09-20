package com.siju.acexplorer.storage.modules.picker.view;

import com.siju.acexplorer.model.FileInfo;
import com.siju.acexplorer.storage.modules.picker.presenter.PickerPresenterImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sj on 20/09/17.
 */

public interface PickerUi {

    void onDataLoaded(ArrayList<FileInfo> data);

    void setListener(Listener listener);

    void onStoragesFetched(List<String> storagesList);

    interface Listener {

    }
}
