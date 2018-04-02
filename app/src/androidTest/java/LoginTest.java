import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import net.ducksmanager.whattheduck.CountryList;
import net.ducksmanager.whattheduck.R;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class LoginTest extends WtdTest {

    @BeforeClass
    public static void initDownloadHelper() {
        initMockServer();
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
