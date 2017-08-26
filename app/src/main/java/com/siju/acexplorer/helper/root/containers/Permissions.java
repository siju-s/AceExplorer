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

package com.siju.acexplorer.helper.root.containers;

public class Permissions
{
    String type;
    String user;
    String group;
    String other;
    String symlink;
    int permissions;

    public String getSymlink()
    {
        return this.symlink;
    }

    public String getType()
    {
        return type;
    }

    public int getPermissions()
    {
        return this.permissions;
    }

    public String getUserPermissions()
    {
        return this.user;
    }

    public String getGroupPermissions()
    {
        return this.group;
    }

    public String getOtherPermissions()
    {
        return this.other;
    }

    public void setSymlink(String symlink)
    {
        this.symlink = symlink;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public void setPermissions(int permissions)
    {
        this.permissions = permissions;
    }

    public void setUserPermissions(String user)
    {
        this.user = user;
    }

    public void setGroupPermissions(String group)
    {
        this.group = group;
    }

    public void setOtherPermissions(String other)
    {
        this.other = other;
    }

    public String getUser()
    {
        return user;
    }

    public void setUser(String user)
    {
        this.user = user;
    }

    public String getGroup()
    {
        return group;
    }

    public void setGroup(String group)
    {
        this.group = group;
    }

    public String getOther()
    {
        return other;
    }

    public void setOther(String other)
    {
        this.other = other;
    }


}
