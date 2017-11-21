package net.ducksmanager.util;

import android.app.Activity;

import java.lang.ref.WeakReference;

public interface SimpleCallback {
    void onDownloadFinished(WeakReference<Activity> activity);
}
