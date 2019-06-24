package com.siju.billingsecure;

public class BillingKey {

    static {
        System.loadLibrary("keys");
    }

    public static String getBillingKey() {
        return getNativeKey();
    }

    private static native String getNativeKey();
}
