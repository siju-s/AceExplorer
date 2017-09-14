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

package com.siju.acexplorer.model.helper.root.rootshell;


import com.siju.acexplorer.model.helper.root.rootshell.containers.RootClass;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@RootClass.Candidate
public class NativeJavaClass
{

    public NativeJavaClass(RootClass.RootArgs args)
    {
        System.out.println("NativeJavaClass says: oh hi there.");
        String p = "/data/data/com.android.browser/cache";
        File f = new File(p);
        String[] fl = f.list();
        if (fl != null)
        {
            System.out.println("Look at all the stuff in your browser's cache:");
            for (String af : fl)
            {
                System.out.println("-" + af);
            }
            System.out.println("Leaving my mark for posterity...");
            File f2 = new File(p + "/rootshell_was_here");
            try
            {
                FileWriter filewriter = new FileWriter(f2);
                BufferedWriter out = new BufferedWriter(filewriter);
                out.write("This is just a file created using RootShell's Sanity check tools..\n");
                out.close();
                System.out.println("Done!");
            }
            catch (IOException e)
            {
                System.out.println("...and I failed miserably.");
                e.printStackTrace();
            }

        }
    }

}
