package com.siju.acexplorer.appmanager.view.compose

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.siju.acexplorer.common.R

@Composable
fun AppMgrActionModeItems(doneClicked: () -> Unit, selectAllClicked: () -> Unit) {
    IconButton(onClick = doneClicked) {
        Icon(
            painter = painterResource(R.drawable.ic_done_white),
            contentDescription = stringResource(R.string.msg_ok),
        )
    }
    IconButton(onClick = selectAllClicked) {
        Icon(
            painter = painterResource(R.drawable.ic_select_all_white),
            contentDescription = stringResource(R.string.action_select_all),
        )
    }
}