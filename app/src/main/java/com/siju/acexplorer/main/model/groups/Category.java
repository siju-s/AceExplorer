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

package com.siju.acexplorer.main.model.groups;

public enum Category {
    FILES(0),
    AUDIO(1),
    VIDEO(2),
    IMAGE(3),
    DOCS(4),
    DOWNLOADS(5),
    ADD(6),
    COMPRESSED(7),
    FAVORITES(8),
    PDF(9),
    APPS(10),
    LARGE_FILES(11),
    ZIP_VIEWER(12),
    GENERIC_LIST(13),
    PICKER(14),
    GIF(15),
    RECENT(16),
    ALBUMS(17),
    ARTISTS(18),
    GENRES(19),
    ALARMS(20),
    NOTIFICATIONS(21),
    RINGTONES(22),
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
    TRASH(34);

    private final int value;

    Category(int value) {
        this.value = value;
    }

    public int getValue() {

        return value;
    }

}
