import net.ducksmanager.whattheduck.CountryList;
import net.ducksmanager.whattheduck.R;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class LoginTest extends WtdTest {

    public LoginTest() {
        super();
    }

    @BeforeClass
    public static void initDownloadHelper() {
        initMockServer();
    }

    @Before
    public void overwriteSettings() {
        super.overwriteSettingsAndHideMessages();
    }

    @Test
    public void testLogin() {
        login(DownloadHandlerMock.TEST_USER, DownloadHandlerMock.TEST_PASS);

        assertCurrentActivityIsInstanceOf(CountryList.class, true);
    }

    @Test
    public void testLoginInvalidCredentials() {
        login(DownloadHandlerMock.TEST_USER, DownloadHandlerMock.TEST_PASS + "_invalid");

        assertCurrentActivityIsInstanceOf(CountryList.class, false);
        assertToastShown(R.string.input_error__invalid_credentials);
    }
}
