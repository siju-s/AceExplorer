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

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;

import com.siju.acexplorer.billing.BillingStatus;
import com.siju.acexplorer.model.FavInfo;
import com.siju.acexplorer.model.FileInfo;
import com.siju.acexplorer.model.groups.Category;
import com.siju.acexplorer.storage.model.CopyData;
import com.siju.acexplorer.storage.model.operations.Operations;
import com.siju.acexplorer.view.dialog.DialogHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Siju on 04 September,2017
 */
public interface StoragesUi {

    void onPause();

    void onResume();

    void onExit();

    void setListener(Listener listener);

    void onDataLoaded(ArrayList<FileInfo> data);

    void handleActivityResult(int requestCode, int resultCode, Intent intent);

    void onConfigChanged(Configuration configuration);

    void init();

    boolean onBackPress();

    void onViewDestroyed();

    void showSAFDialog(String path, Intent data);

    void onFileExists(Operations operation, String msg);

    void showConflictDialog(final List<FileInfo> conflictFiles,
                            List<FileInfo> destFiles, final String destinationDir, final boolean isMove,
                            final DialogHelper.PasteConflictListener pasteConflictListener);

    void onLowSpace();

    void showPasteProgressDialog(String destinationDir, List<FileInfo> files,
                                 List<CopyData> copyData, boolean isMove);

    void onInvalidName(Operations operation);

    void dismissDialog(Operations operation);

    void onPermissionsFetched(ArrayList<Boolean[]> permissionList);

    void onPermissionsSet();

    void onPermissionSetError();

    void refreshList();

    boolean isFabExpanded();

    void collapseFab();

    void showDualPane();

    void reloadList(String directory, Category category);

    void removeHomeFromNavPath();

    void refreshSpan();

    void performVoiceSearch(String query);

    void setPremium();

    void showZipProgressDialog(Intent zipIntent);

    void onOperationFailed(Operations operation);

    void showExtractDialog(Intent intent);

    void setHidden(boolean showHidden);

    void onFavAdded(int count);

    void onFavExists();

    void addHomeNavPath();


    interface Listener {

        void loadData(String currentDir, Category category);

        BillingStatus checkBillingStatus();

        Bundle getUserPrefs();

        void startPasteOperation(String currentDir, boolean isMove, boolean rooted, ArrayList<FileInfo> info);


        void handleSAFResult(Intent operationIntent, Uri treeUri, boolean rooted, int flags);

        void saveOldSAFUri(String path);

        void createDir(String currentDir, String name, boolean rooted);

        void createFile(String currentDir, String name, boolean rooted);

        void deleteFiles(ArrayList<FileInfo> filesToDelete);

        void onExtractPositiveClick(String currentFilePath, String newFileName, boolean isChecked,
                                    String selectedPath);

        void hideUnHideFiles(ArrayList<FileInfo> infoList, ArrayList<Integer> pos);

        void getFilePermissions(String filePath, boolean directory);

        int getSortMode();

        void persistSortMode(int position);

        void onCompressPosClick(String newFilePath, ArrayList<FileInfo> paths);

        void setPermissions(String path, boolean isDir, String permissions);

        void saveSettingsOnExit(int gridCols, int viewMode);

        void updateFavorites(ArrayList<FavInfo> favInfoArrayList);

        void renameFile(String filePath, String newFilePath, String name, int position, boolean rooted);
    }
}
