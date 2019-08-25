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

package com.siju.acexplorer.main.model.groups

enum class Category(val value: Int) {
    FILES(0),
    AUDIO(1),
    VIDEO(2),
    IMAGE(3),
    DOCS(4),
    DOWNLOADS(5),
    //    ADD(6),
    COMPRESSED(7),
    FAVORITES(8),
    PDF(9),
    APPS(10),
    LARGE_FILES(11),
    ZIP_VIEWER(12),
    GENERIC_LIST(13),
    PICKER(14),
    //    GIF(15),
    RECENT(16),
    ALBUMS(17),
    ARTISTS(18),
    GENRES(19),
    //    ALARMS(20),
    //    NOTIFICATIONS(21),
    //    RINGTONES(22),
    PODCASTS(23),
    GENERIC_MUSIC(24),
    ALL_TRACKS(25),
    ALBUM_DETAIL(26),
    ARTIST_DETAIL(27),
    GENRE_DETAIL(28),
    GENERIC_IMAGES(29),
    FOLDER_IMAGES(30),
    GENERIC_VIDEOS(31),
    FOLDER_VIDEOS(32),
    APP_MANAGER(33),
    //    TRASH(34),
    RECENT_IMAGES(35),
    RECENT_AUDIO(36),
    RECENT_VIDEOS(37),
    RECENT_DOCS(38),
    RECENT_APPS(39),
    RECENT_FOLDER(40),
    RECENT_IMAGES_FOLDER(41),
    RECENT_VIDEOS_FOLDER(42),
    RECENT_AUDIO_FOLDER(43),
    RECENT_DOC_FOLDER(44),
    SEARCH_FOLDER_IMAGES(45),
    SEARCH_FOLDER_VIDEOS(46),
    SEARCH_FOLDER_AUDIO(47),
    SEARCH_FOLDER_DOCS(48),
    CAMERA(49),
    SCREENSHOT(50),
    WHATSAPP(51),
    TELEGRAM(52);

    companion object {
        fun valueOf(value: Int): Category? = values().find { it.value == value }
    }
}