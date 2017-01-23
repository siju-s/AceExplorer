package com.siju.acexplorer.filesystem.backstack;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.siju.acexplorer.R;
import com.siju.acexplorer.common.Logger;
import com.siju.acexplorer.filesystem.groups.Category;
import com.siju.acexplorer.filesystem.groups.StoragesGroup;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

import static com.siju.acexplorer.filesystem.utils.FileUtils.getInternalStorage;


public class NavigationInfo {
    private static final String TAG = "NavigationInfo";
    private String currentDir;
    private String initialDir;
    private Context context;
    private NavigationCallback navigationCallback;
    private boolean isCurrentDirRoot;
    private String STORAGE_INTERNAL,STORAGE_ROOT,STORAGE_EXTERNAL;
    private ArrayList<String> externalSDPaths = new ArrayList<>();


    public NavigationInfo(Context context) {
        this.context = context;
        navigationCallback = (NavigationCallback) context;
        STORAGE_ROOT = context.getResources().getString(R.string.nav_menu_root);
        STORAGE_INTERNAL = context.getResources().getString(R.string.nav_menu_internal_storage);
        STORAGE_EXTERNAL = context.getResources().getString(R.string.nav_menu_ext_storage);
        externalSDPaths = new StoragesGroup(context).getExternalSDList();
    }

    public void addHomeNavButton(boolean isHomeScreenEnabled, Category category) {

        if (isHomeScreenEnabled) {
            navigationCallback.clearNavigation();
            ImageButton imageButton = new ImageButton(context);
            imageButton.setImageResource(R.drawable.ic_home_white);
            imageButton.setBackgroundColor(Color.parseColor("#00ffffff"));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.CENTER_VERTICAL;

            imageButton.setLayoutParams(params);
            imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    navigationCallback.onHomeClicked();
                }
            });
            navigationCallback.addViewToNavigation(imageButton);

            ImageView navArrow = new ImageView(context);
            params.leftMargin = 15;
            params.rightMargin = 20;
            navArrow.setImageResource(R.drawable.ic_arrow_nav);
            navArrow.setLayoutParams(params);
            navigationCallback.addViewToNavigation(navArrow);
            addTitleText(category);
        } else {
            addTitleText(category);
        }

    }

    private void addTitleText(Category category) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER_VERTICAL;

        if (!category.equals(Category.FILES)) {
            String title = getTitleForCategory(category).toUpperCase(Locale.getDefault());
            final TextView textView = new TextView(context);
            textView.setText(title);
            textView.setTextColor(ContextCompat.getColor(context, R.color.navButtons));
            textView.setTextSize(19);
            int paddingRight = context.getResources().getDimensionPixelSize(R.dimen.padding_60);
            textView.setPadding(0, 0, paddingRight, 0);
            textView.setLayoutParams(params);
            navigationCallback.addViewToNavigation(textView);
        }
    }

    public String getTitleForCategory(Category category) {
        switch (category) {
            case AUDIO:
                return context.getString(R.string.nav_menu_music);
            case VIDEO:
                return context.getString(R.string.nav_menu_video);
            case IMAGE:
                return context.getString(R.string.nav_menu_image);
            case DOCS:
                return context.getString(R.string.nav_menu_docs);
            case DOWNLOADS:
                return context.getString(R.string.downloads);
            case COMPRESSED:
                return context.getString(R.string.compressed);
            case FAVORITES:
                return context.getString(R.string.nav_header_favourites);
            case PDF:
                return context.getString(R.string.pdf);
            case APPS:
                return context.getString(R.string.apk);
            case LARGE_FILES:
                return context.getString(R.string.library_large);

        }
        return context.getString(R.string.app_name);
    }

    public void setNavDirectory(String path,boolean isHomeScreenEnabled, Category category) {
        String[] parts;
        parts = path.split(File.separator);

        navigationCallback.clearNavigation();
        currentDir = path;
        String dir = "";
        addHomeNavButton(isHomeScreenEnabled, category);

        // If root dir , parts will be 0
        if (parts.length == 0) {

            isCurrentDirRoot = true;
            initialDir = File.separator;
            setNavDir(File.separator, File.separator); // Add Root button
        } else {
            int count = 0;
            for (int i = 1; i < parts.length; i++) {
                dir += File.separator + parts[i];

                if (!dir.contains(initialDir)) {
                    continue;
                }
                /*Count check so that ROOT is added only once in Navigation
                  Handles the scenario :
                  1. When Fav item is a root child and if we click on any folder in that fav item
                     multiple ROOT blocks are not added to Navigation view*/
                if (isCurrentDirRoot && count == 0) {
                    setNavDir(File.separator, File.separator);
                }

                count++;
                setNavDir(dir, parts[i]);
            }
        }

    }

    private void setNavDir(String dir, String parts) {


        int WRAP_CONTENT = LinearLayout.LayoutParams.WRAP_CONTENT;
        if (dir.equals(getInternalStorage().getAbsolutePath())) {
            isCurrentDirRoot = false;
            createNavButton(STORAGE_INTERNAL, dir);
        } else if (dir.equals(File.separator)) {
            createNavButton(STORAGE_ROOT, dir);
        } else if (externalSDPaths.contains(dir)) {
            isCurrentDirRoot = false;
            createNavButton(STORAGE_EXTERNAL, dir);
        } else {
            ImageView navArrow = new ImageView(context);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(WRAP_CONTENT,
                    WRAP_CONTENT);
            layoutParams.leftMargin = 20;
            layoutParams.gravity = Gravity.CENTER_VERTICAL;
            navArrow.setImageResource(R.drawable.ic_arrow_nav);
            navArrow.setLayoutParams(layoutParams);

            navigationCallback.addViewToNavigation(navArrow);
            createNavButton(parts, dir);
        }
    }

    private void createNavButton(String text, final String dir) {

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER_VERTICAL;

        if (text.equals(STORAGE_INTERNAL) || text.equals(STORAGE_EXTERNAL) ||
                text.equals(STORAGE_ROOT)) {
            ImageButton imageButton = new ImageButton(context);
            if (text.equals(STORAGE_INTERNAL)) {
                imageButton.setImageResource(R.drawable.ic_storage_white_nav);
            } else if (text.equals(STORAGE_EXTERNAL)) {
                imageButton.setImageResource(R.drawable.ic_ext_nav);
            } else {
                imageButton.setImageResource(R.drawable.ic_root_white_nav);
            }
            imageButton.setBackgroundColor(Color.parseColor("#00ffffff"));
            imageButton.setLayoutParams(params);
            imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (dir != null) {
                        navButtonOnClick(dir);
                    }
                }
            });
            navigationCallback.addViewToNavigation(imageButton);


        } else {
            final TextView textView = new TextView(context);
            textView.setText(text);
            textView.setAllCaps(true);
            textView.setTextColor(ContextCompat.getColor(context, R.color.navButtons));
            textView.setTextSize(15);
            params.leftMargin = 20;
            textView.setPadding(0, 0, 35, 0);
            textView.setLayoutParams(params);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Logger.log(TAG, "nav button onclick--dir=" + dir);
                    if (dir != null) {
                        navButtonOnClick(dir);
                    }
                }
            });
            navigationCallback.addViewToNavigation(textView);

        }
    }

    private void navButtonOnClick(final String dir) {
        Logger.log(TAG, "Dir=" + dir + " currentDir=" + currentDir);

        navigationCallback.onNavButtonClicked(dir);

    }
}
