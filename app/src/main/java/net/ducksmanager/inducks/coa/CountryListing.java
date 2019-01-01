package net.ducksmanager.inducks.coa;

import android.app.Activity;

import com.koushikdutta.async.future.FutureCallback;

import net.ducksmanager.whattheduck.WhatTheDuck;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;

public class CountryListing extends CoaListing {

    private static HashMap<String,String> countryNames= new HashMap<>();
    public static boolean hasFullList = false;

    public CountryListing(Activity activity, FutureCallback callback) {
        super(activity, ListType.COUNTRY_LIST, callback);
    }

    public static String getCountryFullName (String shortCountryName) {
        return countryNames.get(shortCountryName);
    }

    private static void resetCountries() {
        countryNames = new HashMap<>();
    }

    private static void addCountry(String shortName, String fullName) {
        countryNames.put(shortName, fullName);
    }

    @Override
    protected String getUrlSuffix() {
        return "/coa/list/countries/{locale}";
    }

    @Override
    protected void processData(String response) throws JSONException {
        if (response != null) {
            JSONObject responseObject = new JSONObject(response);
            resetCountries();
            addCountriesFullList(responseObject);
        }
    }

    @SuppressWarnings("unchecked")
    public static void addCountries(JSONObject countryNames) throws JSONException {
        if (!hasFullList) {
            if (countryNames.has("static")) { // Legacy JSON structure
                countryNames = countryNames.getJSONObject("static").getJSONObject("pays");
            }

            Iterator<String> countryIterator = countryNames.keys();
            while (countryIterator.hasNext()) {
                String shortName = countryIterator.next();
                String fullName = countryNames.getString(shortName);

                addCountry(shortName, fullName);
                if (!shortName.equals("zz") && !WhatTheDuck.coaCollection.hasCountry(shortName)) {
                    WhatTheDuck.coaCollection.addCountry(shortName);
                }
            }
        }
    }

    private static void addCountriesFullList(JSONObject object) throws JSONException {
        addCountries(object);
        hasFullList = true;
    }
}
