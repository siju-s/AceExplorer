package com.siju.filemanager.filesystem;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.siju.filemanager.BaseActivity;
import com.siju.filemanager.R;

import java.util.ArrayList;


/**
 * Created by Siju on 13-06-2016.
 */

public class FileListFragment extends Fragment implements LoaderManager.LoaderCallbacks<ArrayList<FileInfo>> {

    private ListView fileList;
    private View root;
    private final int LOADER_ID = 1000;
    private FileListAdapter fileListAdapter;
    private ArrayList<FileInfo> fileInfoArrayList;

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
        fileList = (ListView) root.findViewById(R.id.fileList);

        Bundle args = new Bundle();
        String filePath = null, fileName;

        if (getArguments() != null && getArguments().getString(FileConstants.KEY_PATH) != null) {
            filePath = getArguments().getString(FileConstants.KEY_PATH);
            fileName = getArguments().getString(FileConstants.KEY_FILENAME);
        }
//        else {
//            filePath = getArguments().getString(FileConstants.KEY_PATH);
//        }
//        fileList.setLayoutManager(new LinearLayoutManager(getContext()));
        fileListAdapter = new FileListAdapter(getContext(), fileInfoArrayList);
        fileList.setAdapter(fileListAdapter);
        if (filePath != null) {
            args.putString(FileConstants.KEY_PATH, filePath);
            getLoaderManager().initLoader(LOADER_ID, args, this);
        }
        fileList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Bundle bundle = new Bundle();
                bundle.putString(FileConstants.KEY_PATH, fileInfoArrayList.get(position).getFilePath());
                Intent intent = new Intent(getActivity(), BaseActivity.class);
                intent.setAction(BaseActivity.ACTION_VIEW_FOLDER_LIST);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

    }

    @Override
    public Loader<ArrayList<FileInfo>> onCreateLoader(int id, Bundle args) {
        fileInfoArrayList = new ArrayList<>();
        String path = args.getString(FileConstants.KEY_PATH);
        return new FileListLoader(getContext(), path);

    }

    @Override
    public void onLoadFinished(Loader<ArrayList<FileInfo>> loader, ArrayList<FileInfo> data) {
        if (!data.isEmpty()) {
            fileInfoArrayList = data;
            fileListAdapter.updateAdapter(fileInfoArrayList);
        }
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<FileInfo>> loader) {

    }
}
