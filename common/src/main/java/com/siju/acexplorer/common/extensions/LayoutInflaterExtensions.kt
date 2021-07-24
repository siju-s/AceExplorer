package com.siju.acexplorer.extensions

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

fun LayoutInflater.inflateLayout(layoutId : Int, container: ViewGroup?, attachToRoot : Boolean = false) : View {
    return this.inflate(layoutId, container, attachToRoot)
}