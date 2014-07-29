package net.ducksmanager.inducks.coa;

import net.ducksmanager.whattheduck.CountryList;
import net.ducksmanager.whattheduck.List;
import net.ducksmanager.whattheduck.WhatTheDuck;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class CountryListing extends CoaListing {

    public CountryListing(List list, int progressBarId, String countryShortName, String publicationShortName) {
        super(list, ListType.COUNTRY_LIST, progressBarId, countryShortName, publicationShortName);
    }

    @Override
    protected void onPostExecute(String response) {
        if (response != null) {
            try {
                resetCountries();
                JSONObject object = new JSONObject(response);
                JSONObject countryName = object.getJSONObject("static").getJSONObject("pays");
                @SuppressWarnings("unchecked")
                Iterator<String> countryIterator = countryName.keys();
                while (countryIterator.hasNext()) {
                    String shortName = countryIterator.next();
                    String fullName = countryName.getString(shortName);
                    addCountry(shortName, fullName);
                    WhatTheDuck.coaCollection.addCountry(shortName);
                }
            }
            catch (JSONException e) {
                handleJSONException(e);
            }
        }

        displayedList.show();
    }
}
