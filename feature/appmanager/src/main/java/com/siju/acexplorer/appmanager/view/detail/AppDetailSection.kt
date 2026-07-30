package com.siju.acexplorer.appmanager.view.detail

import androidx.annotation.StringRes

/**
 * A titled group of detail rows, as shown in the expandable app details card.
 */
data class AppDetailSection(@StringRes val titleRes: Int, val rows: List<AppDetailRow>)

/**
 * A single label and value pair. [copyable] rows can be long pressed to copy the value, which is
 * how long values such as paths and certificate digests are made usable.
 */
data class AppDetailRow(@StringRes val labelRes: Int, val value: String, val copyable: Boolean = false)
