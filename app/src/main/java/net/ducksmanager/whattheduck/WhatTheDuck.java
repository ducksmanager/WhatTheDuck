package net.ducksmanager.whattheduck;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import com.pusher.pushnotifications.auth.BeamsTokenProvider;

import net.ducksmanager.api.DmServer;
import net.ducksmanager.persistence.AppDatabase;
import net.ducksmanager.persistence.models.dm.User;

import org.matomo.sdk.Matomo;
import org.matomo.sdk.Tracker;
import org.matomo.sdk.TrackerBuilder;
import org.matomo.sdk.extra.TrackHelper;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.Properties;

import androidx.room.Room;

public class WhatTheDuck extends Application {
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

    public static String applicationVersion;
    public static String locale;

    public static String DB_NAME = "appDB";
    public static AppDatabase appDB = null;

    private static Tracker matomoTracker;

    public static String selectedCountry = null;
    public static String selectedPublication = null;
    public static String selectedIssue = null;
    public static Context applicationContext;

    public static BeamsTokenProvider tokenProvider;

    public enum CollectionType {COA,USER}

    void setup() {
        loadConfig(getAssets());

        applicationContext = getApplicationContext();
        applicationVersion = getApplicationVersion();
        locale = applicationContext.getResources().getConfiguration().locale.getLanguage();

        DmServer.initApi();

        if (appDB == null) {
            appDB = Room.databaseBuilder(applicationContext, AppDatabase.class, DB_NAME)
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build();
        }
    }

    private static void loadConfig(AssetManager assets) {
        if (config != null) {
            System.out.println("Config already loaded, ignoring");
            return;
        }
        InputStream reader = null;
        try {
            reader = assets.open(CONFIG);
            config = new Properties();
            config.load(reader);
        } catch (IOException e) {
            System.err.println("Config file not found, aborting");
            System.exit(-1);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    System.err.println("Error while reading config file, aborting");
                    System.exit(-1);
                }
            }
        }
    }

    private String getApplicationVersion() {
        PackageManager manager = this.getPackageManager();
        PackageInfo info;
        try {
            info = manager.getPackageInfo(this.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            return "Unknown";
        }
        return info.versionName;
    }

    static String getDmUrl() {
        return config.getProperty(CONFIG_KEY_DM_URL);
    }

    static boolean isTestContext(String apiEndpointUrl) {
        return apiEndpointUrl.startsWith("http://");
    }

    public static void info(WeakReference<Activity> activity, int titleId, int duration) {
        Toast.makeText(activity.get(), titleId, duration).show();
    }

    public static void alert(WeakReference<Activity> activityRef, String message) {
        Activity activity = activityRef.get();
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(activity.getString(R.string.error));
        builder.setMessage(message);
        builder.create().show();
    }

    public static void alert(WeakReference<Activity> activity, int messageId) {
        alert(activity, activity.get().getString(messageId));
    }

    public static void alert(WeakReference<Activity> activityRef, int titleId, int messageId) {
        Activity activity = activityRef.get();
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(activity.getString(titleId));
        builder.setMessage(activity.getString(messageId));

        activity.runOnUiThread(() ->
            builder.create().show()
        );
    }

    private static synchronized Tracker getTracker() {
        if (matomoTracker != null) {
            return matomoTracker;
        }
        String matomoUrl = config.getProperty(CONFIG_KEY_MATOMO_URL);
        try {
            matomoTracker = new TrackerBuilder(matomoUrl, 2, "WhatTheDuck").build(Matomo.getInstance(applicationContext));
        }
        catch(RuntimeException e) {
            System.err.println("Couldn't initialize tracker");
        }
        return matomoTracker;
    }

    public void trackActivity(Activity activity) {
        TrackHelper t = TrackHelper.track();

        AppDatabase db = appDB;
        if (db != null) {
            User user = db.userDao().getCurrentUser();
            if (user != null) {
                t.dimension(CONFIG_MATOMO_DIMENSION_USER, user.username);
            }
        }

        try {
            t.dimension(CONFIG_MATOMO_DIMENSION_VERSION, activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0).versionName);
        } catch (PackageManager.NameNotFoundException e) {
            t.dimension(CONFIG_MATOMO_DIMENSION_VERSION, "Unknown");
        }

        if (getTracker() != null) {
            t
                .screen(activity.getClass().getSimpleName())
                .title(activity.getTitle().toString())
                .with(getTracker());
        }

    }

    public static boolean isMobileConnection() {
        ConnectivityManager cm = (ConnectivityManager)applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            return false;
        }
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE;
    }

    public static void trackEvent(String text) {
        System.out.println(text);
        if (getTracker() != null) {
            TrackHelper.track()
                .event("category", "action")
                .name(text)
                .value(1.0f)
                .with(getTracker());
        }
    }
}
