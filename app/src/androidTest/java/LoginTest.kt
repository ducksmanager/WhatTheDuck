import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import net.ducksmanager.whattheduck.CountryList
import net.ducksmanager.whattheduck.R
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class LoginTest : WtdTest() {
    @Before
    fun initActivity() {
        loginActivityRule.launchActivity(Intent())
    }

    @Test
    fun testLogin() {
        login(DownloadHandlerMock.TEST_USER, DownloadHandlerMock.TEST_PASS)
        assertCurrentActivityIsInstanceOf(CountryList::class.java, true)
    }

    @Test
    fun testLoginInvalidCredentials() {
        login(DownloadHandlerMock.TEST_USER, DownloadHandlerMock.TEST_PASS + "_invalid")
        assertCurrentActivityIsInstanceOf(CountryList::class.java, false)
        assertToastShown(R.string.input_error__invalid_credentials)
    }
}