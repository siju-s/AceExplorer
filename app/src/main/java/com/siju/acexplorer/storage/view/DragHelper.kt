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

package com.siju.acexplorer.storage.view

import android.content.ClipDescription
import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.DragEvent
import android.view.View
import com.siju.acexplorer.R
import com.siju.acexplorer.analytics.Analytics
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.view.dialog.DialogHelper
import java.util.*


private const val TEXT_WIDTH_PX = 200
private const val TAG = "DragHelper"

class DragHelper(private val context: Context, private val filesList: FilesList) {

    val dragListener = DragEventListener()

    private fun writeOnDrawable(text: String): BitmapDrawable {

        val bitmap = Bitmap.createBitmap(TEXT_WIDTH_PX, TEXT_WIDTH_PX, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(Color.DKGRAY)

        val paint = Paint()
        paint.style = Paint.Style.FILL
        paint.isAntiAlias = true
        paint.color = Color.WHITE
        val countFont = context.resources
                .getDimensionPixelSize(R.dimen.drag_shadow_font)
        paint.textSize = countFont.toFloat()

        val canvas = Canvas(bitmap)
        val strLength = paint.measureText(text).toInt()
        val x = bitmap.width / 2 - strLength

        // int y = s.titleOffset;
        val y = (bitmap.height - countFont) / 2
        //        drawText(canvas, x, y, title, labelWidth - s.leftMargin - x
        //                - s.titleRightMargin, mTitlePaint);

        canvas.drawText(text, x.toFloat(), (y - paint.fontMetricsInt.ascent).toFloat(), paint)
        //        canvas.drawText(text, bitmap.getWidth() / 2, bitmap.getHeight() / 2, paint);

        return BitmapDrawable(context.resources, bitmap)
    }


    fun getDragShadowBuilder(view: View, count: Int): CustomDragShadowBuilder {
        return CustomDragShadowBuilder(view, count)
    }

    inner class CustomDragShadowBuilder(view: View, count: Int) : View.DragShadowBuilder(view) {

        // The drag shadow image, defined as a drawable thing
        private val shadow: Drawable

        init {
            // Creates a draggable image that will fill the Canvas provided by the system.
            shadow = writeOnDrawable("" + count)

        }

        // Defines a callback that sends the drag shadow dimensions and touch point back to the
        // system.
        override fun onProvideShadowMetrics(size: Point, touch: Point) {
            // Defines local variables
            val width: Int = view.width / 6
            val height: Int = view.height / 2

            // The drag shadow is a ColorDrawable. This sets its dimensions to be the same as the
            // Canvas that the system will provide. As a result, the drag shadow will fill the
            // Canvas.
            shadow.setBounds(0, 0, width, height)

            // Sets the size parameter's width and height values. These get back to the system
            // through the size parameter.
            size.set(width, height)
            // Sets the touch point's position to be in the middle of the drag shadow
            touch.set(2 * width, height * 2)
        }

        // Defines a callback that draws the drag shadow in a Canvas that the system constructs
        // from the dimensions passed in onProvideShadowMetrics().
        override fun onDrawShadow(canvas: Canvas) {

            // Draws the ColorDrawable in the Canvas passed in from the system.
            shadow.draw(canvas)
        }
    }

    fun showDragDialog(sourcePaths: ArrayList<FileInfo>, destinationDir: String?,
                       dragDialogListener: DialogHelper.DragDialogListener) {

        Analytics.getLogger().dragDialogShown()
        DialogHelper.showDragDialog(context, sourcePaths, destinationDir, dragDialogListener)
    }


    inner class DragEventListener : View.OnDragListener {

        private var oldPos = -1

        override fun onDrag(view: View, event: DragEvent): Boolean {

            when (event.action) {

                DragEvent.ACTION_DRAG_STARTED  ->
                    // Determines if this View can accept the dragged data
                    return event.clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_INTENT)

                // Returns false. During the current drag and drop operation, this View will
                // not receive events again until ACTION_DRAG_ENDED is sent.

                DragEvent.ACTION_DRAG_ENTERED  -> return true

                DragEvent.ACTION_DRAG_LOCATION -> {
                    oldPos = filesList.onDragLocationEvent(event, oldPos)
                    return true
                }

                DragEvent.ACTION_DRAG_EXITED   -> return true

                DragEvent.ACTION_DROP          -> {
                    filesList.onDragDropEvent(event)
                    return true
                }

                DragEvent.ACTION_DRAG_ENDED    -> {
                    filesList.onDragEnd(view, event)
                    return true
                }

                // An unknown action type was received.
                else                           -> Log.e(TAG,
                                                        "Unknown action type received by OnDragListener.")
            }

            return false
        }
    }
}
