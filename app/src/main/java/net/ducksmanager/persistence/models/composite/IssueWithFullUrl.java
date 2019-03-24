package net.ducksmanager.persistence.models.composite;

import com.google.gson.annotations.SerializedName;

public class IssueWithFullUrl {
    @SerializedName(value="coverid")
    private String coverId;

    @SerializedName(value="countrycode")
    private String countryCode;

    @SerializedName(value="publicationcode")
    private String publicationCode;

    @SerializedName(value="publicationtitle")
    private String publicationTitle;

    @SerializedName(value="issuenumber")
    private String issueNumber;

    private String fullUrl;

    public IssueWithFullUrl(String coverId, String countryCode, String publicationCode, String publicationTitle, String issueNumber, String fullUrl) {
        this.coverId = coverId;
        this.countryCode = countryCode;
        this.publicationCode = publicationCode;
        this.publicationTitle = publicationTitle;
        this.issueNumber = issueNumber;
        this.fullUrl = fullUrl;
    }

    public String getCoverId() {
        return coverId;
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

    public void setFullUrl(String fullUrl) {
        this.fullUrl = fullUrl;
    }
}
