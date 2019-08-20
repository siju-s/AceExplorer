package com.siju.acexplorer.search.model

import android.content.Context
import com.siju.acexplorer.R

enum class SearchHeaderType(val value: Int) {
    FOLDER(0),
    IMAGE(1),
    VIDEO(2),
    AUDIO(3),
    APP(4),
    DOC(5),
    OTHER(5);

    companion object {

    fun getHeaderName(context: Context, type: Int) =
        when (type) {
            FOLDER.value -> context.getString(R.string.search_type_folder)
            IMAGE.value -> context.getString(R.string.image)
            VIDEO.value -> context.getString(R.string.nav_menu_video)
            AUDIO.value -> context.getString(R.string.audio)
            APP.value -> context.getString(R.string.apk)
            DOC.value -> context.getString(R.string.home_docs)
            else -> context.getString(R.string.search_type_other)

        }

}
}