import android.content.Intent
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import net.ducksmanager.whattheduck.*
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AddIssueTest : WtdTest() {
    @Before
    fun login() {
        loginActivityRule.launchActivity(Intent())
        login(DownloadHandlerMock.TEST_USER, DownloadHandlerMock.TEST_PASS)
    }

    @Test
    fun testShowAddIssueDialog() {
        assertCurrentActivityIsInstanceOf(CountryList::class.java, true)

        Espresso.onView(ViewMatchers.withText("France")).perform(ViewActions.click())
        assertCurrentActivityIsInstanceOf(PublicationList::class.java, true)

        Espresso.onView(ViewMatchers.withText(Matchers.containsString("dynastie"))).perform(ViewActions.click())
        assertCurrentActivityIsInstanceOf(IssueList::class.java, true)

        Espresso.onView(ViewMatchers.withId(R.id.addToCollectionBySelectionButton))
            .perform(forceFloatingActionButtonsVisible(true))
            .perform(ViewActions.click())

        Espresso.onView(ViewMatchers.withText("6")).perform(ViewActions.click())
        assertCurrentActivityIsInstanceOf(AddIssue::class.java, true)

        Espresso.onView(ViewMatchers.withId(R.id.addpurchase)).perform(ViewActions.click())

        Espresso.onView(ViewMatchers.withId(R.id.purchasetitlenew)).perform(ViewActions.typeText("Stockholm loppis"), ViewActions.closeSoftKeyboard())

        Espresso.onView(ViewMatchers.withId(R.id.createpurchase)).perform(ViewActions.click())

        Espresso.onView(Matchers.allOf(
            ViewMatchers.withText("Stockholm loppis"),
            ViewMatchers.withParent(ViewMatchers.withId(R.id.purchase_list))
        ))
    }
}