package com.siju.acexplorer.filesystem.helper;

import java.util.ArrayList;


public class PermissionsHelper {

    public static ArrayList<Boolean[]> parse(String permLine) {
        ArrayList<Boolean[]> arrayList = new ArrayList<>();
        Boolean[] read = new Boolean[]{false, false, false};
        Boolean[] write = new Boolean[]{false, false, false};
        Boolean[] execute = new Boolean[]{false, false, false};
        if (permLine.charAt(1) == 'r') {
            read[0] = true;
        }
        if (permLine.charAt(2) == 'w') {
            write[0] = true;
        }
        if (permLine.charAt(3) == 'x') {
            execute[0] = true;
        }
        if (permLine.charAt(4) == 'r') {
            read[1] = true;
        }
        if (permLine.charAt(5) == 'w') {
            write[1] = true;
        }
        if (permLine.charAt(6) == 'x') {
            execute[1] = true;
        }
        if (permLine.charAt(7) == 'r') {
            read[2] = true;
        }
        if (permLine.charAt(8) == 'w') {
            write[2] = true;
        }
        if (permLine.charAt(9) == 'x') {
            execute[2] = true;
        }
        arrayList.add(read);
        arrayList.add(write);
        arrayList.add(execute);
        return arrayList;
    }
}
