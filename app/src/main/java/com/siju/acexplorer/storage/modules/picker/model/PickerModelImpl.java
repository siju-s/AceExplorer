package com.siju.acexplorer.storage.modules.picker.model;

import android.os.Process;

import java.util.List;

import static com.siju.acexplorer.model.StorageUtils.getStorageDirectories;

/**
 * Created by sj on 20/09/17.
 */

public class PickerModelImpl implements PickerModel {

    private PickerModel.Listener listener;

    @Override
    public void getStoragesList() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                List<String> list = getStorageDirectories();
                listener.onStoragesFetched(list);
            }
        }).start();
    }

    @Override
    public void setListener(Listener listener) {
        this.listener = listener;
    }
}
