package net.ducksmanager.whattheduck;

import java.io.Serializable;

public class IssueWithFullUrl implements Serializable {
    private String countryCode;
    private String publicationCode;
    private String publicationTitle;
    private String issueNumber;
    private String fullUrl;

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
