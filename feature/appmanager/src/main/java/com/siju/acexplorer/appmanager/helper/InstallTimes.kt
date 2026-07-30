package com.siju.acexplorer.appmanager.helper

/** 23 September 2008, the day Android 1.0 shipped. Nothing can have been installed before it. */
private const val EARLIEST_REAL_INSTALL_TIME_MILLIS = 1_222_128_000_000L

/**
 * Install and update times reported by the platform.
 */
object InstallTimes {

    /**
     * Preinstalled apps report install times measured from boot rather than a wall clock, which
     * lands them a few minutes after the Unix epoch and renders as a 1970 date. Anything older
     * than Android itself cannot be a real install date.
     */
    fun isRealInstallTime(timeInMillis: Long): Boolean {
        return timeInMillis >= EARLIEST_REAL_INSTALL_TIME_MILLIS
    }
}
