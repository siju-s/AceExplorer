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
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.siju.acexplorer.R;
import com.siju.acexplorer.analytics.Analytics;
import com.siju.acexplorer.home.model.LoaderHelper;
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
import static com.siju.acexplorer.model.groups.StoragesGroup.STORAGE_EMULATED_0;
import static com.siju.acexplorer.model.groups.StoragesGroup.STORAGE_EMULATED_LEGACY;
import static com.siju.acexplorer.model.groups.StoragesGroup.STORAGE_SDCARD1;
import static com.siju.acexplorer.model.helper.UriHelper.createContentUri;


public class DialogBrowseFragment extends DialogFragment implements
                                                         PickerUi,
                                                         PermissionResultCallback
{

    @SuppressWarnings("unused")
    private final String TAG = this.getClass().getSimpleName();

    private static final int    MY_PERMISSIONS_REQUEST = 1;
    private static final String RINGTONE_PICKER        = "ringtone_picker";
    private static final String FILE_PICKER            = "file_picker";
    private static final String RINGTONE_TYPE          = "ringtone_type";
    private static final String INDEX                  = "index";
    private static final String TOP                    = "top";

    private FastScrollRecyclerView fileList;
    private View                   root;
    private ImageButton            backButton;
    private TextView               textCurrentPath;
    private TextView               title;
    private Button                 okButton;
    private Button                 cancelButton;
    private LinearLayoutManager    layoutManager;
    private TextView               textEmpty;

    private ArrayList<FileInfo> fileInfoList;
    private ArrayList<FileInfo> storagesInfoList;

    private final HashMap<String, Bundle> scrollPosition = new HashMap<>();
    private       List<String>            storagesList   = new ArrayList<>();

    private String  currentPath;
    private int     ringToneType;
    private boolean isRingtonePicker;
    private boolean isFilePicker;
    private boolean isBackPressed;
    private boolean isStoragesList;

    private FileListAdapter  fileListAdapter;
    private PermissionHelper permissionHelper;
    private PickerPresenter  pickerPresenter;
    private Theme            currentTheme;


    public static DialogBrowseFragment getNewInstance(int theme, boolean isRingtonePicker,
                                                      int ringtoneType) {
        DialogBrowseFragment dialogFragment = new DialogBrowseFragment();
        dialogFragment.setStyle(DialogFragment.STYLE_NORMAL, theme);
        Bundle args = new Bundle();
        args.putBoolean(RINGTONE_PICKER, isRingtonePicker);
        args.putBoolean(FILE_PICKER, !isRingtonePicker);
        args.putInt(RINGTONE_TYPE, ringtoneType);
        dialogFragment.setArguments(args);
        return dialogFragment;
    }

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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.dialog_browse, container, false);
        return root;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        currentTheme = Theme.getTheme(ThemeUtils.getTheme(getContext()));
        initializeViews();

        if (getArguments() != null) {
            if (getArguments().getBoolean(RINGTONE_PICKER)) {
                Analytics.getLogger().pickerShown(true);
                title.setText(getString(R.string.dialog_title_picker));
                okButton.setVisibility(View.GONE);
                isRingtonePicker = true;
                ringToneType = getArguments().getInt(RINGTONE_TYPE);
            } else if (getArguments().getBoolean(FILE_PICKER)) {
                Analytics.getLogger().pickerShown(false);
                okButton.setVisibility(View.GONE);
                isFilePicker = true;
            }
        }

        PickerModel pickerModel = new PickerModelImpl();
        pickerPresenter = new PickerPresenterImpl(this, pickerModel, new LoaderHelper(this),
                                                  getActivity().getSupportLoaderManager());

        loadStoragesList();

        if (isRingtonePicker) {
            String path = pickerPresenter.getLastSavedRingtoneDir();
            if (path != null && new File(path).exists()) {
                currentPath = path;
            } else {
                currentPath = getInternalStorage();
            }
        } else {
            currentPath = getInternalStorage();
        }
        textCurrentPath.setText(currentPath);

        fileList.setItemAnimator(new DefaultItemAnimator());
        fileListAdapter = new FileListAdapter(getContext(), fileInfoList, FILES, ViewMode.LIST, null);
        fileList.setAdapter(fileListAdapter);

        setListeners();
        setupPermissions();
    }

    private void setListeners() {
        fileListAdapter.setOnItemClickListener(new FileListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (position >= fileInfoList.size() || position == RecyclerView.NO_POSITION) {
                    return;
                }
                File file = new File(fileInfoList.get(position).getFilePath());
                if (file.isDirectory()) {
                    isStoragesList = false;
                    computeScroll();
                    currentPath = file.getAbsolutePath();
                    textCurrentPath.setText(currentPath);
                    refreshList(currentPath);
                } else if (isStoragesList) {
                    isStoragesList = false;
                    File storagesFile = new File(fileInfoList.get(position).getFilePath());
                    currentPath = storagesFile.getAbsolutePath();
                    textCurrentPath.setText(currentPath);
                    refreshList(currentPath);
                } else {
                    if (isRingtonePicker) {
                        Intent intent = new Intent();
                        Uri mediaStoreUri = MediaStoreHack.getCustomRingtoneUri(getActivity().getContentResolver(),
                                                                                file.getAbsolutePath(), ringToneType);
                        System.out.println(mediaStoreUri + "\t" + FileUtils.getMimeType(file) + "type=" +
                                                   ringToneType);

                        if (mediaStoreUri != null) {
                            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, ringToneType);
                            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI, mediaStoreUri);
                            intent.setData(mediaStoreUri);
                            pickerPresenter.saveLastRingtoneDir(currentPath);
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

            @Override
            public boolean canShowPeek() {
                return false;
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


        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isBackPressed = true;
                if (storagesList.contains(currentPath)) {
                    if (!isStoragesList) {
                        isStoragesList = true;
                        textCurrentPath.setText("");
                        fileInfoList = storagesInfoList;
                        fileListAdapter.setStopAnimation(true);
                        fileListAdapter.updateAdapter(fileInfoList);
                        fileList.setAdapter(fileListAdapter);
                    }
                } else {
                    isStoragesList = false;
                    currentPath = new File(currentPath).getParent();
                    textCurrentPath.setText(currentPath);
                    refreshList(currentPath);
                }


            }
        });

        getDialog().setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
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
        fileList = root.findViewById(R.id.recyclerViewFileList);
        fileList.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        fileList.setLayoutManager(layoutManager);
        SwipeRefreshLayout mSwipeRefreshLayout = root.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setEnabled(false);
        textEmpty = root.findViewById(R.id.textEmpty);
        backButton = root.findViewById(R.id.imageButtonBack);
        textCurrentPath = root.findViewById(R.id.textPath);
        okButton = root.findViewById(R.id.buttonPositive);
        cancelButton = root.findViewById(R.id.buttonNegative);
        title = root.findViewById(R.id.textDialogTitle);

        okButton.setText(getString(R.string.msg_ok));
        cancelButton.setText(getString(R.string.dialog_cancel));
    }

    private void refreshList(String path) {
        fileInfoList = new ArrayList<>();
        if (fileListAdapter != null) {
            fileListAdapter.clearList();
        }
        pickerPresenter.loadData(path, isRingtonePicker);
    }

    private void reloadData() {
        isBackPressed = true;
        currentPath = new File(currentPath).getParent();
        textCurrentPath.setText(currentPath);
        refreshList(currentPath);
    }

    private void computeScroll() {
        View vi = fileList.getChildAt(0);
        int top = (vi == null) ? 0 : vi.getTop();
        int index = layoutManager.findFirstVisibleItemPosition();

        Bundle b = new Bundle();
        b.putInt(INDEX, index);
        b.putInt(TOP, top);
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
        refreshList(currentPath);
    }

    @Override
    public void onPermissionDeclined(String[] permissionName) {

    }

    @Override
    public void onDataLoaded(ArrayList<FileInfo> data) {
        if (data != null) {
            fileInfoList = data;
            fileListAdapter.setStopAnimation(true);
            fileListAdapter.updateAdapter(fileInfoList);
            fileList.setAdapter(fileListAdapter);
            fileList.addItemDecoration(new DividerItemDecoration(getActivity(), currentTheme));
            if (!data.isEmpty()) {
                if (isBackPressed) {

                    if (scrollPosition.containsKey(currentPath)) {
                        Bundle b = scrollPosition.get(currentPath);
                        layoutManager.scrollToPositionWithOffset(b.getInt(INDEX), b.getInt(TOP));
                    }
                    isBackPressed = false;
                }
                fileList.stopScroll();
                textEmpty.setVisibility(View.GONE);
            } else {
                textEmpty.setText(getString(R.string.no_music));
                textEmpty.setVisibility(View.VISIBLE);
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
            if (STORAGE_EMULATED_LEGACY.equals(path) || STORAGE_EMULATED_0.equals(path)) {
                name = STORAGE_INTERNAL;
                icon = R.drawable.ic_phone_white;

            } else if (STORAGE_SDCARD1.equals(path)) {
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
