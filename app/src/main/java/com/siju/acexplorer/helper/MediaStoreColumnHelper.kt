package com.siju.acexplorer.helper

import android.provider.MediaStore
import com.siju.acexplorer.main.model.helper.SdkHelper

object MediaStoreColumnHelper {
    fun getBucketIdColumn() : String {
        return if(SdkHelper.isAtleastAndroid10) {
            return MediaStore.MediaColumns.BUCKET_ID
        }
        else {
            MediaStore.Images.Media.BUCKET_ID
        }
    }
}