package com.siju.acexplorer.filesystem.ui;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import android.support.v7.widget.RecyclerView;
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
import com.siju.acexplorer.filesystem.utils.FileUtils;
import com.siju.acexplorer.filesystem.utils.MediaStoreHack;
import com.siju.acexplorer.filesystem.utils.ThemeUtils;
import com.siju.acexplorer.utils.DialogUtils;
import com.siju.acexplorer.utils.PermissionUtils;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by SIJU on 04-07-2016.
 */

public class DialogBrowseFragment extends DialogFragment implements LoaderManager.LoaderCallbacks<ArrayList<FileInfo>> {

    private final String TAG = this.getClass().getSimpleName();
    private FastScrollRecyclerView recyclerViewFileList;
    private View root;
    private final int LOADER_ID = 1000;
    private FileListAdapter fileListAdapter;
    private ArrayList<FileInfo> fileInfoList;
    private String mFilePath;
    private ImageButton mImageButtonBack;
    private TextView mTextCurrentPath;
    private Button mButtonOk;
    private Button mButtonCancel;
    private String mCurrentPath;
    private boolean mShowHidden;
    private int mSortMode;
    private boolean mIsRingtonePicker;
    private boolean mIsDarkTheme;
    private static final int MY_PERMISSIONS_REQUEST = 1;
    private static final int SETTINGS_REQUEST = 200;
    MaterialDialog materialDialog;
//    private VerticalRecyclerViewFastScroller mFastScroller;
    private SwipeRefreshLayout mSwipeRefreshLayout;




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.dialog_browse, container, false);
        return root;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        mIsDarkTheme = ThemeUtils.isDarkTheme(getActivity());
        initializeViews();

        if (getArguments() != null && getArguments().getBoolean("ringtone_picker")) {
            mButtonOk.setVisibility(View.GONE);
            mIsRingtonePicker = true;
        }
        mCurrentPath = FileUtils.getInternalStorage().getAbsolutePath();
        mTextCurrentPath.setText(mCurrentPath);
        mShowHidden = PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean
                (FileConstants.PREFS_HIDDEN, false);
        mSortMode = PreferenceManager.getDefaultSharedPreferences(getActivity()).getInt(
                FileConstants.KEY_SORT_MODE, FileConstants.KEY_SORT_NAME);
        fileListAdapter = new FileListAdapter(getContext(), fileInfoList, 0, 0);
        recyclerViewFileList.setAdapter(fileListAdapter);


        fileListAdapter.setOnItemClickListener(new FileListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                File file = new File(fileInfoList.get(position).getFilePath());
                if (file.isDirectory()) {
                    mCurrentPath = file.getAbsolutePath();
                    mTextCurrentPath.setText(mCurrentPath);
                    refreshList(mCurrentPath);
                } else {
                    if (mIsRingtonePicker) {
                        Intent intent = new Intent();
                        Uri mediaStoreUri = MediaStoreHack.getUriFromFile(file.getPath(), getActivity());
                        System.out.println(mediaStoreUri.toString() + "\t" + FileUtils.getMimeType(file));
                        intent.setDataAndType(mediaStoreUri, FileUtils.getMimeType(file));
                        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI, mediaStoreUri);
                        fileListAdapter.setStopAnimation(false);
                        getActivity().setResult(AppCompatActivity.RESULT_OK, intent);
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
                Logger.log(TAG,"cancel");
//                Intent intent = new Intent();
                getActivity().setResult(AppCompatActivity.RESULT_CANCELED, null);
                if (mIsRingtonePicker)
                    getActivity().finish();
                else
                    getDialog().dismiss();


    /*            if (mIsRingtonePicker) {
                    getActivity().finish();
                }*/
            }
        });


        mImageButtonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mCurrentPath.equalsIgnoreCase(FileUtils.getInternalStorage().getAbsolutePath())) {
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
                Logger.log(TAG,"Cancel");
                getActivity().setResult(AppCompatActivity.RESULT_CANCELED, null);
                if (mIsRingtonePicker)
                    getActivity().finish();
                else
                    getDialog().dismiss();
            }
        });


    }





 /*   @Override
    public void onDismiss(DialogInterface dialog) {
        getActivity().setResult(AppCompatActivity.RESULT_CANCELED, null);
        if (mIsRingtonePicker)
            getActivity().finish();
        else
            getDialog().dismiss();
        Logger.log(TAG,"onDismiss");
        super.onDismiss(dialog);

    }*/

    private void loadData() {
        getLoaderManager().initLoader(LOADER_ID, null, this);
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

        materialDialog = new DialogUtils().showDialog(getActivity(), texts);
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
    /*    mFastScroller = (VerticalRecyclerViewFastScroller) root.findViewById(R.id.fast_scroller);
        mFastScroller.setRecyclerView(recyclerViewFileList);
        recyclerViewFileList.addOnScrollListener(mFastScroller.getOnScrollListener());*/
        recyclerViewFileList.setHasFixedSize(true);
        RecyclerView.LayoutManager llm = new LinearLayoutManager(getActivity());
        recyclerViewFileList.setLayoutManager(llm);
        mSwipeRefreshLayout = (SwipeRefreshLayout) root.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setEnabled(false);

        mImageButtonBack = (ImageButton) root.findViewById(R.id.imageButtonBack);
        mTextCurrentPath = (TextView) root.findViewById(R.id.textPath);
        mButtonOk = (Button) root.findViewById(R.id.buttonOk);
        mButtonCancel = (Button) root.findViewById(R.id.buttonCancel);

    }

 /*   @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        getActivity().setResult(AppCompatActivity.RESULT_OK, null);
        getDialog().dismiss();
   *//*     if (mIsRingtonePicker) {
            getActivity().finish();
        }*//*
    }*/

    public void refreshList(String path) {

        Bundle args = new Bundle();
        args.putString(FileConstants.KEY_PATH, path);
        getLoaderManager().restartLoader(LOADER_ID, args, this);
    }


    @Override
    public Loader<ArrayList<FileInfo>> onCreateLoader(int id, Bundle args) {
        fileInfoList = new ArrayList<>();
        String path;
        if (args != null) {
            path = args.getString(FileConstants.KEY_PATH, FileUtils.getInternalStorage()
                    .getAbsolutePath());
        } else {
            path = FileUtils.getInternalStorage().getAbsolutePath();
        }
        return new FileListLoader(this, getContext(), path, FileConstants.CATEGORY.FILES.getValue());
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<FileInfo>> loader, ArrayList<FileInfo> data) {
//        Log.d("TAG", "on onLoadFinished--" + data.size());
        if (data != null) {
            Log.d("TAG", "on onLoadFinished--" + data.size());
            if (!data.isEmpty()) {
                fileInfoList = data;
                fileListAdapter.setStopAnimation(true);
                fileListAdapter.updateAdapter(fileInfoList);
                recyclerViewFileList.setItemAnimator(new DefaultItemAnimator());
                recyclerViewFileList.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager
                        .VERTICAL, mIsDarkTheme));
//                ((BaseActivity) getActivity()).setFileListAdapter(fileListAdapter);
            } else {
                TextView textEmpty = (TextView) getActivity().findViewById(R.id.textEmpty);
                textEmpty.setVisibility(View.VISIBLE);
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
