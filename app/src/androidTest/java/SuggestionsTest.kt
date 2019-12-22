
import android.content.Intent
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.contrib.DrawerActions.open
import androidx.test.espresso.contrib.NavigationViewActions.navigateTo
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.filters.LargeTest
import net.ducksmanager.whattheduck.R
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
@LargeTest
class SuggestionsTest(currentLocale: LocaleWithDefaultPublication?) : WtdTest(currentLocale) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun data(): Iterable<Array<Any>> {
            return parameterData()
        }
    }

    @Before
    fun switchLocaleAndLogin() {
        loginActivityRule.launchActivity(Intent())
        switchLocale()

        login(DownloadHandlerMock.TEST_USER, DownloadHandlerMock.TEST_PASS)
    }

    @Test
    fun testNavigationDrawer() {
        onView(withId(R.id.drawerLayout))
            .perform(open())
        ScreenshotTestRule.takeScreenshot("Navigation drawer", activityInstance, screenshotPath)
    }

    @Test
    fun testSuggestionList() {
        onView(withId(R.id.drawerLayout))
            .perform(open())

        onView(withId(R.id.drawerNavigation))
            .perform(navigateTo(R.id.action_suggestions))

        try {
            Thread.sleep(500)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        ScreenshotTestRule.takeScreenshot("Suggestion list", activityInstance, screenshotPath)
    }
}