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
package com.siju.acexplorer.welcome

import android.os.Environment

/**
 * Reports whether the app currently holds All Files Access.
 *
 * Exists so the welcome flow can be unit tested: [Environment.isExternalStorageManager] is a static
 * platform call that a JVM test cannot stub.
 */
interface StorageAccessChecker {
    fun isStorageAccessGranted(): Boolean
}

class SystemStorageAccessChecker : StorageAccessChecker {
    override fun isStorageAccessGranted(): Boolean = Environment.isExternalStorageManager()
}
