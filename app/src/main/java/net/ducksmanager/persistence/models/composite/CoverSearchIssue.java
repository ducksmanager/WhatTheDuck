package net.ducksmanager.persistence.models.composite;

import com.google.gson.annotations.SerializedName;

import org.jetbrains.annotations.NotNull;

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
    private String coverCountryCode;

    @SerializedName(value="publicationcode")
    @ColumnInfo
    private String coverPublicationCode;

    @SerializedName(value="publicationtitle")
    @ColumnInfo
    private String coverPublicationTitle;

    @SerializedName(value="issuenumber")
    @ColumnInfo
    private String coverIssueNumber;

    @ColumnInfo
    private String coverFullUrl;

    public CoverSearchIssue(@NotNull String coverId, String coverCountryCode, String coverPublicationCode, String coverPublicationTitle, String coverIssueNumber, String coverFullUrl) {
        this.coverId = coverId;
        this.coverCountryCode = coverCountryCode;
        this.coverPublicationCode = coverPublicationCode;
        this.coverPublicationTitle = coverPublicationTitle;
        this.coverIssueNumber = coverIssueNumber;
        this.coverFullUrl = coverFullUrl;
    }

    @NonNull
    public String getCoverId() {
        return coverId;
    }

    public String getCoverCountryCode() {
        return coverCountryCode;
    }

    public String getCoverPublicationCode() {
        return coverPublicationCode;
    }

    public String getCoverPublicationTitle() {
        return coverPublicationTitle;
    }

    public String getCoverIssueNumber() {
        return coverIssueNumber;
    }

    public String getCoverFullUrl() {
        return coverFullUrl;
    }

    public void setCoverFullUrl(String coverFullUrl) {
        this.coverFullUrl = coverFullUrl;
    }
}
