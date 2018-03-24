package net.ducksmanager.inducks.coa;

import android.app.Activity;

import com.koushikdutta.async.future.FutureCallback;

import net.ducksmanager.whattheduck.WhatTheDuck;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class PublicationListing extends CoaListing {

    private static final HashMap<String,HashMap<String,String>> publicationNames= new HashMap<>();
    private static final HashSet<String> fullListCountries = new HashSet<>();

    private String countryShortName;

    public PublicationListing(Activity list, String countryShortName, FutureCallback callback) {
        super(list, ListType.PUBLICATION_LIST, callback);
        this.countryShortName = countryShortName;
    }

    public static String getPublicationFullName (String shortCountryName, String shortPublicationName) {
        if (publicationNames.get(shortCountryName) == null) {
            System.out.println("Can't get publications of country "+shortCountryName);
            return null;
        }
        return publicationNames.get(shortCountryName).get(shortPublicationName);
    }

    private static void addPublication(String countryShortName, String shortName, String fullName) {
        if (publicationNames.get(countryShortName) == null)
            publicationNames.put(countryShortName, new HashMap<>());
        publicationNames.get(countryShortName).put(shortName, fullName);
    }

    public static boolean hasFullList(String country) {
        return fullListCountries.contains(country);
    }

    @Override
    protected String getUrlSuffix() {
        return "/coa/list/publications/" + countryShortName;
    }

    @Override
    protected void processData(String response) {
        if (response != null) {
            try {
                addFullPublications(countryShortName, new JSONObject(response));
            }
            catch (JSONException e) {
                handleJSONException(e);
            }
        }
    }

    private static void addFullPublications(String countryShortName, JSONObject object) throws JSONException {
        addPublications(object);
        fullListCountries.add(countryShortName);
    }

    @SuppressWarnings("unchecked")
    public static void addPublications(JSONObject publicationNames) throws JSONException {
        if (publicationNames.has("static")) { // Legacy JSON structure
            publicationNames = publicationNames.getJSONObject("static").getJSONObject("magazines");
        }
        @SuppressWarnings("unchecked")
        Iterator<String> publicationIterator = publicationNames.keys();
        while (publicationIterator.hasNext()) {
            String publicationShortName = publicationIterator.next();
            String publicationFullName = publicationNames.getString(publicationShortName);
            String countryShortName=publicationShortName.split("/")[0];

            addPublication(countryShortName, publicationShortName, publicationFullName);
            WhatTheDuck.coaCollection.addPublication(countryShortName, publicationShortName);
        }
    }
}
