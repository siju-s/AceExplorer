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

package com.siju.acexplorer.helper.root.internal;

import android.content.Context;
import android.util.Log;

import com.siju.acexplorer.helper.root.RootTools;
import com.siju.acexplorer.helper.root.rootshell.execution.Command;
import com.siju.acexplorer.helper.root.rootshell.execution.Shell;

import java.io.IOException;

public class Runner extends Thread
{

    private static final String LOG_TAG = "RootTools::Runner";

    Context context;
    String binaryName;
    String parameter;

    public Runner(Context context, String binaryName, String parameter)
    {
        this.context = context;
        this.binaryName = binaryName;
        this.parameter = parameter;
    }

    public void run()
    {
        String privateFilesPath = null;
        try
        {
            privateFilesPath = context.getFilesDir().getCanonicalPath();
        }
        catch (IOException e)
        {
            if (RootTools.debugMode)
            {
                Log.e(LOG_TAG, "Problem occured while trying to locate private files directory!");
            }
            e.printStackTrace();
        }
        if (privateFilesPath != null)
        {
            try
            {
                Command command = new Command(0, false, privateFilesPath + "/" + binaryName + " " + parameter);
                Shell.startRootShell().add(command);
                commandWait(command);

            }
            catch (Exception e)
            {
            }
        }
    }

    private void commandWait(Command cmd)
    {
        synchronized (cmd)
        {
            try
            {
                if (!cmd.isFinished())
                {
                    cmd.wait(2000);
                }
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }

}
