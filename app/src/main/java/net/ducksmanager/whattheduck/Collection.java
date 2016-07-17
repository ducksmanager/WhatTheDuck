package net.ducksmanager.whattheduck;


import net.ducksmanager.inducks.coa.CountryListing;
import net.ducksmanager.inducks.coa.PublicationListing;
import net.ducksmanager.whattheduck.Issue.IssueCondition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;

public class Collection {
	private final HashMap<String,HashMap<String,ArrayList<Issue>>> issues = new HashMap<>();

	enum CollectionType {COA,USER}
	
	public void addCountry(String country) {
		issues.put(country, new HashMap<String,ArrayList<Issue>>());
	}
	
	public void addPublication(String country, String publication) {
		if (issues.get(country) == null)
			this.addCountry(country);
		issues.get(country).put(publication, new ArrayList<Issue>());
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
	
	public ArrayList<String> getCountryList(String type) {
		ArrayList<String> countryList = new ArrayList<>();
		Set<String> countrySet = issues.keySet();
		for (String shortCountryName : countrySet) {
			countryList.add((type.equals(CollectionType.COA.toString()) && WhatTheDuck.userCollection.hasCountry(shortCountryName) ? "* ":"")
							+ CountryListing.getCountryFullName(shortCountryName));
		}
		Collections.sort(countryList, new NamesComparator());
		return countryList;
	}
	
	public ArrayList<String> getPublicationList(String shortCountryName, String type) {
		ArrayList<String> publicationList = new ArrayList<>();
		HashMap<String, ArrayList<Issue>> publicationMap = issues.get(shortCountryName);
		if (publicationMap != null) {
			for (String shortPublicationName : publicationMap.keySet()) {
				publicationList.add((type.equals(CollectionType.COA.toString()) && WhatTheDuck.userCollection.hasPublication(shortCountryName, shortPublicationName) ? "* " : "")
						+ PublicationListing.getPublicationFullName(shortCountryName, shortPublicationName));
			}
			Collections.sort(publicationList, new NamesComparator());
		}
		return publicationList;
	}
	
	public ArrayList<Issue> getIssueList(String shortCountryName, String shortPublicationName, String type) {
		ArrayList<Issue> finalList = new ArrayList<>();
		HashMap<String, ArrayList<Issue>> publicationMap = issues.get(shortCountryName);
		if (publicationMap != null) {
			ArrayList<Issue> list = publicationMap.get(shortPublicationName);
			if (list != null) {
				for (Issue issue : list) {
					Boolean isCoaCollection = type.equals(CollectionType.COA.toString());
					Boolean isInCollection = false;
					IssueCondition condition = null;
					Issue existingIssue = WhatTheDuck.userCollection.getIssue(shortCountryName, shortPublicationName, issue.getIssueNumber());
					if (existingIssue != null) {
						isInCollection = true;
						condition = existingIssue.getIssueCondition();
					}
					Issue i = new Issue((isInCollection && isCoaCollection ? "* " : "") + issue.getIssueNumber(),
							condition);
					finalList.add(i);
				}
				Collections.sort(finalList, new NaturalOrderComparator());
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
	
	private Issue getIssue(String shortCountryName, String shortPublicationName, String issueNumber) {
		if (!hasPublication(shortCountryName, shortPublicationName))
			return null;
		for (Issue i : issues.get(shortCountryName).get(shortPublicationName)) {
			if (i.getIssueNumber().replaceAll("[ ]+", " ").equals(issueNumber.replaceAll("[ ]+", " ")))
				return i;
		}
		return null;
	}

    
    public static class NaturalOrderComparator extends net.ducksmanager.util.NaturalOrderComparator {

		public int compare(Object issue1, Object issue2) {
			if (issue1 instanceof String)
				return super.compare(((String)issue1).replace("* ", ""), ((String)issue2).replace("* ", ""));
			if (issue1 instanceof Issue)
				return compare(((Issue) issue1).getIssueNumber(), ((Issue) issue2).getIssueNumber());
			return 0;
		}
    }

    
    private static class NamesComparator implements Comparator<String> {

		@Override
		public int compare(String issue1, String issue2) {
			return issue1.replace("* ", "").compareTo(issue2.replace("* ", ""));
		}
    }
}
