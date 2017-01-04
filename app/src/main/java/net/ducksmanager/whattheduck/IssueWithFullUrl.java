package net.ducksmanager.whattheduck;

import java.io.Serializable;

public class IssueWithFullUrl implements Serializable {
    private final String publicationTitle;
    private final String issueNumber;
    private final String fullUrl;
    private final String countryCode;

    public IssueWithFullUrl(String countryCode, String publicationTitle, String issueNumber, String fullUrl) {
        this.countryCode = countryCode;
        this.publicationTitle = publicationTitle;
        this.issueNumber = issueNumber;
        this.fullUrl = fullUrl;
    }

    public String getPublicationTitle() {
        return publicationTitle;
    }

    public String getIssueNumber() {
        return issueNumber;
    }

    public String getFullUrl() {
        return fullUrl;
    }

    public String getCountryCode() {
        return countryCode;
    }

    @Override
    public String toString() {
        return getPublicationTitle()+" "+getIssueNumber();
    }
}
