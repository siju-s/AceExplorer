package com.siju.acexplorer.appmanager.presenter;

import com.siju.acexplorer.appmanager.model.AppModel;
import com.siju.acexplorer.appmanager.view.AppDetailUi;

public class AppPresenterImpl implements AppPresenter, AppDetailUi.Listener {

    private AppDetailUi appDetailUi;
    private AppModel    appModel;

    public AppPresenterImpl(AppDetailUi appDetailUi, AppModel appModel) {
        this.appDetailUi = appDetailUi;
        this.appModel = appModel;
        appDetailUi.setListener(this);
    }

    @Override
    public void setView() {
        appDetailUi.inflateView();
    }

    @Override
    public Object getPackageInfo(String packageName) {
        return appModel.getPackageInfo(packageName);
    }

    @Override
    public String getInstallerSource(String packageName) {
        return appModel.getInstallerSource(packageName);
    }


}
