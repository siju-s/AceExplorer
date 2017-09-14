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

package com.siju.acexplorer.model.helper.root.containers;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public class Mount
{
    final File mDevice;
    final File mMountPoint;
    final String mType;
    final Set<String> mFlags;

    public Mount(File device, File path, String type, String flagsStr)
    {
        mDevice = device;
        mMountPoint = path;
        mType = type;
        mFlags = new LinkedHashSet<String>(Arrays.asList(flagsStr.split(",")));
    }

    public File getDevice()
    {
        return mDevice;
    }

    public File getMountPoint()
    {
        return mMountPoint;
    }

    public String getType()
    {
        return mType;
    }

    public Set<String> getFlags()
    {
        return mFlags;
    }

    @Override
    public String toString()
    {
        return String.format("%s on %s type %s %s", mDevice, mMountPoint, mType, mFlags);
    }
}
