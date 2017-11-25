package net.ducksmanager.whattheduck;


import net.ducksmanager.inducks.coa.CountryListing;
import net.ducksmanager.inducks.coa.PublicationListing;
import net.ducksmanager.whattheduck.Issue.IssueCondition;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class Collection implements Serializable {
    private final HashMap<String,HashMap<String,ArrayList<Issue>>> issues = new HashMap<>();
    private HashMap<Integer, Purchase> purchases = new HashMap<>();

    public void setPurchases(HashMap<Integer, Purchase> purchases) {
        this.purchases = purchases;
    }

    private HashMap<Integer, Purchase> getPurchases() {
        return purchases;
    }

    HashMap<String,Purchase> getPurchasesWithEmptyItem() {
        HashMap<String,Purchase> purchasesWithEmptyItem = new HashMap<>();

        java.util.List<Purchase> values = new ArrayList<>(getPurchases().values());
        values.add(new SpecialPurchase(true, false));
        for (Purchase p : values) {
            purchasesWithEmptyItem.put(p.toString(), p);
        }

        return purchasesWithEmptyItem;
    }

    public enum CollectionType {COA,USER}

    public void addCountry(String country) {
        issues.put(country, new HashMap<String,ArrayList<Issue>>());
    }

    public void addPublication(String country, String publication) {
        if (issues.get(country) == null)
            this.addCountry(country);
        if (issues.get(country).get(publication) == null) {
            issues.get(country).put(publication, new ArrayList<Issue>());
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
        issues.get(country).get(publication).add(issue);
    }

    public ArrayList<CountryAdapter.Country> getCountryList() {
        ArrayList<CountryAdapter.Country> countryList = new ArrayList<>();
        Set<String> countrySet = issues.keySet();
        for (String shortCountryName : countrySet) {
            countryList.add(new CountryAdapter.Country(shortCountryName, CountryListing.getCountryFullName(shortCountryName)));
        }
        return countryList;
    }

    public ArrayList<PublicationAdapter.Publication> getPublicationList(String shortCountryName) {
        ArrayList<PublicationAdapter.Publication> publicationList = new ArrayList<>();
        HashMap<String, ArrayList<Issue>> publicationMap = issues.get(shortCountryName);
        if (publicationMap != null) {
            for (String shortPublicationName : publicationMap.keySet()) {
                publicationList.add(new PublicationAdapter.Publication(shortPublicationName, PublicationListing.getPublicationFullName(shortCountryName, shortPublicationName)));
            }
        }
        return publicationList;
    }

    public ArrayList<Issue> getIssueList(String shortCountryName, String shortPublicationName) {
        ArrayList<Issue> finalList = new ArrayList<>();
        HashMap<String, ArrayList<Issue>> publicationMap = issues.get(shortCountryName);
        if (publicationMap != null) {
            ArrayList<Issue> list = publicationMap.get(shortPublicationName);
            if (list != null) {
                for (Issue issue : list) {
                    IssueCondition condition = null;
                    PurchaseWithDate purchase = null;
                    Issue existingIssue = WhatTheDuck.userCollection.getIssue(shortCountryName, shortPublicationName, issue.getIssueNumber());
                    if (existingIssue != null) {
                        condition = existingIssue.getIssueCondition();
                        purchase = existingIssue.getPurchase();
                    }
                    finalList.add(new Issue(issue.getIssueNumber(), condition, purchase));
                }
            }
        }
        return finalList;
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
        for (Issue i : issues.get(shortCountryName).get(shortPublicationName)) {
            if (i.getIssueNumber().replaceAll("[ ]+", " ").equals(issueNumber.replaceAll("[ ]+", " ")))
                return i;
        }
        return null;
    }
}
