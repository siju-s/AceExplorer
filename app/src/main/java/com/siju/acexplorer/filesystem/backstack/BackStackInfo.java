package com.siju.acexplorer.filesystem.backstack;

import com.siju.acexplorer.common.Logger;
import com.siju.acexplorer.filesystem.groups.Category;
import com.siju.acexplorer.filesystem.model.BackStackModel;
import com.siju.acexplorer.filesystem.utils.FileUtils;

import java.util.ArrayList;


public class BackStackInfo {
    private final ArrayList<BackStackModel> backStack = new ArrayList<>();
    private static final String TAG = "BackStackInfo";

    private Category category;

    public void addToBackStack(String path, Category category) {
        backStack.add(new BackStackModel(path, category));
        Logger.log(TAG, "Back stack--size=" + backStack.size() + " Path=" + path + "Category=" + category);
    }

    private boolean checkIfBackStackExists() {
        int backStackSize;
        backStackSize = backStack.size();
        Logger.log(TAG, "checkIfBackStackExists --size=" + backStackSize);


        if (backStackSize == 1) {
            currentDir = backStack.get(0).getFilePath();
            category = backStack.get(0).getCategory();
            Logger.log(TAG, "checkIfBackStackExists--Path=" + currentDir + "  Category=" + category);
            backStack.clear();
            return false;
        } else if (backStackSize > 1) {
            int newSize = backStackSize - 1;
            backStack.remove(newSize);
            currentDir = backStack.get(newSize - 1).getFilePath();
            category = backStack.get(newSize - 1).getCategory();
            if (FileUtils.checkIfFileCategory(category) && !mIsFromHomePage) {
                initializeStartingDirectory();
            } else {
                hideFab();
            }

            Logger.log(TAG, "checkIfBackStackExists--Path=" + currentDir + "  Category=" + category);
            Logger.log(TAG, "checkIfBackStackExists --New size=" + backStack.size());
            return true;
        }
//        Logger.log(TAG, "checkIfBackStackExists --Path=" + mCurrentDir + "  Category=" + mCategory);
        return false;
    }

    public void clearBackStack() {

    }


}
