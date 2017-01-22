package com.siju.acexplorer.filesystem.groups;

/**
 * Created by SJ on 21-01-2017.
 */

public enum DrawerGroups {

    STORAGE(0),
    FAVORITES(1),
    LIBRARY(2),
    OTHERS(3);

    private final int value;

    DrawerGroups(int value) {
        this.value = value;
    }

    public int getValue() {

        return value;
    }

    public static DrawerGroups getGroupFromPos(int position) {
        switch (position) {
            case 0:
                return STORAGE;
            case 1:
                return FAVORITES;
            case 2:
                return LIBRARY;
            case 3:
                return OTHERS;
        }
        return STORAGE;
    }



}
