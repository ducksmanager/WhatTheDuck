
import android.content.Intent
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import net.ducksmanager.activity.CountryList
import net.ducksmanager.whattheduck.R
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class FilterTest : WtdTest() {
    @Before
    fun login() {
        loginActivityRule.launchActivity(Intent())
        login(DownloadHandlerMock.TEST_USER, DownloadHandlerMock.TEST_PASS)
    }

    @Test
    fun testShowAddIssueDialog() {
        assertCurrentActivityIsInstanceOf(CountryList::class.java, true)

        clickOnActionButton(R.id.addToCollectionBySelectionButton)
        assertCurrentActivityIsInstanceOf(CountryList::class.java, true)

        onView(withId(R.id.filter))
            .perform(ViewActions.typeText("Fr"), ViewActions.closeSoftKeyboard())

        onView(withText("France"))

        onView(withText("Italy")).check(ViewAssertions.doesNotExist())
    }
}