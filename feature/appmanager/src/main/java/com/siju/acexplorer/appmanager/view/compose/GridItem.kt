package com.siju.acexplorer.appmanager.view.compose

import android.text.format.Formatter
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import com.siju.acexplorer.appmanager.types.AppInfo
import com.siju.acexplorer.appmanager.view.compose.LazyItemUtils.getBackgroundColor
import com.siju.acexplorer.appmanager.view.compose.components.BodyText
import com.siju.acexplorer.common.ViewMode
import com.siju.acexplorer.common.theme.LocalDim

private const val TAG = "ListItem"

@OptIn(ExperimentalFoundationApi::class, ExperimentalGlideComposeApi::class)
@Composable
fun GridItem(
    data: AppInfo,
    selected: Boolean,
    modifier: Modifier = Modifier,
    requestManager: RequestManager = Glide.with(LocalContext.current),
    viewMode: ViewMode = ViewMode.GRID,
    onItemClick: (AppInfo) -> Unit,
    onItemLongClick: (AppInfo) -> Unit
) {
    Log.d(
        TAG,
        "GridItem() called with: data = $data, selected = $selected"
    )
    val haptics = LocalHapticFeedback.current
    val bgColor = getBackgroundColor(selected)

    Surface(
        color = bgColor, modifier = modifier.combinedClickable(
            onClick = {
                onItemClick(data)
            },
            onLongClick = {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onItemLongClick(data)
            })
    ) {
        Box(
            modifier = Modifier
//                .defaultMinSize(minHeight = dimensionResource(id = R.dimen.app_list_item_min_height))
                .padding(LocalDim.current.spaceSmall)
        ) {
            GlideImage(
                model = data.packageName,
                contentDescription = "App icon",
                modifier = Modifier
                    .width(LocalDim.current.space80)
                    .height(LocalDim.current.space80)
                    .align(Alignment.TopStart),
                loading = placeholder(com.siju.acexplorer.common.R.drawable.ic_apk_green)
            ) {
                it.thumbnail(
                    requestManager
                        .asDrawable()
                        .load(data.packageName)
                )
            }
            Column(
                Modifier
                    .align(Alignment.TopStart)
                    .padding(top = LocalDim.current.space80 + LocalDim.current.spaceSmall)
            ) {
                BodyText(text = data.name, maxLines = 2)
                BodyText(text = data.packageName, maxLines = 2)
                if (viewMode == ViewMode.GALLERY) {
                    BodyText(
                        text = Formatter.formatFileSize(LocalContext.current, data.size),
                        maxLines = 2
                    )
                }
            }

            if (selected) {
                Image(
                    painterResource(id = com.siju.acexplorer.common.R.drawable.ic_select_checked),
                    contentDescription = "Selected",
                    modifier = Modifier
                        .width(20.dp)
                        .height(20.dp)
                        .zIndex(1f),
                )
            }

//            Text(
//                text = DateUtils.convertDate(data.installDate), modifier = Modifier
//                    .wrapContentHeight()
//                    .align(Alignment.BottomEnd)
//            )

        }

    }
}

