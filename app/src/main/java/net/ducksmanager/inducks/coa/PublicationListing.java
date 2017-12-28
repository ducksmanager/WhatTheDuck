package net.ducksmanager.inducks.coa;

import android.app.Activity;

import net.ducksmanager.util.SimpleCallback;
import net.ducksmanager.whattheduck.WhatTheDuck;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class PublicationListing extends CoaListing {

    private static final HashMap<String,HashMap<String,String>> publicationNames= new HashMap<>();
    private static final HashSet<String> fullListCountries = new HashSet<>();

    public PublicationListing(Activity list, String countryShortName, SimpleCallback callback) {
        super(list, ListType.PUBLICATION_LIST, countryShortName, null, callback);
        this.urlSuffix+="&pays="+countryShortName;
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
    protected void onPostExecute(String response) {
        super.onPostExecute(response);
        if (response != null) {
            try {
                addFullPublications(countryShortName, new JSONObject(response));
            }
            catch (JSONException e) {
                handleJSONException(e);
            }
        }

        callback.onDownloadFinished(activityRef);
    }

    private static void addFullPublications(String countryShortName, JSONObject object) throws JSONException {
        addPublications(object);
        fullListCountries.add(countryShortName);
    }

    @SuppressWarnings("unchecked")
    public static void addPublications(JSONObject object) throws JSONException {
        JSONObject publicationName = object.getJSONObject("static").getJSONObject("magazines");
        @SuppressWarnings("unchecked")
        Iterator<String> publicationIterator = publicationName.keys();
        while (publicationIterator.hasNext()) {
            String publicationShortName = publicationIterator.next();
            String publicationFullName = publicationName.getString(publicationShortName);
            String countryShortName=publicationShortName.split("/")[0];

            addPublication(countryShortName, publicationShortName, publicationFullName);
            WhatTheDuck.coaCollection.addPublication(countryShortName, publicationShortName);
        }
    }
}
