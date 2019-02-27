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

package com.siju.acexplorer.storage.view;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.siju.acexplorer.R;
import com.siju.acexplorer.ads.AdsView;
import com.siju.acexplorer.analytics.Analytics;
import com.siju.acexplorer.base.view.BaseActivity;
import com.siju.acexplorer.billing.BillingStatus;
import com.siju.acexplorer.common.types.FileInfo;
import com.siju.acexplorer.logging.Logger;
import com.siju.acexplorer.main.model.FavInfo;
import com.siju.acexplorer.main.model.FileConstants;
import com.siju.acexplorer.main.model.SharedPreferenceWrapper;
import com.siju.acexplorer.main.model.groups.Category;
import com.siju.acexplorer.main.model.helper.FileUtils;
import com.siju.acexplorer.main.view.AceActivity;
import com.siju.acexplorer.main.view.DrawerListener;
import com.siju.acexplorer.main.view.dialog.DialogHelper;
import com.siju.acexplorer.permission.PermissionUtils;
import com.siju.acexplorer.storage.model.BackStackModel;
import com.siju.acexplorer.storage.model.ViewMode;
import com.siju.acexplorer.storage.model.backstack.BackStackInfo;
import com.siju.acexplorer.storage.model.backstack.NavigationCallback;
import com.siju.acexplorer.storage.model.backstack.NavigationInfo;
import com.siju.acexplorer.storage.model.operations.OperationProgress;
import com.siju.acexplorer.storage.model.operations.Operations;
import com.siju.acexplorer.storage.modules.picker.view.DialogBrowseFragment;
import com.siju.acexplorer.storage.modules.zip.ZipCommunicator;
import com.siju.acexplorer.storage.modules.zip.ZipViewer;
import com.siju.acexplorer.theme.Theme;
import com.siju.acexplorer.utils.InstallHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.siju.acexplorer.main.model.FileConstants.KEY_CATEGORY;
import static com.siju.acexplorer.main.model.groups.Category.DOWNLOADS;
import static com.siju.acexplorer.main.model.groups.Category.FAVORITES;
import static com.siju.acexplorer.main.model.groups.Category.FILES;
import static com.siju.acexplorer.main.model.groups.CategoryHelper.checkIfFileCategory;
import static com.siju.acexplorer.main.model.groups.CategoryHelper.showLibSpecificNavigation;

/**
 * Created by Siju on 02 September,2017
 */
@SuppressLint("ClickableViewAccessibility")
public class StoragesUiView extends CoordinatorLayout implements
                                                      NavigationCallback
{

    private static final int     DIALOG_FRAGMENT = 5000;
    private static final int     SAF_REQUEST     = 2000;
    private static final boolean isRootMode      = true;
    private final        String  TAG             = this.getClass().getSimpleName();

    private Fragment          fragment;
    private CoordinatorLayout mainContainer;
    private Button            buttonPathSelect;
    private FloatingView      floatingView;
    private SharedPreferences preferences;
    private AdsView           adsView;
    private StorageBridge     bridge;

    private Category                         category;
    private NavigationInfo                   navigationInfo;
    private BackStackInfo                    backStackInfo;
    private Theme                            currentTheme;
    private DrawerListener                   drawerListener;
    private MenuControls                     menuControls;
    private ZipViewer                        zipViewer;
    private Intent                           operationIntent;
    private Dialog                           dialog;
    private StoragesUiView.FavoriteOperation favoriteListener;
    private FilesView                        filesView;

    private String  currentDir;
    private String  mSelectedPath;
    private int     currentOrientation;
    private boolean homeScreenEnabled;
    private boolean showHidden;
    private boolean dualModeEnabled;
    private boolean isZipViewer;
    private boolean isHomeClicked;
    private boolean mInstanceStateExists;
    private boolean isSAFShown;


    public StoragesUiView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public static StoragesUiView inflate(ViewGroup parent) {
        return (StoragesUiView) LayoutInflater.from(parent.getContext()).inflate(R.layout.main_list,
                                                                                 parent, false);
    }

    private void setTheme() {
        currentTheme = ((BaseActivity) getActivity()).getCurrentTheme();
        if (currentTheme == Theme.DARK) {
            mainContainer.setBackgroundColor(ContextCompat.getColor(getContext(), R.color
                    .dark_background));
        }
        filesView.setTheme(currentTheme);
        floatingView.setTheme(currentTheme);
    }

    public void setFragment(Fragment fragment) {
        this.fragment = fragment;
    }

    public void setBridgeRef(StorageBridge bridge) {
        this.bridge = bridge;
    }

    void initialize() {
        adsView = new AdsView(this);
        adsView.setAdResultListener(adResultListener);
        filesView = new FilesView(getActivity(), this);
        preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        setTheme();
        checkBillingStatus();
        registerReceivers();
        navigationInfo = new NavigationInfo(this, this);
        backStackInfo = new BackStackInfo();
        currentOrientation = ((AceActivity) getActivity()).getConfiguration().orientation;
        getPreferences();
        getArgs();
        filesView.setCurrentDir(currentDir);
        filesView.setCategory(category);
        setupMenuControls();
        filesView.setMenuControls(menuControls);
        if (shouldShowPathNavigation()) {
            navigationInfo.setNavDirectory(currentDir, homeScreenEnabled, category);
        } else {
            navigationInfo.addHomeNavButton(homeScreenEnabled, category);
        }
        backStackInfo.addToBackStack(currentDir, category);
        refreshList();
        createDualFrag();
    }

    private void setupMenuControls() {
        menuControls = new MenuControls(getActivity(), this, currentTheme);
        menuControls.setCategory(category);
        menuControls.setCurrentDir(currentDir);
    }

    private void checkBillingStatus() {
        BillingStatus billingStatus = bridge.checkBillingStatus();
        switch (billingStatus) {
            case PREMIUM:
                onPremiumVersion();
                break;
            case UNSUPPORTED:
            case FREE:
                onFreeVersion();
                break;
        }
    }

    private AdsView.AdResultListener adResultListener = new AdsView.AdResultListener() {
        @Override
        public void onFreeVersion() {
            StoragesUiView.this.onFreeVersion();
        }

        @Override
        public void onPremiumVersion() {
            StoragesUiView.this.onPremiumVersion();
        }
    };

    private void onFreeVersion() {
        if (getActivity() == null || getActivity().isFinishing()) {
            return;
        }
        adsView.showAds();
    }

    private void onPremiumVersion() {
        adsView.hideAds();
    }

    private void registerReceivers() {
        adsView.register();
    }


    /**
     * Show dual pane in Landscape mode
     */
    public void showDualPane() {
        // For Files category only, show dual pane
        dualModeEnabled = true;
        filesView.refreshSpan(((AceActivity) getActivity()).getConfiguration());
    }

    private void getPreferences() {
        Bundle bundle = bridge.getUserPrefs();
        filesView.setViewMode(bundle.getInt(FileConstants.PREFS_VIEW_MODE, ViewMode.LIST));
        filesView.setGridCols(bundle.getInt(FileConstants.KEY_GRID_COLUMNS, 0));
        homeScreenEnabled = bundle.getBoolean(FileConstants.PREFS_HOMESCREEN, true);
        showHidden = bundle.getBoolean(FileConstants.PREFS_HIDDEN, false);
    }

    private void getArgs() {
        if (getArguments() != null) {
            currentDir = getArguments().getString(FileConstants.KEY_PATH);
            category = (Category) getArguments().getSerializable(KEY_CATEGORY);
            isZipViewer = getArguments().getBoolean(FileConstants.KEY_ZIP, false);
            dualModeEnabled = getArguments().getBoolean(FileConstants.KEY_DUAL_ENABLED, false);

            if (checkIfLibraryCategory(category)) {
                floatingView.hideFab();
            } else {
                floatingView.showFab();
            }
            navigationInfo.showNavigationView();
            if (shouldShowPathNavigation()) {
                navigationInfo.setInitialDir(currentDir);
            }
//            mLastSinglePaneDir = currentDir;
        }
    }

    private Bundle getArguments() {
        return fragment.getArguments();
    }

    private void createDualFrag() {
        if (dualModeEnabled && fragment instanceof FileList) {
            bridge.showDualFrame();
            showDualPane();
        }
    }

    boolean checkIfLibraryCategory(Category category) {
        return !category.equals(FILES) && !category.equals(DOWNLOADS);
    }


    private boolean isZipViewable(String filePath) {
        return filePath.toLowerCase().endsWith(".zip");
    }

    public void setPremium() {
        adsView.hideAds();
    }


    public void onPause() {
        adsView.pauseAds();
        filesView.pauseAutoPlayVid();
    }


    public void onResume() {
        adsView.resumeAds();
    }


    public void handleActivityResult(int requestCode, int resultCode, Intent intent) {
        switch (requestCode) {
            case DIALOG_FRAGMENT:
                if (resultCode == AppCompatActivity.RESULT_OK) {
                    mSelectedPath = intent.getStringExtra("PATH");
                    if (buttonPathSelect != null) {
                        buttonPathSelect.setText(mSelectedPath);
                    }
                }
                break;
            case SAF_REQUEST:
                String uriString = preferences.getString(FileConstants.SAF_URI, null);
                Uri oldUri = uriString != null ? Uri.parse(uriString) : null;

                if (resultCode == Activity.RESULT_OK) {
                    Analytics.getLogger().SAFResult(true);
                    Uri treeUri = intent.getData();
                    bridge.handleSAFResult(operationIntent, treeUri, isRooted(), intent.getFlags());

                } else {
                    Analytics.getLogger().SAFResult(false);
                    // If not confirmed SAF, or if still not writable, then revert settings.
                    if (oldUri != null) {
                        bridge.saveOldSAFUri(oldUri.toString());
                    }

                    Toast.makeText(getContext(), getResources().getString(R.string
                                                                                  .access_denied_external),
                                   Toast.LENGTH_LONG).show();
                }
            case InstallHelper.UNKNOWN_APPS_INSTALL_REQUEST:
                if (resultCode == AppCompatActivity.RESULT_OK) {
                    filesView.openInstallScreen();
                }
                break;
        }
    }

    private boolean isRooted() {
        return isRootMode;
    }

    void openZipViewer(String path) {
        if (path == null) {
            return;
        }
        String extension = FileUtils.getExtensionWithDot(path);
        Analytics.getLogger().zipViewer(extension);
        filesView.calculateScroll(currentDir);
        isZipViewer = true;
        zipViewer = new ZipViewer(zipCommunicator, fragment, path);
        refreshList();
    }


    boolean isTrashEnabled() {
        return false;
//        return isTrashEnabled; TODO COmmented this for future use
    }

    /**
     * @return false to avoid call to super.onBackPressed()
     */
    public boolean onBackPressed() {

        if (filesView.isPeekMode()) {
            filesView.endPeekMode();
        } else if (menuControls.isSearch()) {
            menuControls.endSearch();
            return false;
        } else if (isZipMode()) {
            if (isHomeClicked) {
                zipViewer.onBackPressed(null);
                return true;
            } else {
                zipViewer.onBackPressed();
            }
        } else if (filesView.isActionModeActive() && !menuControls.isPasteOp()) {
            menuControls.endActionMode();
        } else {
            return backOperation();
        }

        return false;
    }


    public boolean isZipMode() {
        return isZipViewer;
    }


    private boolean backOperation() {

        if (checkIfBackStackExists()) {
            filesView.removeScrolledPos(currentDir);
            backStackInfo.removeEntryAtIndex(backStackInfo.getBackStack().size() - 1);

            String currentDir = backStackInfo.getDirAtPosition(backStackInfo.getBackStack().size
                    () - 1);
            Category currentCategory = backStackInfo.getCategoryAtPosition(backStackInfo
                                                                                   .getBackStack
                                                                                           ()
                                                                                   .size() - 1);
            category = currentCategory;
            this.currentDir = currentDir;
            filesView.setCategory(category);
            filesView.setCurrentDir(currentDir);
            menuControls.setCategory(category);
            menuControls.setCurrentDir(currentDir);
            menuControls.setupSortVisibility();
            filesView.setBucketName(null);
            Log.d(TAG, "backOperation: category:" + category);

            if (checkIfFileCategory(currentCategory)) {
//                navigationInfo.setInitialDir();
                if (shouldShowPathNavigation()) {
                    navigationInfo.setInitialDir(currentDir);
                    navigationInfo.setNavDirectory(currentDir, homeScreenEnabled,
                                                   currentCategory);
                } else {
                    navigationInfo.addHomeNavButton(homeScreenEnabled, currentCategory);
                }
            } else if (showLibSpecificNavigation(currentCategory)) {
                navigationInfo.addLibSpecificNavButtons(homeScreenEnabled, category, filesView.getBucketName());
            } else {
                navigationInfo.addHomeNavButton(homeScreenEnabled, currentCategory);
                floatingView.hideFab();
            }
            refreshList();
//            menuControls.setTitleForCategory(currentCategory);
            if (currentCategory.equals(FILES)) {
                floatingView.showFab();
            }

            return false;
        } else {
            removeDualFileFragment();
            if (!homeScreenEnabled) {
                getActivity().finish();
            }
            return true;
        }
    }

    private boolean checkIfBackStackExists() {
        int backStackSize = backStackInfo.getBackStack().size();
        Logger.log(TAG, "checkIfBackStackExists --size=" + backStackSize + "homeCLicked:" + isHomeClicked);

        if (isHomeClicked) {
            return false;
        }

        if (backStackSize == 1) {
            backStackInfo.clearBackStack();
            return false;
        } else if (backStackSize > 1) {
            return true;
        }
        return false;
    }


    private boolean shouldShowPathNavigation() {
        return category.equals(FILES) || category.equals(DOWNLOADS);
    }

    void onReloadList(String path) {
        if (shouldShowPathNavigation()) {
            navigationInfo.setInitialDir(path);
        }
        if (checkIfLibraryCategory(category)) {
            floatingView.hideFab();
        } else {
            floatingView.showFab();
        }
        if (shouldShowPathNavigation()) {
            navigationInfo.setNavDirectory(currentDir, homeScreenEnabled, category);
        } else if (showLibSpecificNavigation(category)) {
            navigationInfo.addLibSpecificNavButtons(homeScreenEnabled, category, filesView.getBucketName());
        } else {
            navigationInfo.addHomeNavButton(homeScreenEnabled, category);
        }
        backStackInfo.addToBackStack(path, category);
    }

    public void refreshList() {
        if (!hasStoragePermission()) {
            return;
        }
        filesView.refreshList();
        if (isZipMode()) {
            zipViewer.loadData();
        } else {
            bridge.loadData(currentDir, category, filesView.getId());
        }
    }

    /**
     * Called from {@link #onBackPressed()} . Does the following:
     * 1. If homescreen enabled, returns to home screen
     * 2. If homescreen disabled, exits the app
     */
    private void removeDualFileFragment() {

        Fragment dualFragment = getActivity().getSupportFragmentManager().findFragmentById(R.id.frame_container_dual);

        backStackInfo.clearBackStack();
        Logger.log(TAG, "RemoveFragmentFromBackStack--dualFragment=" + dualFragment);

        if (dualFragment != null) {
            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            ft.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right, R.anim
                    .enter_from_right, R.anim
                                           .exit_to_left);
            ft.remove(dualFragment);
            ft.commitAllowingStateLoss();
        }
    }

    AppCompatActivity getActivity() {
        return (AppCompatActivity) fragment.getActivity();
    }

    private boolean hasStoragePermission() {
        return PermissionUtils.hasStoragePermission();
    }


    List<FileInfo> getFileList() {
        return filesView.getFileList();
    }


    public void setViewMode(int viewMode) {
        filesView.setViewMode(viewMode);
        menuControls.updateMenuTitle(viewMode == ViewMode.LIST ? ViewMode.GRID : ViewMode.LIST);
        switchView();
    }

    void passViewMode() {
        if (dualModeEnabled) {
            ((AceActivity) getActivity()).switchView(filesView.getViewMode(), !(fragment instanceof DualPaneList));
        }
    }

    void switchView() {
        int viewMode = filesView.getNewViewMode();
        bridge.saveSettingsOnExit(filesView.getGridCols(), viewMode);
        filesView.switchView();
    }

    SparseBooleanArray getSelectedItems() {
        return filesView.getSelectedItems();
    }

    void showRenameDialog(FileInfo fileInfo, String text) {
        filesView.showRenameDialog(fileInfo, text);
    }


    public void showSAFDialog(String path, Intent intent) {
        dismissDialog();
        Analytics.getLogger().SAFShown();
        operationIntent = intent;
        isSAFShown = true;
        String title = getContext().getString(R.string.needsaccess);
        String[] texts = new String[]{title, getContext().getString(R.string
                                                                            .needs_access_summary, path),
                                      getContext().getString(R.string.open), getContext().getString(R.string
                                                                                                            .dialog_cancel)};
        DialogHelper.showSAFDialog(getContext(), R.layout.dialog_saf,
                                   texts, alertDialogListener);
    }

    public void dismissDialog() {
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    public void setDialog(Dialog dialog) {
        this.dialog = dialog;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void triggerStorageAccessFramework() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        if (getActivity().getPackageManager().resolveActivity(intent, 0) != null) {
            fragment.startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE),
                                            SAF_REQUEST);
        } else {
            Toast.makeText(getContext(), getContext().getString(R.string.msg_error_not_supported)
                    , Toast.LENGTH_LONG).show();
        }
    }

    public void onFileExists(Operations operation) {
        switch (operation) {
            case FOLDER_CREATION:
            case FILE_CREATION:
            case EXTRACT:
            case RENAME:
                final EditText editText = dialog.findViewById(R.id.editFileName);
                editText.setError(getContext().getString(R.string.msg_file_exists));
                break;
            case HIDE:
                Toast.makeText(getContext(), getContext().getString(R.string.msg_file_exists),
                               Toast.LENGTH_SHORT).show();
                break;
        }

    }

    public void showConflictDialog(final List<FileInfo> conflictFiles,
                                   List<FileInfo> destFiles, final String destinationDir, final
                                   boolean isMove,
                                   final DialogHelper.PasteConflictListener pasteConflictListener)
    {
        Analytics.getLogger().conflictDialogShown();
        DialogHelper.showConflictDialog(getContext(), conflictFiles, destFiles, destinationDir,
                                        isMove, pasteConflictListener);

    }

    public void showPasteProgressDialog(String destinationDir, List<FileInfo> files,
                                        boolean isMove)
    {
        new OperationProgress().showPasteProgress(getContext(), destinationDir, files,
                                                  isMove);
    }

    public void deleteFiles(ArrayList<FileInfo> filesToDelete) {
        if (filesView.isMediaScannerActive() && filesView.isMediaScanning(filesToDelete.get(0).getFilePath())) {
            onOperationFailed(Operations.DELETE);
            return;
        }
        bridge.deleteFiles(filesToDelete);
    }


    @SuppressWarnings("unused")
    public void onOperationFailed(Operations operation) {
        Toast.makeText(getContext(), R.string.msg_operation_failed, Toast
                .LENGTH_SHORT).show();
    }

    public void sortFiles(int position) {
        bridge.persistSortMode(position);
        refreshList();
        if (dualModeEnabled) {
            ((AceActivity) getActivity()).refreshList(
                    !(fragment instanceof DualPaneList)); //Intentional negation to make the other pane reflect changes
        }
    }

    public void getPermissions(String filePath, boolean directory) {
        bridge.getFilePermissions(filePath, directory);
    }

    public void hideUnHideFiles(ArrayList<FileInfo> infoList, ArrayList<Integer> pos) {
        bridge.hideUnHideFiles(infoList, pos);
    }

    public void onExtractPositiveClick(Dialog dialog, String currentFilePath, String newFileName,
                                       boolean isChecked)
    {

        this.dialog = dialog;
        if (mSelectedPath == null) {
            mSelectedPath = currentDir;
        }
        bridge.onExtractPositiveClick(currentFilePath, newFileName, isChecked, mSelectedPath);
    }

    public void showSelectPathDialog(Button buttonPathSelect) {
        this.buttonPathSelect = buttonPathSelect;
        DialogBrowseFragment dialogFragment = new DialogBrowseFragment();
        dialogFragment.setTargetFragment(fragment, DIALOG_FRAGMENT);
        dialogFragment.setStyle(DialogBrowseFragment.STYLE_NORMAL, getThemeStyle());
        dialogFragment.show(fragment.getFragmentManager(), "Browse Fragment");
    }

    private int getThemeStyle() {
        switch (currentTheme) {
            case DARK:
                return R.style.BaseDarkTheme;
            case LIGHT:
                return R.style.BaseLightTheme;
        }
        return R.style.BaseDarkTheme;
    }

    public void onInvalidName(Operations operation) {
        switch (operation) {
            case EXTRACT:
            case FILE_CREATION:
            case FOLDER_CREATION:
                final EditText editText = dialog.findViewById(R.id.editFileName);
                editText.setError(getContext().getString(R.string.msg_error_invalid_name));
                break;
        }
    }

    public void onPermissionsFetched(ArrayList<Boolean[]> permissionList) {
        menuControls.onPermissionsFetched(permissionList);

    }

    public int getSortMode() {
        return bridge.getSortMode();
    }

    public void onCompressPosClick(Dialog dialog, String newFileName,
                                   String extension, ArrayList<FileInfo> paths)
    {
        this.dialog = dialog;
        String newFilePath = currentDir + File.separator + newFileName + extension;
        bridge.onCompressPosClick(newFilePath, paths);
    }

    public void onPermissionSetError() {
        Toast.makeText(getContext(), getContext().getString(R.string.error), Toast.LENGTH_SHORT)
             .show();
    }

    public void onPermissionsSet() {
        refreshList();
    }

    public void setPermissions(String path, boolean isDir, String permissions) {
        bridge.setPermissions(path, isDir, permissions);
    }

    public void removeHomeFromNavPath() {
        homeScreenEnabled = false;
        navigationInfo.removeHomeFromNavPath();
    }

    public void openDrawer() {
        drawerListener.onDrawerIconClicked();
    }

    public void setDrawerListener(DrawerListener drawerListener) {
        this.drawerListener = drawerListener;
    }

    public void performVoiceSearch(String query) {
        Analytics.getLogger().searchClicked(true);
        menuControls.performVoiceSearch(query);
    }

    public void onQueryTextChange(String query) {
        filesView.onQueryChange(query);
    }

    public void onPasteAction(boolean isMove, ArrayList<FileInfo> filesToPaste, String
            destinationDir)
    {
        if (filesView.isMediaScannerActive() && filesView.isMediaScanning(destinationDir)) {
            onOperationFailed(Operations.COPY);
            return;
        }
        bridge.startPasteOperation(destinationDir, isMove, isRooted(), filesToPaste);
    }


    public void syncDrawer() {
        if (drawerListener != null) {
            drawerListener.syncDrawer();
        }
    }

    public void showZipProgressDialog(ArrayList<FileInfo> files, String destinationPath) {
        new OperationProgress().showZipProgressDialog(getContext(), files, destinationPath);
    }

    public void showExtractDialog(Intent intent) {
        new OperationProgress().showExtractProgressDialog(getContext(), intent);
    }


    private boolean isFilesCategory() {
        return category.equals(FILES);
    }

    void setDualPaneState() {
        boolean isDualPaneInFocus = fragment instanceof DualPaneList;
        ((AceActivity) getActivity()).setDualPaneFocusState(isDualPaneInFocus);
    }

    @Override
    public void onHomeClicked() {
        boolean isDualPaneInFocus = fragment instanceof DualPaneList;
        ((AceActivity) getActivity()).setDualPaneFocusState(isDualPaneInFocus);

        isHomeClicked = true;
        menuControls.endActionMode();
        filesView.onHomeClicked();
        //removeDualFileFragment();
        getActivity().onBackPressed();
    }

    @Override
    public void onNavButtonClicked(String dir) {
        boolean isDualPaneInFocus = fragment instanceof DualPaneList;
        ((AceActivity) getActivity()).setDualPaneFocusState(isDualPaneInFocus);

        if (filesView.isActionModeActive() && !menuControls.isPasteOp()) {
            menuControls.endActionMode();
        }
        if (isZipMode()) {
            menuControls.setCurrentDir(dir);
            zipViewer.onBackPressed(dir);
        } else {
            currentDir = dir;
            menuControls.setCurrentDir(currentDir);
            int position = 0;
            ArrayList<BackStackModel> backStack = backStackInfo.getBackStack();
            for (int i = 0; i < backStack.size(); i++) {
                if (currentDir.equals(backStack.get(i).getFilePath())) {
                    position = i;
                    break;
                }
            }
            for (int j = backStack.size() - 1; j > position; j--) {
                backStackInfo.removeEntryAtIndex(j);
            }
            refreshList();
            navigationInfo.setNavDirectory(currentDir, homeScreenEnabled, FILES);
        }
    }

    @Override
    public void onNavButtonClicked(Category category, String bucketName) {
        boolean isDualPaneInFocus = fragment instanceof DualPaneList;
        ((AceActivity) getActivity()).setDualPaneFocusState(isDualPaneInFocus);

        if (filesView.isActionModeActive() && !menuControls.isPasteOp()) {
            menuControls.endActionMode();
        }
        this.category = category;
        filesView.setCategory(category);
        menuControls.setCategory(category);
        menuControls.setupSortVisibility();

        int position = 0;
        ArrayList<BackStackModel> backStack = backStackInfo.getBackStack();
        for (int i = 0; i < backStack.size(); i++) {
            if (category.equals(backStack.get(i).getCategory())) {
                position = i;
                break;
            }
        }
        for (int j = backStack.size() - 1; j > position; j--) {
            backStackInfo.removeEntryAtIndex(j);
        }

        refreshList();
        navigationInfo.addLibSpecificNavButtons(homeScreenEnabled, category, bucketName);
    }

    void updateFavouritesGroup(ArrayList<FileInfo> fileInfoList) {
        ArrayList<FavInfo> favInfoArrayList = new ArrayList<>();
        for (int i = 0; i < fileInfoList.size(); i++) {
            FileInfo info = fileInfoList.get(i);
            String name = info.getFileName();
            String path = info.getFilePath();
            FavInfo favInfo = new FavInfo();
            favInfo.setFileName(name);
            favInfo.setFilePath(path);
            favInfoArrayList.add(favInfo);
        }
        bridge.updateFavorites(favInfoArrayList);
        favoriteListener.updateFavorites(favInfoArrayList);
    }

    void removeFavorite(List<FileInfo> fileInfoList) {
        ArrayList<FavInfo> favInfoArrayList = new ArrayList<>();
        SharedPreferenceWrapper sharedPreferenceWrapper = new SharedPreferenceWrapper();
        for (int i = 0; i < fileInfoList.size(); i++) {
            FileInfo info = fileInfoList.get(i);
            String name = info.getFileName();
            String path = info.getFilePath();
            FavInfo favInfo = new FavInfo();
            favInfo.setFileName(name);
            favInfo.setFilePath(path);
            if (sharedPreferenceWrapper.removeFavorite(getActivity(), favInfo)) {
                favInfoArrayList.add(favInfo);
            }
        }
        if (favInfoArrayList.size() > 0) {
            if (category.equals(FAVORITES)) {
                refreshList();
            }
            favoriteListener.removeFavorites(favInfoArrayList);
        }
    }

    void changeGridCols() {
        filesView.refreshSpan(((AceActivity) getActivity()).getConfiguration());
    }


    @Override
    public void onConfigurationChanged(final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (currentOrientation != newConfig.orientation) {
            currentOrientation = newConfig.orientation;
            filesView.refreshSpan(newConfig);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        initializeViews();
    }

    private void initializeViews() {
        mainContainer = findViewById(R.id.main_content);
        floatingView = new FloatingView(this);
    }

    public void onDestroy() {
        adsView.destroyAds();
        if (!mInstanceStateExists) {
            unregisterReceivers();
        }
        filesView.onDestroy();
    }

    private void unregisterReceivers() {
        adsView.unregister();
    }

    public void onViewDestroyed() {
        filesView.onDestroyView();
        if (!mInstanceStateExists) {
            bridge.saveSettingsOnExit(filesView.getGridCols(), filesView.getViewMode());
        }
    }

    void clearSelectedPos() {
        filesView.clearSelectedPos();
    }


    public void setHidden(boolean showHidden) {
        this.showHidden = showHidden;
    }

    public void onFavAdded(int count) {
        FileUtils.showMessage(getContext(), String.format(getContext().getString(R.string.msg_added_fav),
                                                          count));
    }

    public void onFavExists() {
        FileUtils.showMessage(getContext(), getContext().getString(R.string.fav_exists));
    }

    public void hideDualPane() {
        dualModeEnabled = false;
    }

    public void setFavListener(StoragesUiView.FavoriteOperation favoriteListener) {
        this.favoriteListener = favoriteListener;
    }

    public void addHomeNavPath() {
        homeScreenEnabled = true;
        if (checkIfFileCategory(category)) {
            if (shouldShowPathNavigation()) {
                navigationInfo.setNavDirectory(currentDir, homeScreenEnabled,
                                               category);
            } else {
                navigationInfo.addHomeNavButton(homeScreenEnabled, category);
            }
        } else if (showLibSpecificNavigation(category)) {
            navigationInfo.addLibSpecificNavButtons(homeScreenEnabled, category, filesView.getBucketName());
        } else {
            navigationInfo.addHomeNavButton(homeScreenEnabled, category);
        }
    }


    public void createDir(String name) {
        bridge.createDir(currentDir, name, isRooted());
    }

    public void createFile(String name) {
        bridge.createDir(currentDir, name, isRooted());
    }

    public boolean isFabExpanded() {
        return floatingView.isFabExpanded();
    }

    public void showCreateDirDialog() {
        floatingView.showCreateDirDialog();
    }

    public void collapseFab() {
        floatingView.collapseFab();
    }

    public Fragment getFragment() {
        return fragment;
    }

    public ZipViewer getZipViewer() {
        return zipViewer;
    }

    public boolean isZipFile(String filePath) {
        if (!isZipMode() && isZipViewable(filePath)) {
            openZipViewer(filePath);
            return true;
        }
        return false;
    }

    public void onZipFileClicked(int position) {
        zipViewer.onFileClicked(position);
    }

    public void onActionModeStarted() {
        floatingView.hideFab();
    }

    public void onActionModeEnded() {
        // FAB should be visible only for Files Category
        if (isFilesCategory()) {
            floatingView.showFab();
        }
    }

    public boolean isDualModeEnabled() {
        return dualModeEnabled;
    }

    void renameFile(String filePath, String parentDir, String name) {
        bridge.renameFile(filePath, parentDir, name, isRooted());
    }


    public void dismissFAB() {
        floatingView.dismissDialog();
    }

    public boolean showHidden() {
        return showHidden;
    }

    public void reloadList(String directory, Category category) {
        filesView.reloadList(directory, category);
    }

    public void onDataLoaded(ArrayList<FileInfo> data) {
        filesView.onDataLoaded(data);
    }

    public boolean isActionModeActive() {
        return filesView.isActionModeActive();
    }

    public int getViewMode() {
        return filesView.getViewMode();
    }

    public void clearSelection() {
        filesView.clearSelection();
    }

    public void endDrag() {
        filesView.endDrag();
    }

    public void endActionMode() {
        filesView.endActionMode();
    }

    public void onSelectAllClicked() {
        filesView.onSelectAllClicked();
    }

    public void setCurrentDir(String currentDir) {
        this.currentDir = currentDir;
    }

    public void setCategory(Category category) {
        this.category = category;
    }


    public interface FavoriteOperation {
        void updateFavorites(ArrayList<FavInfo> favList);

        void removeFavorites(ArrayList<FavInfo> favList);
    }

    private ZipCommunicator                  zipCommunicator     = new ZipCommunicator() {

        @Override
        public void removeZipScrollPos(String newPath) {
            if (newPath == null) {
                return;
            }
            filesView.removeScrolledPos(newPath);
        }

        @Override
        public void endZipMode(String dir) {
            if (dir != null && dir.length() != 0) {
                currentDir = dir;
            }
            isZipViewer = false;
            zipViewer = null;
            ArrayList<BackStackModel> backStack = backStackInfo.getBackStack();
            int backStackSize = backStack.size();
            // If home clicked, backstack will be cleared already
            if (backStackSize == 0) {
                return;
            }
            int position = 0;
            if (currentDir == null) {
                position = backStackSize - 2;
            } else {
                for (int i = 0; i < backStackSize; i++) {
                    if (currentDir.equals(backStack.get(i).getFilePath())) {
                        position = i;
                        break;
                    }
                }
            }
            for (int j = backStackSize - 1; j > position; j--) {
                backStackInfo.removeEntryAtIndex(j);
            }

            refreshList();
            if (shouldShowPathNavigation()) {
                navigationInfo.setNavDirectory(currentDir, homeScreenEnabled, category);
            } else {
                navigationInfo.addHomeNavButton(homeScreenEnabled, category);
            }
        }

        @Override
        public void calculateZipScroll(String dir) {
            filesView.calculateScroll(dir);
        }

        @Override
        public void onZipContentsLoaded(ArrayList<FileInfo> data) {
            filesView.onDataLoaded(data);
        }

        @Override
        public void openZipViewer(String currentDir) {
            StoragesUiView.this.openZipViewer(currentDir);
        }

        @Override
        public void setNavDirectory(String path, boolean isHomeScreenEnabled, Category category) {
            navigationInfo.setNavDirectory(path, isHomeScreenEnabled, category);
        }

        @Override
        public void addToBackStack(String path, Category category) {
            backStackInfo.addToBackStack(path, category);
        }

        @Override
        public void removeFromBackStack() {
            backStackInfo.removeEntryAtIndex(backStackInfo.getBackStack().size() - 1);
        }

        @Override
        public void setInitialDir(String path) {
            navigationInfo.setInitialDir(path);
        }
    };
    // Dialog for SAF and APK dialog
    private DialogHelper.AlertDialogListener alertDialogListener = new DialogHelper
            .AlertDialogListener()
    {

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onPositiveButtonClick(View view) {
            if (isSAFShown) {
                triggerStorageAccessFramework();
            }
        }

        @Override
        public void onNegativeButtonClick(View view) {
            if (isSAFShown) {
                Toast.makeText(getContext(), getContext().getString(R.string.error), Toast
                        .LENGTH_SHORT).show();
            }
        }

        @Override
        public void onNeutralButtonClick(View view) {
        }
    };

}
