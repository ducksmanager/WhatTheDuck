package net.ducksmanager.whattheduck;

import java.io.Serializable;

public class IssueWithFullUrl implements Serializable {
    private String countryCode;
    private String publicationCode;
    private String issueNumber;
    private String fullUrl;

    public IssueWithFullUrl(String countryCode, String publicationCode, String issueNumber, String fullUrl) {
        this.countryCode = countryCode;
        this.publicationCode = publicationCode;
        this.issueNumber = issueNumber;
        this.fullUrl = fullUrl;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getPublicationCode() {
        return publicationCode;
    }

    public void setPublicationCode(String publicationCode) {
        this.publicationCode = publicationCode;
    }

    public String getIssueNumber() {
        return issueNumber;
    }

    public void setIssueNumber(String issueNumber) {
        this.issueNumber = issueNumber;
    }

    public String getFullUrl() {
        return fullUrl;
    }

    public void setFullUrl(String fullUrl) {
        this.fullUrl = fullUrl;
    }
}
