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

package com.siju.acexplorer.base.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;

import com.siju.acexplorer.R;
import com.siju.acexplorer.appmanager.AppDetailActivity;
import com.siju.acexplorer.base.model.BaseModel;
import com.siju.acexplorer.base.model.BaseModelImpl;
import com.siju.acexplorer.base.presenter.BasePresenter;
import com.siju.acexplorer.base.presenter.BasePresenterImpl;
import com.siju.acexplorer.theme.Theme;
import com.siju.acexplorer.utils.LocaleHelper;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


@SuppressLint("Registered")
public class BaseActivity extends AppCompatActivity {

    private Theme currentTheme;

    @Override
    protected void attachBaseContext(Context newBase) {
        BaseModel baseModel = new BaseModelImpl();
        BasePresenter basePresenter = new BasePresenterImpl(baseModel);
        currentTheme = basePresenter.getTheme();
        Context context = LocaleHelper.setLanguage(newBase);
        super.attachBaseContext(context);
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme();
        super.onCreate(savedInstanceState);
    }

    private void setTheme() {
        switch (currentTheme) {
            case DARK:
                if (this instanceof AppDetailActivity) {
                    setTheme(R.style.BaseDarkTheme_AppInfo);
                } else {
                    setTheme(R.style.BaseDarkTheme);
                }
                break;
            case LIGHT:
                if (this instanceof AppDetailActivity) {
                    setTheme(R.style.BaseLightTheme_AppInfo);
                } else {
                    setTheme(R.style.BaseLightTheme);
                }
                break;
        }
    }

    public Theme getCurrentTheme() {
        return currentTheme;
    }

}
