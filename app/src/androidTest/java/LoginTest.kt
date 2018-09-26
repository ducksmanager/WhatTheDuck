import android.support.test.filters.LargeTest
import android.support.test.runner.AndroidJUnit4

import net.ducksmanager.whattheduck.CountryList
import net.ducksmanager.whattheduck.R

import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class LoginTest : WtdTest() {

    @Before
    fun overwriteSettings() {
        super.overwriteSettingsAndHideMessages()
    }

    @Test
    fun testLogin() {
        WtdTest.login(DownloadHandlerMock.TEST_USER, DownloadHandlerMock.TEST_PASS)

        assertCurrentActivityIsInstanceOf(CountryList::class.java, true)
    }

    @Test
    fun testLoginInvalidCredentials() {
        WtdTest.login(DownloadHandlerMock.TEST_USER, DownloadHandlerMock.TEST_PASS + "_invalid")

        assertCurrentActivityIsInstanceOf(CountryList::class.java, false)
        assertToastShown(R.string.input_error__invalid_credentials)
    }

    companion object {

        @BeforeClass @JvmStatic
        fun initDownloadHelper() {
            WtdTest.initMockServer()
        }
    }
}
