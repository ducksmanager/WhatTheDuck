package net.ducksmanager.persistence.models.coa;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "inducks_publication")
public class InducksPublication {

    @PrimaryKey
    @NonNull
    private final String publicationCode;

    @ColumnInfo
    private final String title;

    public InducksPublication(@NonNull String publicationCode, String title) {
        this.publicationCode = publicationCode;
        this.title = title;
    }

    @NonNull
    public String getPublicationCode() {
        return publicationCode;
    }

    public String getTitle() {
        return title;
    }
}
