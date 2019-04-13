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

package com.siju.acexplorer.premium;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;

import com.android.billingclient.api.BillingClient;
import com.siju.acexplorer.R;
import com.siju.acexplorer.billing.BillingManager;
import com.siju.acexplorer.main.view.dialog.DialogHelper;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Created by Siju on 29 August,2017
 */
public class Premium {
    private AppCompatActivity context;
    private BillingManager billingManager;

    public Premium(AppCompatActivity activity, BillingManager billingManager) {
        this.context = activity;
        this.billingManager = billingManager;
    }

    public void showPremiumDialog() {

        String[] text = {context.getString(R.string.unlock_full_version),
                context.getString(R.string.full_version_buy_ask),
                context.getString(R.string.yes),
                context.getString(R.string.no)};

        DialogHelper.showAlertDialog(context, text, alertDialogListener);
    }


    private DialogHelper.AlertDialogListener alertDialogListener = new DialogHelper.AlertDialogListener() {

        @Override
        public void onPositiveButtonClick(View view) {
            showPurchaseDialog(context);
        }

        @Override
        public void onNegativeButtonClick(View view) {
            optOutPremiumDialog();
        }

        @Override
        public void onNeutralButtonClick(View view) {
            //do-nothing
        }
    };

    private void showPurchaseDialog(AppCompatActivity context) {
        billingManager.initiatePurchaseFlow(context, BillingManager.SKU_REMOVE_ADS,
                                                          BillingClient.SkuType.INAPP);
    }


    private void optOutPremiumDialog() {
        SharedPreferences pref = context.getSharedPreferences(PremiumUtils.PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(PremiumUtils.KEY_OPT_OUT, true).apply();
    }
}
