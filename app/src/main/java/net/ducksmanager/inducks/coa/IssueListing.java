package net.ducksmanager.inducks.coa;

import android.app.Activity;

import com.koushikdutta.async.future.FutureCallback;

import net.ducksmanager.whattheduck.Issue;
import net.ducksmanager.whattheduck.WhatTheDuck;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;

public class IssueListing extends CoaListing {

    private static final HashSet<String> fullListPublications = new HashSet<>();

    private final String countryShortName;
    private final String publicationCode;

    public IssueListing(Activity activity, String countryShortName, String publicationCode, FutureCallback callback) {
        super(activity, ListType.ISSUE_LIST, callback);
        this.countryShortName = countryShortName;
        this.publicationCode = publicationCode;
    }

    public static boolean hasFullList(String publicationName) {
        return fullListPublications.contains(publicationName);
    }

    @Override
    protected String getUrlSuffix() {
        return "/coa/list/issues/" + publicationCode;
    }

    @Override
    protected void processData(String response) {
        if (response != null) {
            try {
                JSONArray issues = null;
                try {  // Legacy JSON structure
                    JSONObject object = new JSONObject(response);
                    issues = object.getJSONObject("static").getJSONArray("numeros");
                }
                catch (JSONException e) {
                    issues = new JSONArray(response);
                }
                finally {
                    for (int i = 0; i < issues.length(); i++) {
                        String issue = (String) issues.get(i);
                        WhatTheDuck.coaCollection.addIssue(countryShortName, publicationCode, new Issue(issue));
                    }
                    fullListPublications.add(publicationCode);
                }
            }
            catch (JSONException e) {
                handleJSONException(e);
            }
        }
    }
}
