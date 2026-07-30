package com.siju.acexplorer.appmanager.store

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * Opens a store listing, preferring the store app itself and falling back to the browser.
 */
object AppStoreLauncher {

    /**
     * Tries the store app first, then any app that handles the store scheme, then the web link.
     *
     * The first attempt names the store package explicitly because several store apps claim the
     * same schemes, and an untargeted intent would show a chooser even though the button already
     * told the user which store it opens.
     */
    fun openStoreListing(
        context: Context,
        store: AppStore,
        packageName: String,
        target: StoreLinkTarget
    ): Boolean {
        val storeAppLink = store.storeAppLinkFor(packageName, target)

        val openedInStoreApp = store.storeAppPackages().any { storeAppPackage ->
            startViewIntent(context, storeAppLink, storeAppPackage)
        }
        if (openedInStoreApp) {
            return true
        }
        if (startViewIntent(context, storeAppLink, targetPackage = null)) {
            return true
        }
        return startViewIntent(context, store.webLinkFor(packageName, target), targetPackage = null)
    }

    private fun startViewIntent(context: Context, url: String, targetPackage: String?): Boolean {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        targetPackage?.let(intent::setPackage)
        return try {
            context.startActivity(intent)
            true
        }
        catch (e: ActivityNotFoundException) {
            false
        }
    }
}
