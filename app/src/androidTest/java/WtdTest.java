import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.matcher.BoundedMatcher;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnitRunner;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import android.support.test.runner.lifecycle.Stage;
import android.view.View;

import com.github.clans.fab.FloatingActionButton;

import net.ducksmanager.util.Settings;
import net.ducksmanager.whattheduck.R;
import net.ducksmanager.whattheduck.WhatTheDuck;
import net.ducksmanager.whattheduck.WhatTheDuckApplication;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.BeforeClass;
import org.junit.Rule;

import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;

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

    static Iterable<Object[]> parameterData() {
        return Arrays.asList(new LocaleWithDefaultPublication[][]{
            {new LocaleWithDefaultPublication("se", "sv") {
                String getDefaultPublication() { return "se/KAP"; }
            }},
            {new LocaleWithDefaultPublication("us", "en") {
                String getDefaultPublication() { return "us/WDC"; }
            }},
            {new LocaleWithDefaultPublication("fr", "fr") {
                String getDefaultPublication() { return "fr/DDD"; }
            }}
        });
    }

    abstract static class LocaleWithDefaultPublication {
        final Locale locale;

        LocaleWithDefaultPublication(String country, String language) {
            this.locale = new Locale(language, country);
        }

        String getDefaultCountry() {
            return locale.getCountry();
        }

        Locale getLocale() {
            return locale;
        }

        abstract String getDefaultPublication();
    }

    static LocaleWithDefaultPublication currentLocale;
    static {
        currentLocale = new LocaleWithDefaultPublication("us", "en") {
            String getDefaultPublication() { return "us/WDC"; }
        };
    }

    static MockWebServer mockServer;

    String getScreenshotPath() {
        return ScreenshotTestRule.SCREENSHOTS_PATH_SHOWCASE + "/" + currentLocale.getLocale().getLanguage();
    }

    @Rule
    public ScreenshotTestRule screenshotTestRule = new ScreenshotTestRule();

    @Rule
    public final ActivityTestRule<WhatTheDuck> whatTheDuckActivityRule = new ActivityTestRule<>(WhatTheDuck.class);

    @BeforeClass
    public static void overrideUserSettingsPath() {
        Settings.USER_SETTINGS = "settings_test.properties";
    }

    @BeforeClass
    public static void initDownloadHelper() {
        initMockServer();
    }

    void switchLocale() {
        if (currentLocale != null) {
            Context context = WhatTheDuck.wtd.getApplicationContext();
            Resources resources = context.getResources();
            Configuration configuration = resources.getConfiguration();
            configuration.setLocale(currentLocale.getLocale());
            resources.updateConfiguration(configuration, resources.getDisplayMetrics());
        }
    }

    WtdTest() { }

    WtdTest(LocaleWithDefaultPublication currentLocale) {
        WtdTest.currentLocale = currentLocale;
    }

    static void initMockServer() {
        mockServer = new MockWebServer();
        mockServer.setDispatcher(DownloadHandlerMock.dispatcher);
        WhatTheDuckApplication.config.setProperty(
            WhatTheDuckApplication.CONFIG_KEY_DM_URL,
            mockServer.url("/dm/").toString()
        );
        WhatTheDuckApplication.config.setProperty(
            WhatTheDuckApplication.CONFIG_KEY_API_ENDPOINT_URL,
            mockServer.url("/dm-server/").toString()
        );
        WhatTheDuckApplication.config.setProperty(
            WhatTheDuckApplication.CONFIG_KEY_EDGES_URL,
            mockServer.url("/edges/").toString()
        );
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

    static Matcher<Object> forceFloatingActionButtonsVisible() {
        return new BoundedMatcher<Object, FloatingActionButton>(FloatingActionButton.class) {
            @Override
            public boolean matchesSafely(final FloatingActionButton item) {
                item.setVisibility(View.VISIBLE);
                return true;
            }

            @Override
            public void describeTo(final Description description) {
                description.appendText("Force floating action buttons to be visible");
            }
        };
    }
}
