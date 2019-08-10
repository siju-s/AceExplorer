package com.siju.acexplorer.appmanager.model

class AppInfo(val packageName : String, val appName : String, val source : String, val enabled : Boolean,
              val minSdk : Int, val targetSdk : Int, val installTime : String, val updatedTime : String)