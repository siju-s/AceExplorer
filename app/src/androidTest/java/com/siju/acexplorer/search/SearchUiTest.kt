package com.siju.acexplorer.search

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.siju.acexplorer.R
import com.siju.acexplorer.main.AceActivity
import org.junit.Test
import org.junit.runner.RunWith
import org.hamcrest.Matchers.not

@RunWith(AndroidJUnit4::class)
@LargeTest
class SearchUiTest {

    @Test
    fun searchShowsBrowseSectionsAndEmptyState() {
        ActivityScenario.launch(AceActivity::class.java).use {
            onView(withContentDescription(R.string.action_search)).perform(click())

            onView(withText(R.string.search_quick_access)).check(matches(isDisplayed()))
            onView(withText(R.string.search_browse_by_type)).check(matches(isDisplayed()))
            onView(withText(R.string.search_common_folders)).check(matches(isDisplayed()))
            onView(withText(R.string.search_empty_title)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun selectingFolderAfterReturningFromVideoSearchOpensFolderResults() {
        ActivityScenario.launch(AceActivity::class.java).use {
            onView(withContentDescription(R.string.action_search)).perform(click())
            onView(withId(R.id.chipVideos)).perform(click())

            pressBack()

            onView(withId(R.id.browseContent)).check(matches(isDisplayed()))
            onView(withId(R.id.chipScreenshot)).perform(click())
            onView(withId(R.id.browseContent)).check(matches(not(isDisplayed())))
            onView(withId(R.id.filesList)).check(matches(isDisplayed()))
        }
    }
}
