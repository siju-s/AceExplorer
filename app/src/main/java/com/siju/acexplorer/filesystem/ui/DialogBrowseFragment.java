package com.siju.acexplorer.filesystem.ui;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
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

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.siju.acexplorer.R;
import com.siju.acexplorer.common.Logger;
import com.siju.acexplorer.filesystem.FileConstants;
import com.siju.acexplorer.filesystem.FileListAdapter;
import com.siju.acexplorer.filesystem.FileListLoader;
import com.siju.acexplorer.filesystem.model.FileInfo;
import com.siju.acexplorer.filesystem.modes.ViewMode;
import com.siju.acexplorer.filesystem.theme.ThemeUtils;
import com.siju.acexplorer.filesystem.theme.Themes;
import com.siju.acexplorer.filesystem.utils.FileUtils;
import com.siju.acexplorer.filesystem.utils.MediaStoreHack;
import com.siju.acexplorer.filesystem.views.FastScrollRecyclerView;
import com.siju.acexplorer.permission.PermissionUtils;
import com.siju.acexplorer.utils.Dialogs;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.siju.acexplorer.filesystem.groups.Category.FILES;
import static com.siju.acexplorer.filesystem.groups.Category.PICKER;
import static com.siju.acexplorer.filesystem.helper.UriHelper.createContentUri;
import static com.siju.acexplorer.filesystem.storage.StorageUtils.getInternalStorage;
import static com.siju.acexplorer.filesystem.storage.StorageUtils.getStorageDirectories;


public class DialogBrowseFragment extends DialogFragment implements LoaderManager.LoaderCallbacks<ArrayList<FileInfo>> {

    private final String TAG = this.getClass().getSimpleName();
    private FastScrollRecyclerView recyclerViewFileList;
    private View root;
    private final int LOADER_ID = 1000;
    private FileListAdapter fileListAdapter;
    private ArrayList<FileInfo> fileInfoList;
    private ArrayList<FileInfo> storagesInfoList;
    private ImageButton mImageButtonBack;
    private TextView mTextCurrentPath;
    private TextView mTitle;
    private Button mButtonOk;
    private Button mButtonCancel;
    private String mCurrentPath;
    private boolean mIsRingtonePicker;
    private boolean isFilePicker;
    private int mRingToneType;
    private Themes currentTheme;
    private static final int MY_PERMISSIONS_REQUEST = 1;
    private static final int SETTINGS_REQUEST = 200;
    private MaterialDialog materialDialog;
    private boolean mIsBackPressed;
    private LinearLayoutManager llm;
    private final HashMap<String, Bundle> scrollPosition = new HashMap<>();
    private TextView mTextEmpty;
    private List<String> mStoragesList = new ArrayList<>();
    private boolean mInStoragesList;


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new Dialog(getActivity(), getTheme()) {
            @Override
            public void onBackPressed() {
                if (checkIfRootDir())
                    reloadData();
                else {
                    getActivity().setResult(AppCompatActivity.RESULT_CANCELED, null);
                    if (mIsRingtonePicker)
                        getActivity().finish();
                    else
                        getDialog().dismiss();
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
        currentTheme = Themes.getTheme(ThemeUtils.getTheme(getContext()));

        initializeViews();

        if (getArguments() != null) {
            if (getArguments().getBoolean("ringtone_picker")) {
                mTitle.setText(getString(R.string.dialog_title_picker));
                mButtonOk.setVisibility(View.GONE);
                mIsRingtonePicker = true;
                mRingToneType = getArguments().getInt("ringtone_type");
            } else if (getArguments().getBoolean("file_picker")) {
                mButtonOk.setVisibility(View.GONE);
                isFilePicker = true;
            }
        }

        loadStoragesList();

        mCurrentPath = getInternalStorage();
        mTextCurrentPath.setText(mCurrentPath);
        recyclerViewFileList.setItemAnimator(new DefaultItemAnimator());
        fileListAdapter = new FileListAdapter(getContext(), fileInfoList, FILES, ViewMode.LIST);
        recyclerViewFileList.setAdapter(fileListAdapter);


        fileListAdapter.setOnItemClickListener(new FileListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                File file = new File(fileInfoList.get(position).getFilePath());
                if (file.isDirectory()) {
                    mInStoragesList = false;
                    computeScroll();
                    mCurrentPath = file.getAbsolutePath();
                    mTextCurrentPath.setText(mCurrentPath);
                    refreshList(mCurrentPath);
                } else if (mInStoragesList) {
                    mInStoragesList = false;
                    File storagesFile = new File(fileInfoList.get(position).getFilePath());
                    mCurrentPath = storagesFile.getAbsolutePath();
                    mTextCurrentPath.setText(mCurrentPath);
                    refreshList(mCurrentPath);
                } else {
                    if (mIsRingtonePicker) {
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

        mButtonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent();
                intent.putExtra("PATH", mCurrentPath);
                getTargetFragment().onActivityResult(getTargetRequestCode(), AppCompatActivity.RESULT_OK, intent);
                getDialog().dismiss();
            }
        });

        mButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Logger.log(TAG, "cancel");
                getActivity().setResult(AppCompatActivity.RESULT_CANCELED, null);
                if (mIsRingtonePicker)
                    getActivity().finish();
                else
                    getDialog().dismiss();

            }
        });


        mImageButtonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mIsBackPressed = true;
                if (mStoragesList.contains(mCurrentPath)) {
                    if (!mInStoragesList) {
                        mInStoragesList = true;
                        mTextCurrentPath.setText("");
                        fileInfoList = storagesInfoList;
                        fileListAdapter.setStopAnimation(true);
                        fileListAdapter.updateAdapter(fileInfoList);
                        recyclerViewFileList.setAdapter(fileListAdapter);
                    }
                } else {
                    mInStoragesList = false;
                    mCurrentPath = new File(mCurrentPath).getParent();
                    mTextCurrentPath.setText(mCurrentPath);
                    refreshList(mCurrentPath);
                }


            }
        });

        if (PermissionUtils.isAtLeastM() && !PermissionUtils.hasRequiredPermissions()) {
            requestPermission();
        } else {
            loadData();
        }
        getDialog().setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                Logger.log(TAG, "Cancel");
                getActivity().setResult(AppCompatActivity.RESULT_CANCELED, null);
                if (mIsRingtonePicker)
                    getActivity().finish();
                else
                    getDialog().dismiss();
            }
        });


    }

    private boolean checkIfRootDir() {
        return !mCurrentPath.equals(getInternalStorage());
    }


    private void loadStoragesList() {
        mStoragesList = getStorageDirectories(getActivity());
        storagesInfoList = new ArrayList<>();
        String STORAGE_INTERNAL, STORAGE_EXTERNAL;
        STORAGE_INTERNAL = getResources().getString(R.string.nav_menu_internal_storage);
        STORAGE_EXTERNAL = getResources().getString(R.string.nav_menu_ext_storage);
        for (String path : mStoragesList) {
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

    private void loadData() {

        Bundle args = new Bundle();
        getLoaderManager().initLoader(LOADER_ID, args, this);
    }

    /**
     * Brings up the Permission Dialog
     */
    private void requestPermission() {
        Log.d(TAG, "Permission dialog");
        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission
                .WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[]
                                                   grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST:

                if (PermissionUtils.hasRequiredPermissions()) {
                    // Permission granted
                    Log.d(TAG, "Permission granted");
                    loadData();
                } else {
                    showRationale();
                }
        }
    }


    private void showRationale() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission
                .WRITE_EXTERNAL_STORAGE)) {
            createRationaleDialog(false);
        } else {
            createRationaleDialog(true);
        }

    }

    private void createRationaleDialog(final boolean showSettings) {
        String title = getString(R.string.need_permission);
        String texts[] = new String[]{title, getString(R.string.action_grant), "", getString(R.string
                .dialog_cancel)};
        if (showSettings) {
            texts[1] = getString(R.string.action_settings);
        }

        materialDialog = new Dialogs().showDialog(getActivity(), texts);
        materialDialog.getActionButton(DialogAction.POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                materialDialog.dismiss();
                if (showSettings) {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
                    intent.setData(uri);
                    startActivityForResult(intent, SETTINGS_REQUEST);
                } else
                    requestPermission();
            }
        });

        materialDialog.getActionButton(DialogAction.NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                materialDialog.dismiss();
            }
        });

        materialDialog.show();


    }

    @Override
    public void onResume() {

        if (materialDialog != null && materialDialog.isShowing()) {
            if (PermissionUtils.hasRequiredPermissions()) {
                materialDialog.dismiss();
                loadData();
            }
        }
        super.onResume();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SETTINGS_REQUEST) {
            // User clicked the Setting button and we have permissions,setup the data
            if (PermissionUtils.hasRequiredPermissions()) {
                loadData();
            } else {
                // User clicked the Setting button and we don't permissions,show snackbar again
                createRationaleDialog(true);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void initializeViews() {
        recyclerViewFileList = (FastScrollRecyclerView) root.findViewById(R.id.recyclerViewFileList);
        recyclerViewFileList.setHasFixedSize(true);
        llm = new LinearLayoutManager(getActivity());
        recyclerViewFileList.setLayoutManager(llm);
        SwipeRefreshLayout mSwipeRefreshLayout = (SwipeRefreshLayout) root.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setEnabled(false);
        mTextEmpty = (TextView) root.findViewById(R.id.textEmpty);
        mImageButtonBack = (ImageButton) root.findViewById(R.id.imageButtonBack);
        mTextCurrentPath = (TextView) root.findViewById(R.id.textPath);
        mButtonOk = (Button) root.findViewById(R.id.buttonOk);
        mButtonCancel = (Button) root.findViewById(R.id.buttonCancel);
        mTitle = (TextView) root.findViewById(R.id.textDialogTitle);

    }

    private void refreshList(String path) {
        Bundle args = new Bundle();
        args.putString(FileConstants.KEY_PATH, path);
        getLoaderManager().restartLoader(LOADER_ID, args, this);
    }

    private void reloadData() {
        mIsBackPressed = true;
        mCurrentPath = new File(mCurrentPath).getParent();
        mTextCurrentPath.setText(mCurrentPath);
        refreshList(mCurrentPath);
    }

    private void computeScroll() {
        View vi = recyclerViewFileList.getChildAt(0);
        int top = (vi == null) ? 0 : vi.getTop();
        int index = llm.findFirstVisibleItemPosition();

        Bundle b = new Bundle();
        b.putInt("index", index);
        b.putInt("top", top);
        scrollPosition.put(mCurrentPath, b);
    }


    @Override
    public Loader<ArrayList<FileInfo>> onCreateLoader(int id, Bundle args) {
        fileInfoList = new ArrayList<>();
        if (fileListAdapter != null) {
            fileListAdapter.clearList();
        }
        String path;
        if (args != null) {
            path = args.getString(FileConstants.KEY_PATH, getInternalStorage());
        } else {
            path = getInternalStorage();
        }
        return new FileListLoader(this, path, FILES, mIsRingtonePicker);
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<FileInfo>> loader, ArrayList<FileInfo> data) {
        if (data != null) {
            Log.d("TAG", "on onLoadFinished--" + data.size());
            fileInfoList = data;
            fileListAdapter.setStopAnimation(true);
            fileListAdapter.updateAdapter(fileInfoList);
            recyclerViewFileList.setAdapter(fileListAdapter);
            recyclerViewFileList.addItemDecoration(new DividerItemDecoration(getActivity(), currentTheme));
            if (!data.isEmpty()) {
                if (mIsBackPressed) {
                    Log.d("TEST", "on onLoadFinished scrollpos--" + scrollPosition.entrySet());

                    if (scrollPosition.containsKey(mCurrentPath)) {
                        Bundle b = scrollPosition.get(mCurrentPath);
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
    public void onLoaderReset(Loader<ArrayList<FileInfo>> loader) {

    }

    @Override
    public void onDestroyView() {
        fileListAdapter.setStopAnimation(false);
        getActivity().setResult(AppCompatActivity.RESULT_OK, null);
        super.onDestroyView();
    }

}
