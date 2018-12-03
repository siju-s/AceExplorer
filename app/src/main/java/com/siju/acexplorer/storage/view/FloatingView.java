package com.siju.acexplorer.storage.view;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.siju.acexplorer.R;
import com.siju.acexplorer.analytics.Analytics;
import com.siju.acexplorer.main.view.dialog.DialogHelper;
import com.siju.acexplorer.storage.model.operations.Operations;
import com.siju.acexplorer.theme.Theme;

@SuppressLint("ClickableViewAccessibility")
public class FloatingView implements View.OnClickListener {

    private Context              context;
    private StoragesUiView       storagesUiView;
    private FrameLayout          frameLayoutFab;
    private FloatingActionsMenu  fabCreateMenu;
    private FloatingActionButton fabCreateFolder;
    private FloatingActionButton fabCreateFile;
    private FloatingActionButton fabOperation;
    private Dialog               dialog;

    FloatingView(StoragesUiView storagesUiView) {
        this.storagesUiView = storagesUiView;
        this.context = storagesUiView.getContext();
        initializeViews(storagesUiView);
        setListeners();
    }

    private void initializeViews(View view) {
        frameLayoutFab = view.findViewById(R.id.frameLayoutFab);
        fabCreateMenu = view.findViewById(R.id.fabCreate);
        fabCreateFolder = view.findViewById(R.id.fabCreateFolder);
        fabCreateFile = view.findViewById(R.id.fabCreateFile);
        fabOperation = view.findViewById(R.id.fabOperation);
    }

    private void setListeners() {
        fabCreateFile.setOnClickListener(this);
        fabCreateFolder.setOnClickListener(this);
        fabOperation.setOnClickListener(this);

        fabCreateMenu.setOnFloatingActionsMenuUpdateListener(new FloatingActionsMenu
                .OnFloatingActionsMenuUpdateListener()
        {

            @Override
            public void onMenuExpanded() {
                frameLayoutFab.getBackground().setAlpha(240);
                frameLayoutFab.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        fabCreateMenu.collapse();
                        return true;
                    }
                });
            }

            @Override
            public void onMenuCollapsed() {
                frameLayoutFab.getBackground().setAlpha(0);
                frameLayoutFab.setOnTouchListener(null);
            }
        });
    }

    void setTheme(Theme theme) {
        if (theme == Theme.DARK) {
            frameLayoutFab.setBackgroundColor(ContextCompat.getColor(getContext(), R.color
                    .dark_overlay));
        }
        frameLayoutFab.getBackground().setAlpha(0);
    }

    private Context getContext() {
        return context;
    }

    void showFab() {
        frameLayoutFab.setVisibility(View.VISIBLE);
    }

    void hideFab() {
        frameLayoutFab.setVisibility(View.GONE);
    }

    boolean isFabExpanded() {
        return fabCreateMenu.isExpanded();
    }

    void collapseFab() {
        fabCreateMenu.collapse();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fabCreateFile:
                Analytics.getLogger().operationClicked(Analytics.Logger.EV_FAB);
                showCreateFileDialog();
                fabCreateMenu.collapse();
                break;
            case R.id.fabCreateFolder:
                Analytics.getLogger().operationClicked(Analytics.Logger.EV_FAB);
                showCreateDirDialog();
                fabCreateMenu.collapse();
                break;

        }
    }

    private void showCreateFileDialog() {
        String title = getContext().getString(R.string.new_file);
        String[] texts = new String[]{title, getContext().getString(R.string.enter_name), getContext
                ().getString(R.string.create), getContext().getString(R.string.dialog_cancel)};
        DialogHelper.showInputDialog(getContext(), texts, Operations.FILE_CREATION, null,
                                     dialogListener);
    }

    void showCreateDirDialog() {
        String title = getContext().getString(R.string.new_folder);
        String[] texts = new String[]{title, getContext().getString(R.string.enter_name), getContext
                ().getString(R.string
                                     .create), getContext().getString(R.string.dialog_cancel)};
        DialogHelper.showInputDialog(getContext(), texts, Operations.FOLDER_CREATION, null,
                                     dialogListener);
    }

    private DialogHelper.DialogCallback dialogListener = new DialogHelper.DialogCallback() {


        @Override
        public void onPositiveButtonClick(Dialog dialog, Operations operation, String name) {
            FloatingView.this.dialog = dialog;
            switch (operation) {
                case FOLDER_CREATION:
                    storagesUiView.createDir(name);
                    break;
                case FILE_CREATION:
                    storagesUiView.createFile(name);
                    break;
            }
        }

        @Override
        public void onNegativeButtonClick(Operations operations) {

        }
    };

    void dismissDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }
}
