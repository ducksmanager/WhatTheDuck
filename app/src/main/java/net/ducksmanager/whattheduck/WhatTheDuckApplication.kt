package net.ducksmanager.whattheduck

import android.app.Activity
import android.app.Application
import android.content.pm.PackageManager
import android.content.res.AssetManager

import net.ducksmanager.util.Settings

import org.piwik.sdk.Piwik
import org.piwik.sdk.Tracker
import org.piwik.sdk.TrackerConfig
import org.piwik.sdk.extra.TrackHelper

import java.io.IOException
import java.io.InputStream
import java.util.Properties

class WhatTheDuckApplication : Application() {

    private var mPiwikTracker: Tracker? = null

    private val tracker: Tracker?
        @Synchronized get() {
            if (mPiwikTracker != null) {
                return mPiwikTracker
            }
            val piwikUrl = config!!.getProperty(CONFIG_KEY_PIWIK_URL)
            mPiwikTracker = Piwik.getInstance(this).newTracker(TrackerConfig(piwikUrl, 2, "WhatTheDuck"))
            return mPiwikTracker
        }

    override fun onCreate() {
        super.onCreate()
        loadConfig(assets)
    }

    fun trackActivity(activity: Activity) {
        val t = TrackHelper.track()

        val username = Settings.username
        if (username != null) {
            t.dimension(CONFIG_PIWIK_DIMENSION_USER, username)
            try {
                t.dimension(CONFIG_PIWIK_DIMENSION_VERSION, activity.packageManager.getPackageInfo(activity.packageName, 0).versionName)
            } catch (e: PackageManager.NameNotFoundException) {
                t.dimension(CONFIG_PIWIK_DIMENSION_VERSION, "Unknown")
            }

        }

        t
                .screen(activity.javaClass.getSimpleName())
                .title(activity.title.toString())
                .with(tracker!!)

    }

    fun trackEvent(name: String) {
        TrackHelper.track()
                .event("category", "action")
                .name(name)
                .value(1.0f)
                .with(tracker!!)
    }

    companion object {
        var config: Properties? = null

        private val CONFIG = "config.properties"
        val CONFIG_KEY_API_ENDPOINT_URL = "api_endpoint_url"
        val CONFIG_KEY_DM_URL = "dm_url"
        val CONFIG_KEY_SECURITY_PASSWORD = "security_password"
        val CONFIG_KEY_EDGES_URL = "edges_url"

        private val CONFIG_KEY_PIWIK_URL = "piwik_url"
        private val CONFIG_PIWIK_DIMENSION_USER = 1
        private val CONFIG_PIWIK_DIMENSION_VERSION = 2

        private fun loadConfig(assets: AssetManager) {
            var reader: InputStream? = null
            try {
                reader = assets.open(CONFIG)
                config = Properties()
                config!!.load(reader)
            } catch (e: IOException) {
                WhatTheDuck.wtd!!.alert(R.string.internal_error)
                System.err.println("Config file not found, aborting")
                System.exit(-1)
            } finally {
                if (reader != null) {
                    try {
                        reader.close()
                    } catch (e: IOException) {
                        WhatTheDuck.wtd!!.alert(R.string.internal_error)
                        System.err.println("Error while reading config file, aborting")
                        System.exit(-1)
                    }

                }
            }
        }
    }
}
