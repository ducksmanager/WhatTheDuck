package net.ducksmanager.persistence.models.coa;

import androidx.annotation.NonNull;
import androidx.room.Entity;

@Entity(tableName = "inducks_issue", primaryKeys = {"publicationCode", "issueNumber"} )
public class InducksIssue {

    @NonNull
    private String publicationCode;

    @NonNull
    private String issueNumber;

    public InducksIssue(@NonNull String publicationCode, @NonNull String issueNumber) {
        this.publicationCode = publicationCode;
        this.issueNumber = issueNumber;
    }

    @NonNull
    public String getPublicationCode() {
        return publicationCode;
    }

    @NonNull
    public String getIssueNumber() {
        return issueNumber;
    }

}
