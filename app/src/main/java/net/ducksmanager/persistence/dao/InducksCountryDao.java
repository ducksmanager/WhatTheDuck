package net.ducksmanager.persistence.dao;

import net.ducksmanager.persistence.models.coa.InducksCountryName;
import net.ducksmanager.persistence.models.composite.InducksCountryNameWithPossession;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface InducksCountryDao {
    @Query("SELECT DISTINCT inducks_countryname.*, CASE WHEN issues.country IS NULL THEN 0 ELSE 1 END AS isPossessed FROM inducks_countryname LEFT JOIN issues ON inducks_countryname.countryCode = issues.country ORDER BY inducks_countryname.countryName")
    LiveData<List<InducksCountryNameWithPossession>> findAll();

    @Query("SELECT * FROM inducks_countryname WHERE countryCode = :countryCode")
    LiveData<InducksCountryName> findByCountryCode(String countryCode);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertList(List<InducksCountryName> issueList);

}
