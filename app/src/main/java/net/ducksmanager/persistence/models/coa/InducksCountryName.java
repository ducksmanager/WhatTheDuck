package net.ducksmanager.persistence.models.coa;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "inducks_countryname")
public class InducksCountryName {

    @PrimaryKey
    @NonNull
    private String countryCode;

    @ColumnInfo
    private String countryName;

    public InducksCountryName(@NonNull String countryCode, String countryName) {
        this.countryCode = countryCode;
        this.countryName = countryName;
    }

    @NonNull
    public String getCountryCode() {
        return countryCode;
    }

    public String getCountryName() {
        return countryName;
    }
}
