package net.ducksmanager.persistence.models.coa;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

@Entity(tableName = "inducks_issue", primaryKeys = {"inducksPublicationCode", "inducksIssueNumber"} )
public class InducksIssue {

    @NonNull
    @ColumnInfo
    private String inducksPublicationCode;

    @NonNull
    @ColumnInfo
    private String inducksIssueNumber;

    public InducksIssue(@NonNull String inducksPublicationCode, @NonNull String inducksIssueNumber) {
        this.inducksPublicationCode = inducksPublicationCode;
        this.inducksIssueNumber = inducksIssueNumber;
    }

    @NonNull
    public String getInducksPublicationCode() {
        return inducksPublicationCode;
    }

    @NonNull
    public String getInducksIssueNumber() {
        return inducksIssueNumber;
    }

}
