package net.ducksmanager.whattheduck;

public class IssueComplete {
    private String countryCode;
    private String publicationCode;
    private Issue issue;

    public IssueComplete(String countryCode, String publicationCode, Issue issue) {
        this.countryCode = countryCode;
        this.publicationCode = publicationCode;
        this.issue = issue;
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
}
