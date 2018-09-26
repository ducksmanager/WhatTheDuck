

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.provider.MediaStore
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.intent.Intents
import android.support.test.espresso.intent.Intents.intended
import android.support.test.espresso.intent.Intents.intending
import android.support.test.espresso.intent.matcher.IntentMatchers.hasAction
import android.support.test.espresso.matcher.BoundedMatcher
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.filters.LargeTest
import android.view.View
import android.widget.ImageView
import net.ducksmanager.util.CoverFlowActivity
import net.ducksmanager.util.CoverFlowFileHandler
import net.ducksmanager.util.ReleaseNotes
import net.ducksmanager.util.Settings
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
class CoverSearchTest(currentLocale: WtdTest.LocaleWithDefaultPublication) : WtdTest(currentLocale) {

    @Before
    fun switchLocaleAndLogin() {
        switchLocale()
        overwriteSettingsAndHideMessages(Settings.MESSAGE_KEY_WELCOME, ReleaseNotes.current.messageId.toString())
        WtdTest.login(DownloadHandlerMock.TEST_USER, DownloadHandlerMock.TEST_PASS)
    }

    @Test
    fun testCoverFlowResults() {
        CoverFlowFileHandler.mockedResource = WtdTest.mockServer.url("/internal/covers/2648").toString()

        val resultData = Intent()
        val result = Instrumentation.ActivityResult(Activity.RESULT_OK, resultData)

        val expectedIntent = hasAction(MediaStore.ACTION_IMAGE_CAPTURE)
        Intents.init()
        intending(expectedIntent).respondWith(result)

        onView(allOf(withId(R.id.addToCollectionByPhotoButton), WtdTest.forceFloatingActionButtonsVisible())).perform(click())

        intended(expectedIntent)
        Intents.release()

        try {
            Thread.sleep(500)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        ScreenshotTestRule.takeScreenshot("Cover search result", activityInstance, screenshotPath)

        onView(allOf(withId(R.id.image), coverCurrentlyVisible()))
                .perform(click())

        ScreenshotTestRule.takeScreenshot("Cover search result - add issue", activityInstance, screenshotPath)
    }

    companion object {

        @Parameterized.Parameters
        fun data(): Iterable<Array<Any>> {
            return WtdTest.parameterData()
        }

        private fun coverCurrentlyVisible(): Matcher<Any> {
            return object : BoundedMatcher<Any, ImageView>(ImageView::class.java!!) {
                public override fun matchesSafely(item: ImageView): Boolean {
                    return CoverFlowActivity.currentCoverUrl == item.tag && item.visibility == View.VISIBLE
                }

                override fun describeTo(description: Description) {
                    description.appendText("Currently visible cover")
                }
            }
        }
    }
}
