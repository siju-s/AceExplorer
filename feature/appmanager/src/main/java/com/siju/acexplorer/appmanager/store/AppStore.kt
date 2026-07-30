package com.siju.acexplorer.appmanager.store

import androidx.annotation.StringRes
import com.siju.acexplorer.appmanager.R

private const val PACKAGE_PLAY_STORE = "com.android.vending"
private const val PACKAGE_AMAZON_APPSTORE = "com.amazon.venezia"
private const val PACKAGE_AMAZON_SHOPPING = "com.amazon.mShop.android.shopping"
private const val PACKAGE_GALAXY_STORE = "com.sec.android.app.samsungapps"
private const val PACKAGE_F_DROID = "org.fdroid.fdroid"
private const val PACKAGE_APP_GALLERY = "com.huawei.appmarket"

/**
 * What the store should be asked to show.
 *
 * [LISTING] goes straight to the app page and is right when we know the app came from that store.
 * [SEARCH] is used when the app was sideloaded, because a listing link for an app the store has
 * never heard of lands the user on an "item not found" error.
 */
enum class StoreLinkTarget {
    LISTING,
    SEARCH
}

/**
 * Links a store exposes for one app: a scheme the store app handles, and a browser URL for when
 * the store app is not installed.
 */
private data class StoreLinks(
    val listingScheme: String,
    val listingWeb: String,
    val searchScheme: String,
    val searchWeb: String
)

/**
 * A store that can show the listing for an installed app.
 */
enum class AppStore(
    @StringRes val labelRes: Int,
    private val installerPackages: Set<String>,
    private val links: StoreLinks
) {

    PLAY_STORE(
        R.string.store_play_store,
        setOf(PACKAGE_PLAY_STORE),
        StoreLinks(
            listingScheme = "market://details?id=%1\$s",
            listingWeb = "https://play.google.com/store/apps/details?id=%1\$s",
            searchScheme = "market://search?q=%1\$s&c=apps",
            searchWeb = "https://play.google.com/store/search?q=%1\$s&c=apps"
        )
    ),
    AMAZON_APPSTORE(
        R.string.store_amazon_appstore,
        setOf(PACKAGE_AMAZON_APPSTORE, PACKAGE_AMAZON_SHOPPING),
        StoreLinks(
            listingScheme = "amzn://apps/android?p=%1\$s",
            listingWeb = "https://www.amazon.com/gp/mas/dl/android?p=%1\$s",
            searchScheme = "amzn://apps/android?s=%1\$s",
            searchWeb = "https://www.amazon.com/s?k=%1\$s&i=mobile-apps"
        )
    ),
    GALAXY_STORE(
        R.string.store_galaxy_store,
        setOf(PACKAGE_GALAXY_STORE),
        StoreLinks(
            listingScheme = "samsungapps://ProductDetail/%1\$s",
            listingWeb = "https://galaxystore.samsung.com/detail/%1\$s",
            searchScheme = "samsungapps://SearchResult/%1\$s",
            searchWeb = "https://galaxystore.samsung.com/search/%1\$s"
        )
    ),
    F_DROID(
        R.string.store_f_droid,
        setOf(PACKAGE_F_DROID),
        StoreLinks(
            listingScheme = "fdroid.app://details?id=%1\$s",
            listingWeb = "https://f-droid.org/packages/%1\$s",
            searchScheme = "fdroid.search://%1\$s",
            searchWeb = "https://search.f-droid.org/?q=%1\$s"
        )
    ),
    APP_GALLERY(
        R.string.store_app_gallery,
        setOf(PACKAGE_APP_GALLERY),
        StoreLinks(
            listingScheme = "appmarket://details?id=%1\$s",
            listingWeb = "https://appgallery.huawei.com/search/%1\$s",
            searchScheme = "appmarket://search?q=%1\$s",
            searchWeb = "https://appgallery.huawei.com/search/%1\$s"
        )
    );

    fun storeAppLinkFor(packageName: String, target: StoreLinkTarget): String {
        val template = when (target) {
            StoreLinkTarget.LISTING -> links.listingScheme
            StoreLinkTarget.SEARCH -> links.searchScheme
        }
        return template.format(packageName)
    }

    fun webLinkFor(packageName: String, target: StoreLinkTarget): String {
        val template = when (target) {
            StoreLinkTarget.LISTING -> links.listingWeb
            StoreLinkTarget.SEARCH -> links.searchWeb
        }
        return template.format(packageName)
    }

    /**
     * Packages the store link is aimed at. Several store apps claim the same schemes, so the
     * intent is targeted at these to avoid handing the user a chooser.
     */
    fun storeAppPackages(): Set<String> = installerPackages

    companion object {

        /** Store that [installerPackage] belongs to, or null when it is not a store we know. */
        fun matching(installerPackage: String?): AppStore? {
            installerPackage ?: return null
            return entries.firstOrNull { store -> installerPackage in store.installerPackages }
        }

        /**
         * Store matching [installerPackage], or [PLAY_STORE] when the installer is unknown or
         * sideloaded, so the user still has somewhere to look the app up.
         */
        fun forInstaller(installerPackage: String?): AppStore = matching(installerPackage) ?: PLAY_STORE

        fun isKnownInstaller(installerPackage: String?): Boolean = matching(installerPackage) != null
    }
}
