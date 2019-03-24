package net.ducksmanager.whattheduck;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class Collection implements Serializable {
    private final HashMap<String,HashMap<String,HashMap<String, Issue>>> issues = new HashMap<>();

    public boolean hasCountry (String countryShortName) {
        return issues.get(countryShortName) != null
            && issues.get(countryShortName).size() > 0;
    }

    public boolean hasPublication (String shortCountryName, String shortPublicationName) {
        return hasCountry(shortCountryName)
            && issues.get(shortCountryName).get(shortPublicationName) != null
            && issues.get(shortCountryName).get(shortPublicationName).size() > 0;
    }

    public Issue getIssue(String shortCountryName, String shortPublicationName, String issueNumber) {
        if (!hasPublication(shortCountryName, shortPublicationName))
            return null;
        return issues.get(shortCountryName).get(shortPublicationName).get(issueNumber);
    }
}
