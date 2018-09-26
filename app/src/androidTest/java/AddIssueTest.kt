

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.action.ViewActions.*
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.filters.LargeTest
import android.support.test.runner.AndroidJUnit4
import android.view.View
import net.ducksmanager.util.ReleaseNotes
import net.ducksmanager.util.Settings
import net.ducksmanager.whattheduck.*
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.containsString
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class AddIssueTest : WtdTest() {

    @Before
    fun login() {
        overwriteSettingsAndHideMessages(Settings.MESSAGE_KEY_WELCOME, ReleaseNotes.current.messageId.toString())
        WtdTest.login(DownloadHandlerMock.TEST_USER, DownloadHandlerMock.TEST_PASS)
    }

    @Test
    fun testShowAddIssueDialog() {
        assertCurrentActivityIsInstanceOf(CountryList::class.java, true)
        onView(withText("France")).perform(ViewActions.click())

        assertCurrentActivityIsInstanceOf(PublicationList::class.java, true)
        onView(withText(containsString("dynastie"))).perform(ViewActions.click())

        assertCurrentActivityIsInstanceOf(IssueList::class.java, true)
        onView(allOf<View>(withId(R.id.addToCollectionBySelectionButton), WtdTest.forceFloatingActionButtonsVisible()))
                .perform(click())
        onView(withText("6")).perform(ViewActions.click())

        assertCurrentActivityIsInstanceOf(AddIssue::class.java, true)

        onView(withId(R.id.addpurchase)).perform(click())

        onView(withId(R.id.purchasetitlenew)).perform(typeText("Stockholm loppis"), closeSoftKeyboard())
        onView(withId(R.id.createpurchase)).perform(click())

        onView(allOf<View>(withText("Stockholm loppis"), withParent(withId(R.id.purchase_list))))


    }

    companion object {

        @BeforeClass @JvmStatic
        fun initDownloadHelper() {
            WtdTest.initMockServer()
        }
    }
}
