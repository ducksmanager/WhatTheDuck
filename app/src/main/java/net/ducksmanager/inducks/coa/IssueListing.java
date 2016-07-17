package net.ducksmanager.inducks.coa;

import net.ducksmanager.whattheduck.Issue;
import net.ducksmanager.whattheduck.List;
import net.ducksmanager.whattheduck.WhatTheDuck;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class IssueListing extends CoaListing {

    public IssueListing(List list, String countryShortName, String publicationShortName) {
        super(list, ListType.ISSUE_LIST, countryShortName, publicationShortName);
        this.urlSuffix+="&pays="+countryShortName+"&magazine="+publicationShortName;
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
            }
            catch (JSONException e) {
                handleJSONException(e);
            }
        }

        displayedList.show();
    }
}
