package net.ducksmanager.persistence.models.composite;

import com.google.gson.annotations.SerializedName;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "coversearch_issue")
public class CoverSearchIssue {
    @SerializedName(value="coverid")
    @PrimaryKey
    @NonNull
    private String coverId;

    @SerializedName(value="countrycode")
    @ColumnInfo
    private String countryCode;

    @SerializedName(value="publicationcode")
    @ColumnInfo
    private String publicationCode;

    @SerializedName(value="publicationtitle")
    @ColumnInfo
    private String publicationTitle;

    @SerializedName(value="issuenumber")
    @ColumnInfo
    private String issueNumber;

    @ColumnInfo
    private String fullUrl;

    public CoverSearchIssue(String coverId, String countryCode, String publicationCode, String publicationTitle, String issueNumber, String fullUrl) {
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
