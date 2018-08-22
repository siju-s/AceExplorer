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

package com.siju.acexplorer.home.view;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.siju.acexplorer.R;
import com.siju.acexplorer.analytics.Analytics;
import com.siju.acexplorer.main.model.SectionItems;
import com.siju.acexplorer.main.model.StorageUtils;
import com.siju.acexplorer.main.model.groups.StoragesGroup;
import com.siju.acexplorer.theme.Theme;

import java.util.ArrayList;
import java.util.List;

import static com.siju.acexplorer.main.model.StorageUtils.StorageType.EXTERNAL;
import static com.siju.acexplorer.main.model.StorageUtils.getStorageSpaceText;
import static com.siju.acexplorer.main.model.groups.Category.FILES;

/**
 * Created by Siju on 03 September,2017
 */
class HomeStorages implements View.OnClickListener {

    private HomeUiView              homeUiView;
    private CardView                layoutStorages;
    private LinearLayout            storagesContainer;
    private ArrayList<SectionItems> storagesList;

    private Context context;

    HomeStorages(HomeUiView homeUiView, Theme theme) {
        this.homeUiView = homeUiView;
        this.context = homeUiView.getContext();
        init();
        setTheme(theme);
    }

    private void setTheme(Theme theme) {
        switch (theme) {
            case DARK:
                layoutStorages.setCardBackgroundColor(ContextCompat.getColor(context, R.color.dark_home_card_bg));
                break;
            case LIGHT:
                layoutStorages.setCardBackgroundColor(ContextCompat.getColor(context, R.color.light_home_card_bg));
                break;
        }
    }

    private void init() {
        storagesContainer =  homeUiView.findViewById(R.id.storagesContainer);
        layoutStorages =  homeUiView.findViewById(R.id.cardViewStorages);
        initializeStorageGroup();
        inflateStoragesItem();
    }

    private void initializeStorageGroup() {
        storagesList = new ArrayList<>();
        storagesList = StoragesGroup.getInstance().getStoragesList();
    }


    private void inflateStoragesItem() {
        storagesContainer.removeAllViews();
        List<String> pathNames = new ArrayList<>();
        for (int i = 0; i < storagesList.size(); i++) {
            RelativeLayout storageItemContainer = (RelativeLayout) View.inflate(context, R.layout.storage_item,
                                                                          null);
            ProgressBar progressBarSpace =  storageItemContainer
                    .findViewById(R.id.progressBarSD);
            ImageView imageStorage =  storageItemContainer.findViewById(R.id.imageStorage);
            TextView textStorage =  storageItemContainer.findViewById(R.id.textStorageName);
            TextView textSpace =  storageItemContainer.findViewById(R.id.textStorageSpace);
            View homeStoragesDivider = storageItemContainer.findViewById(R.id.home_storages_divider);

            imageStorage.setImageResource(storagesList.get(i).getIcon());
            pathNames.add(storagesList.get(i).getPath());
            StorageUtils.StorageType storageType = storagesList.get(i).getStorageType();

            if (storageType.equals(EXTERNAL)) {
                textStorage.setText(storagesList.get(i).getFirstLine());
            } else {
                textStorage.setText(StorageUtils.StorageType.getStorageText(context, storageType));
            }
            textSpace.setText(getStorageSpaceText(context, storagesList.get(i).getSecondLine()));
            progressBarSpace.setProgress(storagesList.get(i).getProgress());

            storagesContainer.addView(storageItemContainer);
            storageItemContainer.setOnClickListener(this);
            storageItemContainer.setTag(storagesList.get(i).getPath());
            if (i + 1 == storagesList.size()) {
                homeStoragesDivider.setVisibility(View.GONE);
            } else {
                homeStoragesDivider.setVisibility(View.VISIBLE);
            }

        }
        Analytics.getLogger().homeStorageDisplayed(storagesList.size(), pathNames);
    }

    @Override
    public void onClick(View view) {
        String tag = (String) view.getTag();
        homeUiView.loadFileList(tag, FILES);
    }
}
