package com.siju.filemanager.filesystem.utils;

import java.util.ArrayList;

/**
 * Created by SIJU on 18-07-2016.
 */
public class RootHelperWrapper extends RootHelper {
    @Override
    protected ArrayList<String> getCommandsToExecute() {
        ArrayList<String> commands = new ArrayList<>();
        commands.add("ls -l");
        return commands;
    }
}
