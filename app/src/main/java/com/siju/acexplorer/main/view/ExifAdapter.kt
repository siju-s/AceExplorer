package com.siju.acexplorer.main.view

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.recyclerview.widget.RecyclerView
import com.siju.acexplorer.R
import com.siju.acexplorer.main.model.ExifData

private const val VIEW_MAIN = 0
private const val VIEW_FOOTER = 1

class ExifAdapter(private val exif: ArrayList<ExifData>, private val latLong: DoubleArray?) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    override fun getItemViewType(position: Int): Int {
        if (position == exif.size) {
            return VIEW_FOOTER
        }
        return VIEW_MAIN
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewtype: Int): RecyclerView.ViewHolder {
        return when (viewtype) {
            VIEW_MAIN -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.info_exif_item, parent, false)
                ViewHolder(ComposeView(view.context))
            }
            else      -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.info_exif_gps, parent, false)
                FooterViewHolder(view)
            }
        }

    }

    override fun getItemCount(): Int {
        return exif.size + 1
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        when (viewHolder) {
            is ViewHolder       -> {
                val data = exif[position]
                viewHolder.bind(data.tag, data.value)
            }
            is FooterViewHolder -> {
                val gpsArr = latLong
                if (gpsArr != null) {
                    viewHolder.bind(gpsArr[0], gpsArr[1])
                }
            }
        }

    }

    class ViewHolder(val view: ComposeView) : RecyclerView.ViewHolder(view) {
//        private val labelText: TextView = view.findViewById(R.id.textLabel)
//        private val valueText: TextView = view.findViewById(R.id.textLabelValue)

        fun bind(tag: String, value: String) {
//            labelText.text = tag
//            valueText.text = value
            view.setContent {
                MaterialTheme {
                    Row(modifier = Modifier.fillMaxSize()) {
                        Text(tag, style = TextStyle(fontStyle = FontStyle.Italic))
                        Text(value)
                    }
                }

            }
        }
    }

    class FooterViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val mapsButton: Button = view.findViewById(R.id.buttonMaps)

        fun bind(lat: Double, lang: Double) {
            mapsButton.visibility = View.VISIBLE
            mapsButton.setOnClickListener {
                showGpsCoordinates(it.context, lat, lang)
            }
        }

        private fun showGpsCoordinates(context: Context?, latitude: Double, longitude: Double) {
            context ?: return
            Log.d("info", "showGpsCoordinates() called with: latitude = $latitude, longitude = $longitude")
            val gmmIntentUri = Uri.parse("geo:0,0?q=$latitude,$longitude")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            context.startActivity(mapIntent)
        }

    }


}