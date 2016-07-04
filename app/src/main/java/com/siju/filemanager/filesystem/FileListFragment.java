package com.siju.filemanager.filesystem;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.siju.filemanager.BaseActivity;
import com.siju.filemanager.R;
import com.siju.filemanager.common.Logger;
import com.siju.filemanager.filesystem.model.FileInfo;
import com.siju.filemanager.filesystem.ui.DividerItemDecoration;
import com.siju.filemanager.filesystem.utils.FileUtils;

import java.util.ArrayList;
import java.util.Collections;

import static com.siju.filemanager.BaseActivity.ACTION_VIEW_MODE;


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
    private int mViewMode = FileConstants.KEY_LISTVIEW;
    private String mPath;
    private boolean mIsZip;


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
        setHasOptionsMenu(true);
        initializeViews();

        Bundle args = new Bundle();
        String fileName;

        if (getArguments() != null) {
            if (getArguments().getString(FileConstants.KEY_PATH) != null) {
                mFilePath = getArguments().getString(FileConstants.KEY_PATH);
            }
            mCategory = getArguments().getInt(FileConstants.KEY_CATEGORY, FileConstants.CATEGORY.FILES.getValue());
            mViewMode = getArguments().getInt(BaseActivity.ACTION_VIEW_MODE, FileConstants.KEY_LISTVIEW);
            mIsZip = getArguments().getBoolean(FileConstants.KEY_ZIP, false);
        }

        Log.d("TAG", "on onActivityCreated--Fragment" + mFilePath);
        Log.d("TAG", "View mode=" + mViewMode);

//        else {
//            filePath = getArguments().getString(FileConstants.KEY_PATH);
//        }
//        fileList.setLayoutManager(new LinearLayoutManager(getContext()));
//        fileListAdapter = new FileListAdapter(getContext(), fileInfoList);
//        fileList.setAdapter(fileListAdapter);
        fileListAdapter = new FileListAdapter(getContext(), fileInfoList, mCategory, mViewMode);
        recyclerViewFileList.setAdapter(fileListAdapter);

        args.putString(FileConstants.KEY_PATH, mFilePath);

        getLoaderManager().initLoader(LOADER_ID, args, this);

        fileListAdapter.setOnItemClickListener(new FileListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (((BaseActivity) getActivity()).getActionMode() != null) {
                    itemClick(position);
                } else {
                    handleCategoryItemClick(position);
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

    }

    private void initializeViews() {
        recyclerViewFileList = (RecyclerView) root.findViewById(R.id.recyclerViewFileList);

    }

    private void handleCategoryItemClick(int position) {
        switch (mCategory) {
            case 0:
                // For file, open external apps based on Mime Type
                if (!fileInfoList.get(position).isDirectory()) {
                    String extension = fileInfoList.get(position).getExtension().toLowerCase();
                    if (extension.equalsIgnoreCase("zip")) {
//                        showZipFileOptions(fileInfoList.get(position).getFilePath(),mFilePath);
                        String path = fileInfoList.get(position).getFilePath();
                        Bundle bundle = new Bundle();
                        bundle.putString(FileConstants.KEY_PATH, path);
                        bundle.putInt(BaseActivity.ACTION_VIEW_MODE, mViewMode);
                        bundle.putBoolean(FileConstants.KEY_ZIP, true);
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


                    } else {
                        FileUtils.viewFile(getActivity(), fileInfoList.get(position).getFilePath(), fileInfoList.get
                                (position).getExtension());
                    }

                } else {
                    Bundle bundle = new Bundle();
                    String path = fileInfoList.get(position).getFilePath();
                    bundle.putString(FileConstants.KEY_PATH, path);
                    bundle.putInt(BaseActivity.ACTION_VIEW_MODE, mViewMode);
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
                break;
            case 1:
            case 2:
            case 3:
            case 4:
                FileUtils.viewFile(getActivity(), fileInfoList.get(position).getFilePath(), fileInfoList.get(position)
                        .getExtension());
                break;

        }
    }

   /* private void showZipFileOptions(final String currentFilePath, final String currentDir) {
        final File currentFile = new File(currentFilePath);
        final CharSequence[] items = { getString(R.string.extract_here), getString(R.string.extract_to),
                getString(R.string.open_zip) };
        final AlertDialog.Builder builder = new AlertDialog.Builder(
                getActivity());
        builder.setTitle(getString(R.string.dialog_title_options));
        builder.setItems(items, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dInterface, int item) {
                switch (item) {
                    case 0:
                        new ExtractManager(FileListFragment.this)
                                .extract(currentFile, currentDir);

                        break;

                    case 1:
                        LayoutInflater factory = LayoutInflater
                                .from(getActivity());
                        final View OpenTxtView = factory.inflate(
                                R.layout.dialog_extract, null);
                        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(
                                getActivity());
                        builder.setTitle(getString(R.string.dialog_title_extract_file));
                        dialogBuilder.setView(OpenTxtView);
//                        alert1.setIcon(R.drawable.zip);

                        dialogBuilder.setPositiveButton(getString(R.string.msg_ok),
                                new DialogInterface.OnClickListener() {

                                    public void onClick(DialogInterface arg0,
                                                        int which) {

                                        EditText savefile = (EditText) dialogBuilder
                                                .findViewById(R.id.savetext);
                                        CheckBox current_chkbox = (CheckBox) dialogBuilder
                                                .findViewById(R.id.check_current);
                                        if (current_chkbox.isChecked() == true) {
                                            savefile.setText(file.getParent()
                                                    .toString());
                                        }
                                        String unzipfilepath = savefile
                                                .getText().toString();
                                        if (new File(unzipfilepath).exists()) {

                                            new ExtractManager(
                                                    FilebrowserULTRAActivity.this)
                                                    .extract(file
                                                                    .getAbsoluteFile(),
                                                            unzipfilepath);

                                        } else {
                                            showMessage("Path doesn't exist");
                                        }

                                        getFileList(unzipfilepath);
                                    }
                                });
                        dialogBuilder.setButton2("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int which) {

                                        dialogBuilder.dismiss();

                                    }
                                });

                        dialogBuilder.show();

                        break;
                    case 2:
                        Intent zipIntent = new Intent();
                        zipIntent.setAction(android.content.Intent.ACTION_VIEW);
                        zipIntent.setDataAndType(Uri.fromFile(currentFilePath),
                                "application/zip");
                        try {
                            startActivity(zipIntent);
                        } catch (Exception e) {
                            // TODO: handle exception
                            showMessage("No application to open Zip file");
                        }
                        break;
                }
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }*/

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

    public void toggleSelectAll(boolean selectAll) {
        fileListAdapter.clearSelection();
        for (int i = 0; i < fileListAdapter.getItemCount(); i++) {
            fileListAdapter.toggleSelectAll(i, selectAll);
        }
        SparseBooleanArray checkedItemPos = fileListAdapter.getSelectedItemPositions();
        ((BaseActivity) getActivity()).setSelectedItemPos(checkedItemPos);

        ((BaseActivity) getActivity()).getActionMode().setTitle(String.valueOf(fileListAdapter.getSelectedCount()
        ) + " selected");
        fileListAdapter.notifyDataSetChanged();

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
                RecyclerView.LayoutManager llm;
                if (mViewMode == FileConstants.KEY_LISTVIEW) {
                    llm = new LinearLayoutManager(getActivity());
                } else {
                    llm = new GridLayoutManager(getActivity(), 4);
                }
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.file_base, menu);
//        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_view_list:
                if (mViewMode != FileConstants.KEY_LISTVIEW) {
                    mViewMode = FileConstants.KEY_LISTVIEW;
                    switchView();
                }
                break;
            case R.id.action_view_grid:
                if (mViewMode != FileConstants.KEY_GRIDVIEW) {
                    mViewMode = FileConstants.KEY_GRIDVIEW;
                    switchView();
                }
                break;

            case R.id.action_sort_name_asc:
                sortFiles(fileInfoList, FileConstants.KEY_SORT_NAME);
                fileListAdapter.notifyDataSetChanged();
                break;
            case R.id.action_sort_name_desc:
                sortFiles(fileInfoList, FileConstants.KEY_SORT_NAME_DESC);
                fileListAdapter.notifyDataSetChanged();
                break;

            case R.id.action_sort_type_asc:
                sortFiles(fileInfoList, FileConstants.KEY_SORT_TYPE);
                fileListAdapter.notifyDataSetChanged();
                break;

            case R.id.action_sort_type_desc:
                sortFiles(fileInfoList, FileConstants.KEY_SORT_TYPE_DESC);
                fileListAdapter.notifyDataSetChanged();
                break;

            case R.id.action_sort_size_asc:
                sortFiles(fileInfoList, FileConstants.KEY_SORT_SIZE);
                fileListAdapter.notifyDataSetChanged();

                break;

            case R.id.action_sort_size_desc:
                sortFiles(fileInfoList, FileConstants.KEY_SORT_SIZE_DESC);
                fileListAdapter.notifyDataSetChanged();

                break;
            case R.id.action_sort_date_asc:
                sortFiles(fileInfoList, FileConstants.KEY_SORT_DATE);
                fileListAdapter.notifyDataSetChanged();
                break;
            case R.id.action_sort_date_desc:
                sortFiles(fileInfoList, FileConstants.KEY_SORT_DATE_DESC);
                fileListAdapter.notifyDataSetChanged();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void switchView() {
        Bundle bundle = new Bundle();
        bundle.putString(FileConstants.KEY_PATH, mFilePath);
        Intent intent = new Intent(getActivity(), BaseActivity.class);
        if (FileListFragment.this instanceof FileListDualFragment) {
            intent.setAction(BaseActivity.ACTION_DUAL_VIEW_FOLDER_LIST);
            intent.putExtra(BaseActivity.ACTION_DUAL_PANEL, true);

        } else {
            intent.setAction(BaseActivity.ACTION_VIEW_FOLDER_LIST);
            intent.putExtra(BaseActivity.ACTION_DUAL_PANEL, false);
            intent.putExtra(ACTION_VIEW_MODE, mViewMode);
            intent.putExtra(FileConstants.KEY_CATEGORY, mCategory);
        }

        intent.putExtras(bundle);
        startActivity(intent);
    }


    private void sortFiles(ArrayList<FileInfo> files, int sortMode) {

        switch (sortMode) {
            case 0:
                Collections.sort(files, FileUtils.comparatorByName);
                break;
            case 1:
                Collections.sort(files, FileUtils.comparatorByNameDesc);
                break;
            case 2:
                Collections.sort(files, FileUtils.comparatorByType);
                break;
            case 3:
                Collections.sort(files, FileUtils.comparatorByTypeDesc);
                break;
            case 4:
                Collections.sort(files, FileUtils.comparatorBySize);
                break;
            case 5:
                Collections.sort(files, FileUtils.comparatorBySizeDesc);
                break;
            case 6:
                Collections.sort(files, FileUtils.comparatorByDate);
                break;
            case 7:
                Collections.sort(files, FileUtils.comparatorByDateDesc);
                break;

        }
    }

    @Override
    public void onDestroy() {
//        Log.d("TAG", "on onDestroy--Fragment");
        super.onDestroy();

    }
}
