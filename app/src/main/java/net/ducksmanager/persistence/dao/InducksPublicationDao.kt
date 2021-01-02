package net.ducksmanager.persistence.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import net.ducksmanager.persistence.models.coa.InducksPublication
import net.ducksmanager.persistence.models.composite.InducksPublicationWithPossession

@Dao
interface InducksPublicationDao {
    @Query("SELECT DISTINCT inducks_publication.*, COUNT(user_issues.issueNumber) AS possessedIssues, issue_count.count AS referencedIssues" +
            " FROM inducks_publication" +
            " LEFT JOIN issues AS user_issues ON inducks_publication.publicationCode = user_issues.country || '/' || user_issues.magazine" +
            " LEFT JOIN inducks_issue_count issue_count ON inducks_publication.publicationCode = issue_count.code" +
            " WHERE inducks_publication.publicationCode LIKE (:countryName || '/%') AND issue_count.count > 0" +
            " GROUP BY inducks_publication.publicationCode")
    fun findByCountry(countryName: String): LiveData<List<InducksPublicationWithPossession>>

    @Query("SELECT * FROM inducks_publication WHERE publicationCode = :publicationCode")
    fun findByPublicationCode(publicationCode: String): LiveData<InducksPublication>

    @Query("SELECT * FROM inducks_publication")
    fun findAll(): List<InducksPublication>

    @Insert
    fun insertList(issueList: List<InducksPublication>)

    @Query("DELETE FROM inducks_publication")
    fun deleteAll()
}