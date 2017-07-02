import android.app.Activity;
import android.support.test.espresso.action.ViewActions;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import android.support.test.runner.lifecycle.Stage;

import net.ducksmanager.whattheduck.CountryList;
import net.ducksmanager.whattheduck.R;
import net.ducksmanager.whattheduck.RetrieveTask;
import net.ducksmanager.whattheduck.WhatTheDuck;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collection;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Checks.checkNotNull;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class LoginTest {

    private String mStringToBetyped;

    @Rule
    public ActivityTestRule<WhatTheDuck> whatTheDuckActivityRule = new ActivityTestRule<>(WhatTheDuck.class);

    @Before
    public void initDownloadHelper() {
        RetrieveTask.downloadHandler = new DownloadHandlerMock();
        WhatTheDuck.setPassword(null);
    }

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

    private void assertToastShown(int textId) {
        onView(withText(textId))
            .inRoot(withDecorView(not(is(whatTheDuckActivityRule.getActivity().getWindow().getDecorView()))))
            .check(matches(isDisplayed()));
    }

    private void assertCurrentActivityIsInstanceOf(Class<? extends Activity> activityClass, Boolean assertTrue) {
        Activity currentActivity = getActivityInstance();
        checkNotNull(currentActivity);
        checkNotNull(activityClass);
        boolean isInstance = currentActivity.getClass().isAssignableFrom(activityClass);
        if (assertTrue) {
            assertTrue(isInstance);
        }
        else {
            assertFalse(isInstance);
        }
    }

    private Activity getActivityInstance() {
        final Activity[] currentActivity = new Activity[1];
        getInstrumentation().runOnMainSync(new Runnable() {
            public void run() {
                Collection<Activity> resumedActivities = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED);
                for (Activity act : resumedActivities) {
                    currentActivity[0] = act;
                    break;
                }
            }
        });

        return currentActivity[0];
    }
}
