
import android.content.Intent
import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import net.ducksmanager.activity.CountryList
import net.ducksmanager.activity.Login
import net.ducksmanager.whattheduck.R
import org.hamcrest.CoreMatchers.not
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class OfflineModeTest(currentLocale: LocaleWithDefaultPublication?) : WtdTest(currentLocale) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun data(): List<Array<out LocaleWithDefaultPublication>> = parameterData()
    }

    @Test
    fun testOfflineLoginWithoutOfflineData() {
        DownloadHandlerMock.state["offlineMode"] = true
        loginActivityRule.launchActivity(Intent())
        switchLocale()

        login(DownloadHandlerMock.TEST_USER, DownloadHandlerMock.TEST_PASS)
        assertCurrentActivityIsInstanceOf(Login::class.java, true)
    }

    @Test
    fun testOfflineLoginWithOfflineData() {
        loginActivityRule.launchActivity(Intent())
        switchLocale()

        login(DownloadHandlerMock.TEST_USER, DownloadHandlerMock.TEST_PASS)
        assertCurrentActivityIsInstanceOf(CountryList::class.java, true)

        DownloadHandlerMock.state["offlineMode"] = true
        goToPublicationListView("fr")
        goToIssueListView("fr/DDD")

        Espresso.onView(ViewMatchers.withId(R.id.offlineMode)).check(matches(isDisplayed()))
        Espresso.onView(ViewMatchers.withId(R.id.addToCollectionWrapper)).check(matches(not(isDisplayed())))
    }
}