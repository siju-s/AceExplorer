package com.siju.acexplorer.common.coil

import android.content.Context
import android.content.pm.ApplicationInfo
import android.graphics.drawable.Drawable
import coil3.ImageLoader
import coil3.asImage
import coil3.decode.DataSource
import coil3.fetch.FetchResult
import coil3.fetch.Fetcher
import coil3.fetch.ImageFetchResult
import coil3.request.Options

class AppIconFetcher(
    private val context: Context,
    private val data: ApplicationInfo
) : Fetcher {

    override suspend fun fetch(): FetchResult {
        // Load the app icon as a drawable
        val icon: Drawable = data.loadIcon(context.packageManager)
        return ImageFetchResult(
            icon.asImage(),
            isSampled = false,
            dataSource = DataSource.DISK
        )
    }

    class Factory(private val context: Context) : Fetcher.Factory<ApplicationInfo> {

        override fun create(
            data: ApplicationInfo,
            options: Options,
            imageLoader: ImageLoader
        ): Fetcher {
            return AppIconFetcher(context, data)
        }

    }
}
