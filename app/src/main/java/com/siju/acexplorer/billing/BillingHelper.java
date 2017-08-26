/*
 * Copyright (C) 2017 Ace Explorer owned by Siju Sakaria
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.siju.acexplorer.billing;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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

    private boolean inappBillingSupported = true;

    // The helper object
    private IabHelper mHelper;

    private boolean isPremium = true;
    private static BillingHelper billingInstance;
    private BillingResultCallback billingResultCallback;

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
        if (context instanceof BillingResultCallback) {
            billingResultCallback = (BillingResultCallback) context;
        }
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
            billingResultCallback.onBillingResult(getInAppBillingStatus());

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
            billingResultCallback.onBillingResult(getInAppBillingStatus());

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

    public boolean onActivityResult(int requestCode, int resultCode, Intent intent) {
        return mHelper != null && mHelper.handleActivityResult(requestCode, resultCode, intent);
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
