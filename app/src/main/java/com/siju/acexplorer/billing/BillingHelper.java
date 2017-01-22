package com.siju.acexplorer.billing;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.siju.acexplorer.utils.inappbilling.IabHelper;
import com.siju.acexplorer.utils.inappbilling.IabResult;
import com.siju.acexplorer.utils.inappbilling.Inventory;
import com.siju.acexplorer.utils.inappbilling.Purchase;


public class BillingHelper {
    private static final String TAG = "BillingHelper";
    private static final String SKU_REMOVE_ADS = "com.siju.acexplorer.pro";
    //    static final String SKU_TEST = "android.test.purchased";
    private static final int BILLING_UNAVAILABLE = 3;

    // (arbitrary) request code for the purchase flow
    private static final int RC_REQUEST = 10111;

    private boolean inappBillingSupported;

    // The helper object
    private IabHelper mHelper;

    private boolean isPremium = true;
    private static BillingHelper billingInstance;

    private BillingHelper() {
    // To avoid instantiation outside class
    }

    public static BillingHelper getInstance() {
        if (billingInstance == null) {
            billingInstance = new BillingHelper();
        }
        return billingInstance;
    }

    public void setupBilling(Context context) {
        // Create the helper, passing it our context and the public key to
        // verify signatures with
        Log.d(TAG, "Creating IAB helper.");
        String base64EncodedPublicKey =
                "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAomGBqi0dGhyE1KphvTxc6K3OXsTsWEcAdLNsg22Un" +
                        "/6VJakiajmZMBODktRggHlUgWDZZvFZCw2so53U++pVHRfyevKIbP7" +
                        "/eIkB7mtlartsbOkD3yGQCUVxE1kQ3Olum1CYv7DqBQC4J9h9q22ApcGIfkZq6Os3Jm7vKmuzHHLKN63yWQS1FuwwcLAmpSN2EOX4Has4eElrgZoySu4qv5SOooOJS27Y4fzzxToQX5T50tO9dG+NYKrLmPK4yL5JGB5E3UD0I8vNLD/Wj2qPBE1tiYbjHHeX3PrF9lJhXtZs9uiMnMzox6dxW9+VmPYxNuMXakXrURGfpgaWGK00ZQIDAQAB";
        mHelper = new IabHelper(context, base64EncodedPublicKey);

        // enable debug logging (for a production application, you should set
        // this to false).
        mHelper.enableDebugLogging(false);

        // Start setup. This is asynchronous and the specified listener
        // will be called once setup completes.
        Log.d(TAG, "Starting setup.");
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                Log.d(TAG, "Setup finished.");


                if (result.getResponse() == BILLING_UNAVAILABLE) {
                    inappBillingSupported = false;
                    isPremium = false;
//                    showAds();
                }

                if (!result.isSuccess()) {
                    // Oh noes, there was a problem.
                    complain("Problem setting up in-app billing: " + result);
                    return;
                }

                // Have we been disposed off in the meantime? If so, quit.
                if (mHelper == null)
                    return;

                // IAB is fully set up. Now, let's get an inventory of stuff we
                // own.
                Log.d(TAG, "Setup successful. Querying inventory.");
                inappBillingSupported = true;
                mHelper.queryInventoryAsync(mGotInventoryListener);
            }
        });
    }

    // Listener that's called when we finish querying the items and
    // subscriptions we own
    private final IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper
            .QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result,
                                             Inventory inventory) {
            Log.d(TAG, "Query inventory finished." + mHelper);

            // Have we been disposed of in the meantime? If so, quit.
            if (mHelper == null)
                return;

            // Is it a failure?
            if (result.isFailure()) {
                complain("Failed to query inventory: " + result);
                return;
            }

            Log.d(TAG, "Query inventory was successful.");

            /*
             * Check for items we own. Notice that for each purchase, we check
             * the developer payload to see if it's correct! See
             * verifyDeveloperPayload().
             */

            // Do we have the premium upgrade?
            isPremium = inventory.hasPurchase(SKU_REMOVE_ADS);
/*            if (removeAdsPurchase) {
                // User paid to remove the Ads - so hide 'em
                isPremium = true;
//                hideAds();
//                consumeItems(inventory);
            } else {
                isPremium = false;
//                showAds();
            }*/
            Log.d(TAG, "User has " + (isPremium ? "REMOVED ADS" : "NOT REMOVED ADS"));
            Log.d(TAG, "Initial inventory query finished; enabling main UI.");
        }
    };

    // Callback for when a purchase is finished
    private final IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper
            .OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            Log.d(TAG, "Purchase finished: " + result + ", purchase: "
                    + purchase);

            // if we were disposed of in the meantime, quit.
            if (mHelper == null)
                return;

            if (result.isFailure()) {
                complain("Error purchasing: " + result);
                return;
            }
            Log.d(TAG, "Purchase successful.");

            if (purchase.getSku().equals(SKU_REMOVE_ADS)) {
                // bought the premium upgrade!
                isPremium = true;
            }
        }
    };

    private void complain(String message) {
        Log.e(TAG, "**** Error: " + message);
    }

    public BillingStatus getInAppBillingStatus() {
        if (!inappBillingSupported) {
            return BillingStatus.UNSUPPORTED;
        } else if (isPremium) {
            return BillingStatus.PREMIUM;
        } else {
            return BillingStatus.FREE;
        }
    }

    public void disposeBilling() {
        if (mHelper != null) {
            mHelper.dispose();
        }
        mHelper = null;
    }

    public void launchPurchaseFlow(Activity context) {
        String payload = "REMOVE_ADS";
        mHelper.launchPurchaseFlow(context, SKU_REMOVE_ADS,
                RC_REQUEST, mPurchaseFinishedListener, payload);
    }

    // --Commented out by Inspection START (22-11-2016 11:20 PM):
//    private void consumeItems(Inventory inventory) {
//        Purchase premiumPurchase = inventory.getPurchase(SKU_REMOVE_ADS);
//        boolean isConsumed =  mSharedPreferences.getBoolean("consumed",false);
//        Log.d(TAG, "consumeItems : premiumPurchase="+premiumPurchase+ " consumed="+isConsumed);
//
//        if (premiumPurchase != null && !isConsumed) {
//            mHelper.consumeAsync(premiumPurchase, new IabHelper.OnConsumeFinishedListener() {
//                @Override
//                public void onConsumeFinished(Purchase purchase, IabResult result) {
//                    Log.d(TAG, "Test purchase is consumed.");
//                    mSharedPreferences.edit().putBoolean("consumed",true).apply();
//
//                }
//            });
//        }
//    }
// --Commented out by Inspection STOP (22-11-2016 11:20 PM)
}
