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

package com.siju.acexplorer.main.view;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.siju.acexplorer.R;
import com.siju.acexplorer.analytics.Analytics;
import com.siju.acexplorer.billing.repository.BillingManager;
import com.siju.acexplorer.billing.repository.BillingStatus;
import com.siju.acexplorer.main.model.FavInfo;
import com.siju.acexplorer.main.model.FileConstants;
import com.siju.acexplorer.main.model.SectionGroup;
import com.siju.acexplorer.main.model.StorageItem;
import com.siju.acexplorer.main.model.groups.DrawerGroup;
import com.siju.acexplorer.main.model.helper.FileUtils;
import com.siju.acexplorer.premium.Premium;
import com.siju.acexplorer.settings.SettingsActivity;
import com.siju.acexplorer.theme.Theme;

import java.util.ArrayList;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import static com.siju.acexplorer.main.model.StorageUtils.getDownloadsDirectory;
import static com.siju.acexplorer.main.model.groups.Category.APP_MANAGER;
import static com.siju.acexplorer.main.model.groups.Category.TRASH;

/**
 * Created by Siju on 28 August,2017
 */
@SuppressWarnings("FieldCanBeLocal")
class NavigationDrawer implements View.OnClickListener {

    private final        String TAG                       = this.getClass().getSimpleName();
    private static final int    SETTINGS_REQUEST          = 1000;
    private static final String PLAYSTORE_URL             = "https://play.google.com/store/apps/details?id=";
    private static final String DYNAMIC_LINK              = "https://c3dba.app.goo.gl/acexplorer";
    static               int    DRAWER_HEADER_STORAGE_POS = 0;
    static               int    DRAWER_HEADER_FAV_POS     = 1;
    private static       int    DRAWER_HEADER_TOOLS_POS   = 3;

    private AppCompatActivity activity;
    private Context  context;

    private DrawerLayout          drawerLayout;
    private NavigationView        drawerPane;
    private ExpandableListView    expandableListView;
    private ExpandableListAdapter expandableListAdapter;
    private DrawerGenericAdapter  drawerGenericAdapter;
    private ImageView             imageInvite;
    private ActionBarDrawerToggle drawerToggle;
    private ListView              genericList;

    private ArrayList<SectionGroup> totalGroupData      = new ArrayList<>();
    private ArrayList<StorageItem> favoritesGroupChild = new ArrayList<>();
    private boolean                 isPremium;
    private MainUiView              uiView;
    private BillingManager billingManager;


    NavigationDrawer(AppCompatActivity activity, MainUiView uiView, Theme theme, BillingManager billingManager) {
        this.context = uiView.getContext();
        this.activity = activity;
        this.uiView = uiView;
        this.billingManager = billingManager;
        init();
        setTheme(theme);
        initListeners();
        setAdapter();
        uiView.getTotalGroupData();
    }

    private void init() {
        drawerLayout = uiView.findViewById(R.id.drawer_layout);
        drawerPane = uiView.findViewById(R.id.drawerPane);
        genericList = uiView.findViewById(R.id.listGeneric);
        expandableListView = uiView.findViewById(R.id.expandListDrawer);
        imageInvite = uiView.findViewById(R.id.imageInvite);
        registerContextMenu();
    }

    private void setTheme(Theme theme) {
        switch (theme) {
            case DARK:
                drawerPane.setBackgroundColor(ContextCompat.getColor(context, R.
                        color.dark_background));
                break;
            case LIGHT:
                drawerPane.setBackgroundColor(ContextCompat.getColor(context, R.
                        color.light_background));
                break;
        }
    }

    private void initListeners() {
        drawerToggle = new ActionBarDrawerToggle(activity, drawerLayout, R.string.navigation_drawer_open, R.string
                .navigation_drawer_close) {

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, 0);
            }
        };

        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int
                    childPosition, long id) {
                onDrawerItemClick(groupPosition, childPosition);
                return false;
            }
        });

        expandableListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                return false;
            }
        });
        imageInvite.setOnClickListener(this);
        genericList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                onGenericItemClick(isPremium ? position + 1 : position);
            }
        });


    }

    private void setAdapter() {
        drawerGenericAdapter = new DrawerGenericAdapter(context);
        genericList.setAdapter(drawerGenericAdapter);
    }

    private void registerContextMenu() {
        activity.registerForContextMenu(expandableListView);
    }

    void unregisterContextMenu() {
        activity.unregisterForContextMenu(expandableListView);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imageInvite:
                Analytics.getLogger().appInviteClicked();
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, context.getString(R.string.app_invite_msg) + "\n\n" + DYNAMIC_LINK);
                sendIntent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.app_invite_title));
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent,
                        context.getResources().getText(R.string.action_share)));
                break;

        }
    }

    private void onGenericItemClick(int position) {

        switch (position) {
            case 0:
                Analytics.getLogger().unlockFullClicked();
                if (billingManager.getInAppBillingStatus().equals(BillingStatus
                        .UNSUPPORTED)) {

                    Toast.makeText(context, context.getString(R.string.billing_unsupported), Toast
                            .LENGTH_SHORT).show();
                }
                else {
                    Premium premium = new Premium(activity, billingManager);
                    premium.showPremiumDialog();
                }
                break;
            case 1: // Rate us
                Analytics.getLogger().rateUsClicked();
                Intent intent = new Intent(Intent.ACTION_VIEW);
                // Try Google play
                intent.setData(Uri
                        .parse("market://details?id=" + context.getPackageName()));
                if (FileUtils.isPackageIntentUnavailable(context, intent)) {
                    // Market (Google play) app seems not installed,
                    // let's try to open a webbrowser
                    intent.setData(Uri.parse(PLAYSTORE_URL + context.getPackageName()));
                    if (FileUtils.isPackageIntentUnavailable(context, intent)) {
                        Toast.makeText(context,
                                context.getString(R.string.msg_error_not_supported),
                                Toast.LENGTH_SHORT).show();
                    }
                    else {
                        startActivity(intent);
                    }
                }
                else {
                    startActivity(intent);
                }
                drawerLayout.closeDrawer(drawerPane);
                break;
            case 2: // Settings
                Analytics.getLogger().settingsDisplayed();
                Intent intent1 = new Intent(activity, SettingsActivity.class);
                final int enter_anim = android.R.anim.fade_in;
                final int exit_anim = android.R.anim.fade_out;
                activity.overridePendingTransition(enter_anim, exit_anim);
                activity.startActivityForResult(intent1, SETTINGS_REQUEST);
                expandableListView.setSelection(0);
                drawerLayout.closeDrawer(drawerPane);
                break;
        }
    }

    private void startActivity(Intent intent) {
        activity.startActivity(intent);
    }

    private void onDrawerItemClick(int groupPos, int childPos) {
        String path = totalGroupData.get(groupPos).getmChildItems().get(childPos).getPath();
        Analytics.getLogger().drawerItemClicked();
        displaySelectedGroup(groupPos, childPos, path);
        drawerLayout.closeDrawer(drawerPane);
    }

    private void displaySelectedGroup(int groupPos, int childPos, String path) {
        DrawerGroup drawerGroup = DrawerGroup.getGroupFromPos(groupPos);
        switch (drawerGroup) {
            case STORAGE:
            case FAVORITES:
                uiView.onStorageItemClicked(path);
                break;
            case LIBRARY:
                uiView.onLibraryItemClicked(childPos + 1);
                break;
            case TOOLS:
                int clickedPos;
                if (childPos == 0) {
                    clickedPos = APP_MANAGER.getValue();
                }
                else {
                    clickedPos = TRASH.getValue();
                }
                uiView.onLibraryItemClicked(clickedPos);
                break;
        }
    }

    void updateFavorites(ArrayList<FavInfo> favInfoArrayList) {
        for (int i = 0; i < favInfoArrayList.size(); i++) {
            StorageItem favItem = new StorageItem(favInfoArrayList.get(i).getFileName(),
                    favInfoArrayList.get(i)
                            .getFilePath(), R.drawable.ic_fav_folder, favInfoArrayList.get(i)
                    .getFilePath(), 0, null, null);
            if (!favoritesGroupChild.contains(favItem)) {
                favoritesGroupChild.add(favItem);
            }
        }
        expandableListAdapter.notifyDataSetChanged();

    }

    void removeFavorites(ArrayList<FavInfo> favInfoArrayList) {
        for (int i = 0; i < favInfoArrayList.size(); i++) {
            StorageItem favItem = new StorageItem(favInfoArrayList.get(i).getFileName(),
                    favInfoArrayList.get(i)
                            .getFilePath(), R.drawable.ic_fav_folder, favInfoArrayList.get(i)
                    .getFilePath(), 0, null, null);
            favoritesGroupChild.remove(favItem);
        }
        expandableListAdapter.notifyDataSetChanged();
    }

    FavInfo removeFavorite(int groupPos, int childPos) {
        String path = totalGroupData.get(groupPos).getmChildItems().get(childPos).getPath();
        String name = totalGroupData.get(groupPos).getmChildItems().get(childPos)
                .getFirstLine();
        FavInfo favInfo = new FavInfo();
        favInfo.setFileName(name);
        favInfo.setFilePath(path);
        favoritesGroupChild.remove(childPos);
        expandableListAdapter.notifyDataSetChanged();
        return favInfo;
    }

    private void resetFavouritesGroup() {
        for (int i = favoritesGroupChild.size() - 1; i >= 0; i--) {
            if (!favoritesGroupChild.get(i).getSecondLine().equalsIgnoreCase
                    (INSTANCE.getDownloadsDirectory())) {
                favoritesGroupChild.remove(i);
            }
        }
        expandableListAdapter.notifyDataSetChanged();
        expandableListView.smoothScrollToPosition(0);
    }

    void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch (requestCode) {
            case SETTINGS_REQUEST:
                if (intent == null) {
                    return;
                }
                if (intent.getBooleanExtra(FileConstants.PREFS_RESET, false)) {
                    resetFavouritesGroup();
                    uiView.resetFavoritesData();
                }
                break;
        }
    }

    void setPremium() {
        isPremium = true;
        drawerGenericAdapter.setPremium();
    }

    void syncDrawerState() {
        drawerToggle.syncState();
    }

    void openDrawer() {
        drawerLayout.openDrawer(GravityCompat.START);
    }

    void onTotalGroupDataFetched(ArrayList<SectionGroup> totalData) {
        totalGroupData = totalData;
        favoritesGroupChild = totalGroupData.get(1).getmChildItems();
        expandableListAdapter = new ExpandableListAdapter(context, totalGroupData);
        expandableListView.setAdapter(expandableListAdapter);
        expandableListView.expandGroup(DRAWER_HEADER_STORAGE_POS);
        expandableListView.expandGroup(DRAWER_HEADER_TOOLS_POS);

    }
}
