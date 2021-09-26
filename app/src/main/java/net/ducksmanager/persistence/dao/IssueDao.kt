package net.ducksmanager.persistence.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import net.ducksmanager.persistence.models.composite.CollectionCount
import net.ducksmanager.persistence.models.composite.IssueCountPerMonthAndPublication
import net.ducksmanager.persistence.models.composite.IssuesPerCondition
import net.ducksmanager.persistence.models.dm.Issue

@Dao
interface IssueDao {
    @Query(" SELECT issues.* FROM issues WHERE (issues.country || '/' || issues.magazine) = :publicationCode")
    fun findByPublicationCode(publicationCode: String): LiveData<List<Issue>>

    @Query("SELECT * FROM issues WHERE (issues.country || '/' || issues.magazine || '-' || issues.issueNumber) in (:issueCodes)")
    fun findByIssueCodes(issueCodes: Set<String>): LiveData<List<Issue>>

    @Query("SELECT" +
        "(select COUNT(distinct issues.country) FROM issues) AS countries," +
        "(select COUNT(distinct issues.country || '/' || issues.magazine) FROM issues) AS publications," +
        "(select COUNT(*) FROM issues) AS issues," +
        "(select COUNT(distinct issues.country || '/' || issues.magazine || ' ' || issues.issueNumber) FROM issues) AS distinctIssues")
    fun countDistinct(): LiveData<CollectionCount>

    @Query(" SELECT issues.condition, COUNT(*) AS count FROM issues GROUP BY issues.condition ORDER BY issues.condition")
    fun countPerCondition(): LiveData<List<IssuesPerCondition>>

    @Query("" +
        " SELECT issues.country || '/' || issues.magazine                 AS publicationcode," +
        "        SUBSTR(COALESCE((SELECT purchases.date FROM purchases WHERE purchases.purchaseId = issues.issuePurchaseId)," +
        "                        issues.creationDate, '0000-00-00'), 0, 8) AS month," +
        "        COUNT(*)                                                  AS count" +
        " FROM issues" +
        " GROUP BY month, publicationcode" +
        " ORDER BY month, publicationcode")
    fun countPerMonthAndPublication(): LiveData<List<IssueCountPerMonthAndPublication>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertList(issueList: List<Issue>)

    @Query("DELETE FROM issues")
    fun deleteAll()
}