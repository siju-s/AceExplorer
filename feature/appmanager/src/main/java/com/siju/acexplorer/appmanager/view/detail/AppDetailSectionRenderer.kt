package com.siju.acexplorer.appmanager.view.detail

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.getSystemService
import com.siju.acexplorer.appmanager.R

/**
 * Inflates [AppDetailSection]s into a vertical container.
 *
 * Rendering rows from data keeps the layout free of dozens of fixed ids, so adding a detail row
 * means adding it to [AppDetailSectionFactory] and nothing else.
 */
class AppDetailSectionRenderer(private val container: ViewGroup) {

    private val context: Context = container.context
    private val layoutInflater = LayoutInflater.from(context)

    fun render(sections: List<AppDetailSection>) {
        container.removeAllViews()
        sections.forEach { section ->
            addSectionTitle(section)
            section.rows.forEach(::addRow)
        }
    }

    private fun addSectionTitle(section: AppDetailSection) {
        val titleView = layoutInflater.inflate(R.layout.app_detail_section_header, container, false) as TextView
        titleView.text = context.getString(section.titleRes)
        container.addView(titleView)
    }

    private fun addRow(row: AppDetailRow) {
        val rowView = layoutInflater.inflate(R.layout.app_detail_row, container, false)
        val label = context.getString(row.labelRes)

        rowView.findViewById<TextView>(R.id.textDetailLabel).text = label
        rowView.findViewById<TextView>(R.id.textDetailValue).text = row.value

        if (row.copyable) {
            rowView.setOnLongClickListener {
                copyToClipboard(label, row.value)
                true
            }
        }
        container.addView(rowView)
    }

    private fun copyToClipboard(label: String, value: String) {
        val clipboardManager = context.getSystemService<ClipboardManager>() ?: return
        clipboardManager.setPrimaryClip(ClipData.newPlainText(label, value))
        Toast.makeText(context, context.getString(R.string.copied_to_clipboard, label), Toast.LENGTH_SHORT).show()
    }
}
