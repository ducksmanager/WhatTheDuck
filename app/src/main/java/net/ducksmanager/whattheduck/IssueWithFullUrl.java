package net.ducksmanager.whattheduck;

import java.io.Serializable;

public class IssueWithFullUrl implements Serializable {
    private String countryCode;
    private String publicationCode;
    private Issue issue;
    private String fullUrl;

    public IssueWithFullUrl(String countryCode, String publicationCode, Issue issue, String fullUrl) {
        this.countryCode = countryCode;
        this.publicationCode = publicationCode;
        this.issue = issue;
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

    public Issue getIssue() {
        return issue;
    }

    public void setIssue(Issue issue) {
        this.issue = issue;
    }

    public String getFullUrl() {
        return fullUrl;
    }

    public void setFullUrl(String fullUrl) {
        this.fullUrl = fullUrl;
    }
}
