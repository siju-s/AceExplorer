package com.siju.acexplorer;

/**
 * Created by Siju on 11-09-2016.
 */
public class Factory {

    // Making this volatile because on the unit tests, setInstance is called from a unit test
    // thread, and then it's read on the UI thread.
    private static AceApplication sInstance;


    public static AceApplication get() {
        return sInstance;
    }

    protected static void setInstance(final AceApplication factory) {
        // Not allowed to call this after real application initialization is complete
        sInstance = factory;
    }
}