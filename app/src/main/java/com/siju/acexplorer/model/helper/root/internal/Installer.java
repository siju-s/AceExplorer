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

package com.siju.acexplorer.model.helper.root.internal;

import android.content.Context;
import android.util.Log;

import com.siju.acexplorer.model.helper.root.RootTools;
import com.siju.acexplorer.model.helper.root.rootshell.execution.Command;
import com.siju.acexplorer.model.helper.root.rootshell.execution.Shell;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

class Installer
{

    //-------------
    //# Installer #
    //-------------

    static final String LOG_TAG = "RootTools::Installer";

    static final String BOGUS_FILE_NAME = "bogus";

    Context context;
    String filesPath;

    public Installer(Context context)
            throws IOException
    {

        this.context = context;
        this.filesPath = context.getFilesDir().getCanonicalPath();
    }

    /**
     * This method can be used to unpack a binary from the raw resources folder and store it in
     * /data/data/app.package/files/
     * This is typically useful if you provide your own C- or C++-based binary.
     * This binary can then be executed using sendShell() and its full path.
     *
     * @param sourceId resource id; typically <code>R.raw.id</code>
     * @param destName destination file name; appended to /data/data/app.package/files/
     * @param mode     chmod value for this file
     * @return a <code>boolean</code> which indicates whether or not we were
     * able to create the new file.
     */
    protected boolean installBinary(int sourceId, String destName, String mode)
    {
        File mf = new File(filesPath + File.separator + destName);
        if (!mf.exists() ||
                !getFileSignature(mf).equals(
                        getStreamSignature(
                                context.getResources().openRawResource(sourceId))
                ))
        {
            Log.e(LOG_TAG, "Installing a new version of binary: " + destName);
            // First, does our files/ directory even exist?
            // We cannot wait for android to lazily create it as we will soon
            // need it.
            try
            {
                FileInputStream fis = context.openFileInput(BOGUS_FILE_NAME);
                fis.close();
            }
            catch (FileNotFoundException e)
            {
                FileOutputStream fos = null;
                try
                {
                    fos = context.openFileOutput("bogus", Context.MODE_PRIVATE);
                    fos.write("justcreatedfilesdirectory".getBytes());
                }
                catch (Exception ex)
                {
                    if (RootTools.debugMode)
                    {
                        Log.e(LOG_TAG, ex.toString());
                    }
                    return false;
                }
                finally
                {
                    if (null != fos)
                    {
                        try
                        {
                            fos.close();
                            context.deleteFile(BOGUS_FILE_NAME);
                        }
                        catch (IOException e1)
                        {
                        }
                    }
                }
            }
            catch (IOException ex)
            {
                if (RootTools.debugMode)
                {
                    Log.e(LOG_TAG, ex.toString());
                }
                return false;
            }

            // Only now can we start creating our actual file
            InputStream iss = context.getResources().openRawResource(sourceId);
            ReadableByteChannel rfc = Channels.newChannel(iss);
            FileOutputStream oss = null;
            try
            {
                oss = new FileOutputStream(mf);
                FileChannel ofc = oss.getChannel();
                long pos = 0;
                try
                {
                    long size = iss.available();
                    while ((pos += ofc.transferFrom(rfc, pos, size - pos)) < size)
                    {
                        ;
                    }
                }
                catch (IOException ex)
                {
                    if (RootTools.debugMode)
                    {
                        Log.e(LOG_TAG, ex.toString());
                    }
                    return false;
                }
            }
            catch (FileNotFoundException ex)
            {
                if (RootTools.debugMode)
                {
                    Log.e(LOG_TAG, ex.toString());
                }
                return false;
            }
            finally
            {
                if (oss != null)
                {
                    try
                    {
                        oss.flush();
                        oss.getFD().sync();
                        oss.close();
                    }
                    catch (Exception e)
                    {
                    }
                }
            }
            try
            {
                iss.close();
            }
            catch (IOException ex)
            {
                if (RootTools.debugMode)
                {
                    Log.e(LOG_TAG, ex.toString());
                }
                return false;
            }

            try
            {
                Command command = new Command(0, false, "chmod " + mode + " " + filesPath + File.separator + destName);
                Shell.startRootShell().add(command);
                commandWait(command);

            }
            catch (Exception e)
            {
            }
        }
        return true;
    }

    protected boolean isBinaryInstalled(String destName)
    {
        boolean installed = false;
        File mf = new File(filesPath + File.separator + destName);
        if (mf.exists())
        {
            installed = true;
            // TODO: pass mode as argument and check it matches
        }
        return installed;
    }

    protected String getFileSignature(File f)
    {
        String signature = "";
        try
        {
            signature = getStreamSignature(new FileInputStream(f));
        }
        catch (FileNotFoundException ex)
        {
            Log.e(LOG_TAG, ex.toString());
        }
        return signature;
    }

    /*
     * Note: this method will close any string passed to it
     */
    protected String getStreamSignature(InputStream is)
    {
        String signature = "";
        try
        {
            MessageDigest md = MessageDigest.getInstance("MD5");
            DigestInputStream dis = new DigestInputStream(is, md);
            byte[] buffer = new byte[4096];
            while (-1 != dis.read(buffer))
            {
                ;
            }
            byte[] digest = md.digest();
            StringBuffer sb = new StringBuffer();

            for (int i = 0; i < digest.length; i++)
            {
                sb.append(Integer.toHexString(digest[i] & 0xFF));
            }

            signature = sb.toString();
        }
        catch (IOException ex)
        {
            Log.e(LOG_TAG, ex.toString());
        }
        catch (NoSuchAlgorithmException ex)
        {
            Log.e(LOG_TAG, ex.toString());
        }
        finally
        {
            try
            {
                is.close();
            }
            catch (IOException e)
            {
            }
        }
        return signature;
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
            catch (InterruptedException ex)
            {
                Log.e(LOG_TAG, ex.toString());
            }
        }
    }
}
