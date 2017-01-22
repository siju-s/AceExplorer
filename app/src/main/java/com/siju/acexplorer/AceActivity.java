package com.siju.acexplorer;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.kobakei.ratethisapp.RateThisApp;
import com.siju.acexplorer.billing.BillingHelper;
import com.siju.acexplorer.billing.BillingResultCallback;
import com.siju.acexplorer.billing.BillingStatus;
import com.siju.acexplorer.common.Logger;
import com.siju.acexplorer.common.SharedPreferenceWrapper;
import com.siju.acexplorer.filesystem.BaseFileList;
import com.siju.acexplorer.filesystem.FileConstants;
import com.siju.acexplorer.filesystem.HomeScreenFragment;
import com.siju.acexplorer.filesystem.groups.Category;
import com.siju.acexplorer.filesystem.groups.DrawerGroups;
import com.siju.acexplorer.filesystem.groups.DrawerItems;
import com.siju.acexplorer.filesystem.helper.FileOpsHelper;
import com.siju.acexplorer.filesystem.model.BackStackModel;
import com.siju.acexplorer.filesystem.model.CopyData;
import com.siju.acexplorer.filesystem.model.FavInfo;
import com.siju.acexplorer.filesystem.model.FileInfo;
import com.siju.acexplorer.filesystem.task.CopyService;
import com.siju.acexplorer.filesystem.task.DeleteTask;
import com.siju.acexplorer.filesystem.task.MoveFiles;
import com.siju.acexplorer.filesystem.theme.Themes;
import com.siju.acexplorer.filesystem.utils.FileUtils;
import com.siju.acexplorer.filesystem.utils.PremiumUtils;
import com.siju.acexplorer.model.SectionGroup;
import com.siju.acexplorer.model.SectionItems;
import com.siju.acexplorer.permission.PermissionHelper;
import com.siju.acexplorer.permission.PermissionResultCallback;
import com.siju.acexplorer.permission.PermissionUtils;
import com.siju.acexplorer.settings.SettingsActivity;
import com.siju.acexplorer.ui.ScrimInsetsRelativeLayout;
import com.siju.acexplorer.utils.DialogUtils;
import com.siju.acexplorer.utils.LocaleHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import eu.chainfire.libsuperuser.Shell;

import static com.siju.acexplorer.R.id.frameLayoutFab;
import static com.siju.acexplorer.filesystem.FileConstants.ADS;
import static com.siju.acexplorer.filesystem.FileConstants.KEY_PREMIUM;
import static com.siju.acexplorer.filesystem.groups.Category.AUDIO;
import static com.siju.acexplorer.filesystem.groups.Category.DOCS;
import static com.siju.acexplorer.filesystem.groups.Category.FILES;
import static com.siju.acexplorer.filesystem.groups.Category.IMAGE;
import static com.siju.acexplorer.filesystem.groups.Category.VIDEO;
import static com.siju.acexplorer.filesystem.utils.FileUtils.getInternalStorage;


public class AceActivity extends BaseActivity
        implements View.OnClickListener,
        PermissionResultCallback,
        BillingResultCallback {

    public static final String PREFS_FIRST_RUN = "first_app_run";
    public static final int PERMISSIONS_REQUEST = 100;
    public static final int SETTINGS_REQUEST = 200;
    private static final int PREFS_REQUEST = 1000;
    public static final int SAF_REQUEST = 3000;
    private static final int REQUEST_INVITE = 4000;

    public int mOperation = -1;
    public String mOldFilePath;
    public String mNewFilePath;
    public String mFileName;
    public ArrayList<FileInfo> mFiles = new ArrayList<>();
    public ArrayList<FileInfo> mTotalFiles = new ArrayList<>();
    public ArrayList<CopyData> mCopyData = new ArrayList<>();
    public FileOpsHelper mFileOpsHelper;
    public int mRenamedPosition;

    private final String TAG = this.getClass().getSimpleName();
    private ExpandableListAdapter expandableListAdapter;
    private ExpandableListView expandableListView;
    private ArrayList<SectionGroup> totalGroupData = new ArrayList<>();
    private DrawerLayout drawerLayout;
    private ScrimInsetsRelativeLayout relativeLayoutDrawerPane;
    private String mCurrentDir;
    private String mCurrentDirDualPane;


    private final ArrayList<SectionItems> favouritesGroupChild = new ArrayList<>();
    private SharedPreferenceWrapper sharedPreferenceWrapper;
    private SharedPreferences mSharedPreferences;
    private ArrayList<FavInfo> savedFavourites = new ArrayList<>();
    private View mViewSeperator;
    private Category mCategory = FILES;
    private int mCategoryDual = FileConstants.CATEGORY.FILES.getValue();
    private CoordinatorLayout mMainLayout;
    private final int MENU_FAVOURITES = 1;
    private boolean mIsFirstRun;
    private boolean mIsDualPaneEnabled;
    private boolean mShowDualPane;
    private boolean mIsHomeScreenEnabled;
    private boolean mShowHidden;
    private boolean mIsHomePageAdded;

    private boolean isCurrentDirRoot;
    private boolean isCurrentDualDirRoot;
    private String mStartingDir;
    private String mStartingDirDualPane;
    private boolean mIsDualModeEnabled;
    private boolean mIsFromHomePage;
    private int mCurrentTheme = FileConstants.THEME_LIGHT;
    private final ArrayList<String> mExternalSDPaths = new ArrayList<>();
    private final ArrayList<BackStackModel> mBackStackList = new ArrayList<>();
    private final ArrayList<BackStackModel> mBackStackListDual = new ArrayList<>();
    private int mCurrentOrientation;
    private boolean mIsRootMode;
    private boolean mIsTablet;
    private HomeScreenFragment mHomeScreenFragment;
    private boolean inappShortcutMode;

    private RelativeLayout unlockPremium;
    private RelativeLayout rateUs;
    private RelativeLayout settings;
    private ImageView imageInvite;


    public static Shell.Interactive shellInteractive;
    private static Handler handler;
    private static HandlerThread handlerThread;
    private PermissionHelper permissionHelper;
    private DrawerItems drawerItems;
    private BasePresenterImpl basePresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        LocaleHelper.setLanguage(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        basePresenter = new BasePresenterImpl(this);
        BillingHelper.getInstance().setupBilling(this);
        PreferenceManager.setDefaultValues(this, R.xml.pref_settings, false);
        Logger.log(TAG, "onCreate");
        initViews();
        setViewTheme();

//        removeFragmentsOnPermissionRevoked(savedInstanceState);
        permissionHelper = new PermissionHelper(this);
        permissionHelper.checkPermissions();
        setupInitialData();
        registerReceivers();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.main_container);
            if (fragment instanceof BaseFileList) {
                ((BaseFileList) fragment).performVoiceSearch(query);
            }
        }
    }


    private void initViews() {
        mFrameDualPane = (FrameLayout) findViewById(R.id.frame_container_dual);

        mMainLayout = (CoordinatorLayout) findViewById(R.id.main_content);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        relativeLayoutDrawerPane = (ScrimInsetsRelativeLayout) findViewById(R.id.drawerPane);
        mViewSeperator = findViewById(R.id.viewSeperator);
        unlockPremium = (RelativeLayout) findViewById(R.id.unlockPremium);
        rateUs = (RelativeLayout) findViewById(R.id.rateUs);
        settings = (RelativeLayout) findViewById(R.id.layoutSettings);
        expandableListView = (ExpandableListView) findViewById(R.id.expand_list_drawer);
        View list_header = View.inflate(this, R.layout.drawerlist_header, null);
        expandableListView.addHeaderView(list_header);
        imageInvite = (ImageView) findViewById(R.id.imageInvite);
        mFileOpsHelper = new FileOpsHelper(this);
    }

    private void registerReceivers() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_LOCALE_CHANGED);
        registerReceiver(mLocaleListener, filter);
    }

    private void removeFragmentsOnPermissionRevoked(Bundle savedInstance) {

        if (savedInstance != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();

            // Remove previous fragments (case of the app was restarted after changed permission on android 6 and
            // higher)
            List<Fragment> fragmentList = fragmentManager.getFragments();
            if (fragmentList != null) {
                for (Fragment fragment : fragmentList) {
                    if (fragment != null) {
                        Logger.log(TAG, "onCreate--Fragment=" + fragment);
                        fragmentManager.beginTransaction().remove(fragment).commit();
                    }
                }
            }
        }
    }

    private void setViewTheme() {
        Themes theme = getCurrentTheme();
        switch (theme) {
            case DARK:
                relativeLayoutDrawerPane.setBackgroundColor(ContextCompat.getColor(this, R.color.dark_colorPrimary));
                mMainLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.dark_colorPrimary));
                break;
            case LIGHT:
                relativeLayoutDrawerPane.setBackgroundColor(ContextCompat.getColor(this, R.color.navDrawerBg));
                break;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[]
            grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST:
                permissionHelper.onPermissionResult();
                break;
        }
    }


    private void setupInitialData() {
        checkPreferences();
//        initializeInteractiveShell();
        if (!checkIfInAppShortcut(getIntent())) {
            initialScreenSetup(mIsHomeScreenEnabled);
        }
        initListeners();
        setListAdapter();
    }


    private void checkPreferences() {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (mSharedPreferences.getBoolean(PREFS_FIRST_RUN, true)) {
            mIsFirstRun = true;
            mSharedPreferences.edit().putInt(FileConstants.KEY_SORT_MODE, FileConstants
                    .KEY_SORT_NAME).apply();
        }
        mIsHomeScreenEnabled = mSharedPreferences.getBoolean(FileConstants.PREFS_HOMESCREEN, true);
        mIsRootMode = mSharedPreferences.getBoolean(FileConstants.PREFS_ROOTED, false);
    }

    /**
     * Initializes an interactive shell, which will stay throughout the app lifecycle
     * The shell is associated with a handler thread which maintain the message queue from the
     * callbacks of shell as we certainly cannot allow the callbacks to run on same thread because
     * of possible deadlock situation and the asynchronous behaviour of LibSuperSU
     */
    private void initializeInteractiveShell() {

        // only one looper can be associated to a thread. So we're making sure not to create new
        // handler threads every time the code relaunch.
        if (mIsRootMode) {

            handlerThread = new HandlerThread("root_handler");
            handlerThread.start();
            handler = new Handler(handlerThread.getLooper());
            shellInteractive = (new Shell.Builder()).useSU().setHandler(handler).open();
        }
    }


    private void initListeners() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, R.string.navigation_drawer_open, R.string
                .navigation_drawer_close) {

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, 0);
            }
        };

/*        DrawerArrowDrawable mArrowDrawable = new DrawerArrowDrawable(this);
        mToolbar.setNavigationIcon(mArrowDrawable);*/
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int
                    childPosition, long id) {
                onDrawerItemClick(groupPosition, childPosition);
                return false;
            }
        });

        expandableListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                return false;
            }
        });
        registerForContextMenu(expandableListView);
        rateUs.setOnClickListener(this);
        unlockPremium.setOnClickListener(this);
        settings.setOnClickListener(this);
        imageInvite.setOnClickListener(this);
    }




/*    private void showAds() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isFinishing()) {
                    MobileAds.initialize(getApplicationContext(), "ca-app-pub-3940256099942544~3347511713");
                    Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.main_container);
                    if (fragment instanceof BaseFileList) {
                        ((BaseFileList) fragment).setTrial();
                    } else if (fragment instanceof HomeScreenFragment) {
                        ((HomeScreenFragment) fragment).setTrial();
                    }
                }
            }
        }, 2000);
        if (inappBillingSupported) {
            PremiumUtils.onStart(this);
            if (PremiumUtils.shouldShowPremiumDialog()) {
                showPremiumDialog();
            }
        }
    }*/


    private void setListAdapter() {
        totalGroupData = basePresenter.getTotalGroupData();
        expandableListAdapter = new ExpandableListAdapter(this, totalGroupData);
        expandableListView.setAdapter(expandableListAdapter);
        expandableListView.expandGroup(0);
    }

    private void initialScreenSetup(boolean isHomeScreenEnabled) {
        if (isHomeScreenEnabled) {
            hideFab();
            mNavigationLayout.setVisibility(View.GONE);
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            Bundle args = new Bundle();
            args.putBoolean(FileConstants.KEY_HOME, true);
            args.putBoolean(AceActivity.PREFS_FIRST_RUN, mIsFirstRun);
            args.putBoolean(FileConstants.KEY_DUAL_ENABLED, mIsDualModeEnabled);
//            args.putBoolean(FileConstants.KEY_PREMIUM, isPremium);
            mHomeScreenFragment = new HomeScreenFragment();
            mHomeScreenFragment.setArguments(args);
//            ft.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right, R.anim.enter_from_right, R.anim
//                    .exit_to_left);
//            ft.setCustomAnimations(R.anim.slide_in_left,R.anim.slide_out_right);
            ft.replace(R.id.main_container, mHomeScreenFragment);
            ft.addToBackStack(null);
            Logger.log(TAG, "initialScreenSetup");
            ft.commitAllowingStateLoss();
        } else {
            showFab();
            // Initialising only if Home screen disabled
            currentScreenSetup(getInternalStorage().getAbsolutePath(), FileConstants.CATEGORY.FILES.getValue(), false);
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            Bundle args = new Bundle();
            args.putBoolean(FileConstants.KEY_HOME, false);
            args.putString(FileConstants.KEY_PATH, mCurrentDir);
//            args.putBoolean(FileConstants.KEY_PREMIUM, isPremium);
            BaseFileList baseFileList = new BaseFileList();
            baseFileList.setArguments(args);
//            ft.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right, R.anim.enter_from_right, R.anim
//                    .exit_to_left);
            ft.replace(R.id.main_container, baseFileList, mCurrentDir);
            ft.commitAllowingStateLoss();
            if (mIsDualModeEnabled) {
                toggleDualPaneVisibility(true);
                showDualPane();
            }
        }
    }

    private static final String ACTION_IMAGES = "android.intent.action.SHORTCUT_IMAGES";
    private static final String ACTION_MUSIC = "android.intent.action.SHORTCUT_MUSIC";
    private static final String ACTION_VIDEOS = "android.intent.action.SHORTCUT_VIDEOS";


    private boolean checkIfInAppShortcut(Intent intent) {
        if (intent != null && intent.getAction() != null) {
            Category category = IMAGE;
            switch (intent.getAction()) {
                case ACTION_IMAGES:
                    category = IMAGE;
                    break;
                case ACTION_MUSIC:
                    category = AUDIO;
                    break;
                case ACTION_VIDEOS:
                    category = VIDEO;
                    break;

            }
            inappShortcutMode = true;
            mCategory = category;
            if (PermissionUtils.hasRequiredPermissions()) {
                displayFileList(null, category);
            }

            mIsHomePageAdded = true;
            return true;
        }
        return false;
    }


    private final BroadcastReceiver mLocaleListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction().equals(Intent.ACTION_LOCALE_CHANGED)) {
                restartApp(true);
            }
        }
    };


    @Override
    protected void onStart() {
        super.onStart();
        // Monitor launch times and interval from installation
        RateThisApp.onStart(this);
        // If the criteria is satisfied, "Rate this app" dialog will be shown
        RateThisApp.showRateDialogIfNeeded(this);
    }

    @Override
    protected void onResume() {
        permissionHelper.onResume();
        super.onResume();
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        // Called when user returns from the settings screen
        Log.d(TAG, "onActivityResult(" + requestCode + "," + resultCode + ","
                + intent);
        if (!mHelper.handleActivityResult(requestCode, resultCode, intent)) {
            if (requestCode == PREFS_REQUEST) {
                Logger.log(TAG, "OnActivityResult=" + resultCode);
                if (resultCode == RESULT_OK) {
                    if (intent != null && intent.getBooleanExtra(FileConstants.PREFS_RESET, false)) {
                        resetFavouritesGroup();
                        expandableListView.smoothScrollToPosition(0);
                    }
                    onSharedPrefsChanged();

                }
            } else if (requestCode == SAF_REQUEST) {
                String uriString = mSharedPreferences.getString(FileConstants.SAF_URI, null);

                Uri oldUri = uriString != null ? Uri.parse(uriString) : null;

                if (resultCode == Activity.RESULT_OK) {
                    Uri treeUri = intent.getData();
                    Log.d(TAG, "tree uri=" + treeUri + " old uri=" + oldUri);
                    // Get Uri from Storage Access Framework.
                    // Persist URI - this is required for verification of writability.
                    if (treeUri != null) {
                        mSharedPreferences.edit().putString(FileConstants.SAF_URI, treeUri.toString()).apply();
                        int takeFlags = intent.getFlags();
                        takeFlags &= (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        getContentResolver().takePersistableUriPermission(treeUri, takeFlags);

                        switch (mOperation) {
                            case FileConstants.DELETE:
                                new DeleteTask(this, mIsRootMode, mFiles).execute();
                                mFiles = new ArrayList<>();
                                break;
                            case FileConstants.COPY:
                                Intent intent1 = new Intent(this, CopyService.class);
                                intent1.putParcelableArrayListExtra("FILE_PATHS", mFiles);
                                intent1.putExtra("COPY_DIRECTORY", mNewFilePath);
                                intent.putParcelableArrayListExtra("ACTION", mCopyData);
                                intent1.putParcelableArrayListExtra("TOTAL_LIST", mTotalFiles);
                                new FileUtils().showCopyProgressDialog(this, intent1);
                                break;
                            case FileConstants.MOVE:
                                new MoveFiles(this, mFiles, mCopyData).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                                        mNewFilePath);
                                break;
                            case FileConstants.FOLDER_CREATE:
                                mFileOpsHelper.mkDir(mIsRootMode, mNewFilePath, mFileName);
                                break;
                            case FileConstants.RENAME:
                                mFileOpsHelper.renameFile(mIsRootMode, new File(mOldFilePath), new File(mNewFilePath),
                                        mRenamedPosition, isDualPaneInFocus);
                                break;
                            case FileConstants.FILE_CREATE:
                                mFileOpsHelper.mkFile(mIsRootMode, new File(mNewFilePath));
                                break;
                            case FileConstants.EXTRACT:
                                mFileOpsHelper.extractFile(new File(mOldFilePath), new File(mNewFilePath));
                                break;
                            case FileConstants.COMPRESS:
                                mFileOpsHelper.compressFile(new File(mNewFilePath), mFiles);
                        }
                    }
                }
                // If not confirmed SAF, or if still not writable, then revert settings.
                else {

                    if (oldUri != null)
                        mSharedPreferences.edit().putString(FileConstants.SAF_URI, oldUri.toString()).apply();

                    Toast.makeText(this, getString(R.string.access_denied_external), Toast.LENGTH_LONG).show();
                    return;
                }

                mOperation = -1;
                mNewFilePath = null;
            } else if (requestCode == REQUEST_INVITE) {
                if (resultCode == RESULT_OK) {
                    // Get the invitation IDs of all sent messages
                    String[] ids = AppInviteInvitation.getInvitationIds(resultCode, intent);
                    for (String id : ids) {
                        Logger.log(TAG, "onActivityResult: sent invitation " + id);
                    }
                } else {
                    Toast.makeText(this, getString(R.string.app_invite_failed), Toast.LENGTH_SHORT).show();
                }
            }
            super.onActivityResult(requestCode, resultCode, intent);
        }
    }

    /**
     * Show the rate dialog
     *
     * @param context
     */
    private void showPremiumDialog() {
        int color = new DialogUtils().getCurrentThemePrimary(this);

        final MaterialDialog.Builder builder = new MaterialDialog.Builder(this);
        builder.iconRes(R.drawable.no_ads);
        builder.title(getString(R.string.unlock_full_version));
        builder.content(getString(R.string.full_version_buy_ask));
        builder.positiveText(getString(R.string.yes));
        builder.positiveColor(color);
        builder.negativeText(getString(R.string.no));
        builder.negativeColor(color);
        final MaterialDialog materialDialog = builder.build();

        materialDialog.getActionButton(DialogAction.POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                materialDialog.dismiss();
                showPurchaseDialog();
            }
        });

        materialDialog.getActionButton(DialogAction.NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                materialDialog.dismiss();
                optOutPremiumDialog();
            }
        });

        materialDialog.show();
    }

    private void optOutPremiumDialog() {
        SharedPreferences pref = getSharedPreferences(PremiumUtils.PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(PremiumUtils.KEY_OPT_OUT, true).apply();
    }


    public void updateFavourites(ArrayList<FavInfo> favInfoArrayList) {
        int count = 0;
        for (int i = 0; i < favInfoArrayList.size(); i++) {
            SectionItems favItem = new SectionItems(favInfoArrayList.get(i).getFileName(), favInfoArrayList.get(i)
                    .getFilePath(), R.drawable.ic_fav_folder, favInfoArrayList.get(i).getFilePath(), 0);
            if (!favouritesGroupChild.contains(favItem)) {
                favouritesGroupChild.add(favItem);
                count++;
            }
        }
        if (mIsHomeScreenEnabled && mHomeScreenFragment != null) {
            mHomeScreenFragment.updateFavoritesCount(count);
        }
        expandableListAdapter.notifyDataSetChanged();

    }

    public void removeFavourites(ArrayList<FavInfo> favInfoArrayList) {
        int count = 0;
        for (int i = 0; i < favInfoArrayList.size(); i++) {
            SectionItems favItem = new SectionItems(favInfoArrayList.get(i).getFileName(), favInfoArrayList.get(i)
                    .getFilePath(), R.drawable.ic_fav_folder, favInfoArrayList.get(i).getFilePath(), 0);
            if (favouritesGroupChild.contains(favItem)) {
                favouritesGroupChild.remove(favItem);
                count++;
            }
        }
        if (mIsHomeScreenEnabled && mHomeScreenFragment != null) {
            mHomeScreenFragment.removeFavorites(count);
        }
        expandableListAdapter.notifyDataSetChanged();
    }






    private void endActionMode() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.main_container);
        if (fragment instanceof BaseFileList && ((BaseFileList) fragment).isInSelectionMode()) {
            ((BaseFileList) fragment).endActionMode();
        }
/*        Fragment dualFragment = getSupportFragmentManager().findFragmentById(R.id.frame_container_dual);
        if (dualFragment instanceof FileListDualFragment && ((FileListDualFragment) dualFragment).isInSelectionMode()) {
            ((FileListDualFragment) dualFragment).endActionMode();
        }*/
    }





    /**
     * Show dual pane in Landscape mode
     */
    private void showDualPane() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.main_container);

        if (fragment instanceof HomeScreenFragment) {
            mIsDualModeEnabled = true;
            ((HomeScreenFragment) fragment).setDualModeEnabled(true);
        }
        // For Files category only, show dual pane
        else if (mCategory == FileConstants.CATEGORY.FILES.getValue()) {
            mIsDualModeEnabled = true;
            isDualPaneInFocus = true;
            toggleDualPaneVisibility(true);
            ((BaseFileList) fragment).refreshSpan();
            currentScreenSetup(FileUtils.getInternalStorage().getAbsolutePath(), FileConstants
                    .CATEGORY.FILES.getValue(), isDualPaneInFocus);
//            createDualFragment();
        }
    }

    @Override
    public void onClick(final View view) {
        switch (view.getId()) {

            case R.id.fabCreateFileDual:
            case R.id.fabCreateFolderDual:
                fabCreateMenuDual.collapse();
            case R.id.fabCreateFile:
            case R.id.fabCreateFolder:
                if (view.getId() == R.id.fabCreateFolder || view.getId() == R.id.fabCreateFile) {
                    fabCreateMenu.collapse();
                }
                if (view.getId() == R.id.fabCreateFolder || view.getId() == R.id
                        .fabCreateFolderDual) {
                    mOperation = FileConstants.FOLDER_CREATE;
                    String path = isDualPaneInFocus ? mCurrentDirDualPane : mCurrentDir;
                    new FileUtils().createDirDialog(this, mIsRootMode, path);
                } else {
                    mOperation = FileConstants.FILE_CREATE;
                    String path = isDualPaneInFocus ? mCurrentDirDualPane : mCurrentDir;
                    new FileUtils().createFileDialog(this, mIsRootMode, path);
                }
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.main_container);
                ((BaseFileList) fragment).setBackPressed();
/*                if (isDualPaneInFocus) {
                    Fragment dualFragment = getSupportFragmentManager().findFragmentById(R.id.frame_container_dual);
                    ((FileListDualFragment) dualFragment).setBackPressed();
                }*/
                break;

            case R.id.unlockPremium:
                if (inappBillingSupported) {
                    showPremiumDialog();
                } else {
                    Toast.makeText(this, getString(R.string.billing_unsupported), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.rateUs: // Rate us
                Intent intent = new Intent(Intent.ACTION_VIEW);
                // Try Google play
                intent.setData(Uri
                        .parse("market://details?id=" + getPackageName()));
                if (FileUtils.isPackageIntentUnavailable(this, intent)) {
                    // Market (Google play) app seems not installed,
                    // let's try to open a webbrowser
                    intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=" +
                            getPackageName()));
                    if (FileUtils.isPackageIntentUnavailable(this, intent)) {
                        Toast.makeText(this,
                                getString(R.string.msg_error_not_supported),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        startActivity(intent);
                    }
                } else {
                    startActivity(intent);
                }
                drawerLayout.closeDrawer(relativeLayoutDrawerPane);
                break;
            case R.id.layoutSettings: // Settings
                Intent intent1 = new Intent(this, SettingsActivity.class);
                final int enter_anim = android.R.anim.fade_in;
                final int exit_anim = android.R.anim.fade_out;
                overridePendingTransition(enter_anim, exit_anim);
                startActivityForResult(intent1,
                        PREFS_REQUEST);
                expandableListView.setSelection(0);
                drawerLayout.closeDrawer(relativeLayoutDrawerPane);
                break;
            case R.id.imageInvite:
                Intent inviteIntent = new AppInviteInvitation.IntentBuilder(getString(R.string.app_invite_title))
                        .setMessage(getString(R.string.app_invite_msg))
                        .build();
                startActivityForResult(inviteIntent, REQUEST_INVITE);
                break;

        }
    }

    private boolean checkIfSameItemClicked(String path, int category) {
        if (!isDualPaneInFocus)
            return mCurrentDir != null && mCurrentDir.equals(path) && mCategory == category;
        else
            return mCurrentDirDualPane != null && mCurrentDirDualPane.equals(path) && mCategoryDual
                    == category;

    }

    public void onDrawerItemClick(int groupPos, int childPos) {
        String path = totalGroupData.get(groupPos).getmChildItems().get(childPos).getPath();
        displaySelectedGroup(groupPos, childPos, path);
        drawerLayout.closeDrawer(relativeLayoutDrawerPane);
    }

    private void displaySelectedGroup(int groupPos, int childPos, String path) {
        DrawerGroups drawerGroups = DrawerGroups.getGroupFromPos(groupPos);
        switch (drawerGroups) {
            case STORAGE:
            case FAVORITES:
                onStorageItemClicked(groupPos, path);
                break;
            case LIBRARY:
                onLibraryItemClicked(childPos);
                break;
        }
    }

    private void onStorageItemClicked(int groupPos, String path) {
        initializeStartingDirectory();
        checkIfFavIsRootDir(groupPos);
        displayFileList(path, Category.FILES);
    }

    private void onLibraryItemClicked(int childPos) {
        Category category = Category.getCategory(childPos);
        displayFileList(null, category);
    }


    public void addToBackStack(String path, int category) {
        if (!isDualPaneInFocus) {
            mBackStackList.add(new BackStackModel(path, category));
            Logger.log(TAG, "Back stack--size=" + mBackStackList.size() + " Path=" + path + "Category=" + category);

        } else {
            mBackStackListDual.add(new BackStackModel(path, category));
            Logger.log(TAG, "Back stack DUAL--size=" + mBackStackList.size() + " Path=" + path +
                    "Category=" + category);
        }
    }

    private void checkIfFavIsRootDir(int groupPos) {
        if (groupPos == 1) {

            if (!isDualPaneInFocus) {
                if (!mCurrentDir.contains(getInternalStorage().getAbsolutePath()) && !mExternalSDPaths.contains
                        (mCurrentDir)) {
                    isCurrentDirRoot = true;
                    mStartingDir = File.separator;
                }
            } else {
                if (!mCurrentDirDualPane.contains(getInternalStorage().getAbsolutePath()) && !mExternalSDPaths
                        .contains(mCurrentDirDualPane)) {
                    isCurrentDualDirRoot = true;
                    mStartingDirDualPane = File.separator;
                }
            }
        }
    }

    public void initializeStartingDirectory() {
        if (!isDualPaneInFocus) {
            if (mCurrentDir.contains(FileUtils.getInternalStorage().getAbsolutePath())) {
                mStartingDir = FileUtils.getInternalStorage().getAbsolutePath();
                isCurrentDirRoot = false;
            } else if (mExternalSDPaths.size() > 0) {
                for (String path : mExternalSDPaths) {
                    if (mCurrentDir.contains(path)) {
                        mStartingDir = path;
                        isCurrentDirRoot = false;
                        return;
                    }
                }
                mStartingDir = File.separator;
            } else {
                mStartingDir = File.separator;
            }
            Logger.log(TAG, "initializeStartingDirectory--startingdir=" + mStartingDir);

        } else {
            if (mCurrentDirDualPane.contains(FileUtils.getInternalStorage().getAbsolutePath())) {
                mStartingDirDualPane = FileUtils.getInternalStorage().getAbsolutePath();
                isCurrentDualDirRoot = false;
            } else if (mExternalSDPaths.size() > 0) {
                for (String path : mExternalSDPaths) {
                    if (mCurrentDirDualPane.contains(path)) {
                        mStartingDirDualPane = path;
                        isCurrentDualDirRoot = false;
                        return;
                    }
                }
                mStartingDirDualPane = File.separator;
            } else {
                mStartingDirDualPane = File.separator;
            }
            Logger.log(TAG, "initializeStartingDirectory--startingdirDUAL=" + mStartingDirDualPane);
        }
    }

    private void setTitleForCategory(int category) {
        switch (category) {
            case 0:
                mToolbar.setTitle(R.string.app_name);
                break;
            case 1:
                mToolbar.setTitle(MUSIC);
                break;
            case 2:
                mToolbar.setTitle(VIDEO);
                break;
            case 3:
                mToolbar.setTitle(IMAGES);
                break;
            case 4:
                mToolbar.setTitle(DOCS);
                break;
            default:
                mToolbar.setTitle(R.string.app_name);
        }
    }

    private void displayFileList(String directory, Category category) {

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Bundle args = new Bundle();
        args.putString(FileConstants.KEY_PATH, directory);
        args.putSerializable(FileConstants.KEY_CATEGORY, category);

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.main_container);
        if (fragment instanceof HomeScreenFragment) {
            BaseFileList baseFileList = new BaseFileList();
            baseFileList.setArguments(args);
            ft.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right, R.anim.enter_from_right, R.anim
                    .exit_to_left);
            ft.replace(R.id.main_container, baseFileList, directory);
            ft.addToBackStack(null);
            ft.commit();
        } else {
            ((BaseFileList) fragment).reloadList(directory);
        }

    }




    private void currentScreenSetup(String directory, int category, boolean isDualPane) {
        setDir(directory, isDualPane);
        setCurrentCategory(category);
        addToBackStack(directory, category);
    }

    private void showPurchaseDialog() {
        BillingHelper.getInstance().launchPurchaseFlow(this);
    }


    private void hideAds() {
        Log.d(TAG, "hideAds:");

 /*       if (othersGroupChild.get(0) != null && othersGroupChild.get(0).getIcon() == (R.drawable.ic_unlock_full)) {
            othersGroupChild.remove(0);
            expandableListAdapter.notifyDataSetChanged();
            Log.d(TAG, "Hide ads SUCCESS");
        }*/

        unlockPremium.setVisibility(View.GONE);
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.main_container);
        if (fragment instanceof BaseFileList) {
            ((BaseFileList) fragment).setPremium();
        } else if (fragment instanceof HomeScreenFragment) {
            ((HomeScreenFragment) fragment).setPremium();
        }
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        if (menuInfo instanceof ExpandableListView.ExpandableListContextMenuInfo) {
            ExpandableListView.ExpandableListContextMenuInfo info =
                    (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;

            int type =
                    ExpandableListView.getPackedPositionType(info.packedPosition);

            int group =
                    ExpandableListView.getPackedPositionGroup(info.packedPosition);

            // Only for Favorites
            if (group == DrawerGroups.FAVORITES.getValue()) {
                // Only create a context menu for child items
                if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
                    menu.add(0, MENU_FAVOURITES, 0, getString(R.string.delete_fav));
                }
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem menuItem) {
        ExpandableListView.ExpandableListContextMenuInfo info =
                (ExpandableListView.ExpandableListContextMenuInfo) menuItem.getMenuInfo();

        int groupPos = 0, childPos = 0;

        int type = ExpandableListView.getPackedPositionType(info.packedPosition);
        if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
            groupPos = ExpandableListView.getPackedPositionGroup(info.packedPosition);
            childPos = ExpandableListView.getPackedPositionChild(info.packedPosition);
        }

        switch (menuItem.getItemId()) {
            case MENU_FAVOURITES:
                String path = totalGroupData.get(groupPos).getmChildItems().get(childPos).getPath();
                String name = totalGroupData.get(groupPos).getmChildItems().get(childPos)
                        .getFirstLine();
                FavInfo favInfo = new FavInfo();
                favInfo.setFileName(name);
                favInfo.setFilePath(path);
                favouritesGroupChild.remove(childPos);
                sharedPreferenceWrapper.removeFavorite(this, favInfo);
                if (mIsHomeScreenEnabled && mHomeScreenFragment != null) {
                    mHomeScreenFragment.removeFavorites(1);
                }
                expandableListAdapter.notifyDataSetChanged();
                return true;


            default:
                return super.onContextItemSelected(menuItem);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Logger.log(TAG, "onConfigurationChanged" + newConfig.orientation);
        if (mCurrentOrientation != newConfig.orientation) {
            mCurrentOrientation = newConfig.orientation;
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.main_container);

            if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE && mIsDualPaneEnabled) {
                showDualPane();
            } else {
                mIsDualModeEnabled = false;
                isDualPaneInFocus = false;
                if (fragment instanceof HomeScreenFragment) {
                    ((HomeScreenFragment) fragment).setDualModeEnabled(false);
                } else {
                    toggleDualPaneVisibility(false);
                }
            }
        }
        super.onConfigurationChanged(newConfig);
    }

    /**
     * Dual pane mode to be shown only for File Category
     *
     * @param isFilesCategory True if files category
     */
    public void toggleDualPaneVisibility(boolean isFilesCategory) {
        if (isFilesCategory) {
            if (mIsDualModeEnabled) {
                FrameLayout frameLayout = (FrameLayout) findViewById(R.id
                        .frame_container_dual);
                frameLayout.setVisibility(View.VISIBLE);
                frameLayoutFabDual.setVisibility(View.VISIBLE);
                mViewSeperator.setVisibility(View.VISIBLE);
                scrollNavigationDualPane.setVisibility(View.VISIBLE);
            }
        } else {
            FrameLayout frameLayout = (FrameLayout) findViewById(R.id.frame_container_dual);
            frameLayout.setVisibility(View.GONE);
            frameLayoutFabDual.setVisibility(View.GONE);
            mViewSeperator.setVisibility(View.GONE);
            scrollNavigationDualPane.setVisibility(View.GONE);
            isDualPaneInFocus = false;
            mBackStackListDual.clear();
        }

    }

/*    public void createDualFragment() {
        FragmentTransaction ft = getSupportFragmentManager()
                .beginTransaction();
        String internalStoragePath = getInternalStorage().getAbsolutePath();
        Bundle args = new Bundle();
        args.putString(FileConstants.KEY_PATH, internalStoragePath);

        args.putString(FileConstants.KEY_PATH_OTHER, mCurrentDir);
        args.putBoolean(FileConstants.KEY_FOCUS_DUAL, true);

        args.putBoolean(FileConstants.KEY_DUAL_MODE, true);
        FileListDualFragment dualFragment = new FileListDualFragment();
        dualFragment.setArguments(args);
        ft.replace(R.id.frame_container_dual, dualFragment);
//        ft.commit();
        ft.commitAllowingStateLoss();
    }*/


    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        int backStackEntryCount = fragmentManager.getBackStackEntryCount();

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.main_container);

        Logger.log(TAG, "Onbackpress--fragment=" + fragment + " " +
                "mHomePageRemoved=" + mIsHomePageRemoved + "home added=" + mIsHomePageAdded + " " +
                "backstack=" + backStackEntryCount);
//        Logger.log(TAG, "Onbackpress--fab exp=" + fabCreateMenu.isExpanded()+"fabDUAL exp="+fabCreateMenuDual
// .isExpanded());

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (fabCreateMenu.isExpanded()) {
            fabCreateMenu.collapse();
        } else if (fabCreateMenuDual.isExpanded()) {
            fabCreateMenuDual.collapse();
        } else if (mIsHomePageRemoved) {
//            super.onBackPressed();
            if (backStackEntryCount != 0) {
                finish();
                /*getSupportFragmentManager().popBackStack();
                super.onBackPressed();
 */
            }
        } else if (mIsHomePageAdded) {
            initialScreenSetup(true);
            setTitleForCategory(100); // Setting title to App name
            mCurrentDir = null;
            mCurrentDirDualPane = null;
            mStartingDir = null;
            mStartingDirDualPane = null;
            isDualPaneInFocus = false;
            mFrameDualPane.setVisibility(View.GONE);
            mViewSeperator.setVisibility(View.GONE);
//            cleanUpFileScreen();
            mIsDualModeEnabled = false;
            mIsHomePageAdded = false;
        } else if (fragment instanceof BaseFileList) {
            if (((BaseFileList) fragment).isSearchVisible()) {
                ((BaseFileList) fragment).hideSearchView();
                ((BaseFileList) fragment).removeSearchTask();
            }
            backOperation(fragment);
        } else {
            // Remove HomeScreen Frag & Exit App
            Logger.log(TAG, "Onbackpress--ELSE=");
            mCurrentDir = null;
            mStartingDir = null;
            mStartingDirDualPane = null;
            mCurrentDirDualPane = null;
            finish();
//            super.onBackPressed();
        }
    }


    private void backOperation(Fragment fragment) {


        if (((BaseFileList) fragment).isZipMode()) {

            if (((BaseFileList) fragment).checkZipMode()) {
                int newSize = mBackStackList.size() - 1;

                mBackStackList.remove(newSize);
                mCurrentDir = mBackStackList.get(newSize - 1).getFilePath();
                mCategory = mBackStackList.get(newSize - 1).getCategory();
                ((BaseFileList) fragment).reloadList(true, mCurrentDir);
                if (!mIsFromHomePage) {
                    setNavDirectory(mCurrentDir, false);
                } else {
                    hideFab();
                }
            }
        } else if (mStartingDir == null) {
            removeFragmentFromBackStack();
        } else if (checkIfBackStackExists()) {
            if (!isDualPaneInFocus) {
                ((BaseFileList) fragment).setCategory(mCategory);
                ((BaseFileList) fragment).reloadList(true, mCurrentDir);
            } else {
                if (mCategoryDual == FileConstants.CATEGORY.GENERIC_LIST.getValue()) {
                    super.onBackPressed();
                }
            }
            setTitleForCategory(mCategory);
            if (mCategory == FileConstants.CATEGORY.FILES.getValue()) {
                showFab();
                if (!isDualPaneInFocus)
                    setNavDirectory(mCurrentDir, isDualPaneInFocus);
                else
                    setNavDirectory(mCurrentDirDualPane, isDualPaneInFocus);

            }

        } else {
            removeFragmentFromBackStack();
            if (!mIsHomeScreenEnabled) {
                finish();
            }
        }


    }


    private boolean checkIfBackStackExists() {
        int backStackSize;
        if (!isDualPaneInFocus) {
            backStackSize = mBackStackList.size();
            Logger.log(TAG, "checkIfBackStackExists --size=" + backStackSize);
        } else {
            backStackSize = mBackStackListDual.size();
            Logger.log(TAG, "checkIfBackStackExists --DUAL size=" + backStackSize);
        }

        if (backStackSize == 1) {
            if (!isDualPaneInFocus) {
                mCurrentDir = mBackStackList.get(0).getFilePath();
                mCategory = mBackStackList.get(0).getCategory();
                Logger.log(TAG, "checkIfBackStackExists--Path=" + mCurrentDir + "  Category=" + mCategory);
                mIsFromHomePage = false;
                mBackStackList.clear();
            } else {
                mCurrentDirDualPane = mBackStackListDual.get(0).getFilePath();
                mCategoryDual = mBackStackListDual.get(0).getCategory();
                Logger.log(TAG, "checkIfBackStackExists--DUAL Path=" + mCurrentDirDualPane + "  " +
                        "Category=" + mCategoryDual);
                mBackStackListDual.clear();
            }
            return false;
        } else if (backStackSize > 1) {
            int newSize = backStackSize - 1;
            if (!isDualPaneInFocus) {
                mBackStackList.remove(newSize);
                mCurrentDir = mBackStackList.get(newSize - 1).getFilePath();
                mCategory = mBackStackList.get(newSize - 1).getCategory();
                if (FileUtils.checkIfFileCategory(mCategory) && !mIsFromHomePage) {
                    initializeStartingDirectory();
                } else {
                    hideFab();
                }

                Logger.log(TAG, "checkIfBackStackExists--Path=" + mCurrentDir + "  Category=" + mCategory);
                Logger.log(TAG, "checkIfBackStackExists --New size=" + mBackStackList.size());
            } else {
                mBackStackListDual.remove(newSize);
                mCurrentDirDualPane = mBackStackListDual.get(newSize - 1).getFilePath();
                mCategoryDual = mBackStackListDual.get(newSize - 1).getCategory();
                if (FileUtils.checkIfFileCategory(mCategoryDual)) {
                    initializeStartingDirectory();
                } else {
                    hideFab();
                }
                Logger.log(TAG, "checkIfBackStackExists--DUAL Path=" + mCurrentDirDualPane + "  " +
                        "Category=" + mCategoryDual);
                Logger.log(TAG, "checkIfBackStackExists --DUAL New size=" + mBackStackListDual.size());
            }
            return true;
        }
//        Logger.log(TAG, "checkIfBackStackExists --Path=" + mCurrentDir + "  Category=" + mCategory);
        return false;
    }

    public void setCurrentCategory(int category) {
        if (!isDualPaneInFocus) mCategory = category;
        else mCategoryDual = category;
    }

    public void setIsFromHomePage() {
        mIsFromHomePage = true;
    }

    /**
     * Called from {@link #onBackPressed()} . Does the following:
     * 1. If homescreen enabled, returns to home screen
     * 2. If homescreen disabled, exits the app
     */
    private void removeFragmentFromBackStack() {

        int backStackCount = getSupportFragmentManager().getBackStackEntryCount();
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.main_container);
        Fragment dualFragment = getSupportFragmentManager().findFragmentById(R.id.frame_container_dual);


        Logger.log(TAG, "RemoveFragmentFromBackStack --backstack==" + backStackCount +
                "home_enabled=" + mIsHomeScreenEnabled + " frag=" + fragment);

        mBackStackList.clear();
        mBackStackListDual.clear();
        cleanUpFileScreen();
        Logger.log(TAG, "RemoveFragmentFromBackStack--frag=" + fragment);
        FragmentTransaction ft = getSupportFragmentManager()
                .beginTransaction();
        ft.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right, R.anim.enter_from_right, R.anim
                .exit_to_left);

        ft.remove(fragment);

        if (dualFragment != null) {
            ft.remove(dualFragment);
        }


        if (mHomeScreenFragment != null) {
            ft.show(mHomeScreenFragment);
            toggleNavigationVisibility(false);
        }
        ft.commitAllowingStateLoss();

    }

    private void cleanUpFileScreen() {
        if (mIsHomeScreenEnabled) {
            setTitleForCategory(100); // Setting title to App name
            hideFab();
            mCurrentDir = null;
            mCurrentDirDualPane = null;
            mStartingDir = null;
            mStartingDirDualPane = null;
            toggleDualPaneVisibility(false);
        }

    }

    @Override
    protected void onDestroy() {
        Logger.log(TAG, "onDestroy");

        unregisterForContextMenu(expandableListView);
        unregisterReceiver(mLocaleListener);
        mSharedPreferences.edit().putInt(FileConstants.CURRENT_THEME, mCurrentTheme).apply();
        if (mIsRootMode) {
            // close interactive shell and handler thread associated with it
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                // let it finish up first with what it's doing
                handlerThread.quitSafely();
            } else handlerThread.quit();
            shellInteractive.close();
     /*       try {
                RootTools.closeAllShells();
            } catch (IOException e) {
                e.printStackTrace();
            }*/
        }
        BillingHelper.getInstance().disposeBilling();
        super.onDestroy();
    }


    public void toggleFab(boolean isActionMode) {
        if (isActionMode) {
            fabCreateMenu.setVisibility(View.GONE);
        } else {
            fabCreateMenu.setVisibility(View.VISIBLE);
        }
    }

    public void setCurrentDir(String dir, boolean isDualPaneInFocus) {
        if (isDualPaneInFocus) {
            mCurrentDirDualPane = dir;
        } else {
            mCurrentDir = dir;
        }
        this.isDualPaneInFocus = isDualPaneInFocus;
    }

    public void setDir(String dir, boolean isDualPaneInFocus) {
        Logger.log(TAG, "setDir=Dir=" + dir + "dualPane=" + isDualPaneInFocus);
        if (isDualPaneInFocus) {
            mCurrentDirDualPane = dir;
            mStartingDirDualPane = dir;
        } else {
            mCurrentDir = dir;
            mStartingDir = dir;
        }
        this.isDualPaneInFocus = isDualPaneInFocus;
    }


    private boolean mIsHomeSettingToggled;
    private boolean mIsHomePageRemoved;

    /**
     * Using this method since user can go to Settings page and can ENABLE HOMESCREEN  or
     * ENABLE DUAL PANE.We can't add fragments without loss of state that time. Hence,we keep
     * a flag on value change and while coming back from Settings we add Fragments accordingly
     */
    @Override
    protected void onPostResume() {
        super.onPostResume();
        Logger.log(TAG, "onPostResume" + mIsHomeSettingToggled);
        if (mIsHomeSettingToggled) {

            currentScreenSetup(FileUtils.getInternalStorage().getAbsolutePath(), FileConstants
                    .CATEGORY.FILES.getValue(), false);
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            Bundle args = new Bundle();
            args.putBoolean(FileConstants.KEY_HOME, false);
            args.putString(FileConstants.KEY_PATH, FileUtils.getInternalStorage().getAbsolutePath());
            BaseFileList baseFileList = new BaseFileList();
            baseFileList.setArguments(args);
            ft.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right, R.anim.enter_from_right, R.anim
                    .exit_to_left);
            ft.replace(R.id.main_container, baseFileList);
            ft.commit();
            mIsHomeSettingToggled = false;
            mIsHomePageRemoved = true;
        } else if (mShowDualPane) {
            showDualPane();
            mShowDualPane = false;
        }
    }

    private void restartApp(boolean isLocaleChanged) {

        final int enter_anim = android.R.anim.fade_in;
        final int exit_anim = android.R.anim.fade_out;
        overridePendingTransition(enter_anim, exit_anim);
        finish();
        overridePendingTransition(enter_anim, exit_anim);
        if (!isLocaleChanged)
            startActivity(getIntent());

    }


    /**
     * Called on return from Settings screen
     */
    private void onSharedPrefsChanged() {

        String value = mSharedPreferences.getString(FileConstants.PREFS_THEME, "");
        if (!value.isEmpty()) {
            int theme = Integer.valueOf(value);
            if (theme != mCurrentTheme) {
                mCurrentTheme = theme;
                restartApp(false);
            }
        }

        mIsRootMode = mSharedPreferences.getBoolean(FileConstants.PREFS_ROOTED, false);

        String language = mSharedPreferences.getString(LocaleHelper.SELECTED_LANGUAGE, Locale.getDefault()
                .getLanguage());
        if (!language.equals(mCurrentLanguage)) {
            mCurrentLanguage = language;
            LocaleHelper.setLocale(this, language);
            restartApp(false);
        }

        boolean showHidden = mSharedPreferences.getBoolean(FileConstants.PREFS_HIDDEN, false);

        if (showHidden != mShowHidden) {
            mShowHidden = showHidden;
            Logger.log(TAG, "OnPrefschanged PREFS_HIDDEN" + mShowHidden);
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.main_container);

           /* BaseFileList dualPaneFragment = (FileListDualFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.frame_container_dual);*/
            if (fragment instanceof BaseFileList) {
                ((BaseFileList) fragment).refreshList();
            }
     /*       if (dualPaneFragment != null) {
                dualPaneFragment.refreshList();
            }*/
        }

        boolean isHomeScreenEnabled = mSharedPreferences.getBoolean(FileConstants
                .PREFS_HOMESCREEN, true);

        if (isHomeScreenEnabled != mIsHomeScreenEnabled) {

            mIsHomeScreenEnabled = isHomeScreenEnabled;
            Logger.log(TAG, "OnPrefschanged PREFS_HOMESCREEN" + mIsHomeScreenEnabled);
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.main_container);
            // If homescreen disabled
            if (!isHomeScreenEnabled) {

                // If user on Home page, replace it with BaseFileList
                if (fragment instanceof HomeScreenFragment) {
                    mIsHomeSettingToggled = true;
                } else {
                    Logger.log(TAG, "Nav directory count=" + navDirectory.getChildCount());

                    for (int i = 0; i < Math.min(navDirectory.getChildCount(), 2); i++) {
                        navDirectory.removeViewAt(0);
                    }

                    if (navDirectoryDualPane.getChildCount() > 0) {
                        for (int i = 0; i < Math.min(navDirectoryDualPane.getChildCount(), 2); i++) {
                            navDirectoryDualPane.removeViewAt(0);
                        }
                    }
                    // Set a flag so that it can be removed on backPress
                    mIsHomePageRemoved = true;
                    mIsHomePageAdded = false;
                }

            } else {
                // Clearing the flags necessary as user can click checkbox multiple times
                mIsHomeSettingToggled = false;
                mIsHomePageAdded = true;
                mIsHomePageRemoved = false;
            }
        }
/*
        boolean isDualPaneEnabledSettings = mSharedPreferences.getBoolean(FileConstants
                .PREFS_DUAL_PANE, mIsTablet);
        if (isDualPaneEnabledSettings != mIsDualPaneEnabled) {
            mIsDualPaneEnabled = isDualPaneEnabledSettings;

            Logger.log(TAG, "OnPrefschanged PREFS_DUAL_PANE" + mIsDualPaneEnabled);

            if (!mIsDualPaneEnabled) {
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id
                        .main_container);
                toggleDualPaneVisibility(false);
                if (fragment instanceof HomeScreenFragment) {
                    ((HomeScreenFragment) fragment).setDualModeEnabled(false);
                } else {
                    ((BaseFileList) fragment).refreshSpan(); // For changing the no of columns in non-dual mode
                }

                mIsDualModeEnabled = false;
                mShowDualPane = false;
            } else {
                checkScreenOrientation();
                if (mCategory == FileConstants.Category.FILES.getValue() && mIsDualModeEnabled) {
                    mShowDualPane = true;
                }
            }
        }*/

    }

    private void resetFavouritesGroup() {

        for (int i = favouritesGroupChild.size() - 1; i >= 0; i--) {
            if (!favouritesGroupChild.get(i).getSecondLine().equalsIgnoreCase(FileUtils
                    .getDownloadsDirectory().getAbsolutePath())) {
                favouritesGroupChild.remove(i);
            }
        }
        sharedPreferenceWrapper.resetFavourites(this);
        expandableListAdapter.notifyDataSetChanged();

    }


    @Override
    public void onPermissionGranted(String[] permissionName) {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.main_container);
        if (fragment instanceof HomeScreenFragment) {
            ((HomeScreenFragment) fragment).onPermissionGranted();
        } else {
            ((BaseFileList) fragment).onPermissionGranted();
        }

    }

    @Override
    public void onPermissionDeclined(String[] permissionName) {

    }

    @Override
    public void onBillingResult(BillingStatus billingStatus) {
        switch (billingStatus) {
            case UNSUPPORTED:
                PremiumUtils.onStart(this);
                if (PremiumUtils.shouldShowPremiumDialog()) {
                    showPremiumDialog();
                }
            case FREE:
                setupAds();
                break;

        }
    }

    private void setupAds() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isFinishing()) {
                    MobileAds.initialize(getApplicationContext(), "ca-app-pub-3940256099942544~3347511713");
                    Intent intent = new Intent(ADS);
                    intent.putExtra(KEY_PREMIUM, false);
                    LocalBroadcastManager.getInstance(AceActivity.this).sendBroadcast(intent);
                }
            }
        }, 2000);

    }
}

