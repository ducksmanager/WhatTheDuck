package net.ducksmanager.persistence.models.composite;

import net.ducksmanager.persistence.models.coa.InducksPublication;

import androidx.room.ColumnInfo;
import androidx.room.Embedded;

public class InducksPublicationWithPossession {
    @Embedded
    private InducksPublication publication;

    @ColumnInfo
    private Boolean isPossessed;

    public InducksPublication getPublication() {
        return publication;
    }

    public void setPublication(InducksPublication publication) {
        this.publication = publication;
    }

    public Boolean getPossessed() {
        return isPossessed;
    }

    public void setPossessed(Boolean possessed) {
        isPossessed = possessed;
    }
}
