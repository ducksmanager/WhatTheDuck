package net.ducksmanager.whattheduck;

import java.io.Serializable;

public class IssueWithFullUrl implements Serializable {
    private final String publicationTitle;
    private final String issueNumber;
    private final String fullUrl;

    public IssueWithFullUrl(String publicationTitle, String issueNumber, String fullUrl) {
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

    @Override
    public String toString() {
        return getPublicationTitle()+" "+getIssueNumber();
    }
}
