package com.siju.acexplorer.filesystem;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.siju.acexplorer.R;
import com.siju.acexplorer.common.SharedPreferenceWrapper;
import com.siju.acexplorer.filesystem.helper.SimpleItemTouchHelperCallback;
import com.siju.acexplorer.filesystem.model.LibrarySortModel;

import java.util.ArrayList;


/**
 * Created by Siju on 23-07-2016.
 */
public class LibrarySortActivity extends AppCompatActivity implements OnStartDragListener {
    private RecyclerView mRecyclerViewLibrarySort;
    private ItemTouchHelper mItemTouchHelper;
    private LibrarySortAdapter mSortAdapter;
    private SharedPreferenceWrapper sharedPreferenceWrapper;
    private ArrayList<LibrarySortModel> savedLibraries = new ArrayList<>();
    private ArrayList<LibrarySortModel> totalLibraries = new ArrayList<>();
    private int mResourceIds[];
    private String mLabels[];
    private int mCategoryIds[];
    private Toolbar mToolbar;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.library_sort);

        sharedPreferenceWrapper = new SharedPreferenceWrapper();
        initConstants();
        initializeLibraries();
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mToolbar.setTitle(getString(R.string.nav_header_collections));
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mRecyclerViewLibrarySort = (RecyclerView) findViewById(R.id.recyclerViewLibrarySort);
        mSortAdapter = new LibrarySortAdapter(this, this, totalLibraries);

        mRecyclerViewLibrarySort.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setAutoMeasureEnabled(false);
        mRecyclerViewLibrarySort.setLayoutManager(llm);
        mRecyclerViewLibrarySort.setAdapter(mSortAdapter);

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mSortAdapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(mRecyclerViewLibrarySort);
 /*       mSortAdapter.setOnItemClickListener(new LibrarySortAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

            }
        });*/

    }

    private void initConstants() {
        mResourceIds = new int[]{R.drawable.ic_library_images, R.drawable.ic_library_music,
                R.drawable.ic_library_videos, R.drawable.ic_library_docs,
                R.drawable.ic_library_downloads,
                R.drawable.ic_library_compressed, R.drawable.ic_library_favorite,
                R.drawable.ic_library_pdf, R.drawable.ic_library_app, R.drawable.ic_library_large};
        // No Add Label to be shown
        mLabels = new String[]{getString(R.string
                .nav_menu_image), getString(R.string
                .nav_menu_music), getString(R.string
                .nav_menu_video), getString(R.string
                .home_docs), getString(R.string
                .downloads), getString(R.string
                .compressed), getString(R.string
                .nav_header_favourites), getString(R.string
                .pdf), getString(R.string
                .apk), getString(R.string
                .library_large)};
        mCategoryIds = new int[]{FileConstants.CATEGORY.IMAGE.getValue(),
                FileConstants.CATEGORY.AUDIO.getValue(),
                FileConstants.CATEGORY.VIDEO.getValue(),
                FileConstants.CATEGORY.DOCS.getValue(),
                FileConstants.CATEGORY.DOWNLOADS.getValue(),
                FileConstants.CATEGORY.COMPRESSED.getValue(),
                FileConstants.CATEGORY.FAVORITES.getValue(),
                FileConstants.CATEGORY.PDF.getValue(),
                FileConstants.CATEGORY.APPS.getValue(),
                FileConstants.CATEGORY.LARGE_FILES.getValue()};
    }


    private void initializeLibraries() {
        savedLibraries = sharedPreferenceWrapper.getLibraries(this);
        if (savedLibraries != null) {
            for (int j = 0; j < savedLibraries.size(); j++) {
                totalLibraries.add(new LibrarySortModel(savedLibraries.get(j).getCategoryId(),
                        savedLibraries.get(j).getLibraryName(), true));
            }
        }

        for (int i = 0; i < mResourceIds.length; i++) {
            LibrarySortModel model = new LibrarySortModel(mCategoryIds[i], mLabels[i],
                    true);
            if (!totalLibraries.contains(model)) {
                model.setChecked(false);
                totalLibraries.add(model);
            }
        }
    }


    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.library_sort, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_ok:
                savedLibraries = new ArrayList<>();
                for (int i = 0; i < totalLibraries.size(); i++) {
                    if (totalLibraries.get(i).isChecked()) {
//                        sharedPreferenceWrapper.addLibrary(this, totalLibraries.get(i));
                        savedLibraries.add(totalLibraries.get(i));
                    }
                }
                Intent dataIntent = new Intent();
                dataIntent.putParcelableArrayListExtra(FileConstants.KEY_LIB_SORTLIST,
                        savedLibraries);
                setResult(RESULT_OK, dataIntent);
                finish();
                break;
            case R.id.action_cancel:
                setResult(RESULT_CANCELED);
                finish();
                break;

            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }
}