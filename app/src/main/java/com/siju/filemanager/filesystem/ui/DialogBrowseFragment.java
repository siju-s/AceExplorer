package com.siju.filemanager.filesystem.ui;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.siju.filemanager.BaseActivity;
import com.siju.filemanager.R;
import com.siju.filemanager.common.Logger;
import com.siju.filemanager.filesystem.FileConstants;
import com.siju.filemanager.filesystem.FileListAdapter;
import com.siju.filemanager.filesystem.FileListLoader;
import com.siju.filemanager.filesystem.model.FileInfo;
import com.siju.filemanager.filesystem.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;

import static android.media.CamcorderProfile.get;

/**
 * Created by SIJU on 04-07-2016.
 */

public class DialogBrowseFragment extends DialogFragment implements LoaderManager.LoaderCallbacks<ArrayList<FileInfo>> {

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.dialog_browse, container, false);
        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        initializeViews();
        mCurrentPath = FileUtils.getInternalStorage().getAbsolutePath();
        mTextCurrentPath.setText(mCurrentPath);
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
                getDialog().dismiss();
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
        return new FileListLoader(getContext(), path, 0);
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<FileInfo>> loader, ArrayList<FileInfo> data) {
//        Log.d("TAG", "on onLoadFinished--" + data.size());
        if (data != null) {
            Log.d("TAG", "on onLoadFinished--" + data.size());
            if (!data.isEmpty()) {
                fileInfoList = data;
                fileListAdapter.updateAdapter(fileInfoList);
                recyclerViewFileList.setHasFixedSize(true);
                RecyclerView.LayoutManager llm;
                llm = new LinearLayoutManager(getActivity());
                llm.setAutoMeasureEnabled(false);
                recyclerViewFileList.setLayoutManager(llm);
                recyclerViewFileList.setItemAnimator(new DefaultItemAnimator());
                recyclerViewFileList.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager
                        .VERTICAL));
                ((BaseActivity) getActivity()).setFileListAdapter(fileListAdapter);
            } else {
                TextView textEmpty = (TextView) getActivity().findViewById(R.id.textEmpty);
                textEmpty.setVisibility(View.VISIBLE);
            }
        }

    }

    @Override
    public void onLoaderReset(Loader<ArrayList<FileInfo>> loader) {

    }

    /*
     * Implemented in {@link BaseActivity}
     */
    public interface SelectedPathListener {

        void getSelectedPath(String path);
    }

}
