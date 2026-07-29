package com.siju.acexplorer.smb

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.siju.acexplorer.R
import com.siju.acexplorer.main.AceActivity
import com.siju.acexplorer.storage.modules.picker.types.PickerType
import com.siju.acexplorer.storage.modules.picker.view.PickerFragment
import org.junit.Test
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class CopyDestinationUiTest {

    private val testServer = SmbSavedServer(
        host = "192.0.2.10",
        username = "ui-test",
        connectionType = SmbConnectionType.LAN
    )

    @After
    fun removeTestServer() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val store = SmbServerStore(context)
        store.remove(testServer)
        assertTrue(store.load().none { server ->
            server.host == testServer.host && server.connectionType == testServer.connectionType
        })
    }

    @Test
    fun copyPickerShowsStorageAndSavedNetworkLocation() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        SmbServerStore(context).save(testServer, password = "", rememberPassword = false)

        ActivityScenario.launch(AceActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                PickerFragment.newInstance(PickerType.COPY)
                    .show(activity.supportFragmentManager, "copy_destination")
                activity.supportFragmentManager.executePendingTransactions()
            }

            onView(withText(R.string.dialog_title_browse)).check(matches(isDisplayed()))
            onView(withText(R.string.nav_menu_internal_storage)).check(matches(isDisplayed()))
            onView(withText("ui-test@192.0.2.10")).check(matches(isDisplayed()))
        }
    }
}
