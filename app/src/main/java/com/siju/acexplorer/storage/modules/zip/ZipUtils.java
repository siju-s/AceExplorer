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

package com.siju.acexplorer.storage.modules.zip;


public class ZipUtils {

    public static final String EXT_ZIP = ".zip";
    public static final String EXT_APK= ".apk";
    public static final String EXT_TAR = ".tar";
    public static final String EXT_TAR_GZ = ".tar.gz";



    /**
     * To be used when RAR as viewable not needed
     *
     * @param filePath
     * @return
     */
    public static boolean isZipViewable(String filePath) {
        return filePath.toLowerCase().endsWith(EXT_ZIP) ||
                filePath.toLowerCase().endsWith(EXT_APK);
    }
}
