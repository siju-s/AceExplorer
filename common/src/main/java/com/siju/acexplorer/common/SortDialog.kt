package com.siju.acexplorer.common

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.siju.acexplorer.common.SortMode.Companion.getSortModeFromValue
import com.siju.acexplorer.extensions.inflateLayout

object SortDialog {
    fun show(context: Context, sortMode: SortMode, sortDialogListener: Listener, hideType: Boolean = false) {
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
        ascendingOrderCheckbox.isChecked = SortMode.isAscending(sortMode)
        textTitle.text = title
        positiveButton.text = texts[1]
        negativeButton.text = texts[3]
        val radioButton = radioGroup.getChildAt(indexToCheck(sortMode))
        radioGroup.check(radioButton.id)
        if (hideType) {
            radioGroup.getChildAt(1).visibility = View.GONE
        }

        positiveButton.setOnClickListener {
            val checkedId = radioGroup.checkedRadioButtonId
            val checkedButton = radioGroup.findViewById<View>(checkedId)
            val index = radioGroup.indexOfChild(checkedButton)
            val sortModeNew = getSortModeFromValue(newSortMode(index, ascendingOrderCheckbox.isChecked))
            sortDialogListener.onPositiveButtonClick(sortModeNew)
            alertDialog.dismiss()
        }
        negativeButton.setOnClickListener {
            alertDialog.dismiss()
        }
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

    interface Listener {
        fun onPositiveButtonClick(sortMode: SortMode)
    }
}