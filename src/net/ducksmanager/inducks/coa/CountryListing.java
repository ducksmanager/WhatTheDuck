package net.ducksmanager.inducks.coa;

import net.ducksmanager.whattheduck.List;
import net.ducksmanager.whattheduck.WhatTheDuck;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;

public class CountryListing extends CoaListing {

    static HashMap<String,String> countryNames=new HashMap<String,String>();

    public CountryListing(List list, int progressBarId) {
        super(list, ListType.COUNTRY_LIST, progressBarId, null, null);
    }

    public static String getCountryFullName (String shortCountryName) {
        return countryNames.get(shortCountryName);
    }

    public static String getCountryShortName(String fullCountryName) {
        for (String shortCountryName : countryNames.keySet()) {
            if (countryNames.get(shortCountryName).equals(fullCountryName))
                return shortCountryName;
        }
        return null;
    }

    static void resetCountries() {
        countryNames = new HashMap<String,String>();
    }

    public static void addCountry(String shortName, String fullName) {
		countryNames.put(shortName, fullName);
	}

    @Override
    protected void onPostExecute(String response) {
        super.onPostExecute(response);
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
