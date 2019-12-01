import android.app.Activity
import android.content.Intent
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.filters.LargeTest
import net.ducksmanager.util.CoverFlowActivity
import net.ducksmanager.util.CoverFlowFileHandler
import net.ducksmanager.whattheduck.R
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
@LargeTest
class CoverSearchTest(currentLocale: LocaleWithDefaultPublication?) : WtdTest(currentLocale) {
    @Before
    fun switchLocaleAndLogin() {
        loginActivityRule.launchActivity(Intent())
        switchLocale()
        login(DownloadHandlerMock.TEST_USER, DownloadHandlerMock.TEST_PASS)
    }

    @Test
    fun testCoverFlowResults() {
        CoverFlowFileHandler.mockedResource = mockServer!!.url("/internal/covers/2648").toString()

        val result = ActivityResult(Activity.RESULT_OK, Intent())
        val expectedIntent = IntentMatchers.hasAction(MediaStore.ACTION_IMAGE_CAPTURE)

        Intents.init()
        Intents.intending(expectedIntent).respondWith(result)

        Espresso.onView(ViewMatchers.withId(R.id.addToCollectionByPhotoButton))
            .perform(forceFloatingActionButtonsVisible(true))
            .perform(ViewActions.click())

        Intents.intended(expectedIntent)
        Intents.release()
        try {
            Thread.sleep(500)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        ScreenshotTestRule.takeScreenshot("Cover search result", activityInstance, screenshotPath)
        Espresso.onView(Matchers.allOf(ViewMatchers.withId(R.id.image), coverCurrentlyVisible())).perform(ViewActions.click())
        ScreenshotTestRule.takeScreenshot("Cover search result - add issue", activityInstance, screenshotPath)
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun data(): Iterable<Array<Any>> {
            return parameterData()
        }

        private fun coverCurrentlyVisible(): Matcher<Any> {
            return object : BoundedMatcher<Any, ImageView>(ImageView::class.java) {
                public override fun matchesSafely(item: ImageView): Boolean {
                    return CoverFlowActivity.currentSuggestion!!.coverSearchIssue.coverUrl == item.tag
                        && item.visibility == View.VISIBLE
                }

                override fun describeTo(description: Description) {
                    description.appendText("Currently visible cover")
                }
            }
        }
    }
}