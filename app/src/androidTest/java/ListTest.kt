
import android.content.Intent
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.filters.LargeTest
import net.ducksmanager.activity.IssueList
import net.ducksmanager.activity.PublicationList
import net.ducksmanager.whattheduck.R
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
        fun data(): List<Array<out LocaleWithDefaultPublication>> = parameterData()
    }

    @Before
    fun switchLocaleAndLogin() {
        loginActivityRule.launchActivity(Intent())
        switchLocale()

        login(DownloadHandlerMock.TEST_USER, DownloadHandlerMock.TEST_PASS)
    }

    @Test
    fun testPublicationList() {
        onView(withId(R.id.addToCollectionWrapper)).perform(ViewActions.click())
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

        onView(withId(R.id.addToCollectionWrapper)).perform(ViewActions.click())

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
}