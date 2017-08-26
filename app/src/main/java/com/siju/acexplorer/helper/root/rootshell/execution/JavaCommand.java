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

package com.siju.acexplorer.helper.root.rootshell.execution;

import android.content.Context;

public class JavaCommand extends Command
{
    /**
     * Constructor for executing Java commands rather than binaries
     *
     * @param context     needed to execute java command.
     */
    public JavaCommand(int id, Context context, String... command) {
        super(id, command);
        this.context = context;
        this.javaCommand = true;
    }

    /**
     * Constructor for executing Java commands rather than binaries
     *
     * @param context     needed to execute java command.
     */
    public JavaCommand(int id, boolean handlerEnabled, Context context, String... command) {
        super(id, handlerEnabled, command);
        this.context = context;
        this.javaCommand = true;
    }

    /**
     * Constructor for executing Java commands rather than binaries
     *
     * @param context     needed to execute java command.
     */
    public JavaCommand(int id, int timeout, Context context, String... command) {
        super(id, timeout, command);
        this.context = context;
        this.javaCommand = true;
    }


    @Override
    public void commandOutput(int id, String line)
    {
        super.commandOutput(id, line);
    }

    @Override
    public void commandTerminated(int id, String reason)
    {
        // pass
    }

    @Override
    public void commandCompleted(int id, int exitCode)
    {
        // pass
    }
}
