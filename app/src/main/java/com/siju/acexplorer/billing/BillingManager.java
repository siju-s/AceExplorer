package com.siju.acexplorer.billing;

import android.util.Log;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClient.BillingResponse;
import com.android.billingclient.api.BillingClient.FeatureType;
import com.android.billingclient.api.BillingClient.SkuType;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.Purchase.PurchasesResult;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.siju.acexplorer.AceApplication;
import com.siju.acexplorer.logging.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Handles all the interactions with Play Store (via Billing library), maintains connection to
 * it through BillingClient and caches temporary states/data if needed
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class BillingManager implements PurchasesUpdatedListener {
    public static final  String         SKU_REMOVE_ADS = "com.siju.acexplorer.pro"; //"android.test.purchased";
    public static final  int            BILLING_MANAGER_NOT_INITIALIZED = -1;
    private static final String         TAG = "BillingManager";

    static {
        System.loadLibrary("keys");
    }

    private final List<Purchase> mPurchases = new ArrayList<>();
    private        boolean        inappBillingSupported = true;
    private        boolean        isPremium             = true;
    /**
     * A reference to BillingClient
     **/
    private BillingClient billingClient;
    /**
     * True if billing service is connected now.
     */
    private boolean serviceConnected;
    private BillingUpdatesListener billingUpdatesListener;
    private Set<String> mTokensToBeConsumed;
    private int mBillingClientResponseCode = BILLING_MANAGER_NOT_INITIALIZED;


    public void setupBilling(final BillingUpdatesListener updatesListener) {
        billingUpdatesListener = updatesListener;
        billingClient = BillingClient.newBuilder(AceApplication.getAppContext()).setListener(this).build();

        // Start setup. This is asynchronous and the specified listener will be called
        // once setup completes.
        // It also starts to report all the new purchases through onPurchasesUpdated() callback.
        startServiceConnection(new Runnable() {
            @Override
            public void run() {
                // Notifying the listener that billing client is ready
                billingUpdatesListener.onBillingClientSetupFinished();
                // IAB is fully set up. Now, let's get an inventory of stuff we own.
                queryPurchases();
            }
        });
    }

    public void startServiceConnection(final Runnable executeOnSuccess) {
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@BillingResponse int billingResponseCode) {
                if (billingResponseCode == BillingResponse.OK) {
                    serviceConnected = true;
                    if (executeOnSuccess != null) {
                        executeOnSuccess.run();
                    }
                }
                mBillingClientResponseCode = billingResponseCode;
            }

            @Override
            public void onBillingServiceDisconnected() {
                serviceConnected = false;
            }
        });
    }

    /**
     * Query purchases across various use cases and deliver the result in a formalized way through
     * a listener
     */
    public void queryPurchases() {
        Runnable queryToExecute = new Runnable() {
            @Override
            public void run() {
                long time = System.currentTimeMillis();
                if (billingClient == null) {
                    return;
                }
                PurchasesResult purchasesResult = billingClient.queryPurchases(SkuType.INAPP);
                Logger.log(TAG, "Querying purchases elapsed time: " + (System.currentTimeMillis() - time)
                                + "ms");
                // If there are subscriptions supported, we add subscription rows as well
                if (areSubscriptionsSupported()) {
                    PurchasesResult subscriptionResult
                            = billingClient.queryPurchases(SkuType.SUBS);
                    Logger.log(TAG, "Querying purchases and subscriptions elapsed time: "
                                    + (System.currentTimeMillis() - time) + "ms");
                    Logger.log(TAG, "Querying subscriptions result code: "
                                    + subscriptionResult.getResponseCode()
                                    + " res: " + subscriptionResult.getPurchasesList().size());

                    if (subscriptionResult.getResponseCode() == BillingResponse.OK) {
                        purchasesResult.getPurchasesList().addAll(
                                subscriptionResult.getPurchasesList());
                    } else {
                        Logger.log(TAG, "Got an error response trying to query subscription purchases");
                    }
                } else if (purchasesResult.getResponseCode() == BillingResponse.OK) {
                    Logger.log(TAG, "Skipped subscription purchases query since they are not supported");
                } else {
                    Logger.log(TAG, "queryPurchases() got an error response code: "
                                    + purchasesResult.getResponseCode());
                }
                onQueryPurchasesFinished(purchasesResult);
//                if (purchasesResult != null && purchasesResult.getPurchasesList().size() > 0) {
//                consumeAsync(purchasesResult.getPurchasesList().get(0).getPurchaseToken());
//                }
            }
        };

        executeServiceRequest(queryToExecute);
    }

    /**
     * Checks if subscriptions are supported for current client
     * <p>Note: This method does not automatically retry for RESULT_SERVICE_DISCONNECTED.
     * It is only used in unit tests and after queryPurchases execution, which already has
     * a retry-mechanism implemented.
     * </p>
     */
    public boolean areSubscriptionsSupported() {
        int responseCode = billingClient.isFeatureSupported(FeatureType.SUBSCRIPTIONS);
        if (responseCode != BillingResponse.OK) {
            Logger.log(TAG, "areSubscriptionsSupported() got an error response: " + responseCode);
        }
        return responseCode == BillingResponse.OK;
    }

    /**
     * Handle a result from querying of purchases and report an updated list to the listener
     */
    private void onQueryPurchasesFinished(PurchasesResult result) {
        // Have we been disposed of in the meantime? If so, or bad result code, then quit
        if (billingClient == null || result.getResponseCode() != BillingResponse.OK) {
            Logger.log(TAG, "Billing client was null or result code (" + result.getResponseCode()
                            + ") was bad - quitting");
            return;
        }

        if (result.getResponseCode() == BillingResponse.BILLING_UNAVAILABLE) {
            inappBillingSupported = false;
            isPremium = false;
            return;
        }

        // Update the UI and purchases inventory with new list of purchases
        mPurchases.clear();
        onPurchasesUpdated(BillingResponse.OK, result.getPurchasesList());
    }

    private void executeServiceRequest(Runnable runnable) {
        if (serviceConnected) {
            runnable.run();
        } else {
            // If billing service was disconnected, we try to reconnect 1 time.
            // (feel free to introduce your retry policy here).
            startServiceConnection(runnable);
        }
    }

    /**
     * Handle a callback that purchases were updated from the Billing library
     */
    @Override
    public void onPurchasesUpdated(int resultCode, List<Purchase> purchases) {
        if (resultCode == BillingResponse.OK) {
            for (Purchase purchase : purchases) {
                handlePurchase(purchase);
            }
            billingUpdatesListener.onPurchasesUpdated(mPurchases);
        } else if (resultCode == BillingResponse.USER_CANCELED) {
            Logger.log(TAG, "onPurchasesUpdated() - user cancelled the purchase flow - skipping");
        } else {
            Logger.log(TAG, "onPurchasesUpdated() got unknown resultCode: " + resultCode);
        }
    }

    /**
     * Handles the purchase
     * <p>Note: Notice that for each purchase, we check if signature is valid on the client.
     * It's recommended to move this check into your backend.
     * </p>
     *
     * @param purchase Purchase to be handled
     */
    private void handlePurchase(Purchase purchase) {
        if (!verifyValidSignature(purchase.getOriginalJson(), purchase.getSignature())) {
            return;
        }

        isPremium = purchase.getSku().equals(SKU_REMOVE_ADS);

        Logger.log(TAG, "User has " + (isPremium ? "REMOVED ADS" : "NOT REMOVED ADS"));
        mPurchases.add(purchase);
    }

    /**
     * Verifies that the purchase was signed correctly for this developer's public key.
     * <p>Note: It's strongly recommended to perform such check on your backend since hackers can
     * replace this method with "constant true" if they decompile/rebuild your app.
     * </p>
     */
    private boolean verifyValidSignature(String signedData, String signature) {
        String key = getNativeKey();
        try {
            return Security.verifyPurchase(key, signedData, signature);
        }
        catch (IOException e) {
            Log.e(TAG, "Got an exception trying to validate a purchase: " + e);
            return false;
        }
    }

    public native String getNativeKey();

    /**
     * Start a purchase flow
     */
    public void initiatePurchaseFlow(AppCompatActivity context, final String skuId, final @SkuType String billingType) {
        initiatePurchaseFlow(context, skuId, null, billingType);
    }

    /**
     * Start a purchase or subscription replace flow
     */
    @SuppressWarnings("SameParameterValue")
    public void initiatePurchaseFlow(final AppCompatActivity activity, final String skuId, final String oldSku,
                                     final @SkuType String billingType)
    {
        Runnable purchaseFlowRequest = new Runnable() {
            @Override
            public void run() {
                if (activity == null) {
                    return;
                }
                BillingFlowParams purchaseParams = BillingFlowParams.newBuilder()
                                                                    .setSku(skuId).setType(billingType)
                                                                    .setOldSku(oldSku).build();
                billingClient.launchBillingFlow(activity, purchaseParams);
            }
        };

        executeServiceRequest(purchaseFlowRequest);
    }


    public void querySkuDetailsAsync(@SkuType final String itemType, final List<String> skuList,
                                     final SkuDetailsResponseListener listener)
    {
        // Creating a runnable from the request to use it inside our connection retry policy below
        Runnable queryRequest = new Runnable() {
            @Override
            public void run() {
                // Query the purchase async
                SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
                params.setSkusList(skuList).setType(itemType);
                billingClient.querySkuDetailsAsync(params.build(),
                                                   new SkuDetailsResponseListener() {
                                                       @Override
                                                       public void onSkuDetailsResponse(int responseCode,
                                                                                        List<SkuDetails> skuDetailsList)
                                                       {
                                                           listener.onSkuDetailsResponse(responseCode, skuDetailsList);
                                                       }
                                                   });
            }
        };

        executeServiceRequest(queryRequest);
    }

    public void consumeAsync(final String purchaseToken) {
        // If we've already scheduled to consume this token - no action is needed (this could happen
        // if you received the token when querying purchases inside onReceive() and later from
        // onActivityResult()
        if (mTokensToBeConsumed == null) {
            mTokensToBeConsumed = new HashSet<>();
        } else if (mTokensToBeConsumed.contains(purchaseToken)) {
            return;
        }
        mTokensToBeConsumed.add(purchaseToken);

        // Generating Consume Response listener
        final ConsumeResponseListener onConsumeListener = new ConsumeResponseListener() {
            @Override
            public void onConsumeResponse(@BillingResponse int responseCode, String purchaseToken) {
                // If billing service was disconnected, we try to reconnect 1 time
                // (feel free to introduce your retry policy here).

                billingUpdatesListener.onConsumeFinished(purchaseToken, responseCode);
            }
        };

        // Creating a runnable from the request to use it inside our connection retry policy below
        Runnable consumeRequest = new Runnable() {
            @Override
            public void run() {
                // Consume the purchase async
                billingClient.consumeAsync(purchaseToken, onConsumeListener);
            }
        };

        executeServiceRequest(consumeRequest);
    }

    /**
     * Returns the value Billing client response code or BILLING_MANAGER_NOT_INITIALIZED if the
     * clien connection response was not received yet.
     */
    public int getBillingClientResponseCode() {
        return mBillingClientResponseCode;
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

    /**
     * Clear the resources
     */
    public void destroy() {
        billingUpdatesListener = null;
        if (billingClient != null && billingClient.isReady()) {
            billingClient.endConnection();
            billingClient = null;
        }
    }

    /**
     * Listener to the updates that happen when purchases list was updated or consumption of the
     * item was finished
     */
    public interface BillingUpdatesListener {
        void onBillingClientSetupFinished();

        void onConsumeFinished(String token, @BillingResponse int result);

        void onPurchasesUpdated(List<Purchase> purchases);
    }

    /**
     * Listener for the Billing client state to become connected
     */
    public interface ServiceConnectedListener {
        void onServiceConnected(@BillingResponse int resultCode);
    }


}

