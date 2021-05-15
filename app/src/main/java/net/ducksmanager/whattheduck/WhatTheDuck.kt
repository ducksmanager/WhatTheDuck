package net.ducksmanager.whattheduck

import android.app.Activity
import android.app.AlertDialog
import android.app.Application
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.widget.Toast
import androidx.room.Room
import com.pusher.pushnotifications.BeamsCallback
import com.pusher.pushnotifications.PushNotifications
import com.pusher.pushnotifications.PusherCallbackError
import com.pusher.pushnotifications.auth.BeamsTokenProvider
import com.squareup.picasso.Picasso
import com.squareup.picasso.PicassoCache
import net.ducksmanager.api.DmServer.Companion.initApi
import net.ducksmanager.persistence.AppDatabase
import net.ducksmanager.persistence.models.coa.InducksCountryName
import net.ducksmanager.persistence.models.coa.InducksPublication
import net.ducksmanager.persistence.models.composite.UserSetting
import net.ducksmanager.persistence.models.dm.User
import org.matomo.sdk.Matomo
import org.matomo.sdk.Tracker
import org.matomo.sdk.TrackerBuilder
import org.matomo.sdk.extra.TrackHelper
import timber.log.Timber
import java.io.IOException
import java.io.InputStream
import java.lang.ref.WeakReference
import java.util.*
import kotlin.system.exitProcess

class WhatTheDuck : Application() {
    enum class CollectionType {
        COA, USER
    }

    companion object {
        private lateinit var instance: WhatTheDuck
        var loadedConfig: Properties? = null

        val config: Properties
            get() {
                if (loadedConfig == null) {
                    var reader: InputStream? = null
                    try {
                        if (instance.assets == null) {
                            return Properties()
                        }
                        reader = instance.assets.open(CONFIG)
                        loadedConfig = Properties()
                        loadedConfig!!.load(reader)
                    } catch (e: IOException) {
                        System.err.println("Config file not found, aborting")
                        exitProcess(-1)
                    } finally {
                        if (reader != null) {
                            try {
                                reader.close()
                            } catch (e: IOException) {
                                System.err.println("Error while reading config file, aborting")
                                exitProcess(-1)
                            }
                        }
                    }
                }
                return loadedConfig!!
            }
        var isTestContext = false
        var isOfflineMode = false
        var isNewVersionAvailable = false

        private const val CONFIG = "config.properties"
        private const val CONFIG_KEY_PUSHER_INSTANCE_ID = "pusher_instance_id"
        const val CONFIG_KEY_API_ENDPOINT_URL = "api_endpoint_url"
        const val CONFIG_KEY_APPFOLLOW_API_ENDPOINT_URL = "appfollow_api_endpoint_url"
        const val CONFIG_KEY_APPFOLLOW_API_USER = "appfollow_api_secret"
        const val CONFIG_KEY_DM_URL = "dm_url"
        const val CONFIG_KEY_ROLE_NAME = "role_name"
        const val CONFIG_KEY_ROLE_PASSWORD = "role_password"
        const val CONFIG_KEY_EDGES_URL = "edges_url"
        private const val CONFIG_KEY_MATOMO_URL = "matomo_url"
        private const val CONFIG_MATOMO_DIMENSION_USER = 1
        private const val CONFIG_MATOMO_DIMENSION_VERSION = 2

        lateinit var locale: String

        lateinit var applicationVersion: String
        lateinit var applicationPackage: String

        var connectivityManager: ConnectivityManager? = null

        var DB_NAME = "appDB"
        var appDB: AppDatabase? = null

        private var matomoTracker: Tracker? = null

        var currentUser: User? = null

        var selectedCountry: InducksCountryName? = null
        var selectedPublication: InducksPublication? = null
        var selectedIssues: MutableList<String> = mutableListOf()

        @JvmField
        var applicationContext: Context? = null

        var tokenProvider: BeamsTokenProvider? = null

        val dmUrl: String
            get() = config.getProperty(CONFIG_KEY_DM_URL)

        fun info(activity: WeakReference<Activity?>, titleId: Int, duration: Int) {
            Toast.makeText(activity.get(), titleId, duration).show()
        }

        fun alert(activityRef: WeakReference<Activity>, message: String) {
            val activity = activityRef.get()
            val builder = AlertDialog.Builder(activity)
            builder.setTitle(activity!!.getString(R.string.error))
            builder.setMessage(message)
            builder.create().show()
        }

        fun alert(activity: WeakReference<Activity>, messageId: Int) {
            alert(activity, activity.get()!!.getString(messageId))
        }

        fun alert(activityRef: WeakReference<Activity>, titleId: Int, messageId: Int) {
            val activity = activityRef.get()
            val builder = AlertDialog.Builder(activity)
            builder.setTitle(activity!!.getString(titleId))
            builder.setMessage(activity.getString(messageId))
            activity.runOnUiThread { builder.create().show() }
        }

        fun alert(activityRef: WeakReference<Activity>, titleId: Int, message: String) {
            val activity = activityRef.get()
            val builder = AlertDialog.Builder(activity)
            builder.setTitle(activity!!.getString(titleId))
            builder.setMessage(message)
            activity.runOnUiThread { builder.create().show() }
        }

        fun registerForNotifications(activityRef: WeakReference<Activity>) {
            if (!isTestContext) {
                try {
                    PushNotifications.start(activityRef.get(), config.getProperty(CONFIG_KEY_PUSHER_INSTANCE_ID))
                    PushNotifications.setUserId(currentUser!!.username, tokenProvider, object : BeamsCallback<Void, PusherCallbackError> {
                        override fun onSuccess(vararg values: Void) {
                            Timber.i("Successfully registered for notifications")
                        }

                        override fun onFailure(error: PusherCallbackError) {
                            Timber.i("PusherBeams : Pusher Beams authentication failed: %s", error.message)
                        }
                    })
                } catch (e: Exception) {
                    Timber.e("Pusher init failed : %s", e.message)
                }
            }
        }

        fun unregisterFromNotifications() {
            if (!isTestContext) {
                try {
                    PushNotifications.stop()
                    Timber.i("Successfully unregistered from notifications")
                } catch (e: Exception) {
                    Timber.e("Pusher init failed to stop : %s", e.message)
                }
                val setting = appDB!!.userSettingDao().findByKey(UserSetting.SETTING_KEY_NOTIFICATIONS_ENABLED)
                setting?.value = "0"
                if (setting != null) {
                    appDB!!.userSettingDao().update(setting)
                }
            }
        }

        @get:Synchronized
        private val tracker: Tracker?
            get() {
                if (matomoTracker != null) {
                    return matomoTracker
                }

                val matomoUrl = config.getProperty(CONFIG_KEY_MATOMO_URL) ?: return null
                try {
                    matomoTracker = TrackerBuilder.createDefault(matomoUrl, 2).build(Matomo.getInstance(applicationContext))
                } catch (e: RuntimeException) {
                    System.err.println("Couldn't initialize tracker")
                }
                return matomoTracker
            }

        val isMobileConnection: Boolean
            get() {
                if (connectivityManager == null) {
                    return false
                }
                return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    connectivityManager!!.activeNetworkInfo != null && connectivityManager!!.activeNetworkInfo.isConnected && connectivityManager!!.activeNetworkInfo.type == ConnectivityManager.TYPE_MOBILE
                } else {
                    connectivityManager!!.activeNetwork != null && connectivityManager!!.getNetworkCapabilities(connectivityManager!!.activeNetwork)?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ?: false
                }
            }

        fun trackEvent(text: String?) {
            println(text)
            if (tracker != null) {
                TrackHelper.track()
                    .event("category", "action")
                    .name(text)
                    .value(1.0f)
                    .with(tracker)
            }
        }
    }

    fun setup() {
        instance = this
        initApplicationVersion()
        initConnectivityManager()
        Companion.applicationContext = applicationContext
        locale = applicationContext.resources.configuration.locale.language

        if (!isTestContext) {
            appDB = Room.databaseBuilder(applicationContext, AppDatabase::class.java, DB_NAME)
                .allowMainThreadQueries()
                .addMigrations(
                    AppDatabase.MIGRATION_7_8,
                    AppDatabase.MIGRATION_8_9,
                    AppDatabase.MIGRATION_9_10,
                    AppDatabase.MIGRATION_10_11,
                    AppDatabase.MIGRATION_11_12
                )
                .fallbackToDestructiveMigration()
                .build()
        }

        initApi()
        PicassoCache.clearCache(Picasso.with(applicationContext))

    }

    private fun initConnectivityManager() {
        connectivityManager = applicationContext!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    private fun initApplicationVersion() {
        val manager = this.packageManager
        val info: PackageInfo = try {
            manager.getPackageInfo(this.packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            applicationVersion = "Unknown"
            return
        }
        applicationVersion = info.versionName
        applicationPackage = info.packageName
    }

    fun trackActivity(activity: Activity) {
        val t = TrackHelper.track()

        if (currentUser != null) {
            t.dimension(CONFIG_MATOMO_DIMENSION_USER, currentUser!!.username)
        }
        try {
            t.dimension(CONFIG_MATOMO_DIMENSION_VERSION, activity.packageManager.getPackageInfo(activity.packageName, 0).versionName)
        } catch (e: PackageManager.NameNotFoundException) {
            t.dimension(CONFIG_MATOMO_DIMENSION_VERSION, "Unknown")
        }
        if (tracker != null) {
            t
                .screen(activity.javaClass.simpleName)
                .title(activity.title.toString())
                .with(tracker)
        }
    }
}