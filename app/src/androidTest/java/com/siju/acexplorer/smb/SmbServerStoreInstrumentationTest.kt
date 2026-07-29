package com.siju.acexplorer.smb

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SmbServerStoreInstrumentationTest {

    @Test
    fun removeDeletesTestServerFromLiveStore() {
        val server = SmbSavedServer(
            host = "192.0.2.10",
            username = "ui-test",
            connectionType = SmbConnectionType.LAN
        )
        val store = SmbServerStore(InstrumentationRegistry.getInstrumentation().targetContext)
        store.remove(server)
        assertTrue(store.load().none {
            it.host == server.host && it.connectionType == server.connectionType
        })
    }
}
