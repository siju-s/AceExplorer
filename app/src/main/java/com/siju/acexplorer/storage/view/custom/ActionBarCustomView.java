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

package com.siju.acexplorer.storage.view.custom;

/**
 * Created by SJ on 05-02-2017.
 */

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public class ActionBarCustomView extends RelativeLayout {
    public ActionBarCustomView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    public ActionBarCustomView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public ActionBarCustomView(Context context) {
        super(context);
    }
}