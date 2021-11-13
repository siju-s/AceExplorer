package com.siju.acexplorer.main

import com.siju.acexplorer.main.helper.UpdateChecker

interface MainCommunicator {
    fun getUpdateChecker() : UpdateChecker?
}