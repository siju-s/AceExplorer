package com.siju.acexplorer.filesystem.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
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

import com.siju.acexplorer.R;
import com.siju.acexplorer.filesystem.FileConstants;
import com.siju.acexplorer.filesystem.FileListAdapter;
import com.siju.acexplorer.filesystem.FileListLoader;
import com.siju.acexplorer.filesystem.model.FileInfo;
import com.siju.acexplorer.filesystem.utils.FileUtils;
import com.siju.acexplorer.filesystem.utils.MediaStoreHack;
import com.siju.acexplorer.filesystem.utils.ThemeUtils;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by SIJU on 04-07-2016.
 */

public class DialogBrowseFragment extends DialogFragment implements LoaderManager.LoaderCallbacks<ArrayList<FileInfo>> {

    private final String TAG = this.getClass().getSimpleName();
    private RecyclerView recyclerViewFileList;
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.dialog_browse, container, false);
        return root;
    }

    /*public static DialogBrowseFragment newInstance(boolean isRingtonePicker) {
        DialogBrowseFragment dialogBrowseFragment = new DialogBrowseFragment();
        Bundle args = new Bundle();
        args.putBoolean("ringtone_picker",isRingtonePicker);

    }*/

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
        getLoaderManager().initLoader(LOADER_ID, null, this);

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
                SelectedPathListener listener = (SelectedPathListener) getActivity();
                listener.getSelectedPath(mCurrentPath);
                getDialog().dismiss();
            }
        });

        mButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().setResult(AppCompatActivity.RESULT_OK, null);
                getActivity().finish();
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


    }

    private void initializeViews() {
        recyclerViewFileList = (RecyclerView) root.findViewById(R.id.recyclerViewFileList);
        mImageButtonBack = (ImageButton) root.findViewById(R.id.imageButtonBack);
        mTextCurrentPath = (TextView) root.findViewById(R.id.textPath);
        mButtonOk = (Button) root.findViewById(R.id.buttonOk);
        mButtonCancel = (Button) root.findViewById(R.id.buttonCancel);

    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        getActivity().setResult(AppCompatActivity.RESULT_OK, null);
        getActivity().finish();
    }

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
        return new FileListLoader(this,getContext(), path, 0, mShowHidden, mSortMode);
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
                recyclerViewFileList.setHasFixedSize(true);
                RecyclerView.LayoutManager llm;
                llm = new LinearLayoutManager(getActivity());
                llm.setAutoMeasureEnabled(false);
                recyclerViewFileList.setLayoutManager(llm);
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

    /*
         * Implemented in {@link BaseActivity}
         */
    public interface SelectedPathListener {

        void getSelectedPath(String path);
    }

}
