import android.support.test.espresso.action.ViewActions;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import net.ducksmanager.whattheduck.CountryList;
import net.ducksmanager.whattheduck.R;

import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class LoginTest extends WtdTest {

    @Test
    public void testLogin() {
        onView(withId(R.id.username))
            .perform(clearText())
            .perform(typeText(DownloadHandlerMock.TEST_USER), closeSoftKeyboard());

        onView(withId(R.id.password))
            .perform(clearText())
            .perform(typeText(DownloadHandlerMock.TEST_PASS), closeSoftKeyboard());
        onView(withId(R.id.login)).perform(ViewActions.click());

        assertCurrentActivityIsInstanceOf(CountryList.class, true);
    }

    @Test
    public void testLoginInvalidCredentials() {
        onView(withId(R.id.username))
            .perform(clearText())
            .perform(typeText(DownloadHandlerMock.TEST_USER), closeSoftKeyboard());

        onView(withId(R.id.password))
            .perform(clearText())
            .perform(typeText(DownloadHandlerMock.TEST_PASS + "_invalid"), closeSoftKeyboard());
        onView(withId(R.id.login)).perform(ViewActions.click());

        assertCurrentActivityIsInstanceOf(CountryList.class, false);
        assertToastShown(R.string.input_error__invalid_credentials);
    }
}
