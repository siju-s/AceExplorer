package com.siju.acexplorer.appmanager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.MenuItem;

import com.siju.acexplorer.R;
import com.siju.acexplorer.appmanager.model.AppModelImpl;
import com.siju.acexplorer.appmanager.presenter.AppPresenter;
import com.siju.acexplorer.appmanager.presenter.AppPresenterImpl;
import com.siju.acexplorer.appmanager.view.AppDetailUi;
import com.siju.acexplorer.base.view.BaseActivity;


@SuppressWarnings("ConstantConditions")
public class AppDetailActivity extends BaseActivity {

    private AppDetailUi appDetailUi;

    public static void openAppInfo(Context context, String packageName) {
        Intent intent = new Intent(context, AppDetailActivity.class);
        intent.putExtra(AppDetailUi.EXTRA_PACKAGE_NAME, packageName);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_detail);

        appDetailUi = findViewById(R.id.appDetailUi);
        appDetailUi.setActivity(this);
        AppPresenter appPresenter = new AppPresenterImpl(appDetailUi, new AppModelImpl());
        appPresenter.setView();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        appDetailUi.handleActivityResult(requestCode, resultCode, data);
    }


    @Override
    protected void onResume() {
        super.onResume();
        appDetailUi.onResume();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
