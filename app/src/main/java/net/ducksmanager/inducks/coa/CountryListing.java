package net.ducksmanager.inducks.coa;

import net.ducksmanager.whattheduck.List;
import net.ducksmanager.whattheduck.WhatTheDuck;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;

public class CountryListing extends CoaListing {

    private static HashMap<String,String> countryNames= new HashMap<>();
    public static boolean hasFullList = false;

    public CountryListing(List list) {
        super(list, ListType.COUNTRY_LIST, null, null);
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

    private static void resetCountries() {
        countryNames = new HashMap<>();
    }

    private static void addCountry(String shortName, String fullName) {
		countryNames.put(shortName, fullName);
	}

    @Override
    protected void onPostExecute(String response) {
        super.onPostExecute(response);
        if (response != null) {
            try {
                resetCountries();
                addCountriesFullList(new JSONObject(response));
            }
            catch (JSONException e) {
                handleJSONException(e);
            }
        }

        displayedList.show();
    }

    @SuppressWarnings("unchecked")
    public static void addCountries(JSONObject object) throws JSONException {
        if (!hasFullList) {
            JSONObject countryNames = object.getJSONObject("static").getJSONObject("pays");
            Iterator<String> countryIterator = countryNames.keys();
            while (countryIterator.hasNext()) {
                String shortName = countryIterator.next();
                String fullName = countryNames.getString(shortName);

                addCountry(shortName, fullName);
                WhatTheDuck.coaCollection.addCountry(shortName);
            }
        }
    }

    private static void addCountriesFullList(JSONObject object) throws JSONException {
        addCountries(object);
        hasFullList = true;
    }
}
