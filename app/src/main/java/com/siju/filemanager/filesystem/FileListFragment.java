package com.siju.filemanager.filesystem;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.siju.filemanager.BaseActivity;
import com.siju.filemanager.R;
import com.siju.filemanager.common.Logger;
import com.siju.filemanager.ui.DividerItemDecoration;

import java.util.ArrayList;


/**
 * Created by Siju on 13-06-2016.
 */

public class FileListFragment extends Fragment implements LoaderManager.LoaderCallbacks<ArrayList<FileInfo>> {

    //    private ListView fileList;
    private RecyclerView recyclerViewFileList;
    private View root;
    private final int LOADER_ID = 1000;
    private FileListAdapter fileListAdapter;
    private ArrayList<FileInfo> fileInfoList;
    private boolean isDualMode;
    private String mFilePath;
    private int mCategory;


    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.file_list, container, false);
        return root;

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initializeViews();

        Bundle args = new Bundle();
        String fileName;

        if (getArguments() != null) {
            if (getArguments().getString(FileConstants.KEY_PATH) != null) {
                mFilePath = getArguments().getString(FileConstants.KEY_PATH);
            }
            mCategory = getArguments().getInt(FileConstants.KEY_CATEGORY, FileConstants.CATEGORY.FILES.getValue());
        }

        Log.d("TAG", "on onActivityCreated--Fragment" + mFilePath);

//        else {
//            filePath = getArguments().getString(FileConstants.KEY_PATH);
//        }
//        fileList.setLayoutManager(new LinearLayoutManager(getContext()));
//        fileListAdapter = new FileListAdapter(getContext(), fileInfoList);
//        fileList.setAdapter(fileListAdapter);
        fileListAdapter = new FileListAdapter(getContext(), fileInfoList, mCategory);
        recyclerViewFileList.setAdapter(fileListAdapter);
/*        recyclerViewFileList.setHasFixedSize(true);
        RecyclerView.LayoutManager llm = new LinearLayoutManager(getActivity());
        recyclerViewFileList.setLayoutManager(llm);*/
/*        recyclerViewFileList.setItemAnimator(new DefaultItemAnimator());
        recyclerViewFileList.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL)
        );*/
//        if (mFilePath != null) {
        args.putString(FileConstants.KEY_PATH, mFilePath);

        getLoaderManager().initLoader(LOADER_ID, args, this);
//        }
        fileListAdapter.setOnItemClickListener(new FileListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (((BaseActivity) getActivity()).getActionMode() != null) {
                    itemClick(position);
                } else {
                    // For file, open external apps based on Mime Type
                    if (!fileInfoList.get(position).isDirectory()) {
                        FileUtils.viewFile(getActivity(), fileInfoList.get(position).getFilePath(), fileInfoList.get
                                (position).getExtension());
                    } else {
                        Bundle bundle = new Bundle();
                        String path = fileInfoList.get(position).getFilePath();
                        bundle.putString(FileConstants.KEY_PATH, path);
                        Intent intent = new Intent(getActivity(), BaseActivity.class);
                        if (FileListFragment.this instanceof FileListDualFragment) {
                            intent.setAction(BaseActivity.ACTION_DUAL_VIEW_FOLDER_LIST);
                            intent.putExtra(BaseActivity.ACTION_DUAL_PANEL, true);
                        } else {
                            intent.setAction(BaseActivity.ACTION_VIEW_FOLDER_LIST);
                            intent.putExtra(BaseActivity.ACTION_DUAL_PANEL, false);
                        }

                        intent.putExtras(bundle);
                        startActivity(intent);
                    }
                }
            }
        });

        fileListAdapter.setOnItemLongClickListener(new FileListAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(View view, int position) {
                Logger.log("TAG", "On long click");
                itemClick(position);
            }
        });

//        fileList.setOnLong(new AdapterView.OnItemLongClickListener() {
//            @Override
//            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
//                //                }
//                Logger.log("TAG", "On long click");
//                itemClick(position);
//
////                // Start the CAB using the ActionMode.Callback defined above
////                mActionMode = ((AppCompatActivity) getActivity())
////                        .startSupportActionMode(new ActionModeCallback());
//                return true;
////        return false;
//            }
//        });

    }

    private void initializeViews() {
        recyclerViewFileList = (RecyclerView) root.findViewById(R.id.recyclerViewFileList);

    }

    private void itemClick(int position) {
        fileListAdapter.toggleSelection(position);
        boolean hasCheckedItems = fileListAdapter.getSelectedCount() > 0;
        ActionMode actionMode = ((BaseActivity) getActivity()).getActionMode();
        if (hasCheckedItems && actionMode == null) {
            // there are some selected items, start the actionMode
            ((BaseActivity) getActivity()).startActionMode();
            ((BaseActivity) getActivity()).setFileList(fileInfoList);
        } else if (!hasCheckedItems && actionMode != null) {
            // there no selected items, finish the actionMode
            actionMode.finish();
        }
        if (((BaseActivity) getActivity()).getActionMode() != null) {
            SparseBooleanArray checkedItemPos = fileListAdapter.getSelectedItemPositions();
            ((BaseActivity) getActivity()).setSelectedItemPos(checkedItemPos);
            ((BaseActivity) getActivity()).getActionMode().setTitle(String.valueOf(fileListAdapter.getSelectedCount()
            ) + " selected");
        }
    }

    public void clearSelection() {
        fileListAdapter.removeSelection();

    }

    public void refreshList() {
        Bundle args = new Bundle();
        args.putString(FileConstants.KEY_PATH, mFilePath);
        getLoaderManager().restartLoader(LOADER_ID, args, this);
    }


    @Override
    public Loader<ArrayList<FileInfo>> onCreateLoader(int id, Bundle args) {
        fileInfoList = new ArrayList<>();
        String path = args.getString(FileConstants.KEY_PATH);
        return new FileListLoader(getContext(), path, mCategory);
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
                RecyclerView.LayoutManager llm = new LinearLayoutManager(getActivity());
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


    @Override
    public void onDestroy() {
//        Log.d("TAG", "on onDestroy--Fragment");
        super.onDestroy();

    }
}
