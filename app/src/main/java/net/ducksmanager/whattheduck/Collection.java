package net.ducksmanager.whattheduck;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class Collection implements Serializable {
    private final HashMap<String,HashMap<String,HashMap<String, Issue>>> issues = new HashMap<>();
    private HashMap<Integer, PurchaseAdapter.Purchase> purchases = new HashMap<>();

    public void setPurchases(HashMap<Integer, PurchaseAdapter.Purchase> purchases) {
        this.purchases = purchases;
    }

    private HashMap<Integer, PurchaseAdapter.Purchase> getPurchases() {
        return purchases;
    }

    HashMap<String,PurchaseAdapter.Purchase> getPurchasesWithEmptyItem() {
        HashMap<String,PurchaseAdapter.Purchase> purchasesWithEmptyItem = new HashMap<>();

        java.util.List<PurchaseAdapter.Purchase> values = new ArrayList<>(getPurchases().values());
        values.add(new PurchaseAdapter.SpecialPurchase());
        for (PurchaseAdapter.Purchase p : values) {
            purchasesWithEmptyItem.put(p.toString(), p);
        }

        return purchasesWithEmptyItem;
    }

    public void addCountry(String country) {
        issues.put(country, new HashMap<>());
    }

    public void addPublication(String country, String publication) {
        if (issues.get(country) == null)
            this.addCountry(country);
        if (issues.get(country).get(publication) == null) {
            issues.get(country).put(publication, new HashMap<>());
        }
    }

    public void addIssue(String countryAndPublication, Issue issue) {
        String country=countryAndPublication.split("/")[0];
        addIssue(country, countryAndPublication, issue);
    }

    public void addIssue(String country, String publication, Issue issue) {
        if (issues.get(country) == null)
            this.addCountry(country);
        if (issues.get(country).get(publication) == null)
            this.addPublication(country, publication);
        issues.get(country).get(publication).put(issue.getIssueNumber(), issue);
    }

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
