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

	static String countryShortName;
    static String publicationShortName;
	
	
	CoaListing(List list, ListType type, int progressBarId, String countryShortName, String publicationShortName) {
        super(urlSuffixes.get(type), progressBarId);
		displayedList = list;
		CoaListing.countryShortName = countryShortName;
		CoaListing.publicationShortName = publicationShortName;
	}

    @Override
    protected void onPreExecute() {
        WhatTheDuck.wtd.toggleProgressbarLoading(displayedList, progressBarId, true);
    }

    @Override
    protected void onPostExecute(String response) {
        super.onPostExecute(response);
        WhatTheDuck.wtd.toggleProgressbarLoading(displayedList, progressBarId, false);
    }

    void handleJSONException(JSONException e) {
        WhatTheDuck.wtd.alert(displayedList,
                R.string.internal_error,"",
                R.string.internal_error__malformed_list," : " + e.getMessage());
    }
}
