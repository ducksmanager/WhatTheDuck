package net.ducksmanager.inducks.coa;


import android.app.Activity;

import com.koushikdutta.async.future.FutureCallback;

import net.ducksmanager.whattheduck.RetrieveTask;
import net.ducksmanager.whattheduck.WhatTheDuck;

import org.json.JSONException;

import java.lang.ref.WeakReference;
import java.util.Locale;

public abstract class CoaListing {
    private final WeakReference<Activity> activityRef;
    private final FutureCallback afterProcessCallback;

    public enum ListType {COUNTRY_LIST, PUBLICATION_LIST, ISSUE_LIST}

    CoaListing(Activity activity, ListType type, FutureCallback afterProcessCallback) {
        this.activityRef = new WeakReference<>(activity);
        this.afterProcessCallback = afterProcessCallback;

        WhatTheDuck.trackEvent("list/coa/" + type.name().toLowerCase(Locale.FRANCE));
    }

    protected abstract String getUrlSuffix();

    protected abstract void processData(String result) throws JSONException;

    public void fetch() {
        try {
            WhatTheDuck.wtd.toggleProgressbarLoading(activityRef, true);
            WhatTheDuck.wtd.retrieveOrFailDmServer(getUrlSuffix(), (e, result) -> {
                if (e != null) {
                    RetrieveTask.handleResultExceptionOnActivity(e, activityRef);
                }
                else {
                    try {
                        processData(result);
                    }
                    catch(JSONException jsonException) {
                        RetrieveTask.handleResultExceptionOnActivity(jsonException, activityRef);
                    }
                    if (afterProcessCallback != null) {
                        afterProcessCallback.onCompleted(null, result);
                    }
                    WhatTheDuck.wtd.toggleProgressbarLoading(activityRef, false);
                }
            }, null, null);
        } catch (Exception e) {
            RetrieveTask.handleResultExceptionOnActivity(e, activityRef);
        }
    }
}
