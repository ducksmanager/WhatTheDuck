package net.ducksmanager.whattheduck;

import java.io.Serializable;

public class IssueWithFullUrl implements Serializable {
    private final String countryCode;
    private final String publicationCode;
    private final String publicationTitle;
    private final String issueNumber;
    private final String fullUrl;

    public IssueWithFullUrl(String countryCode, String publicationCode, String publicationTitle, String issueNumber, String fullUrl) {
        this.countryCode = countryCode;
        this.publicationCode = publicationCode;
        this.publicationTitle = publicationTitle;
        this.issueNumber = issueNumber;
        this.fullUrl = fullUrl;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public String getPublicationCode() {
        return publicationCode;
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
}
