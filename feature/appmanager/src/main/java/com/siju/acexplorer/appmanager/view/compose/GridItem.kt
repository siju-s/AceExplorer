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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.siju.acexplorer.appmanager.view.compose.LazyItemUtils.getSelectionDrawable
import com.siju.acexplorer.appmanager.view.compose.components.BodyText
import com.siju.acexplorer.appmanager.viewmodel.AppMgr
import com.siju.acexplorer.common.ViewMode
import com.siju.acexplorer.common.theme.LocalDim

private const val TAG = "ListItem"

@OptIn(ExperimentalFoundationApi::class, ExperimentalGlideComposeApi::class)
@Composable
fun GridItem(
    data: AppInfo, modifier: Modifier = Modifier,
    requestManager: RequestManager = Glide.with(LocalContext.current),
    selected: Boolean,
    onItemClick: (AppInfo) -> Unit,
    onItemLongClick: (AppInfo) -> Unit,
    appMgr: AppMgr,
    viewMode: ViewMode = ViewMode.GRID
) {
    Log.d(
        TAG,
        "GridItem() called with: data = $data, theme = ${LocalContext.current.theme}, selected = $selected, onItemClick = $onItemClick, onItemLongClick = $onItemLongClick"
    )
    var visible by remember { mutableStateOf(false) }
    var selectedPos by remember { mutableStateOf(false) }
    val drawableResource = getSelectionDrawable(selectedPos)
    val haptics = LocalHapticFeedback.current
    val bgColor = getBackgroundColor(selectedPos)

    Surface(
        color = bgColor, modifier = modifier.combinedClickable(
            onClick = {
                if (appMgr.isActionModeActive()) {
                    selectedPos = !selectedPos
                    visible = !visible
                }
                println("onclick Visible :$visible")
                onItemClick(data)
            },
            onLongClick = {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                visible = !appMgr.isActionModeActive()
                selectedPos = !selectedPos
                println("longclick Visible :$visible")
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

            if (visible) {
                Image(
                    painterResource(id = drawableResource),
                    contentDescription = "Select",
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

