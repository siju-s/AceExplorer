package com.siju.acexplorer.filesystem.operations;


public enum Operations {

    CUT(0),
    COPY(1),
    FILE_CREATION(2),
    FOLDER_CREATION(3),
    RENAME(4),
    DELETE(5),
    EXTRACT(6),
    COMPRESS(7);

    private final int value;

    Operations(int value) {
        this.value = value;
    }

    public int getValue() {

        return value;
    }



}
