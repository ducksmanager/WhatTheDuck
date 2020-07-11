
import android.content.Intent
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import net.ducksmanager.activity.IssueList
import net.ducksmanager.whattheduck.R
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.containsString
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AddIssuesTest : WtdTest() {
    @Before
    fun login() {
        loginActivityRule.launchActivity(Intent())
        login(DownloadHandlerMock.TEST_USER, DownloadHandlerMock.TEST_PASS)
    }

    @Test
    fun testShowAddIssueDialog() {

        onView(
            allOf(withId(R.id.itemtitle), withText(containsString("Fr")), isDisplayed())
        ).perform(ViewActions.click())

        onView(withText(containsString("dynastie"))).perform(ViewActions.click())
        assertCurrentActivityIsInstanceOf(IssueList::class.java, true)

        onView(withId(R.id.addToCollectionWrapper)).perform(ViewActions.click())

        clickOnActionButton(R.id.addToCollectionBySelectionButton)

        onView(withText("6")).perform(ViewActions.click())

        onView(withId(R.id.validateSelection)).perform(ViewActions.click())

        onView(withId(R.id.addpurchase)).perform(ViewActions.click())

        onView(withId(R.id.purchasetitlenew)).perform(ViewActions.typeText("Stockholm loppis"), closeSoftKeyboard())

        onView(withId(R.id.createpurchase)).perform(ViewActions.click())

        onView(allOf(
            withText("Stockholm loppis"),
            withParent(withId(R.id.purchase_list))
        ))
    }
}