package net.ducksmanager.inducks.coa;


import android.app.Activity;

import com.koushikdutta.async.future.FutureCallback;

import net.ducksmanager.whattheduck.R;
import net.ducksmanager.whattheduck.RetrieveTask;
import net.ducksmanager.whattheduck.WhatTheDuck;
import net.ducksmanager.whattheduck.WhatTheDuckApplication;

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

        ((WhatTheDuckApplication) WhatTheDuck.wtd.getApplication()).trackEvent("list/coa/" + type.name().toLowerCase(Locale.FRANCE));
    }

    protected abstract String getUrlSuffix();

    protected abstract void processData(String result);

    public void fetch() {
        try {
            WhatTheDuck.wtd.toggleProgressbarLoading(activityRef, R.id.progressBarLoading, true);
            WhatTheDuck.wtd.retrieveOrFailDmServer(getUrlSuffix(), (e, result) -> {
                WhatTheDuck.wtd.toggleProgressbarLoading(activityRef, R.id.progressBarLoading, false);
                if (e != null) {
                    RetrieveTask.handleResultException(e);
                }
                else {
                    processData(result);
                    if (afterProcessCallback != null) {
                        afterProcessCallback.onCompleted(null, result);
                    }
                }
            }, null, null);
        } catch (Exception e) {
            RetrieveTask.handleResultException(e);
        }
    }

    void handleJSONException(JSONException e) {
        WhatTheDuck.wtd.alert(activityRef,
                R.string.internal_error,
                R.string.internal_error__malformed_list," : " + e.getMessage());
    }
}
