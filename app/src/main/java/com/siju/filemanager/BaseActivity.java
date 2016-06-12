package com.siju.filemanager;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;

import com.siju.filemanager.group.StorageGroup;
import com.siju.filemanager.model.SectionGroup;
import com.siju.filemanager.model.SectionItems;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class BaseActivity extends AppCompatActivity {

    ExpandableListAdapter expandableListAdapter;
    ExpandableListView expandableListView;
    String[] listDataHeader;
    List<String> mListHeader;
    HashMap<String, List<String>> listDataChild;
    //    private ArrayList<SectionGroup> sectionGroupArrayList;
    ArrayList<SectionGroup> storageGroup;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        // get the listview
        expandableListView = (ExpandableListView) findViewById(R.id.expand_list_drawer);
//
//        DisplayMetrics metrics;
//        int width;
//        metrics = new DisplayMetrics();
//        getWindowManager().getDefaultDisplay().getMetrics(metrics);
//        width = metrics.widthPixels;
//        expListView.setIndicatorBoundsRelative(width - GetDipsFromPixel(50), width - GetDipsFromPixel(10));

        // preparing list data
        prepareListData();

        expandableListAdapter = new ExpandableListAdapter(this, storageGroup);


        // setting list adapter
        expandableListView.setAdapter(expandableListAdapter);
        for (int i = 0; i < expandableListAdapter.getGroupCount(); i++)
            expandableListView.expandGroup(i);


    }

    public int GetDipsFromPixel(float pixels) {
        // Get the screen's density scale
        final float scale = getResources().getDisplayMetrics().density;
        // Convert the dps to pixels, based on density scale
        return (int) (pixels * scale + 0.5f);
    }

    /*
* Preparing the list data
*/
    private void prepareListData() {

        listDataChild = new HashMap<>();
        listDataHeader = getResources().getStringArray(R.array.expand_headers);

        mListHeader = Arrays.asList(listDataHeader);

        storageGroup = new ArrayList<>();
        StorageGroup storageGroup1 = new StorageGroup(this);
        ArrayList<SectionItems> storageChild = new ArrayList<>();
        File root = storageGroup1.getRootDirectory();
        File internalSD = storageGroup1.getInternalStorage();
        File extSD = storageGroup1.getExternalStorage();
        String freePlaceholder = " " + getResources().getString(R.string.msg_free) + " ";
        String rootSpace = storageGroup1.getSpaceLeft(root) +  freePlaceholder +  storageGroup1.getTotalSpace(root);
        String internalSDSpace = storageGroup1.getSpaceLeft(internalSD) + freePlaceholder + storageGroup1.getTotalSpace(internalSD);

        storageChild.add(new SectionItems(StorageGroup.STORAGE_ROOT, rootSpace, R.drawable.ic_root_black));
        storageChild.add(new SectionItems(StorageGroup.STORAGE_INTERNAL, internalSDSpace, R.drawable.ic_storage_black));
        if (extSD != null) {
            String externalSDSpace = storageGroup1.getSpaceLeft(extSD) + freePlaceholder + storageGroup1.getTotalSpace(extSD);
            storageChild.add(new SectionItems(StorageGroup.STORAGE_EXTERNAL, externalSDSpace, R.drawable.ic_ext_sd_black));
        }


        storageGroup.add(new SectionGroup(mListHeader.get(0), storageChild));

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.base, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
