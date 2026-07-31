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
package com.siju.acexplorer.welcome

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.siju.acexplorer.R

/**
 * The features advertised on the welcome screen.
 *
 * This is the single place to edit when a feature ships or is retired. The welcome screen renders
 * whatever is listed here, so it never needs a layout change to stay current.
 */
enum class WelcomeFeature(
    @DrawableRes val iconRes: Int,
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int
) {
    STORAGE(
        iconRes = R.drawable.ic_welcome_storage,
        titleRes = R.string.welcome_feature_storage_title,
        descriptionRes = R.string.welcome_feature_storage_desc
    ),
    RECYCLE_BIN(
        iconRes = R.drawable.ic_welcome_recycle_bin,
        titleRes = R.string.welcome_feature_trash_title,
        descriptionRes = R.string.welcome_feature_trash_desc
    ),
    APP_MANAGER(
        iconRes = R.drawable.ic_welcome_apps,
        titleRes = R.string.welcome_feature_apps_title,
        descriptionRes = R.string.welcome_feature_apps_desc
    ),
    NETWORK(
        iconRes = R.drawable.ic_welcome_network,
        titleRes = R.string.welcome_feature_network_title,
        descriptionRes = R.string.welcome_feature_network_desc
    ),
    DUAL_PANE(
        iconRes = R.drawable.ic_welcome_dual_pane,
        titleRes = R.string.welcome_feature_dual_pane_title,
        descriptionRes = R.string.welcome_feature_dual_pane_desc
    ),
    SEARCH(
        iconRes = R.drawable.ic_welcome_search,
        titleRes = R.string.welcome_feature_search_title,
        descriptionRes = R.string.welcome_feature_search_desc
    ),
    ARCHIVES(
        iconRes = R.drawable.ic_welcome_archive,
        titleRes = R.string.welcome_feature_archive_title,
        descriptionRes = R.string.welcome_feature_archive_desc
    ),
    THEMES(
        iconRes = R.drawable.ic_welcome_themes,
        titleRes = R.string.welcome_feature_themes_title,
        descriptionRes = R.string.welcome_feature_themes_desc
    )
}
