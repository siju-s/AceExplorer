package com.siju.acexplorer.filesystem.groups;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.siju.acexplorer.R;
import com.siju.acexplorer.common.SharedPreferenceWrapper;
import com.siju.acexplorer.filesystem.model.FavInfo;
import com.siju.acexplorer.filesystem.utils.FileUtils;
import com.siju.acexplorer.model.SectionGroup;
import com.siju.acexplorer.model.SectionItems;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.siju.acexplorer.BaseActivity.PREFS_FIRST_RUN;


/**
 * Created by SJ on 20-01-2017.
 */

public class DrawerItems {

    private String DOWNLOADS;
    private String IMAGES;
    private String VIDEO;
    private String MUSIC;
    private String DOCS;

    private Context context;
    private final ArrayList<SectionGroup> totalGroup = new ArrayList<>();
    private final ArrayList<SectionItems> favouritesGroupChild = new ArrayList<>();
    private SharedPreferenceWrapper sharedPreferenceWrapper = new SharedPreferenceWrapper();
    private SharedPreferences mSharedPreferences;
    private ArrayList<FavInfo> savedFavourites = new ArrayList<>();
    private List<String> drawerListHeaders;


    public DrawerItems(Context context) {
        this.context = context;
    }


    private void initConstants() {

        DOWNLOADS = context.getResources().getString(R.string.downloads);
        MUSIC = context.getResources().getString(R.string.nav_menu_music);
        VIDEO = context.getResources().getString(R.string.nav_menu_video);
        DOCS = context.getResources().getString(R.string.nav_menu_docs);
        IMAGES = context.getResources().getString(R.string.nav_menu_image);
        String[] listDataHeader = context.getResources().getStringArray(R.array.expand_headers);
        drawerListHeaders = Arrays.asList(listDataHeader);
    }

    private void initializeGroups() {
        initConstants();
        initializeFavouritesGroup();
        initializeLibraryGroup();
    }

    private void initializeFavouritesGroup() {

        addDefaultFavorites();
        addSavedFavorites();
        populateDrawerItems(new SectionGroup(drawerListHeaders.get(1), favouritesGroupChild));
    }

    private void addDefaultFavorites() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean isFirstRun = sharedPreferences.getBoolean(PREFS_FIRST_RUN, true);
        if (isFirstRun) {
            String path = FileUtils
                    .getAbsolutePath(FileUtils.getDownloadsDirectory());
            favouritesGroupChild.add(new SectionItems(DOWNLOADS, path, R.drawable.ic_download,
                    path, 0));
            FavInfo favInfo = new FavInfo();
            favInfo.setFileName(DOWNLOADS);
            favInfo.setFilePath(path);
            sharedPreferenceWrapper.addFavorite(context, favInfo);

        }
    }

    private void addSavedFavorites() {
        savedFavourites = sharedPreferenceWrapper.getFavorites(context);

        if (savedFavourites != null && savedFavourites.size() > 0) {
            for (int i = 0; i < savedFavourites.size(); i++) {
                String savedPath = savedFavourites.get(i).getFilePath();
                favouritesGroupChild.add(new SectionItems(savedFavourites.get(i).getFileName(),
                        savedPath, R.drawable
                        .ic_fav_folder,
                        savedPath, 0));
            }
        }
    }

    private void populateDrawerItems(SectionGroup group) {
        totalGroup.add(group);
    }

    private void initializeLibraryGroup() {

        populateDrawerItems(new SectionGroup(drawerListHeaders.get(2), addLibraryItems()));
    }

    private ArrayList<SectionItems> addLibraryItems() {
        ArrayList<SectionItems> libraryGroupChild = new ArrayList<>();
        libraryGroupChild.add(new SectionItems(MUSIC, null, R.drawable.ic_music_white, null, 0));
        libraryGroupChild.add(new SectionItems(VIDEO, null, R.drawable.ic_video_white, null, 0));
        libraryGroupChild.add(new SectionItems(IMAGES, null, R.drawable.ic_photos_white, null, 0));
        libraryGroupChild.add(new SectionItems(DOCS, null, R.drawable.ic_file_white, null, 0));
        return libraryGroupChild;
    }

}
