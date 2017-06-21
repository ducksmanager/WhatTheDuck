package net.ducksmanager.inducks.coa;

import android.app.Activity;

import net.ducksmanager.util.SimpleCallback;
import net.ducksmanager.whattheduck.Issue;
import net.ducksmanager.whattheduck.WhatTheDuck;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;

public class IssueListing extends CoaListing {

    private static final HashSet<String> fullListPublications = new HashSet<>();

    public IssueListing(Activity activity, String countryShortName, String publicationShortName, SimpleCallback callback) {
        super(activity, ListType.ISSUE_LIST, countryShortName, publicationShortName, callback);
        this.urlSuffix+="&pays="+countryShortName+"&magazine="+publicationShortName;
    }

    public static boolean hasFullList(String publicationName) {
        return fullListPublications.contains(publicationName);
    }

    @Override
    protected void onPostExecute(String response) {
        super.onPostExecute(response);
        if (response != null) {
            try {
                JSONObject object = new JSONObject(response);
                JSONArray issues = object.getJSONObject("static").getJSONArray("numeros");
                for (int i = 0; i < issues.length(); i++) {
                    String issue = (String) issues.get(i);
                    WhatTheDuck.coaCollection.addIssue(CoaListing.countryShortName, CoaListing.publicationShortName, new Issue(issue, Issue.NO_CONDITION));
                }
                fullListPublications.add(CoaListing.publicationShortName);
            }
            catch (JSONException e) {
                handleJSONException(e);
            }
        }

        callback.onDownloadFinished(activity);
    }
}
