package net.ducksmanager.inducks.coa;


import net.ducksmanager.whattheduck.List;
import net.ducksmanager.whattheduck.R;
import net.ducksmanager.whattheduck.RetrieveTask;
import net.ducksmanager.whattheduck.WhatTheDuck;
import org.json.JSONException;

import java.util.HashMap;

public abstract class CoaListing extends RetrieveTask {
	public static List displayedList;
	public static enum ListType {COUNTRY_LIST, PUBLICATION_LIST, ISSUE_LIST}

    private static final HashMap<ListType, String> urlSuffixes = new HashMap<ListType, String>();
    static {
        urlSuffixes.put(ListType.COUNTRY_LIST, "&coa=true&liste_pays=true");
        urlSuffixes.put(ListType.PUBLICATION_LIST, "&coa=true&liste_magazines=true");
        urlSuffixes.put(ListType.ISSUE_LIST, "&coa=true&liste_numeros=true");
    }

    private static HashMap<String,String> countryNames=new HashMap<String,String>();
	private static HashMap<String,HashMap<String,String>> publicationNames=new HashMap<String,HashMap<String,String>>();

    private final int progressBarId;
	final String countryShortName;
    final String publicationShortName;
	
	
	CoaListing(List list, ListType type, int progressBarId, String countryShortName, String publicationShortName) {
        super(urlSuffixes.get(type));
		displayedList = list;
		this.progressBarId = progressBarId;
		this.countryShortName = countryShortName;
		this.publicationShortName = publicationShortName;
	}
	
	public static String getCountryFullName (String shortCountryName) {
		return countryNames.get(shortCountryName);
	}
	
	public static String getPublicationFullName (String shortCountryName, String shortPublicationName) {
		if (publicationNames.get(shortCountryName) == null) {
			System.out.println("Can't get publications of country "+shortCountryName);
		}
		return publicationNames.get(shortCountryName).get(shortPublicationName);
	}
	
	public static String getCountryShortName(String fullCountryName) {
		for (String shortCountryName : countryNames.keySet()) {
			if (countryNames.get(shortCountryName).equals(fullCountryName))
				return shortCountryName;
		}
		return null;
	}
	
	public static String getPublicationShortName (String shortCountryName, String fullPublicationName) {
		HashMap<String,String> countryPublications = publicationNames.get(shortCountryName);
		for (String shortPublicationName : countryPublications.keySet()) {
			if (countryPublications.get(shortPublicationName).equals(fullPublicationName))
				return shortPublicationName;
		}
		return null;
	}
		
	static void resetCountries() {
		countryNames=new HashMap<String,String>();
	}
	
	static void resetPublications() {
		publicationNames=new HashMap<String,HashMap<String,String>>();
	}
	
	
	public static void addCountry(String shortName, String fullName) {
		countryNames.put(shortName, fullName);
	}
	
	public static void addPublication(String countryAndPublicationShortNames, String fullName) {
		String country=countryAndPublicationShortNames.split("/")[0];
		addPublication(country, countryAndPublicationShortNames, fullName);
	}
	
	static void addPublication(String countryShortName, String shortName, String fullName) {
		if (publicationNames.get(countryShortName) == null)
			publicationNames.put(countryShortName, new HashMap<String, String>());
		publicationNames.get(countryShortName).put(shortName, fullName);
		
	}

    @Override
    protected void onPreExecute() {
        WhatTheDuck.wtd.toggleProgressbarLoading(progressBarId, true);
    }

    @Override
    protected void onPostExecute(String response) {
        super.onPostExecute(response);
        WhatTheDuck.wtd.toggleProgressbarLoading(progressBarId, false);
    }

    void handleJSONException(JSONException e) {
        WhatTheDuck.wtd.alert(displayedList,
                R.string.internal_error,"",
                R.string.internal_error__malformed_list," : " + e.getMessage());
    }
}
