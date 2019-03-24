package net.ducksmanager.persistence.models.composite;

import net.ducksmanager.persistence.models.coa.InducksCountryName;

import androidx.room.ColumnInfo;
import androidx.room.Embedded;

public class InducksCountryNameWithPossession {
    @Embedded
    private InducksCountryName country;

    @ColumnInfo
    private Boolean isPossessed;

    public InducksCountryName getCountry() {
        return country;
    }

    public void setCountry(InducksCountryName country) {
        this.country = country;
    }

    public Boolean getPossessed() {
        return isPossessed;
    }

    public void setPossessed(Boolean possessed) {
        isPossessed = possessed;
    }
}
