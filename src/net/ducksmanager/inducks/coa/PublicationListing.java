package net.ducksmanager.inducks.coa;

import net.ducksmanager.whattheduck.CountryList;
import net.ducksmanager.whattheduck.List;
import net.ducksmanager.whattheduck.PublicationList;
import net.ducksmanager.whattheduck.WhatTheDuck;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class PublicationListing extends CoaListing {

    public PublicationListing(List list, int progressBarId, String countryShortName, String publicationShortName) {
        super(list, ListType.PUBLICATION_LIST, progressBarId, countryShortName, publicationShortName);
        this.urlSuffix+="&pays="+countryShortName;
    }

    @Override
    protected void onPostExecute(String response) {
        if (response != null) {
            try {
                resetPublications();
                JSONObject object = new JSONObject(response);
                JSONObject publicationName = object.getJSONObject("static").getJSONObject("magazines");
                @SuppressWarnings("unchecked")
                Iterator<String> publicationIterator = publicationName.keys();
                while (publicationIterator.hasNext()) {
                    String shortName = publicationIterator.next();
                    String fullName = publicationName.getString(shortName);
                    addPublication(countryShortName, shortName, fullName);
                    WhatTheDuck.coaCollection.addPublication(countryShortName, shortName);
                }
            }
            catch (JSONException e) {
                handleJSONException(e);
            }
        }

        displayedList.show();
    }
}
