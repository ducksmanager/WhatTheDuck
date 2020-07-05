package net.ducksmanager.persistence.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import net.ducksmanager.persistence.models.coa.InducksPublication
import net.ducksmanager.persistence.models.composite.InducksPublicationWithPossession

@Dao
interface InducksPublicationDao {
    @Query("SELECT DISTINCT inducks_publication.*, CASE WHEN issues.country IS NULL THEN 0 ELSE 1 END AS isPossessed FROM inducks_publication LEFT JOIN issues ON inducks_publication.publicationCode = issues.country || '/' || issues.magazine WHERE publicationCode LIKE (:countryName || '/%')")
    fun findByCountry(countryName: String): LiveData<List<InducksPublicationWithPossession>>

    @Query("SELECT * FROM inducks_publication WHERE publicationCode = :publicationCode")
    fun findByPublicationCode(publicationCode: String): LiveData<InducksPublication>

    @Query("SELECT * FROM inducks_publication")
    fun findAll(): List<InducksPublication>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertList(issueList: List<InducksPublication>)
}