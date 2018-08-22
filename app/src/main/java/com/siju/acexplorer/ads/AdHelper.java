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

package com.siju.acexplorer.ads;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.ads.MobileAds;

import static com.siju.acexplorer.main.model.FileConstants.ADS;
import static com.siju.acexplorer.main.model.FileConstants.KEY_PREMIUM;

/**
 * Created by Siju on 29 August,2017
 */
public class AdHelper {

    public static void setupAds(final Activity activity) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!activity.isFinishing()) {
                    MobileAds.initialize(activity.getApplicationContext(), "ca-app-pub-3940256099942544~3347511713");
                    Intent intent = new Intent(ADS);
                    intent.putExtra(KEY_PREMIUM, false);
                    LocalBroadcastManager.getInstance(activity).sendBroadcast(intent);
                }
            }
        }, 2000);

    }
}
