package com.siju.acexplorer.filesystem.backstack;

import com.siju.acexplorer.common.Logger;
import com.siju.acexplorer.filesystem.groups.Category;
import com.siju.acexplorer.filesystem.model.BackStackModel;

import java.util.ArrayList;


public class BackStackInfo {
    private final ArrayList<BackStackModel> backStack = new ArrayList<>();
    private static final String TAG = "BackStackInfo";

    private Category category;

    public void addToBackStack(String path, Category category) {
        backStack.add(new BackStackModel(path, category));
        Logger.log(TAG, "Back stack--size=" + backStack.size() + " Path=" + path + "Category=" + category);
    }



    public void clearBackStack() {
         backStack.clear();
    }

    public  ArrayList<BackStackModel> getBackStack() {
        return backStack;
    }

    public void removeEntry(int index) {
        backStack.remove(index);
    }

    public String getCurrentDir(int index) {
        return backStack.get(index).getFilePath();
    }

    public Category getCurrentCategory(int index) {
        return backStack.get(index).getCategory();
    }



}
