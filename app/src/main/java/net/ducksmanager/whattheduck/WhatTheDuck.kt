package net.ducksmanager.whattheduck

import android.app.Activity
import android.app.AlertDialog
import android.app.Application
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.widget.Toast
import androidx.room.Room
import com.pusher.pushnotifications.BeamsCallback
import com.pusher.pushnotifications.PushNotifications
import com.pusher.pushnotifications.PusherCallbackError
import com.pusher.pushnotifications.auth.BeamsTokenProvider
import net.ducksmanager.api.DmServer.initApi
import net.ducksmanager.persistence.AppDatabase
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
        lateinit var config: Properties
        var isTestContext = false

        private const val CONFIG = "config.properties"
        const val CONFIG_KEY_PUSHER_INSTANCE_ID = "pusher_instance_id"
        const val CONFIG_KEY_API_ENDPOINT_URL = "api_endpoint_url"
        const val CONFIG_KEY_DM_URL = "dm_url"
        const val CONFIG_KEY_ROLE_NAME = "role_name"
        const val CONFIG_KEY_ROLE_PASSWORD = "role_password"
        const val CONFIG_KEY_EDGES_URL = "edges_url"
        private const val CONFIG_KEY_MATOMO_URL = "matomo_url"
        private const val CONFIG_MATOMO_DIMENSION_USER = 1
        private const val CONFIG_MATOMO_DIMENSION_VERSION = 2

        lateinit var locale: String

        lateinit var applicationVersion: String

        var DB_NAME = "appDB"
        lateinit var appDB: AppDatabase

        private var matomoTracker: Tracker? = null

        var selectedCountry: String? = null
        var selectedPublication: String? = null
        var selectedIssue: String? = null

        @JvmField
        var applicationContext: Context? = null

        var tokenProvider: BeamsTokenProvider? = null

        private fun loadConfig(assets: AssetManager) {
            var reader: InputStream? = null
            try {
                reader = assets.open(CONFIG)
                config = Properties()
                config.load(reader)
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

        val dmUrl: String
            get() = config.getProperty(CONFIG_KEY_DM_URL)

        fun isTestContext(apiEndpointUrl: String): Boolean {
            return apiEndpointUrl.startsWith("http://")
        }

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

        fun registerForNotifications(activityRef: WeakReference<Activity>) {
            if (!isTestContext) {
                try {
                    PushNotifications.start(activityRef.get(), config.getProperty(CONFIG_KEY_PUSHER_INSTANCE_ID))
                    PushNotifications.setUserId(appDB.userDao().currentUser!!.username, tokenProvider, object : BeamsCallback<Void, PusherCallbackError> {
                        override fun onSuccess(vararg values: Void) {
                            Timber.i("Successfully authenticated with Pusher Beams")
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

        @get:Synchronized
        private val tracker: Tracker?
            get() {
                if (matomoTracker != null) {
                    return matomoTracker
                }

                val matomoUrl = config.getProperty(CONFIG_KEY_MATOMO_URL)
                try {
                    matomoTracker = TrackerBuilder(matomoUrl, 2, "WhatTheDuck").build(Matomo.getInstance(applicationContext))
                } catch (e: RuntimeException) {
                    System.err.println("Couldn't initialize tracker")
                }
                return matomoTracker
            }

        val isMobileConnection: Boolean
            get() {
                val cm = applicationContext!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

                return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    cm.activeNetworkInfo != null && cm.activeNetworkInfo.isConnected && cm.activeNetworkInfo.type == ConnectivityManager.TYPE_MOBILE
                } else {
                    cm.activeNetwork != null && cm.getNetworkCapabilities(cm.activeNetwork).hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
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
        initApplicationVersion()
        Companion.applicationContext = applicationContext
        locale = applicationContext.resources.configuration.locale.language

        if (!isTestContext) {
            loadConfig(assets)
            appDB = Room.databaseBuilder(applicationContext, AppDatabase::class.java, DB_NAME)
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build()
        }

        initApi()

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
    }

    fun trackActivity(activity: Activity) {
        val t = TrackHelper.track()
        val user = appDB.userDao().currentUser

        if (user != null) {
            t.dimension(CONFIG_MATOMO_DIMENSION_USER, user.username)
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