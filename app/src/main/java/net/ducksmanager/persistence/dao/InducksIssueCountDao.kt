package net.ducksmanager.persistence.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import net.ducksmanager.persistence.models.composite.InducksIssueCount

@Dao
interface InducksIssueCountDao {
    @Insert
    fun insertList(issueList: List<InducksIssueCount>)

    @Query("DELETE FROM inducks_issue_count")
    fun deleteAll()

    @Query(" INSERT INTO inducks_issue_count" +
                 " SELECT substr(code, 0, instr(code, '/')) AS countryCode, SUM(count) AS countryCount" +
                 " FROM inducks_issue_count" +
                 " GROUP BY substr(code, 0, instr(code, '/'))")
    fun createCountryCountsFromPublicationCounts()
}