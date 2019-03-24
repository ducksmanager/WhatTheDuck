package net.ducksmanager.persistence.dao;

import net.ducksmanager.persistence.models.coa.InducksPublication;
import net.ducksmanager.persistence.models.composite.InducksPublicationWithPossession;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface InducksPublicationDao {
    @Query("SELECT DISTINCT inducks_publication.*, CASE WHEN issues.country IS NULL THEN 0 ELSE 1 END AS isPossessed FROM inducks_publication LEFT JOIN issues ON inducks_publication.publicationCode = issues.country || '/' || issues.magazine WHERE publicationCode LIKE (:countryName || '/%')")
    LiveData<List<InducksPublicationWithPossession>> findByCountry(String countryName);

    @Query("SELECT * FROM inducks_publication WHERE publicationCode = :publicationCode")
    LiveData<InducksPublication> findByPublicationCode(String publicationCode);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertList(List<InducksPublication> issueList);

}
