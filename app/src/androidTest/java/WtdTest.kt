import android.app.Activity
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Checks
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnitRunner
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import net.ducksmanager.activity.CountryList
import net.ducksmanager.activity.IssueList
import net.ducksmanager.activity.Login
import net.ducksmanager.persistence.AppDatabase
import net.ducksmanager.whattheduck.R
import net.ducksmanager.whattheduck.WhatTheDuck
import okhttp3.mockwebserver.MockWebServer
import org.hamcrest.CoreMatchers
import org.junit.Assert
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import java.util.*

open class WtdTest : AndroidJUnitRunner {
    abstract class LocaleWithDefaultPublication(country: String, language: String) {
        val locale: Locale = Locale(language, country)
        val defaultCountry: String
            get() = locale.country

        abstract val defaultPublication: String

    }

    companion object {
        fun parameterData(): List<Array<out LocaleWithDefaultPublication>> {
            return listOf(
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
            }))
        }

        var currentLocale: LocaleWithDefaultPublication? = null
        var mockServer: MockWebServer? = null

        @BeforeClass
        @JvmStatic
        fun beforeClass() {
            initMockServer()
        }

        private fun initMockServer() {
            mockServer = MockWebServer()
            mockServer!!.dispatcher = DownloadHandlerMock.dispatcher

            WhatTheDuck.config = Properties()
            WhatTheDuck.config.setProperty(WhatTheDuck.CONFIG_KEY_ROLE_NAME, "test")
            WhatTheDuck.config.setProperty(WhatTheDuck.CONFIG_KEY_ROLE_PASSWORD, "test")

            WhatTheDuck.config.setProperty(
                WhatTheDuck.CONFIG_KEY_DM_URL,
                mockServer!!.url("/dm/").toString()
            )
            WhatTheDuck.config.setProperty(
                WhatTheDuck.CONFIG_KEY_API_ENDPOINT_URL,
                mockServer!!.url("/dm-server/").toString()
            )
            WhatTheDuck.config.setProperty(
                WhatTheDuck.CONFIG_KEY_EDGES_URL,
                mockServer!!.url("/edges/").toString()
            )
        }

        fun login(user: String?, password: String?) {
            onView(ViewMatchers.withId(R.id.username))
                .perform(ViewActions.clearText())
                .perform(ViewActions.typeText(user), closeSoftKeyboard())

            onView(ViewMatchers.withId(R.id.password))
                .perform(ViewActions.clearText())
                .perform(ViewActions.typeText(password), closeSoftKeyboard())

            onView(ViewMatchers.withId(R.id.login)).perform(ViewActions.click())
        }

        val activityInstance: Activity?
            get() {
                val currentActivity = arrayOfNulls<Activity>(1)
                getInstrumentation().runOnMainSync {
                    val resumedActivities = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED)
                    currentActivity[0] = resumedActivities.iterator().next()
                }
                return currentActivity[0]
            }
    }

    init {
        currentLocale = object : LocaleWithDefaultPublication("us", "en") {
            override val defaultPublication: String
                get() = "us/WDC"
        }
        WhatTheDuck.DB_NAME = "appDB_test"
        WhatTheDuck.isTestContext = true
    }

    val screenshotPath: String
        get() = ScreenshotTestRule.SCREENSHOTS_PATH_SHOWCASE + "/" + currentLocale!!.locale.language

    @get:Rule
    var screenshotTestRule = ScreenshotTestRule()

    @get:Rule
    val loginActivityRule = ActivityTestRule(
        Login::class.java,
        true,
        false
    )

    @Before
    fun resetDownloadMockState() {
        DownloadHandlerMock.state.remove("server_offline")
    }

    @Before
    fun resetDb() {
        WhatTheDuck.appDB = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @Before
    fun resetConfigs() {
        CountryList.hasFullList = false
        IssueList.viewType = IssueList.ViewType.LIST_VIEW
    }

    fun switchLocale() {
        if (currentLocale != null) {
            val resources = WhatTheDuck.applicationContext!!.resources
            val configuration = resources.configuration
            configuration.setLocale(currentLocale!!.locale)
            resources.updateConfiguration(configuration, resources.displayMetrics)
        }
    }

    constructor()

    constructor(currentLocale: LocaleWithDefaultPublication?) {
        Companion.currentLocale = currentLocale
    }

    fun assertToastShown(textId: Int) {
        onView(withText(textId))
            .inRoot(RootMatchers.withDecorView(CoreMatchers.not(CoreMatchers.`is`(loginActivityRule.activity.window.decorView))))
            .check(matches(ViewMatchers.isDisplayed()))
    }

    fun assertCurrentActivityIsInstanceOf(activityClass: Class<*>, assertTrue: Boolean) {
        Checks.checkNotNull(activityInstance)
        Checks.checkNotNull(activityClass)
        val isInstance = activityInstance!!::class.java.name == activityClass.name
        if (assertTrue) {
            Assert.assertTrue(isInstance)
        } else {
            Assert.assertFalse(isInstance)
        }
    }

    protected fun clickOnActionButton(buttonId: Int) {
        val viewMatcher = onView(ViewMatchers.withId(buttonId))
        try {
            Thread.sleep(500)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        viewMatcher.perform(ViewActions.click())
    }
}