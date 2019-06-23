package com.siju.acexplorer.main.model.data.music

import android.content.Context
import android.database.Cursor
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.data.DataFetcher
import com.siju.acexplorer.main.model.groups.Category
import java.util.*

class GenericMusicFetcher : DataFetcher {
    override fun fetchData(context: Context, path: String?,
                           category: Category): ArrayList<FileInfo> {

        val data = ArrayList<FileInfo>()

        val albumCursor = AlbumCommonData.queryAlbums(context)
        data.addAll(getCursorData(albumCursor, category, Category.ALBUMS))

        val artistCursor = ArtistCommonData.queryArtist(context)
        data.addAll(getCursorData(artistCursor, category, Category.ARTISTS))

        val genreCursor = GenreCommonData.queryGenres(context)
        data.addAll(getCursorData(genreCursor, category, Category.GENRES))

        val tracksCursor = TracksCommonData.queryTracks(context)
        data.addAll(getCursorData(tracksCursor, category, Category.ALL_TRACKS))


        val podcastCursor = PodcastCommonData.queryPodcasts(context)
        data.addAll(getCursorData(podcastCursor, category, Category.PODCASTS))

        return data
    }

    override fun fetchCount(context: Context, path: String?): Int {
        val tracksCursor = TracksCommonData.queryTracks(context)
        return getCursorCount(tracksCursor)
    }

    private fun getCursorData(cursor: Cursor?, category: Category, subcategory: Category): ArrayList<FileInfo> {
        val fileInfoList = ArrayList<FileInfo>()

        if (cursor == null) {
            return fileInfoList
        }
        if (cursor.moveToFirst()) {
            fileInfoList.add(FileInfo(category, subcategory, cursor.count))
        }
        cursor.close()
        return fileInfoList
    }
}