package com.siju.filemanager.filesystem;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.siju.filemanager.BaseActivity;
import com.siju.filemanager.R;
import com.siju.filemanager.common.Logger;
import com.siju.filemanager.filesystem.model.HomeLibraryInfo;
import com.siju.filemanager.filesystem.model.HomeStoragesInfo;
import com.siju.filemanager.filesystem.utils.FileUtils;
import com.siju.filemanager.model.SectionGroup;
import com.siju.filemanager.model.SectionItems;

import java.io.File;
import java.util.ArrayList;

import static com.siju.filemanager.filesystem.utils.FileUtils.getInternalStorage;

/**
 * Created by SIJU on 20-07-2016.
 */
public class HomeScreenFragment extends Fragment {

    View root;
    private Toolbar mToolbar;
    private int mResourceIds[];
    private String mLabels[];
    private int mCategoryIds[];

    private RecyclerView recyclerViewLibrary;
    private RecyclerView recyclerViewStorages;
    private HomeLibraryAdapter homeLibraryAdapter;
    private HomeStoragesAdapter homeStoragesAdapter;
    private LinearLayoutManager llmStorage;
    private GridLayoutManager gridLayoutManagerLibrary;

    private ArrayList<HomeLibraryInfo> homeLibraryInfoArrayList ;
    private ArrayList<HomeStoragesInfo> homeStoragesInfoArrayList ;
    public String STORAGE_INTERNAL, STORAGE_EXTERNAL;


    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.homescreen, container, false);
        return root;

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        initializeViews();
        initConstants();
        homeLibraryInfoArrayList = new ArrayList<>();
        homeStoragesInfoArrayList = new ArrayList<>();

        for (int i = 0; i < mResourceIds.length; i++) {
            homeLibraryInfoArrayList.add(new HomeLibraryInfo(mCategoryIds[i], mLabels[i],
                    mResourceIds[i], 0));
        }
        initializeStorageGroup();
        homeLibraryAdapter = new HomeLibraryAdapter(getActivity(), homeLibraryInfoArrayList);
        homeStoragesAdapter = new HomeStoragesAdapter(getActivity(), homeStoragesInfoArrayList);
        initListeners();
        Logger.log("TAG", "Homescreen--Librarylist=" + homeLibraryInfoArrayList.size() +
                "storage=" + homeStoragesInfoArrayList.size());

        recyclerViewLibrary.setAdapter(homeLibraryAdapter);
        recyclerViewStorages.setAdapter(homeStoragesAdapter);


    }

    private void initializeViews() {
/*          mToolbar = (Toolbar) root.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setHomeButtonEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        mToolbar.setTitle(getString(R.string.app_name));*/
        recyclerViewLibrary = (RecyclerView) root.findViewById(R.id.recyclerViewLibrary);
        recyclerViewStorages = (RecyclerView) root.findViewById(R.id.recyclerViewStorages);
        recyclerViewLibrary.setHasFixedSize(true);
        recyclerViewStorages.setHasFixedSize(true);
        llmStorage = new LinearLayoutManager(getActivity());
        gridLayoutManagerLibrary = new GridLayoutManager(getActivity(), 3);

        recyclerViewLibrary.setLayoutManager(gridLayoutManagerLibrary);

        recyclerViewLibrary.setItemAnimator(new DefaultItemAnimator());

        recyclerViewStorages.setLayoutManager(llmStorage);
        recyclerViewStorages.setItemAnimator(new DefaultItemAnimator());

    }

    private void initListeners() {
        homeLibraryAdapter.setOnItemClickListener(new HomeLibraryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                homeLibraryInfoArrayList.get(position).getCategoryName();
                FragmentTransaction ft = getActivity().getSupportFragmentManager()
                        .beginTransaction();
                Bundle args = new Bundle();
                args.putBoolean(FileConstants.KEY_HOME, true);

            }
        });

        homeStoragesAdapter.setOnItemClickListener(new HomeStoragesAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                FragmentTransaction ft = getActivity().getSupportFragmentManager()
                        .beginTransaction();
                Bundle args = new Bundle();
                args.putBoolean(FileConstants.KEY_HOME, true);
                if (position == 0) {

                    args.putString(FileConstants.KEY_PATH, FileUtils.getInternalStorage()
                            .getAbsolutePath());
//                    args.putInt(BaseActivity.ACTION_VIEW_MODE, mViewMode);
                    args.putInt(BaseActivity.ACTION_GROUP_POS, 0); // Storage Group
                    args.putInt(BaseActivity.ACTION_CHILD_POS, 1); // Internal Storage child
//          args.putInt(BaseActivity.ACTION_VIEW_MODE, mViewMode);

                } else {
                    args.putString(FileConstants.KEY_PATH, FileUtils.getExternalStorage()
                            .getAbsolutePath());
//                    args.putInt(BaseActivity.ACTION_VIEW_MODE, mViewMode);
                    args.putInt(BaseActivity.ACTION_GROUP_POS, 0); // Storage Group
                    args.putInt(BaseActivity.ACTION_CHILD_POS, 2); // Internal Storage child
                }
                StoragesFragment storagesFragment = new StoragesFragment();
                storagesFragment.setArguments(args);
                ft.replace(R.id.frame_home, storagesFragment);
                ft.addToBackStack(null);
                ft.commitAllowingStateLoss();
            }
        });
    }

    private void initConstants() {
        mResourceIds = new int[]{R.drawable.ic_library_images, R.drawable.ic_library_music,
                R.drawable.ic_library_videos, R.drawable.ic_library_docs,
                R.drawable.ic_library_downloads, R.drawable.ic_library_add};
        mLabels = new String[]{getActivity().getString(R.string
                .nav_menu_image), getActivity().getString(R.string
                .nav_menu_music), getActivity().getString(R.string
                .nav_menu_video), getActivity().getString(R.string
                .home_docs), getActivity().getString(R.string
                .downloads), getActivity().getString(R.string
                .home_add)};
        mCategoryIds = new int[]{FileConstants.HOME_CATEGORY.IMAGE.getValue(),
                FileConstants.HOME_CATEGORY.AUDIO.getValue(),
                FileConstants.HOME_CATEGORY.VIDEO.getValue(),
                FileConstants.HOME_CATEGORY.DOCS.getValue(),
                FileConstants.HOME_CATEGORY.DOWNLOADS.getValue(),
                FileConstants.HOME_CATEGORY.ADD.getValue()};
        STORAGE_INTERNAL = getResources().getString(R.string.nav_menu_internal_storage);
        STORAGE_EXTERNAL = getResources().getString(R.string.nav_menu_ext_storage);
    }

    private void initializeStorageGroup() {


        File internalSD = getInternalStorage();
        File extSD = FileUtils.getExternalStorage();

        long spaceLeft = getSpaceLeft(internalSD);
        long totalSpace = getTotalSpace(internalSD);
        int usedProgress = (int) (((float) spaceLeft / totalSpace) * 100);
        int remainingProgress = 100 - usedProgress;
        String spaceText = storageSpace(internalSD);

        homeStoragesInfoArrayList.add(new HomeStoragesInfo(STORAGE_INTERNAL, R.drawable
                .ic_storage_white,
                remainingProgress, spaceText));
        if (extSD != null) {
            spaceLeft = getSpaceLeft(extSD);
            totalSpace = getTotalSpace(extSD);
            usedProgress = (int) ((spaceLeft / totalSpace) * 100);
            remainingProgress = 100 - usedProgress;
            spaceText = storageSpace(extSD);
            homeStoragesInfoArrayList.add(new HomeStoragesInfo(STORAGE_EXTERNAL, R.drawable
                    .ic_ext_white,
                    remainingProgress, spaceText));
        }

    }

    private long getSpaceLeft(File file) {
        return file.getFreeSpace();
    }

    private long getTotalSpace(File file) {
        return file.getTotalSpace();
    }

    private String storageSpace(File file) {
        String freePlaceholder = " " + getResources().getString(R.string.msg_free) + " ";
        return FileUtils.getSpaceLeft(getActivity(), file) + freePlaceholder + FileUtils
                .getTotalSpace
                        (getActivity(), file);
    }
}
