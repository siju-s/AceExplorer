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
package com.siju.acexplorer.main.view.dialog

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.siju.acexplorer.R
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.extensions.inflateLayout
import com.siju.acexplorer.main.model.helper.FileUtils.getFileNameWithoutExt
import com.siju.acexplorer.main.model.helper.UriHelper.canGrantUriPermission
import com.siju.acexplorer.main.view.PasteConflictAdapter
import com.siju.acexplorer.storage.model.SortMode
import com.siju.acexplorer.storage.model.SortMode.Companion.getSortModeFromValue
import com.siju.acexplorer.storage.model.SortMode.Companion.isAscending
import com.siju.acexplorer.storage.model.operations.Operations
import com.siju.acexplorer.storage.model.operations.PasteConflictCheckData
import com.stericson.RootTools.RootTools
import java.io.File
import java.util.*

/**
 * Created by Siju on 29 August,2017
 */
object DialogHelper {
    fun showDeleteDialog(context: Context, files: ArrayList<FileInfo>,
                         trashEnabled: Boolean, deleteDialogListener: DeleteDialogListener) {
        val title = context.getString(R.string.dialog_delete_title, files.size)
        val texts = arrayOf(title, context.getString(R.string.msg_ok), "", context
                .getString(R.string.dialog_cancel))
        val builder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)
        val dialogView = inflater.inflateLayout(R.layout.dialog_delete, null)
        builder.setView(dialogView)
        builder.setCancelable(true)
        val alertDialog = builder.create()
        val textTitle = dialogView.findViewById<TextView>(R.id.textTitle)
        val positiveButton = dialogView.findViewById<Button>(R.id.buttonPositive)
        val negativeButton = dialogView.findViewById<Button>(R.id.buttonNegative)
        val checkBoxTrash = dialogView.findViewById<CheckBox>(R.id.checkBoxTrash)
        checkBoxTrash.isChecked = trashEnabled
        textTitle.text = title
        positiveButton.text = texts[1]
        negativeButton.text = texts[3]
        positiveButton.setOnClickListener { view ->
            val isChecked = false
            deleteDialogListener.onPositiveButtonClick(view, isChecked, files)
            alertDialog.dismiss()
        }
        negativeButton.setOnClickListener { alertDialog.dismiss() }
        alertDialog.show()
    }

    fun showAlertDialog(context: Context, text: Array<String>, dialogListener: AlertDialogListener) {
        val builder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)
        val dialogView = inflater.inflateLayout(R.layout.alert_dialog, null)
        builder.setView(dialogView)
        builder.setCancelable(false)
        val alertDialog = builder.create()
        val title = dialogView.findViewById<TextView>(R.id.textTitle)
        val msg = dialogView.findViewById<TextView>(R.id.textMessage)
        val positiveButton = dialogView.findViewById<Button>(R.id.buttonPositive)
        val negativeButton = dialogView.findViewById<Button>(R.id.buttonNegative)
        val neutralButton = dialogView.findViewById<Button>(R.id.buttonNeutral)
        title.text = text[0]
        msg.text = text[1]
        positiveButton.text = text[2]
        positiveButton.setOnClickListener { v ->
            dialogListener.onPositiveButtonClick(v)
            alertDialog.dismiss()
        }
        if (text.size > 3) {
            negativeButton.visibility = View.VISIBLE
            negativeButton.text = text[3]
            negativeButton.setOnClickListener { v ->
                dialogListener.onNegativeButtonClick(v)
                alertDialog.dismiss()
            }
        }
        if (text.size > 4) {
            neutralButton.visibility = View.VISIBLE
            neutralButton.text = text[4]
            neutralButton.setOnClickListener { v ->
                dialogListener.onNeutralButtonClick(v)
                alertDialog.dismiss()
            }
        }
        alertDialog.show()
        alertDialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    fun showSortDialog(context: Context, sortMode: SortMode, sortDialogListener: SortDialogListener) {
        val title = context.getString(R.string.action_sort)
        val texts = arrayOf(title, context.getString(R.string.msg_ok), "", context
                .getString(R.string.dialog_cancel))
        val builder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)
        val dialogView = inflater.inflateLayout(R.layout.dialog_sort, null)
        builder.setView(dialogView)
        builder.setCancelable(true)
        val alertDialog = builder.create()
        val textTitle = dialogView.findViewById<TextView>(R.id.textTitle)
        val radioGroup = dialogView.findViewById<RadioGroup>(R.id.radioGroupSort)
        val positiveButton = dialogView.findViewById<Button>(R.id.buttonPositive)
        val negativeButton = dialogView.findViewById<Button>(R.id.buttonNegative)
        val ascendingOrderCheckbox = dialogView.findViewById<CheckBox>(R.id.checkBoxOrder)
        ascendingOrderCheckbox.isChecked = isAscending(sortMode)
        textTitle.text = title
        positiveButton.text = texts[1]
        negativeButton.text = texts[3]
        val radioButton = radioGroup.getChildAt(indexToCheck(sortMode))
        radioGroup.check(radioButton.id)

        positiveButton.setOnClickListener {
            val checkedId = radioGroup.checkedRadioButtonId
            val checkedButton = radioGroup.findViewById<View>(checkedId)
            val index = radioGroup.indexOfChild(checkedButton)
            val sortModeNew = getSortModeFromValue(newSortMode(index, ascendingOrderCheckbox.isChecked))
            sortDialogListener.onPositiveButtonClick(sortModeNew)
            alertDialog.dismiss()
        }
        negativeButton.setOnClickListener { alertDialog.dismiss() }
        alertDialog.show()
    }

    private fun indexToCheck(sortMode: SortMode): Int {
        return sortMode.value / 2
    }

    private fun newSortMode(selectedOption: Int, isAscending: Boolean): Int {
        var newSortMode = selectedOption * 2
        if (!isAscending) {
            newSortMode += 1
        }
        return newSortMode
    }

    fun showInputDialog(context: Context, text: Array<String>,
                        operation: Operations?,
                        textEdit: String?, dialogListener: DialogCallback) {
        val builder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)
        val dialogView = inflater.inflateLayout(R.layout.dialog_rename, null)
        builder.setView(dialogView)
        builder.setCancelable(false)
        val alertDialog = builder.create()
        val title = dialogView.findViewById<TextView>(R.id.textTitle)
        val msg = dialogView.findViewById<TextView>(R.id.textMessage)
        val inputText = dialogView.findViewById<EditText>(R.id.editFileName)
        val positiveButton = dialogView.findViewById<Button>(R.id.buttonPositive)
        val negativeButton = dialogView.findViewById<Button>(R.id.buttonNegative)
        title.text = text[0]
        msg.text = text[1]
        positiveButton.text = text[2]
        negativeButton.text = text[3]
        val fileName = getFileNameWithoutExt(textEdit)
        if (fileName != null) {
            inputText.setText(fileName)
        }
        positiveButton.setOnClickListener {
            val name = inputText.text.toString().trim { it <= ' ' }
            dialogListener.onPositiveButtonClick(alertDialog, operation, name)
        }
        negativeButton.setOnClickListener {
            dialogListener.onNegativeButtonClick(operation)
            alertDialog.dismiss()
        }
        alertDialog.show()
    }

    fun showApkDialog(context: Context, text: Array<String>, path: String?,
                      dialogListener: ApkDialogListener) {
        val builder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)
        val dialogView = inflater.inflateLayout(R.layout.alert_dialog, null)
        builder.setView(dialogView)
        builder.setCancelable(false)
        val alertDialog = builder.create()
        val title = dialogView.findViewById<TextView>(R.id.textTitle)
        val msg = dialogView.findViewById<TextView>(R.id.textMessage)
        val positiveButton = dialogView.findViewById<Button>(R.id.buttonPositive)
        val negativeButton = dialogView.findViewById<Button>(R.id.buttonNegative)
        val neutralButton = dialogView.findViewById<Button>(R.id.buttonNeutral)
        title.text = text[0]
        msg.text = text[1]
        positiveButton.text = text[2]
        positiveButton.setOnClickListener {
            dialogListener.onInstallClicked(path)
            alertDialog.dismiss()
        }
        if (text.size > 3) {
            negativeButton.visibility = View.VISIBLE
            negativeButton.text = text[3]
            negativeButton.setOnClickListener {
                dialogListener.onCancelClicked()
                alertDialog.dismiss()
            }
        }
        if (text.size > 4) {
            neutralButton.visibility = View.VISIBLE
            neutralButton.text = text[4]
            neutralButton.setOnClickListener {
                dialogListener.onOpenApkClicked(path)
                alertDialog.dismiss()
            }
        }
        alertDialog.show()
        alertDialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    fun showSAFDialog(context: Context, path: String?, dialogListener: AlertDialogListener) {
        val builder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)
        val dialogView = inflater.inflateLayout(R.layout.dialog_saf, null)
        builder.setView(dialogView)
        builder.setCancelable(false)
        val dialogTitle = context.getString(R.string.needsaccess)
        val text = arrayOf(dialogTitle, context.getString(R.string.needs_access_summary, path),
                context.getString(R.string.open), context.getString(R.string.dialog_cancel))
        val alertDialog = builder.create()
        val title = dialogView.findViewById<TextView>(R.id.textTitle)
        val msg = dialogView.findViewById<TextView>(R.id.textMessage)
        val positiveButton = dialogView.findViewById<Button>(R.id.buttonPositive)
        val negativeButton = dialogView.findViewById<Button>(R.id.buttonNegative)
        title.text = text[0]
        msg.text = text[1]
        positiveButton.text = text[2]
        positiveButton.setOnClickListener { v ->
            dialogListener.onPositiveButtonClick(v)
            alertDialog.dismiss()
        }
        negativeButton.visibility = View.VISIBLE
        negativeButton.text = text[3]
        negativeButton.setOnClickListener { v ->
            dialogListener.onNegativeButtonClick(v)
            alertDialog.dismiss()
        }
        alertDialog.show()
    }

    fun showConflictDialog(context: Context, pasteConflictCheckData: PasteConflictCheckData,
                           pasteConflictListener: PasteConflictListener) {
        val texts = arrayOf(context.getString(R.string.msg_file_exists),
                context.getString(R.string.dialog_skip), context.getString(R.string.dialog_keep_both),
                context.getString(R.string.dialog_replace))
        val builder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)
        val dialogView = inflater.inflateLayout(R.layout.dialog_paste_conflict, null)
        builder.setView(dialogView)
        builder.setCancelable(false)
        val dialog = builder.create()
        val listView = dialogView.findViewById<ListView>(R.id.listFiles)
        val footerView = inflater.inflateLayout(R.layout.paste_conflict_footer, null, false)

        listView.addFooterView(footerView)
        val fileInfoList: MutableList<FileInfo> = ArrayList()
        val conflictFiles: List<FileInfo> = pasteConflictCheckData.conflictFiles
        val destFiles: List<FileInfo> = pasteConflictCheckData.destFiles
        val destinationDir = pasteConflictCheckData.destinationDir
        val operation = pasteConflictCheckData.operations
        val fileToPaste = conflictFiles[0]
        val sourcePath = fileToPaste.filePath ?: return
        val targetPath = fileToPaste.filePath ?: return

        val sourceFile = File(sourcePath)
        fileInfoList.add(fileToPaste)
        fileInfoList.add(destFiles[0])
        val pasteConflictAdapter = PasteConflictAdapter(context, fileInfoList)
        listView.adapter = pasteConflictAdapter
        val checkBox = footerView.findViewById<CheckBox>(R.id.checkBox)
        if (conflictFiles.size == 1) {
            checkBox.visibility = View.GONE
        }
        val positiveButton = footerView.findViewById<Button>(R.id.buttonPositive)
        val negativeButton = footerView.findViewById<Button>(R.id.buttonNegative)
        val neutralButton = footerView.findViewById<Button>(R.id.buttonKeepBoth)
        positiveButton.text = texts[1]
        negativeButton.text = texts[3]
        // POSITIVE BUTTON ->SKIP   NEGATIVE ->REPLACE    NEUTRAL ->KEEP BOTH
        if (sourceFile.parent == destinationDir) {
            if (operation === Operations.CUT) {
                neutralButton.visibility = View.GONE
                negativeButton.visibility = View.GONE
            } else {
                negativeButton.visibility = View.GONE
            }
        }
        if (File(targetPath).isDirectory) {
            neutralButton.visibility = View.GONE
        }
        val checked = checkBox.isChecked
        positiveButton.setOnClickListener {
            dialog.dismiss()
            pasteConflictListener.onSkipClicked(pasteConflictCheckData, checked)
        }
        negativeButton.setOnClickListener {
            dialog.dismiss()
            pasteConflictListener.onReplaceClicked(pasteConflictCheckData, checked)
        }
        neutralButton.setOnClickListener {
            dialog.dismiss()
            pasteConflictListener.onKeepBothClicked(pasteConflictCheckData, checked)
        }
        dialog.show()
    }

    fun showPermissionsDialog(context: Context, path: String?, isDir: Boolean, permissions: ArrayList<Array<Boolean>>,
                              permissionDialogListener: PermissionDialogListener) {
        val builder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)
        val dialogView = inflater.inflateLayout(R.layout.dialog_permission, null)
        builder.setView(dialogView)
        builder.setCancelable(false)
        val alertDialog = builder.create()

        val readOwnerState = dialogView.findViewById<CheckBox>(R.id.readOwnerState)
        val readGroupState = dialogView.findViewById<CheckBox>(R.id.readGroupState)
        val readOtherState = dialogView.findViewById<CheckBox>(R.id.readOtherState)

        val writeOwnerState = dialogView.findViewById<CheckBox>(R.id.writeOwnerState)
        val writeGroupState = dialogView.findViewById<CheckBox>(R.id.writeGroupState)
        val writeOtherState = dialogView.findViewById<CheckBox>(R.id.writeOtherState)

        val exeOwnerState = dialogView.findViewById<CheckBox>(R.id.execOwnerState)
        val exeGroupState = dialogView.findViewById<CheckBox>(R.id.execGroupState)
        val exeOtherState = dialogView.findViewById<CheckBox>(R.id.execOtherState)

        val positiveButton = dialogView.findViewById<Button>(R.id.buttonPositive)
        val negativeButton = dialogView.findViewById<Button>(R.id.buttonNegative)

        positiveButton.text = context.getString(R.string.msg_ok)
        negativeButton.text = context.getString(R.string.dialog_cancel)

        val read = permissions[0]
        val write = permissions[1]
        val exe = permissions[2]

        readOwnerState.isChecked = read[0]
        readGroupState.isChecked = read[1]
        readOtherState.isChecked = read[2]
        writeOwnerState.isChecked = write[0]
        writeGroupState.isChecked = write[1]
        writeOtherState.isChecked = write[2]
        exeOwnerState.isChecked = exe[0]
        exeGroupState.isChecked = exe[1]
        exeOtherState.isChecked = exe[2]

        positiveButton.setOnClickListener(View.OnClickListener {
            alertDialog.dismiss()
            if (!RootTools.isAccessGiven()) {
                return@OnClickListener
            }
            var a = 0
            var b = 0
            var c = 0
            if (readOwnerState.isChecked) {
                a = 4
            }
            if (writeOwnerState.isChecked) {
                b = 2
            }
            if (exeOwnerState.isChecked) {
                c = 1
            }
            val owner = a + b + c
            var d = 0
            var e = 0
            var f = 0
            if (readGroupState.isChecked) {
                d = 4
            }
            if (writeGroupState.isChecked) {
                e = 2
            }
            if (exeGroupState.isChecked) {
                f = 1
            }
            val group = d + e + f
            var g = 0
            var h = 0
            var i = 0
            if (readOtherState.isChecked) {
                g = 4
            }
            if (writeOtherState.isChecked) {
                h = 2
            }
            if (exeOtherState.isChecked) {
                i = 1
            }
            val other = g + h + i
            val finalValue = owner.toString() + "" + group + "" + other
            permissionDialogListener.onPositiveButtonClick(path, isDir, finalValue)
        })
        negativeButton.setOnClickListener { alertDialog.dismiss() }
        alertDialog.show()
    }

    fun showCompressDialog(context: Context, paths: ArrayList<FileInfo>,
                           dialogListener: CompressDialogListener) {
        val fileName = paths[0].fileName
        val filePath = paths[0].filePath
        filePath ?: return
        fileName ?: return
        var zipName = fileName
        if (!File(filePath).isDirectory) {
            zipName = fileName.substring(0, fileName.lastIndexOf("."))
        }
        val title = context.getString(R.string.action_archive)
        val builder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)
        val dialogView = inflater.inflateLayout(R.layout.dialog_rename, null)
        builder.setView(dialogView)
        builder.setCancelable(false)
        val alertDialog = builder.create()
        val titleText = dialogView.findViewById<TextView>(R.id.textTitle)
        val msgText = dialogView.findViewById<TextView>(R.id.textMessage)
        val inputText = dialogView.findViewById<EditText>(R.id.editFileName)
        val positiveButton = dialogView.findViewById<Button>(R.id.buttonPositive)
        val negativeButton = dialogView.findViewById<Button>(R.id.buttonNegative)
        msgText.visibility = View.GONE
        titleText.text = title
        inputText.setText(zipName)
        positiveButton.text = context.getString(R.string.create)
        negativeButton.text = context.getString(R.string.dialog_cancel)
        positiveButton.setOnClickListener {
            val name = inputText.text.toString().trim { it <= ' ' }
            dialogListener.onPositiveButtonClick(alertDialog, Operations.COMPRESS, name,
                    paths)
        }
        negativeButton.setOnClickListener {
            dialogListener.onNegativeButtonClick(Operations.COMPRESS)
            alertDialog.dismiss()
        }
        alertDialog.show()
    }

    fun showExtractDialog(context: Context, zipFilePath: String?,
                          destinationDir: String?, extractDialogListener: ExtractDialogListener) {
        zipFilePath ?: return
        destinationDir ?: return
        val currentFileName = zipFilePath.substring(zipFilePath.lastIndexOf("/")
                + 1, zipFilePath
                .lastIndexOf("."))
        val texts = arrayOf(context.getString(R.string.extract), context.getString(R.string.dialog_cancel))
        val builder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)
        val dialogView = inflater.inflateLayout(R.layout.dialog_extract, null)
        builder.setView(dialogView)
        val alertDialog = builder.create()

        val extractToPathButton = dialogView.findViewById<RadioButton>(R.id.radioButtonSpecifyPath)
        val selectPathButton = dialogView.findViewById<Button>(R.id.buttonPathSelect)
        val radioGroupPath = dialogView.findViewById<RadioGroup>(R.id.radioGroupPath)
        val editFileName = dialogView.findViewById<EditText>(R.id.editFileName)

        editFileName.setText(currentFileName)
        radioGroupPath.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.radioButtonCurrentPath) {
                selectPathButton.visibility = View.GONE
            } else {
                selectPathButton.visibility = View.VISIBLE
            }
        }
        selectPathButton.setOnClickListener { extractDialogListener.onSelectButtonClicked(alertDialog) }
        val positiveButton = dialogView.findViewById<Button>(R.id.buttonPositive)
        val negativeButton = dialogView.findViewById<Button>(R.id.buttonNegative)
        positiveButton.text = texts[0]
        negativeButton.text = texts[1]
        positiveButton.setOnClickListener {
            val fileName = editFileName.text.toString()
            var newDir = destinationDir
            if (extractToPathButton.isChecked) {
                newDir = selectPathButton.text.toString()
            }
            extractDialogListener.onPositiveButtonClick(Operations.EXTRACT, zipFilePath, fileName,
                    newDir)
            alertDialog.dismiss()
        }
        negativeButton.setOnClickListener { alertDialog.dismiss() }
        alertDialog.show()
    }

    fun showDragDialog(context: Context, filesToPaste: ArrayList<FileInfo>,
                       destinationDir: String?, dragDialogListener: DragDialogListener) {
        val builder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)
        val dialogView = inflater.inflateLayout(R.layout.dialog_drag, null)
        builder.setView(dialogView)
        val alertDialog = builder.create()
        val radioButtonCopy = dialogView.findViewById<RadioButton>(R.id.radioCopy)
        val textMessage = dialogView.findViewById<TextView>(R.id.textMessage)
        textMessage.text = String.format("%s %s", context.getString(R.string.dialog_to_placeholder), destinationDir)
        val positiveButton = dialogView.findViewById<Button>(R.id.buttonPositive)
        val negativeButton = dialogView.findViewById<Button>(R.id.buttonNegative)
        positiveButton.text = context.getString(R.string.msg_ok)
        negativeButton.text = context.getString(R.string.dialog_cancel)
        val operation: Operations
        operation = if (radioButtonCopy.isChecked) {
            Operations.COPY
        } else {
            Operations.CUT
        }
        positiveButton.setOnClickListener {
            alertDialog.dismiss()
            dragDialogListener.onPositiveButtonClick(filesToPaste, destinationDir,
                    operation)
        }
        negativeButton.setOnClickListener { alertDialog.dismiss() }
        alertDialog.show()
    }

    fun openWith(uri: Uri?, context: Context) {
        val bottomSheetDialog = BottomSheetDialog(context)
        val sheetView = LayoutInflater.from(context).inflateLayout(R.layout.dialog_open_as, null)
        bottomSheetDialog.setContentView(sheetView)
        val items = ArrayList<String>()
        items.add(context.getString(R.string.text))
        items.add(context.getString(R.string.image))
        items.add(context.getString(R.string.audio))
        items.add(context.getString(R.string.other))
        val textTitle = sheetView.findViewById<TextView>(R.id.textTitle)
        val listView = sheetView.findViewById<ListView>(R.id.listOpenAs)
        textTitle.text = context.getString(R.string.open_as)
        val itemsAdapter: ArrayAdapter<String> = object : ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, items) {
        }
        listView.adapter = itemsAdapter
        listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val intent = Intent(Intent.ACTION_VIEW)
            when (position) {
                0 -> intent.setDataAndType(uri, "text/*")
                1 -> intent.setDataAndType(uri, "image/*")
                2 -> intent.setDataAndType(uri, "video/*")
                3 -> intent.setDataAndType(uri, "audio/*")
                4 -> intent.setDataAndType(uri, "*/*")
            }
            val canGrant = canGrantUriPermission(context, intent)
            if (canGrant) {
                context.startActivity(intent)
            }
            bottomSheetDialog.dismiss()
        }
        bottomSheetDialog.show()
    }

    interface ExtractDialogListener {
        fun onPositiveButtonClick(operation: Operations, sourceFilePath: String, newFileName: String,
                                  destinationDir: String)

        fun onSelectButtonClicked(dialog: Dialog)
    }

    interface PermissionDialogListener {
        fun onPositiveButtonClick(path: String?, isDir: Boolean, permissions: String?)
    }

    interface CompressDialogListener {
        fun onPositiveButtonClick(dialog: Dialog?, operation: Operations, newFileName: String,
                                  paths: ArrayList<FileInfo>)

        fun onNegativeButtonClick(operation: Operations?)
    }

    interface AlertDialogListener {
        fun onPositiveButtonClick(view: View)
        fun onNegativeButtonClick(view: View)
        fun onNeutralButtonClick(view: View)
    }

    interface ApkDialogListener {
        fun onInstallClicked(path: String?)
        fun onCancelClicked()
        fun onOpenApkClicked(path: String?)
    }

    interface SortDialogListener {
        fun onPositiveButtonClick(sortMode: SortMode)
    }

    interface DeleteDialogListener {
        fun onPositiveButtonClick(view: View, isTrashEnabled: Boolean, filesToDelete: ArrayList<FileInfo>)
    }

    interface DialogCallback {
        fun onPositiveButtonClick(dialog: Dialog?, operation: Operations?, name: String?)
        fun onNegativeButtonClick(operations: Operations?)
    }

    interface PasteConflictListener {
        fun onSkipClicked(pasteConflictCheckData: PasteConflictCheckData, isChecked: Boolean)
        fun onReplaceClicked(pasteConflictCheckData: PasteConflictCheckData, isChecked: Boolean)
        fun onKeepBothClicked(pasteConflictCheckData: PasteConflictCheckData, isChecked: Boolean)
    }

    interface DragDialogListener {
        fun onPositiveButtonClick(filesToPaste: ArrayList<FileInfo>, destinationDir: String?,
                                  operation: Operations)
    }
}