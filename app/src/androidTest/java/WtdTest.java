import android.app.Activity;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.view.View;

import com.github.clans.fab.FloatingActionButton;

import net.ducksmanager.persistence.AppDatabase;
import net.ducksmanager.whattheduck.CountryList;
import net.ducksmanager.whattheduck.IssueList;
import net.ducksmanager.whattheduck.Login;
import net.ducksmanager.whattheduck.R;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;

import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.Properties;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.matcher.BoundedMatcher;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnitRunner;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import androidx.test.runner.lifecycle.Stage;
import okhttp3.mockwebserver.MockWebServer;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Checks.checkNotNull;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static net.ducksmanager.whattheduck.WhatTheDuck.CONFIG_KEY_API_ENDPOINT_URL;
import static net.ducksmanager.whattheduck.WhatTheDuck.CONFIG_KEY_DM_URL;
import static net.ducksmanager.whattheduck.WhatTheDuck.CONFIG_KEY_EDGES_URL;
import static net.ducksmanager.whattheduck.WhatTheDuck.CONFIG_KEY_ROLE_NAME;
import static net.ducksmanager.whattheduck.WhatTheDuck.CONFIG_KEY_ROLE_PASSWORD;
import static net.ducksmanager.whattheduck.WhatTheDuck.DB_NAME;
import static net.ducksmanager.whattheduck.WhatTheDuck.appDB;
import static net.ducksmanager.whattheduck.WhatTheDuck.applicationContext;
import static net.ducksmanager.whattheduck.WhatTheDuck.config;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;

class WtdTest extends AndroidJUnitRunner {

    static Iterable<Object[]> parameterData() {
        return Arrays.asList(new LocaleWithDefaultPublication[][]{
            {new LocaleWithDefaultPublication("se", "sv") {
                String getDefaultPublication() {
                    return "se/KAP";
                }
            }},
            {new LocaleWithDefaultPublication("us", "en") {
                String getDefaultPublication() {
                    return "us/WDC";
                }
            }},
            {new LocaleWithDefaultPublication("fr", "fr") {
                String getDefaultPublication() {
                    return "fr/DDD";
                }
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
            String getDefaultPublication() {
                return "us/WDC";
            }
        };
        DB_NAME = "appDB_test";
    }

    static MockWebServer mockServer;

    String getScreenshotPath() {
        return ScreenshotTestRule.SCREENSHOTS_PATH_SHOWCASE + "/" + currentLocale.getLocale().getLanguage();
    }

    @Rule
    public ScreenshotTestRule screenshotTestRule = new ScreenshotTestRule();

    @Rule
    public final ActivityTestRule<Login> loginActivityRule = new ActivityTestRule<>(
        Login.class,
        true,
        false
    );

    @BeforeClass
    public static void initDownloadHelper() {
        initMockServer();
    }

    @Before
    public void resetDownloadMockState() {
        DownloadHandlerMock.state.remove("server_offline");
    }

    @Before
    public void resetDb() {
        appDB = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), AppDatabase.class).allowMainThreadQueries().build();
    }

    @Before
    public void resetConfigs() {
        CountryList.hasFullList = false;
        IssueList.viewType = IssueList.ViewType.LIST_VIEW;
    }

    void switchLocale() {
        if (currentLocale != null) {
            Resources resources = applicationContext.getResources();
            Configuration configuration = resources.getConfiguration();
            configuration.setLocale(currentLocale.getLocale());
            resources.updateConfiguration(configuration, resources.getDisplayMetrics());
        }
    }

    WtdTest() {
    }

    WtdTest(LocaleWithDefaultPublication currentLocale) {
        WtdTest.currentLocale = currentLocale;
    }

    private static void initMockServer() {
        mockServer = new MockWebServer();
        mockServer.setDispatcher(DownloadHandlerMock.dispatcher);

        config = new Properties();
        config.setProperty(CONFIG_KEY_ROLE_NAME, "test");
        config.setProperty(CONFIG_KEY_ROLE_PASSWORD, "test");
        config.setProperty(
            CONFIG_KEY_DM_URL,
            mockServer.url("/dm/").toString()
        );
        config.setProperty(
            CONFIG_KEY_API_ENDPOINT_URL,
            mockServer.url("/dm-server/").toString()
        );
        config.setProperty(
            CONFIG_KEY_EDGES_URL,
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
            .inRoot(withDecorView(not(is(loginActivityRule.getActivity().getWindow().getDecorView()))))
            .check(matches(isDisplayed()));
    }

    void assertCurrentActivityIsInstanceOf(Class<? extends Activity> activityClass, Boolean assertTrue) {
        Activity currentActivity = getActivityInstance();
        checkNotNull(currentActivity);
        checkNotNull(activityClass);
        boolean isInstance = currentActivity.getClass().isAssignableFrom(activityClass);
        if (assertTrue) {
            assertTrue(isInstance);
        } else {
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

    static ViewAction forceFloatingActionButtonsVisible(final boolean value) {
        return new ViewAction() {

            @Override
            public Matcher<View> getConstraints() {
                return isAssignableFrom(FloatingActionButton.class);
            }

            @Override
            public void perform(UiController uiController, View view) {
                view.setVisibility(value ? View.VISIBLE : View.GONE);
            }

            @Override
            public String getDescription() {
                return "Force floating action buttons to be visible";
            }
        };
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
