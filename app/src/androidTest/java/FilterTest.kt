

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.*
import android.support.test.espresso.assertion.ViewAssertions.doesNotExist
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.withText
import android.support.test.filters.LargeTest
import android.support.test.runner.AndroidJUnit4
import android.view.View
import net.ducksmanager.util.ReleaseNotes
import net.ducksmanager.util.Settings
import net.ducksmanager.whattheduck.CountryList
import net.ducksmanager.whattheduck.R
import org.hamcrest.Matchers.allOf
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class FilterTest : WtdTest() {

    @Before
    fun login() {
        overwriteSettingsAndHideMessages(Settings.MESSAGE_KEY_WELCOME, ReleaseNotes.current.messageId.toString())
        WtdTest.login(DownloadHandlerMock.TEST_USER, DownloadHandlerMock.TEST_PASS)
    }

    @Test
    fun testShowAddIssueDialog() {
        assertCurrentActivityIsInstanceOf(CountryList::class.java, true)
        onView(allOf<View>(withId(R.id.addToCollectionBySelectionButton), WtdTest.forceFloatingActionButtonsVisible()))
                .perform(click())

        assertCurrentActivityIsInstanceOf(CountryList::class.java, true)

        onView(withId(R.id.filter))
                .perform(typeText("Fr"), closeSoftKeyboard())

        onView(withText("France"))
        onView(withText("Italy")).check(doesNotExist())


    }

    companion object {

        @BeforeClass @JvmStatic
        fun initDownloadHelper() {
            WtdTest.initMockServer()
        }
    }
}
