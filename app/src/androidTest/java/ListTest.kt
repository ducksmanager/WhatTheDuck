import android.support.test.espresso.matcher.BoundedMatcher
import android.support.test.filters.LargeTest
import android.support.v7.widget.RecyclerView

import net.ducksmanager.util.ReleaseNotes
import net.ducksmanager.util.Settings
import net.ducksmanager.whattheduck.IssueList
import net.ducksmanager.whattheduck.ItemAdapter
import net.ducksmanager.whattheduck.PublicationList
import net.ducksmanager.whattheduck.R

import org.hamcrest.Description
import org.hamcrest.Matcher
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.contrib.RecyclerViewActions.actionOnHolderItem
import android.support.test.espresso.contrib.RecyclerViewActions.scrollToHolder
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.view.View
import org.hamcrest.Matchers.allOf

@RunWith(Parameterized::class)
@LargeTest
class ListTest(currentLocale: WtdTest.LocaleWithDefaultPublication) : WtdTest(currentLocale) {

    companion object {
        @Parameterized.Parameters
        fun data(): Iterable<Array<Any>> {
            return WtdTest.parameterData()
        }
    }

    @Before
    fun switchLocaleAndLogin() {
        switchLocale()
        overwriteSettingsAndHideMessages(
                Settings.MESSAGE_KEY_WELCOME,
                Settings.MESSAGE_KEY_WELCOME_BOOKCASE_VIEW,
                Settings.MESSAGE_KEY_DATA_CONSUMPTION,
                ReleaseNotes.current.messageId.toString()
        )
        WtdTest.login(DownloadHandlerMock.TEST_USER, DownloadHandlerMock.TEST_PASS)
    }

    @Before
    fun resetDownloadMockState() {
        DownloadHandlerMock.state.remove("server_offline")
    }

    @Test
    fun testPublicationList() {
        onView(allOf<View>(withId(R.id.addToCollectionBySelectionButton), WtdTest.forceFloatingActionButtonsVisible()))
                .perform(click())

        goToPublicationListView(WtdTest.currentLocale.defaultCountry)
        assertCurrentActivityIsInstanceOf(PublicationList::class.java, true)

        ScreenshotTestRule.takeScreenshot("Select issue - Publication list", activityInstance, screenshotPath)
    }

    @Test
    fun testIssueList() {
        goToPublicationListView(WtdTest.currentLocale.defaultCountry)
        goToIssueListView(WtdTest.currentLocale.defaultPublication)

        assertCurrentActivityIsInstanceOf(IssueList::class.java, true)

        onView(allOf<View>(withId(R.id.addToCollectionByPhotoButton), WtdTest.forceFloatingActionButtonsVisible())).check(matches(isDisplayed()))
        onView(allOf<View>(withId(R.id.addToCollectionBySelectionButton), WtdTest.forceFloatingActionButtonsVisible())).check(matches(isDisplayed()))

        ScreenshotTestRule.takeScreenshot("Collection - Issue list", activityInstance, screenshotPath)
    }

    @Test
    fun testIssueListEdgeView() {
        goToPublicationListView("fr")
        goToIssueListView("fr/DDD")

        onView(withId(R.id.switchView)).perform(click())

        assertCurrentActivityIsInstanceOf(IssueList::class.java, true)

        try {
            Thread.sleep(500)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        ScreenshotTestRule.takeScreenshot("Collection - Issue list - edge view", activityInstance, screenshotPath)
    }

    private fun goToPublicationListView(countryCode: String) {
        val countryMatcher = getItemMatcher(countryCode.toLowerCase())
        onView(withId(R.id.itemList)).perform(scrollToHolder<RecyclerView.ViewHolder>(countryMatcher), actionOnHolderItem<RecyclerView.ViewHolder>(countryMatcher, click()))
    }

    private fun goToIssueListView(publicationCode: String) {
        val publicationMatcher = getItemMatcher(publicationCode)
        onView(withId(R.id.itemList)).perform(scrollToHolder<RecyclerView.ViewHolder>(publicationMatcher), actionOnHolderItem<RecyclerView.ViewHolder>(publicationMatcher, click()))
    }

    private fun getItemMatcher(identifier: String): Matcher<RecyclerView.ViewHolder> {
        return object : BoundedMatcher<RecyclerView.ViewHolder, RecyclerView.ViewHolder>(RecyclerView.ViewHolder::class.java) {
            override fun matchesSafely(item: RecyclerView.ViewHolder): Boolean {
                // TODO FIXME
                return item.titleTextView!!.getTag() == identifier
            }

            override fun describeTo(description: Description) {
                description.appendText("view holder with ID: $identifier")
            }
        }
    }
}
