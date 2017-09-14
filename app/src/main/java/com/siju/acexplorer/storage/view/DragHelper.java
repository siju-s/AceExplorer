/*
 * Copyright (C) 2017 Ace Explorer owned by Siju Sakaria
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.siju.acexplorer.storage.view;

import android.content.ClipDescription;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.view.ViewParent;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.siju.acexplorer.R;
import com.siju.acexplorer.logging.Logger;
import com.siju.acexplorer.model.FileInfo;
import com.siju.acexplorer.storage.model.task.PasteConflictChecker;
import com.siju.acexplorer.utils.Dialogs;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Siju on 04 September,2017
 */
public class DragHelper {
    private final String TAG = this.getClass().getSimpleName();
    private Context context;
    private DragEventListener dragEventListener;

    DragHelper(Context context) {
        this.context = context;
        dragEventListener = new DragEventListener();
    }

    private BitmapDrawable writeOnDrawable(String text) {

        Bitmap bm = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888);
        bm.eraseColor(Color.DKGRAY);

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        paint.setColor(Color.WHITE);
        int countFont = context.getResources()
                .getDimensionPixelSize(R.dimen.drag_shadow_font);
        paint.setTextSize(countFont);

        Canvas canvas = new Canvas(bm);
        int strLength = (int) paint.measureText(text);
        int x = bm.getWidth() / 2 - strLength;

        // int y = s.titleOffset;
        int y = (bm.getHeight() - countFont) / 2;
//        drawText(canvas, x, y, title, labelWidth - s.leftMargin - x
//                - s.titleRightMargin, mTitlePaint);

        canvas.drawText(text, x, y - paint.getFontMetricsInt().ascent, paint);
//        canvas.drawText(text, bm.getWidth() / 2, bm.getHeight() / 2, paint);

        return new BitmapDrawable(context.getResources(), bm);
    }



    MyDragShadowBuilder getDragShadowBuilder(View view, int count) {
        return new MyDragShadowBuilder(view, count);
    }

    public View.OnDragListener getDragEventListener() {
        return dragEventListener;
    }


    class MyDragShadowBuilder extends View.DragShadowBuilder {

        // The drag shadow image, defined as a drawable thing
        private final Drawable shadow;

        // Defines the constructor for myDragShadowBuilder
        MyDragShadowBuilder(View v, int count) {

            // Stores the View parameter passed to myDragShadowBuilder.
            super(v);
            // Creates a draggable image that will fill the Canvas provided by the system.
            shadow = writeOnDrawable("" + count);

        }

        // Defines a callback that sends the drag shadow dimensions and touch point back to the
        // system.
        @Override
        public void onProvideShadowMetrics(Point size, Point touch) {
            // Defines local variables
            int width, height;

            // Sets the width of the shadow to half the width of the original View
            width = getView().getWidth() / 6;
//            width = 100;
            Log.d(TAG, "width=" + width);

            // Sets the height of the shadow to half the height of the original View
            height = getView().getHeight() / 2;
//            height = 100;

            Log.d(TAG, "height=" + height);


            // The drag shadow is a ColorDrawable. This sets its dimensions to be the same as the
            // Canvas that the system will provide. As a result, the drag shadow will fill the
            // Canvas.
            shadow.setBounds(0, 0, width, height);

            // Sets the size parameter's width and height values. These get back to the system
            // through the size parameter.
            size.set(width, height);
            // Sets the touch point's position to be in the middle of the drag shadow
            touch.set(2 * width, height * 2);
        }

        // Defines a callback that draws the drag shadow in a Canvas that the system constructs
        // from the dimensions passed in onProvideShadowMetrics().
        @Override
        public void onDrawShadow(Canvas canvas) {

            // Draws the ColorDrawable in the Canvas passed in from the system.
            shadow.draw(canvas);
        }
    }

    private void showDragDialog(final ArrayList<FileInfo> sourcePaths, final String
            destinationDir) {

        int color = new Dialogs().getCurrentThemePrimary(getActivity());
        boolean canWrite = new File(destinationDir).canWrite();
        Logger.log(TAG, "Can write=" + canWrite);

        final MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity());
        CharSequence items[] = new String[]{getString(R.string.action_copy), getString(R.string
                .move)};
        builder.title(getString(R.string.drag));
        builder.content(getString(R.string.dialog_to_placeholder) + " " + destinationDir);
        builder.positiveText(getString(R.string.msg_ok));
        builder.positiveColor(color);
        builder.items(items);
        builder.negativeText(getString(R.string.dialog_cancel));
        builder.negativeColor(color);
        builder.itemsCallbackSingleChoice(0, new MaterialDialog.ListCallbackSingleChoice() {
            @Override
            public boolean onSelection(MaterialDialog dialog, View itemView, int position,
                                       CharSequence text) {

                final boolean isMoveOperation = position == 1;
                ArrayList<FileInfo> info = new ArrayList<>();
                info.addAll(sourcePaths);
                PasteConflictChecker conflictChecker = new PasteConflictChecker(BaseFileList
                        .this, destinationDir,
                        mIsRootMode, isMoveOperation, info);
                conflictChecker.execute();
                clearSelectedPos();
                if (actionMode != null) {
                    actionMode.finish();
                }
                return true;
            }
        });

        final MaterialDialog materialDialog = builder.build();

        materialDialog.getActionButton(DialogAction.NEGATIVE).setOnClickListener(new View
                .OnClickListener() {
            @Override
            public void onClick(View view) {
                if (actionMode != null) {
                    actionMode.finish();
                }
                materialDialog.dismiss();
            }
        });

        materialDialog.show();
    }


    private class DragEventListener implements View.OnDragListener {

        int oldPos = -1;

        // This is the method that the system calls when it dispatches a drag event to the
        // listener.
        public boolean onDrag(View v, DragEvent event) {

            // Defines a variable to store the action type for the incoming event
            final int action = event.getAction();

            // Handles each of the expected events
            switch (action) {

                case DragEvent.ACTION_DRAG_STARTED:

                    Log.d(TAG, "DRag started" + v);

                    // Determines if this View can accept the dragged data
                    if (event.getClipDescription().hasMimeType(ClipDescription
                            .MIMETYPE_TEXT_INTENT)) {

                        // returns true to indicate that the View can accept the dragged data.
                        return true;

                    }

                    // Returns false. During the current drag and drop operation, this View will
                    // not receive events again until ACTION_DRAG_ENDED is sent.
                    return false;

                case DragEvent.ACTION_DRAG_ENTERED:
                    Log.d(TAG, "DRag entered");
                    return true;

                case DragEvent.ACTION_DRAG_LOCATION:

                    View onTopOf = recyclerViewFileList.findChildViewUnder(event.getX(), event
                            .getY());
                    int newPos = recyclerViewFileList.getChildAdapterPosition(onTopOf);
//                    Log.d(TAG, "DRag location --pos=" + newPos);

                    if (oldPos != newPos && newPos != RecyclerView.NO_POSITION) {
/*                        int visiblePos = ((LinearLayoutManager) layoutManager)
.findLastVisibleItemPosition();
                        if (newPos + 2 >= visiblePos) {
                            ((LinearLayoutManager) layoutManager).scrollToPosition(newPos + 1);
                        }
//                        recyclerViewFileList.smoothScrollToPosition(newPos+2);
                        Logger.log(TAG, "drag old pos=" + oldPos + "new pos=" + newPos+"Last " +
                                "visible="+visiblePos);*/
                        // For scroll up
                        if (oldPos != RecyclerView.NO_POSITION && newPos < oldPos) {
                            int changedPos = newPos - 2;
                            Logger.log(TAG, "drag Location old pos=" + oldPos + "new pos=" +
                                    newPos +
                                    "changed pos=" + changedPos);
                            if (changedPos >= 0) {
                                recyclerViewFileList.smoothScrollToPosition(changedPos);
                            }
                        }
                        else {
                            int changedPos = newPos + 2;
                            // For scroll down
                            if (changedPos < fileInfoList.size()) {
                                recyclerViewFileList.smoothScrollToPosition(newPos + 2);
                            }
                            Logger.log(TAG, "drag Location old pos=" + oldPos + "new pos=" +
                                    newPos +
                                    "changed pos=" + changedPos);

                        }
                        oldPos = newPos;
                        fileListAdapter.setDraggedPos(newPos);
                    }
                    // Ignore the event
                    return true;

                case DragEvent.ACTION_DRAG_EXITED:
                    Log.d(TAG, "DRag exit");
                    fileListAdapter.clearDragPos();
                    draggedData = new ArrayList<>();
                    return true;

                case DragEvent.ACTION_DROP:
//                    Log.d(TAG,"DRag drop"+pos);

                    View top = recyclerViewFileList.findChildViewUnder(event.getX(), event.getY());
                    int position = recyclerViewFileList.getChildAdapterPosition(top);
                    Logger.log(TAG, "DROP new pos=" + position);
                    fileListAdapter.clearDragPos();
                    @SuppressWarnings("unchecked")
                    ArrayList<FileInfo> draggedFiles = (ArrayList<FileInfo>) event.getLocalState();
                    ArrayList<String> paths = new ArrayList<>();

                  /*  ArrayList<FileInfo> paths = dragData.getParcelableArrayListExtra(FileConstants
                            .KEY_PATH);*/

                    String destinationDir;
                    if (position != -1) {
                        destinationDir = fileInfoList.get(position).getFilePath();
                    }
                    else {
                        destinationDir = currentDir;
                    }

                    for (FileInfo info : draggedFiles) {
                        paths.add(info.getFilePath());
                    }

                    String sourceParent = new File(draggedFiles.get(0).getFilePath()).getParent();
                    if (!new File(destinationDir).isDirectory()) {
                        destinationDir = new File(destinationDir).getParent();
                    }

                    boolean value = destinationDir.equals(sourceParent);
                    Logger.log(TAG, "Source parent=" + sourceParent + " " + value);


                    if (!paths.contains(destinationDir)) {
                        if (!destinationDir.equals(sourceParent)) {
                            Logger.log(TAG, "Source parent=" + sourceParent + " Dest=" +
                                    destinationDir);
                            showDragDialog(draggedFiles, destinationDir);
                        }
                        else {
                            final boolean isMoveOperation = false;
                            ArrayList<FileInfo> info = new ArrayList<>();
                            info.addAll(draggedFiles);
                            PasteConflictChecker conflictChecker = new PasteConflictChecker
                                    (BaseFileList.this,
                                            destinationDir, mIsRootMode, isMoveOperation, info);
                            conflictChecker.execute();
                            clearSelectedPos();
                            Logger.log(TAG, "Source=" + draggedFiles.get(0) + "Dest=" +
                                    destinationDir);
                            actionMode.finish();
                        }
                    }

                    draggedData = new ArrayList<>();
                    return true;

                case DragEvent.ACTION_DRAG_ENDED:

                    View top1 = recyclerViewFileList.findChildViewUnder(event.getX(), event.getY());
                    int position1 = recyclerViewFileList.getChildAdapterPosition(top1);
                    @SuppressWarnings("unchecked")
                    ArrayList<FileInfo> dragPaths = (ArrayList<FileInfo>) event.getLocalState();


                    Logger.log(TAG, "DRAG END new pos=" + position1);
                    Logger.log(TAG, "DRAG END Local state=" + dragPaths);
                    Logger.log(TAG, "DRAG END result=" + event.getResult());
                    Logger.log(TAG, "DRAG END currentDirSingle=" + mLastSinglePaneDir);
                    Log.d(TAG, "DRag end");
                    fileListAdapter.clearDragPos();
                    if (!event.getResult() && position1 == RecyclerView.NO_POSITION) {
                        ViewParent parent1 = v.getParent().getParent();

                        if (((View) parent1).getId() == R.id.frame_container_dual) {
                            Logger.log(TAG, "DRAG END parent dual =" + true);
/*                            FileListDualFragment dualPaneFragment = (FileListDualFragment)
                                    getFragmentManager()
                                            .findFragmentById(R
                                                    .id.frame_container_dual);
                            Logger.log(TAG, "DRAG END Dual dir=" + mLastDualPaneDir);

//                            Logger.log(TAG, "Source=" + draggedData.get(0) + "Dest=" +
mLastDualPaneDir);
                            if (dualPaneFragment != null && new File(mLastDualPaneDir).list()
                            .length == 0 &&
                                    dragPaths.size() != 0) {
//                                if (!destinationDir.equals(paths.get(0))) {
                                showDragDialog(dragPaths, mLastDualPaneDir);
//                                }
                            }*/
                        }
                        else {
                            Logger.log(TAG, "DRAG END parent dual =" + false);
                            BaseFileList singlePaneFragment = (BaseFileList)
                                    getFragmentManager()
                                            .findFragmentById(R
                                                    .id.main_container);
                            Logger.log(TAG, "DRAG END single dir=" + mLastSinglePaneDir);

//                            Logger.log(TAG, "Source=" + draggedData.get(0) + "Dest=" +
// mLastDualPaneDir);
                            if (singlePaneFragment != null && new File(mLastSinglePaneDir).list()
                                    .length == 0 &&
                                    dragPaths.size() != 0) {
//                                if (!destinationDir.equals(paths.get(0))) {
                                showDragDialog(dragPaths, mLastSinglePaneDir);
//                                }
                            }
                        }

                    }
                    draggedData = new ArrayList<>();
                    // returns true; the value is ignored.
                    return true;

                // An unknown action type was received.
                default:
                    Log.e(TAG, "Unknown action type received by OnDragListener.");
                    break;
            }

            return false;
        }
    }
}
