

import android.app.Activity
import android.support.test.InstrumentationRegistry.getInstrumentation
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.action.ViewActions.*
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.intent.Checks.checkNotNull
import android.support.test.espresso.matcher.BoundedMatcher
import android.support.test.espresso.matcher.RootMatchers.withDecorView
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnitRunner
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import android.support.test.runner.lifecycle.Stage
import android.view.View
import com.github.clans.fab.FloatingActionButton
import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertTrue
import net.ducksmanager.util.Settings
import net.ducksmanager.whattheduck.IssueList
import net.ducksmanager.whattheduck.R
import net.ducksmanager.whattheduck.WhatTheDuck
import net.ducksmanager.whattheduck.WhatTheDuckApplication
import okhttp3.mockwebserver.MockWebServer
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import java.util.*

open class WtdTest : AndroidJUnitRunner {

    val screenshotPath: String
        get() = ScreenshotTestRule.SCREENSHOTS_PATH_SHOWCASE + "/" + currentLocale.locale.language

    @Rule
    var screenshotTestRule = ScreenshotTestRule()

    @Rule
    val whatTheDuckActivityRule = ActivityTestRule<WhatTheDuck>(WhatTheDuck::class.java)

    abstract class LocaleWithDefaultPublication(country: String, language: String) {
        val locale: Locale = Locale(language, country)

        val defaultCountry: String
            get() = locale.country

        internal abstract val defaultPublication: String

    }

    @Before
    fun resetListType() {
        IssueList.viewType = IssueList.ViewType.LIST_VIEW
    }

    fun switchLocale() {
        val context = WhatTheDuck.wtd!!.applicationContext
        val resources = context.resources
        val configuration = resources.configuration
        configuration.setLocale(currentLocale.locale)
        resources.updateConfiguration(configuration, resources.displayMetrics)
    }

    fun overwriteSettingsAndHideMessages(vararg alreadyShownMessageKeys: String) {
        WhatTheDuck.wtd!!.deleteFile(Settings.USER_SETTINGS)
        Settings.loadUserSettings() // Reset settings
        for (alreadyShownMessageKey in alreadyShownMessageKeys) {
            Settings.addToMessagesAlreadyShown(alreadyShownMessageKey)
        }
    }

    constructor()

    constructor(currentLocale: LocaleWithDefaultPublication) {
        WtdTest.currentLocale = currentLocale
    }

    fun assertToastShown(textId: Int) {
        onView(withText(textId))
                .inRoot(withDecorView(not(`is`(whatTheDuckActivityRule.activity.window.decorView))))
                .check(matches(isDisplayed()))
    }

    fun assertCurrentActivityIsInstanceOf(activityClass: Class<out Activity>, assertTrue: Boolean?) {
        val currentActivity = activityInstance
        checkNotNull(currentActivity)
        checkNotNull(activityClass)
        val isInstance = currentActivity.javaClass.isAssignableFrom(activityClass)
        if (assertTrue!!) {
            assertTrue(isInstance)
        } else {
            assertFalse(isInstance)
        }
    }

    companion object {

        fun parameterData(): Iterable<Array<Any>> {
            return Arrays.asList<Array<Any>>(
                arrayOf(object : LocaleWithDefaultPublication("se", "sv") {
                    override val defaultPublication: String
                        get() = "se/KAP"
                }),
                arrayOf(object : LocaleWithDefaultPublication("us", "en") {
                    override val defaultPublication: String
                        get() = "us/WDC"
                }),
                arrayOf(object : LocaleWithDefaultPublication("fr", "fr") {
                    override val defaultPublication: String
                    get() = "fr/DDD"
                })
            )
        }

        var currentLocale: LocaleWithDefaultPublication = object : LocaleWithDefaultPublication("us", "en") {
            override val defaultPublication: String
                get() = "us/WDC"
        }

        lateinit var mockServer: MockWebServer

        @BeforeClass
        fun overrideUserSettingsPath() {
            Settings.USER_SETTINGS = "settings_test.properties"
        }

        @BeforeClass
        fun initDownloadHelper() {
            initMockServer()
        }

        fun initMockServer() {
            mockServer = MockWebServer()
            mockServer.setDispatcher(DownloadHandlerMock.dispatcher)
            WhatTheDuckApplication.config!!.setProperty(
                    WhatTheDuckApplication.CONFIG_KEY_DM_URL,
                    mockServer.url("/dm/").toString()
            )
            WhatTheDuckApplication.config!!.setProperty(
                    WhatTheDuckApplication.CONFIG_KEY_API_ENDPOINT_URL,
                    mockServer.url("/dm-server/").toString()
            )
            WhatTheDuckApplication.config!!.setProperty(
                    WhatTheDuckApplication.CONFIG_KEY_EDGES_URL,
                    mockServer.url("/edges/").toString()
            )
        }

        fun login(user: String, password: String) {
            onView(withId(R.id.username))
                    .perform(clearText())
                    .perform(typeText(user), closeSoftKeyboard())

            onView(withId(R.id.password))
                    .perform(clearText())
                    .perform(typeText(password), closeSoftKeyboard())
            onView(withId(R.id.login)).perform(ViewActions.click())
        }

        val activityInstance: Activity
            get() {
                val currentActivity = arrayOfNulls<Activity>(1)
                getInstrumentation().runOnMainSync {
                    val resumedActivities = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED)
                    currentActivity[0] = resumedActivities.iterator().next()
                }

                return currentActivity[0]!!
            }

        fun forceFloatingActionButtonsVisible(): Matcher<Any> {
            return object : BoundedMatcher<Any, FloatingActionButton>(FloatingActionButton::class.java) {
                public override fun matchesSafely(item: FloatingActionButton): Boolean {
                    item.visibility = View.VISIBLE
                    return true
                }

                override fun describeTo(description: Description) {
                    description.appendText("Force floating action buttons to be visible")
                }
            }
        }
    }
}
