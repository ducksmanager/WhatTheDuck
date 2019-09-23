package net.ducksmanager.whattheduck;

import android.app.Activity;
import android.app.Application;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;

import net.ducksmanager.persistence.AppDatabase;
import net.ducksmanager.persistence.models.dm.User;

import org.matomo.sdk.Matomo;
import org.matomo.sdk.Tracker;
import org.matomo.sdk.TrackerBuilder;
import org.matomo.sdk.extra.TrackHelper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class WhatTheDuckApplication extends Application {
    public static Properties config = null;

    private static final String CONFIG = "config.properties";
    public static final String CONFIG_KEY_PUSHER_INSTANCE_ID = "pusher_instance_id";
    public static final String CONFIG_KEY_API_ENDPOINT_URL = "api_endpoint_url";
    public static final String CONFIG_KEY_DM_URL = "dm_url";
    public static final String CONFIG_KEY_ROLE_NAME = "role_name";
    public static final String CONFIG_KEY_ROLE_PASSWORD = "role_password";
    public static final String CONFIG_KEY_EDGES_URL = "edges_url";

    private static final String CONFIG_KEY_MATOMO_URL = "matomo_url";
    private static final Integer CONFIG_MATOMO_DIMENSION_USER = 1;
    private static final Integer CONFIG_MATOMO_DIMENSION_VERSION = 2;

    private Tracker matomoTracker;

    public void onCreate() {
        super.onCreate();
        loadConfig(getAssets());
    }

    private static void loadConfig(AssetManager assets) {
        InputStream reader = null;
        try {
            reader = assets.open(CONFIG);
            config = new Properties();
            config.load(reader);
        } catch (IOException e) {
            WhatTheDuck.wtd.alert(R.string.internal_error);
            System.err.println("Config file not found, aborting");
            System.exit(-1);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    WhatTheDuck.wtd.alert(R.string.internal_error);
                    System.err.println("Error while reading config file, aborting");
                    System.exit(-1);
                }
            }
        }
    }

    private synchronized Tracker getTracker() {
        if (matomoTracker != null) {
            return matomoTracker;
        }
        String matomoUrl = config.getProperty(CONFIG_KEY_MATOMO_URL);
        matomoTracker = new TrackerBuilder(matomoUrl, 2, "WhatTheDuck").build(Matomo.getInstance(this));
        return matomoTracker;
    }

    public void trackActivity(Activity activity) {
        TrackHelper t = TrackHelper.track();

        AppDatabase db = WhatTheDuck.appDB;
        if (db != null) {
            User user = db.userDao().getCurrentUser();
            if (user != null) {
                t.dimension(CONFIG_MATOMO_DIMENSION_USER, user.getUsername());
            }
        }

        try {
            t.dimension(CONFIG_MATOMO_DIMENSION_VERSION, activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0).versionName);
        } catch (PackageManager.NameNotFoundException e) {
            t.dimension(CONFIG_MATOMO_DIMENSION_VERSION, "Unknown");
        }

        t
            .screen(activity.getClass().getSimpleName())
            .title(activity.getTitle().toString())
            .with(getTracker());

    }

    public void trackEvent(String name) {
        TrackHelper.track()
            .event("category", "action")
            .name(name)
            .value(1.0f)
            .with(getTracker());
    }
}
