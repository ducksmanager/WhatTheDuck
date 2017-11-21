package net.ducksmanager.inducks.coa;


import android.app.Activity;

import net.ducksmanager.util.SimpleCallback;
import net.ducksmanager.whattheduck.R;
import net.ducksmanager.whattheduck.RetrieveTask;
import net.ducksmanager.whattheduck.WhatTheDuck;
import net.ducksmanager.whattheduck.WhatTheDuckApplication;

import org.json.JSONException;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Locale;

public abstract class CoaListing extends RetrieveTask {
    WeakReference<Activity> activityRef;
    SimpleCallback callback;
    public enum ListType {COUNTRY_LIST, PUBLICATION_LIST, ISSUE_LIST}

    private static final HashMap<ListType, String> urlSuffixes = new HashMap<>();
    static {
        urlSuffixes.put(ListType.COUNTRY_LIST, "&coa=true&liste_pays=true");
        urlSuffixes.put(ListType.PUBLICATION_LIST, "&coa=true&liste_magazines=true");
        urlSuffixes.put(ListType.ISSUE_LIST, "&coa=true&liste_numeros=true");
    }

    static String countryShortName;
    static String publicationShortName;


    CoaListing(Activity activity, ListType type, String countryShortName, String publicationShortName, SimpleCallback callback) {
        super(urlSuffixes.get(type), R.id.progressBarLoading);
        ((WhatTheDuckApplication) WhatTheDuck.wtd.getApplication()).trackEvent("list/coa/" + type.name().toLowerCase(Locale.FRANCE));
        this.activityRef = new WeakReference<Activity>(activity);
        this.callback = callback;
        CoaListing.countryShortName = countryShortName;
        CoaListing.publicationShortName = publicationShortName;
    }

    @Override
    protected void onPreExecute() {
        WhatTheDuck.wtd.toggleProgressbarLoading(activityRef, progressBarId, true);
    }

    @Override
    protected void onPostExecute(String response) {
        super.onPostExecute(response);
        WhatTheDuck.wtd.toggleProgressbarLoading(activityRef, progressBarId, false);
    }

    void handleJSONException(JSONException e) {
        WhatTheDuck.wtd.alert(activityRef,
                R.string.internal_error,
                R.string.internal_error__malformed_list," : " + e.getMessage());
    }
}
