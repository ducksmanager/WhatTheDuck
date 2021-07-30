
import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.LocaleList
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.intent.Checks
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnitRunner
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import net.ducksmanager.activity.Login
import net.ducksmanager.adapter.ItemAdapter
import net.ducksmanager.persistence.AppDatabase
import net.ducksmanager.whattheduck.R
import net.ducksmanager.whattheduck.WhatTheDuck
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.CONFIG_KEY_API_ENDPOINT_URL
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.CONFIG_KEY_DM_URL
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.CONFIG_KEY_EDGES_URL
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.CONFIG_KEY_ROLE_NAME
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.CONFIG_KEY_ROLE_PASSWORD
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.appDB
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.config
import okhttp3.mockwebserver.MockWebServer
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.junit.*
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

            WhatTheDuck.loadedConfig = Properties()
            config.setProperty(CONFIG_KEY_ROLE_NAME, "test")
            config.setProperty(CONFIG_KEY_ROLE_PASSWORD, "test")

            config.setProperty(
                CONFIG_KEY_DM_URL,
                mockServer!!.url("/dm/").toString()
            )
            config.setProperty(
                CONFIG_KEY_API_ENDPOINT_URL,
                mockServer!!.url("/dm-server/").toString()
            )
            config.setProperty(
                CONFIG_KEY_EDGES_URL,
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


        protected fun getItemMatcher(identifier: String): Matcher<RecyclerView.ViewHolder> {
            return object : BoundedMatcher<RecyclerView.ViewHolder, RecyclerView.ViewHolder>(ItemAdapter.ViewHolder::class.java) {
                override fun matchesSafely(item: RecyclerView.ViewHolder): Boolean = (item as ItemAdapter<*>.ViewHolder).titleTextView!!.tag == identifier

                override fun describeTo(description: Description) {
                    description.appendText("view holder with ID: $identifier")
                }
            }
        }
    }

    init {
        currentLocale = object : LocaleWithDefaultPublication("us", "en") {
            override val defaultPublication: String = "us/WDC"
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
    fun resetDb() {
        appDB = Room.inMemoryDatabaseBuilder(
            getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        WhatTheDuck.currentUser = null
    }

    @After
    fun closeDB() {
        appDB!!.close()
    }

    @Before
    fun resetConfigs() {
        WhatTheDuck.isOfflineMode = false
        DownloadHandlerMock.state["offlineMode"] = false
    }

    fun switchLocale() {
        if (currentLocale != null) {
            Locale.setDefault(currentLocale!!.locale)
            // here we update locale for app resources
            val context: Context = getApplicationContext()
            val res: Resources = context.resources
            val config: Configuration = res.configuration
            config.setLocales(LocaleList(currentLocale!!.locale))
            res.updateConfiguration(config, res.displayMetrics)
        }
    }

    constructor()

    constructor(currentLocale: LocaleWithDefaultPublication?) {
        Companion.currentLocale = currentLocale
    }

    fun assertToastShown(textId: Int) {
        onView(withText(textId))
            .inRoot(RootMatchers.withDecorView(not(`is`(loginActivityRule.activity.window.decorView))))
            .check(matches(isDisplayed()))
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

    protected fun goToPublicationListView(countryCode: String) {
        val countryMatcher = getItemMatcher(countryCode.toLowerCase())

        onView(ViewMatchers.withId(R.id.itemList))
            .perform(
                RecyclerViewActions.scrollToHolder(countryMatcher),
                RecyclerViewActions.actionOnHolderItem(countryMatcher, ViewActions.click())
            )
    }

    protected fun goToIssueListView(publicationCode: String) {
        val publicationMatcher = getItemMatcher(publicationCode)
        onView(ViewMatchers.withId(R.id.itemList))
            .perform(
                RecyclerViewActions.scrollToHolder(publicationMatcher),
                RecyclerViewActions.actionOnHolderItem(publicationMatcher, ViewActions.click())
            )
    }
}