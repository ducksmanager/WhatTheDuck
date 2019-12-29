
import android.content.Intent
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.filters.LargeTest
import net.ducksmanager.activity.IssueList
import net.ducksmanager.activity.PublicationList
import net.ducksmanager.adapter.ItemAdapter
import net.ducksmanager.whattheduck.R
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
@LargeTest
class ListTest(currentLocale: LocaleWithDefaultPublication?) : WtdTest(currentLocale) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun data(): Iterable<Array<Any>> {
            return parameterData()
        }

        private fun getItemMatcher(identifier: String): Matcher<RecyclerView.ViewHolder> {
            return object : BoundedMatcher<RecyclerView.ViewHolder, RecyclerView.ViewHolder>(ItemAdapter.ViewHolder::class.java) {
                override fun matchesSafely(item: RecyclerView.ViewHolder): Boolean {
                    return (item as ItemAdapter<*>.ViewHolder).titleTextView!!.tag == identifier
                }

                override fun describeTo(description: Description) {
                    description.appendText("view holder with ID: $identifier")
                }
            }
        }
    }

    @Before
    fun switchLocaleAndLogin() {
        loginActivityRule.launchActivity(Intent())
        switchLocale()

        login(DownloadHandlerMock.TEST_USER, DownloadHandlerMock.TEST_PASS)
    }

    @Test
    fun testPublicationList() {
        clickOnActionButton(R.id.addToCollectionBySelectionButton)

        goToPublicationListView(currentLocale!!.defaultCountry)

        assertCurrentActivityIsInstanceOf(PublicationList::class.java, true)
        ScreenshotTestRule.takeScreenshot("Select issue - Publication list", activityInstance, screenshotPath)
    }

    @Test
    fun testIssueList() {
        goToPublicationListView(currentLocale!!.defaultCountry)
        goToIssueListView(currentLocale!!.defaultPublication)

        assertCurrentActivityIsInstanceOf(IssueList::class.java, true)

        onView(allOf(withId(R.id.addToCollectionByPhotoButton), forceFloatingActionButtonsVisible()))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        onView(allOf(withId(R.id.addToCollectionBySelectionButton), forceFloatingActionButtonsVisible()))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        ScreenshotTestRule.takeScreenshot("Collection - Issue list", activityInstance, screenshotPath)
    }

    @Test
    fun testIssueListEdgeView() {
        goToPublicationListView("fr")
        goToIssueListView("fr/DDD")

        onView(withId(R.id.switchView))
            .perform(ViewActions.click())

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

        onView(withId(R.id.itemList))
            .perform(
                RecyclerViewActions.scrollToHolder(countryMatcher),
                RecyclerViewActions.actionOnHolderItem(countryMatcher, ViewActions.click())
            )
    }

    private fun goToIssueListView(publicationCode: String) {
        val publicationMatcher = getItemMatcher(publicationCode)
        onView(withId(R.id.itemList))
            .perform(
                RecyclerViewActions.scrollToHolder(publicationMatcher),
                RecyclerViewActions.actionOnHolderItem(publicationMatcher, ViewActions.click())
            )
    }
}