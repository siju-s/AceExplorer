package com.siju.acexplorer.appmanager.view.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.siju.acexplorer.appmanager.R
import com.siju.acexplorer.appmanager.types.AppInfo
import com.siju.acexplorer.appmanager.view.compose.LazyItemUtils.getBackgroundColor
import com.siju.acexplorer.appmanager.view.compose.components.BodyText
import com.siju.acexplorer.common.extensions.getAppInfo
import com.siju.acexplorer.common.theme.LocalDim
import com.siju.acexplorer.common.utils.DateUtils

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ListItem(
    data: AppInfo,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onItemClick: (AppInfo) -> Unit,
    onItemLongClick: (AppInfo) -> Unit
) {
    val haptics = LocalHapticFeedback.current
    val bgColor = getBackgroundColor(selected)

    val appInfo = LocalContext.current.getAppInfo(data.packageName)
    val imageModel = ImageRequest.Builder(LocalContext.current).data(appInfo).build()

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
                .defaultMinSize(minHeight = dimensionResource(id = R.dimen.app_list_item_min_height))
                .padding(LocalDim.current.spaceSmall)
                .height(IntrinsicSize.Min)
        ) {
            AsyncImage(
                model = imageModel,
                placeholder = painterResource(com.siju.acexplorer.common.R.drawable.ic_apk_green),
                contentDescription = "App icon",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(50.dp)
            )
            Column(
                Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = LocalDim.current.space50 + LocalDim.current.spaceSmall)
            ) {
                BodyText(data.name)
                BodyText(data.packageName)
            }
            Spacer(Modifier.fillMaxSize(1f))

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

            BodyText(
                text = DateUtils.convertDate(data.installDate),
                modifier = Modifier
                    .wrapContentHeight()
                    .align(Alignment.BottomEnd)
            )

        }

    }
}
