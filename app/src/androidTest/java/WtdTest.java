import android.app.Activity;
import android.support.test.espresso.action.ViewActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnitRunner;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import android.support.test.runner.lifecycle.Stage;

import net.ducksmanager.whattheduck.R;
import net.ducksmanager.whattheduck.WhatTheDuck;
import net.ducksmanager.whattheduck.WhatTheDuckApplication;

import org.junit.BeforeClass;
import org.junit.Rule;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockWebServer;

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

class WtdTest extends AndroidJUnitRunner {
    static HashMap<String, MockWebServer> mockServers = new HashMap<>();

    @Rule
    public ScreenshotTestRule screenshotTestRule = new ScreenshotTestRule();

    @Rule
    public final ActivityTestRule<WhatTheDuck> whatTheDuckActivityRule = new ActivityTestRule<>(WhatTheDuck.class);

    @BeforeClass
    public static void overrideUserSettingsPath() {
        WhatTheDuck.USER_SETTINGS = "settings_test.properties";
    }

    static void initMockServers() {
        HashMap<String, Dispatcher> dmDispatcher = new HashMap<>();
        dmDispatcher.put(WhatTheDuckApplication.CONFIG_KEY_DM_URL, DownloadHandlerMock.dispatcherForDm);

        HashMap<String, Dispatcher> dmServerDispatcher = new HashMap<>();
        dmServerDispatcher.put(WhatTheDuckApplication.CONFIG_KEY_API_ENDPOINT_URL, DownloadHandlerMock.dispatcherForDmServer);

        HashMap<String, HashMap<String, Dispatcher>> mockServerDispatchers = new HashMap<>();
        mockServerDispatchers.put("dm", dmDispatcher);
        mockServerDispatchers.put("dm-server", dmServerDispatcher);

        for (String dispatcherName : mockServerDispatchers.keySet()) {
            MockWebServer mockServer = new MockWebServer();
            try {
                mockServer.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
            for (String configKey : mockServerDispatchers.get(dispatcherName).keySet()) {
                WhatTheDuckApplication.config.setProperty(configKey, mockServer.url("/").url().toString());
                mockServer.setDispatcher(mockServerDispatchers.get(dispatcherName).get(configKey));
            }

            mockServers.put(dispatcherName, mockServer);
        }
    }

    static void login(String user, String password) {
        onView(withId(R.id.username))
            .perform(clearText())
            .perform(typeText(user), closeSoftKeyboard());

        onView(withId(R.id.password))
            .perform(clearText())
            .perform(typeText(password), closeSoftKeyboard());
        onView(withId(R.id.login)).perform(ViewActions.click());
    }

    void assertToastShown(int textId) {
        onView(withText(textId))
            .inRoot(withDecorView(not(is(whatTheDuckActivityRule.getActivity().getWindow().getDecorView()))))
            .check(matches(isDisplayed()));
    }

    void assertCurrentActivityIsInstanceOf(Class<? extends Activity> activityClass, Boolean assertTrue) {
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

    static Activity getActivityInstance() {
        final Activity[] currentActivity = new Activity[1];
        getInstrumentation().runOnMainSync(() -> {
            Collection<Activity> resumedActivities = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED);
            currentActivity[0] = resumedActivities.iterator().next();
        });

        return currentActivity[0];
    }
}
