package com.siju.acexplorer;

import android.content.Context;

import com.siju.acexplorer.filesystem.groups.DrawerItems;
import com.siju.acexplorer.model.SectionGroup;

import java.util.ArrayList;

public class BasePresenterImpl implements BasePresenter {
    private BaseView baseView;
    private Context context;
    private DrawerItems drawerItems;


    public BasePresenterImpl(Context context) {
        this.context = context;
        if (context instanceof BaseView) {
            this.baseView = (BaseView) context;
        }
        drawerItems = new DrawerItems(context);
    }


    @Override
    public ArrayList<SectionGroup> getTotalGroupData() {
        return drawerItems.getTotalGroupData();
    }
}
