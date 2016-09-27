package com.siju.acexplorer;

/**
 * Created by Siju on 11-09-2016.
 */
public class Factory {

    private static AceApplication sInstance;


    public static AceApplication get() {
        return sInstance;
    }

    static void setInstance(final AceApplication factory) {
        // Not allowed to call this after real application initialization is complete
        sInstance = factory;
    }
}