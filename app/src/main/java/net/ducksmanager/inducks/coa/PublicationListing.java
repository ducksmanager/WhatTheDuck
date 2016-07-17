package net.ducksmanager.inducks.coa;

import net.ducksmanager.whattheduck.List;
import net.ducksmanager.whattheduck.WhatTheDuck;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;

public class PublicationListing extends CoaListing {

    private static HashMap<String,HashMap<String,String>> publicationNames= new HashMap<>();

    public PublicationListing(List list, String countryShortName) {
        super(list, ListType.PUBLICATION_LIST, countryShortName, null);
        this.urlSuffix+="&pays="+countryShortName;
    }

    public static String getPublicationFullName (String shortCountryName, String shortPublicationName) {
		if (publicationNames.get(shortCountryName) == null) {
			System.out.println("Can't get publications of country "+shortCountryName);
		}
		return publicationNames.get(shortCountryName).get(shortPublicationName);
	}

    public static String getPublicationShortName (String shortCountryName, String fullPublicationName) {
		HashMap<String,String> countryPublications = publicationNames.get(shortCountryName);
		for (String shortPublicationName : countryPublications.keySet()) {
			if (countryPublications.get(shortPublicationName).equals(fullPublicationName))
				return shortPublicationName;
		}
		return null;
	}

    private static void resetPublications() {
		publicationNames = new HashMap<>();
	}

    private static void addPublication(String countryShortName, String shortName, String fullName) {
		if (publicationNames.get(countryShortName) == null)
			publicationNames.put(countryShortName, new HashMap<String, String>());
		publicationNames.get(countryShortName).put(shortName, fullName);
	}

    @Override
    protected void onPostExecute(String response) {
        super.onPostExecute(response);
        if (response != null) {
            try {
                resetPublications();
                addPublications(new JSONObject(response));
            }
            catch (JSONException e) {
                handleJSONException(e);
            }
        }

        displayedList.show();
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
