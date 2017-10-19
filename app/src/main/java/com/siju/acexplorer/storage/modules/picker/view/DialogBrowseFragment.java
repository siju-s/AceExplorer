/*
 * Copyright (C) 2017 Ace Explorer owned by Siju Sakaria
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.siju.acexplorer.storage.modules.picker.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.siju.acexplorer.R;
import com.siju.acexplorer.home.model.LoaderHelper;
import com.siju.acexplorer.logging.Logger;
import com.siju.acexplorer.model.FileInfo;
import com.siju.acexplorer.model.helper.FileUtils;
import com.siju.acexplorer.model.helper.MediaStoreHack;
import com.siju.acexplorer.permission.PermissionHelper;
import com.siju.acexplorer.permission.PermissionResultCallback;
import com.siju.acexplorer.storage.model.ViewMode;
import com.siju.acexplorer.storage.modules.picker.model.PickerModel;
import com.siju.acexplorer.storage.modules.picker.model.PickerModelImpl;
import com.siju.acexplorer.storage.modules.picker.presenter.PickerPresenter;
import com.siju.acexplorer.storage.modules.picker.presenter.PickerPresenterImpl;
import com.siju.acexplorer.storage.view.FileListAdapter;
import com.siju.acexplorer.storage.view.custom.DividerItemDecoration;
import com.siju.acexplorer.storage.view.custom.recyclerview.FastScrollRecyclerView;
import com.siju.acexplorer.theme.Theme;
import com.siju.acexplorer.theme.ThemeUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.siju.acexplorer.model.StorageUtils.getInternalStorage;
import static com.siju.acexplorer.model.groups.Category.FILES;
import static com.siju.acexplorer.model.groups.Category.PICKER;
import static com.siju.acexplorer.model.helper.UriHelper.createContentUri;


public class DialogBrowseFragment extends DialogFragment implements
        PickerUi,
        PermissionResultCallback {

    private final String TAG = this.getClass().getSimpleName();
    private FastScrollRecyclerView recyclerViewFileList;
    private View root;
    private FileListAdapter fileListAdapter;
    private ArrayList<FileInfo> fileInfoList;
    private ArrayList<FileInfo> storagesInfoList;
    private ImageButton mImageButtonBack;
    private TextView textCurrentPath;
    private TextView mTitle;
    private Button okButton;
    private Button cancelButton;
    private String currentPath;
    private boolean isRingtonePicker;
    private boolean isFilePicker;
    private int mRingToneType;
    private Theme currentTheme;
    private static final int MY_PERMISSIONS_REQUEST = 1;
    private boolean mIsBackPressed;
    private LinearLayoutManager llm;
    private final HashMap<String, Bundle> scrollPosition = new HashMap<>();
    private TextView mTextEmpty;
    private List<String> storagesList = new ArrayList<>();
    private boolean mInStoragesList;
    private PermissionHelper permissionHelper;
    private PickerPresenter pickerPresenter;


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new Dialog(getActivity(), getTheme()) {
            @Override
            public void onBackPressed() {
                if (checkIfRootDir()) {
                    reloadData();
                } else {
                    exitPicker();
                }
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.dialog_browse, container, false);
        return root;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        currentTheme = Theme.getTheme(ThemeUtils.getTheme(getContext()));
        Log.d(TAG, "onActivityCreated: ");
        initializeViews();

        if (getArguments() != null) {
            if (getArguments().getBoolean("ringtone_picker")) {
                mTitle.setText(getString(R.string.dialog_title_picker));
                okButton.setVisibility(View.GONE);
                isRingtonePicker = true;
                mRingToneType = getArguments().getInt("ringtone_type");
            } else if (getArguments().getBoolean("file_picker")) {
                okButton.setVisibility(View.GONE);
                isFilePicker = true;
            }
        }

        PickerModel pickerModel = new PickerModelImpl();
        pickerPresenter = new PickerPresenterImpl(this, pickerModel, new LoaderHelper(this),
                getActivity().getSupportLoaderManager());

        loadStoragesList();

        currentPath = getInternalStorage();
        textCurrentPath.setText(currentPath);

        recyclerViewFileList.setItemAnimator(new DefaultItemAnimator());
        fileListAdapter = new FileListAdapter(getContext(), fileInfoList, FILES, ViewMode.LIST);
        recyclerViewFileList.setAdapter(fileListAdapter);

        setListeners();
        setupPermissions();
    }

    private void setListeners() {
        fileListAdapter.setOnItemClickListener(new FileListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Log.d(TAG, "onItemClick: "+" fileList:"+fileInfoList.size());
                File file = new File(fileInfoList.get(position).getFilePath());
                if (file.isDirectory()) {
                    mInStoragesList = false;
                    computeScroll();
                    currentPath = file.getAbsolutePath();
                    textCurrentPath.setText(currentPath);
                    refreshList(currentPath);
                } else if (mInStoragesList) {
                    mInStoragesList = false;
                    File storagesFile = new File(fileInfoList.get(position).getFilePath());
                    currentPath = storagesFile.getAbsolutePath();
                    textCurrentPath.setText(currentPath);
                    refreshList(currentPath);
                } else {
                    if (isRingtonePicker) {
                        Intent intent = new Intent();
                        Uri mediaStoreUri = MediaStoreHack.getCustomRingtoneUri(getActivity().getContentResolver(),
                                file.getAbsolutePath(), mRingToneType);
                        System.out.println(mediaStoreUri + "\t" + FileUtils.getMimeType(file) + "type=" +
                                mRingToneType);

                        if (mediaStoreUri != null) {
                            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, mRingToneType);
                            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI, mediaStoreUri);
                            intent.setData(mediaStoreUri);
                            fileListAdapter.setStopAnimation(false);
                            getActivity().setResult(Activity.RESULT_OK, intent);
                        } else {
                            getActivity().setResult(Activity.RESULT_CANCELED, intent);
                        }
                        getActivity().finish();
                    } else if (isFilePicker) {
                        Intent intent = new Intent();
                        intent.setData(createContentUri(getActivity(), file.getAbsolutePath()));
                        getActivity().setResult(Activity.RESULT_OK, intent);
                        getActivity().finish();
                    }
                }

            }
        });

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent();
                intent.putExtra("PATH", currentPath);
                getTargetFragment().onActivityResult(getTargetRequestCode(), AppCompatActivity.RESULT_OK, intent);
                getDialog().dismiss();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exitPicker();
            }
        });


        mImageButtonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: ");
                mIsBackPressed = true;
                if (storagesList.contains(currentPath)) {
                    if (!mInStoragesList) {
                        mInStoragesList = true;
                        textCurrentPath.setText("");
                        fileInfoList = storagesInfoList;
                        fileListAdapter.setStopAnimation(true);
                        fileListAdapter.updateAdapter(fileInfoList);
                        recyclerViewFileList.setAdapter(fileListAdapter);
                    }
                } else {
                    mInStoragesList = false;
                    currentPath = new File(currentPath).getParent();
                    textCurrentPath.setText(currentPath);
                    refreshList(currentPath);
                }


            }
        });

        getDialog().setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                Logger.log(TAG, "Cancel");
                exitPicker();
            }
        });
    }

    private void setupPermissions() {
        permissionHelper = new PermissionHelper(getActivity(), this);
        permissionHelper.checkPermissions();
    }

    private void exitPicker() {
        getActivity().setResult(AppCompatActivity.RESULT_CANCELED, null);
        if (isRingtonePicker) {
            getActivity().finish();
        } else {
            getDialog().dismiss();
        }
    }

    private boolean checkIfRootDir() {
        return !currentPath.equals(getInternalStorage());
    }


    private void loadStoragesList() {
        pickerPresenter.getStoragesList();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[]
                                                   grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST:
                permissionHelper.onPermissionResult();
                break;
        }
    }


    @Override
    public void onResume() {
        permissionHelper.onResume();
        super.onResume();
    }


    private void initializeViews() {
        recyclerViewFileList = root.findViewById(R.id.recyclerViewFileList);
        recyclerViewFileList.setHasFixedSize(true);
        llm = new LinearLayoutManager(getActivity());
        recyclerViewFileList.setLayoutManager(llm);
        SwipeRefreshLayout mSwipeRefreshLayout = root.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setEnabled(false);
        mTextEmpty = root.findViewById(R.id.textEmpty);
        mImageButtonBack = root.findViewById(R.id.imageButtonBack);
        textCurrentPath = root.findViewById(R.id.textPath);
        okButton = root.findViewById(R.id.buttonOk);
        cancelButton = root.findViewById(R.id.buttonCancel);
        mTitle = root.findViewById(R.id.textDialogTitle);

    }

    private void refreshList(String path) {
        fileInfoList = new ArrayList<>();
        if (fileListAdapter != null) {
            fileListAdapter.clearList();
        }
        Log.d(TAG, "refreshList: "+path);
        pickerPresenter.loadData(path, isRingtonePicker);
    }

    private void reloadData() {
        Log.d(TAG, "reloadData: "+fileInfoList.size());
        mIsBackPressed = true;
        currentPath = new File(currentPath).getParent();
        textCurrentPath.setText(currentPath);
        refreshList(currentPath);
    }

    private void computeScroll() {
        View vi = recyclerViewFileList.getChildAt(0);
        int top = (vi == null) ? 0 : vi.getTop();
        int index = llm.findFirstVisibleItemPosition();

        Bundle b = new Bundle();
        b.putInt("index", index);
        b.putInt("top", top);
        scrollPosition.put(currentPath, b);
    }


    @Override
    public void onDestroyView() {
        fileListAdapter.setStopAnimation(false);
        getActivity().setResult(AppCompatActivity.RESULT_OK, null);
        super.onDestroyView();
    }

    @Override
    public void onPermissionGranted(String[] permissionName) {
        Log.d(TAG, "onPermissionGranted: ");
        refreshList(currentPath);
    }

    @Override
    public void onPermissionDeclined(String[] permissionName) {

    }

    @Override
    public void onDataLoaded(ArrayList<FileInfo> data) {
        if (data != null) {
            fileInfoList = data;
            Log.d("TAG", "on onLoadFinished--" + fileInfoList.size() + " this:"+DialogBrowseFragment.this);
            fileListAdapter.setStopAnimation(true);
            fileListAdapter.updateAdapter(fileInfoList);
            recyclerViewFileList.setAdapter(fileListAdapter);
            recyclerViewFileList.addItemDecoration(new DividerItemDecoration(getActivity(), currentTheme));
            if (!data.isEmpty()) {
                if (mIsBackPressed) {
                    Log.d("TEST", "on onLoadFinished scrollpos--" + scrollPosition.entrySet());

                    if (scrollPosition.containsKey(currentPath)) {
                        Bundle b = scrollPosition.get(currentPath);
                        llm.scrollToPositionWithOffset(b.getInt("index"), b.getInt("top"));
                    }
                    mIsBackPressed = false;
                }
                recyclerViewFileList.stopScroll();
                mTextEmpty.setVisibility(View.GONE);
            } else {
                mTextEmpty.setText(getString(R.string.no_music));
                mTextEmpty.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void setListener(Listener listener) {

    }

    @Override
    public void onStoragesFetched(List<String> storagesList) {
        this.storagesList = storagesList;
        storagesInfoList = new ArrayList<>();
        String STORAGE_INTERNAL, STORAGE_EXTERNAL;
        STORAGE_INTERNAL = getResources().getString(R.string.nav_menu_internal_storage);
        STORAGE_EXTERNAL = getResources().getString(R.string.nav_menu_ext_storage);
        for (String path : this.storagesList) {
            File file = new File(path);
            int icon;
            String name;
            if ("/storage/emulated/legacy".equals(path) || "/storage/emulated/0".equals(path)) {
                name = STORAGE_INTERNAL;
                icon = R.drawable.ic_phone_white;

            } else if ("/storage/sdcard1".equals(path)) {
                name = STORAGE_EXTERNAL;
                icon = R.drawable.ic_ext_white;
            } else {
                name = file.getName();
                icon = R.drawable.ic_ext_white;
            }
            if (!file.isDirectory() || file.canExecute()) {
                storagesInfoList.add(new FileInfo(PICKER, name, path, icon));
            }
        }
    }
}
