package net.ducksmanager.persistence.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import net.ducksmanager.persistence.models.coa.InducksIssueQuotation

@Dao
interface InducksIssueQuotationDao {

    @Insert
    fun insertList(issueList: List<InducksIssueQuotation>)

    @Query(
        "SELECT" +
                " SUM((CASE WHEN estimationMax IS NULL THEN estimationMin ELSE (estimationMin + estimationMax)/2 END) * (CASE WHEN condition = 'bon' THEN 1 WHEN condition = 'moyen' THEN 0.7 WHEN condition = 'mauvais' THEN 0.3 ELSE 0.7 END)) AS totalEstimation" +
                " FROM inducks_issuequotation INNER JOIN issues" +
                "    ON inducks_issuequotation.publicationCode = issues.country || '/' || issues.magazine " +
                "    AND inducks_issuequotation.issueNumber = issues.issueNumber "
    )
    fun getTotalEstimation(): LiveData<Float>

    @Query(
        "SELECT" +
                " inducks_issuequotation.publicationCode," +
                " inducks_issuequotation.issueNumber," +
                " (CASE WHEN estimationMax IS NULL THEN estimationMin ELSE (estimationMin + estimationMax)/2 END) * (CASE WHEN condition = 'bon' THEN 1 WHEN condition = 'moyen' THEN 0.7 WHEN condition = 'mauvais' THEN 0.3 ELSE 0.7 END) AS estimationMin" +
                " FROM inducks_issuequotation" +
                " INNER JOIN issues" +
                "    ON inducks_issuequotation.publicationCode = issues.country || '/' || issues.magazine " +
                "    AND inducks_issuequotation.issueNumber = issues.issueNumber" +
                " ORDER BY estimationMin DESC" +
                " LIMIT 1"
    )
    fun getMostRatedIssue(): LiveData<InducksIssueQuotation>

    @Query("DELETE FROM inducks_issuequotation")
    fun deleteAll()
}