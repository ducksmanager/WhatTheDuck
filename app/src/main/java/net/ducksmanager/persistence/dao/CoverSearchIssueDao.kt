package net.ducksmanager.persistence.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import net.ducksmanager.persistence.models.composite.CoverSearchIssue
import net.ducksmanager.persistence.models.composite.CoverSearchIssueWithUserIssueAndScore

@Dao
interface CoverSearchIssueDao {
    @Query(
        " SELECT coversearch_issue.*, issues.*, suggested_issues.suggestionScore" +
            " FROM coversearch_issue" +
            " LEFT JOIN issues ON coversearch_issue.coverPublicationCode = issues.country || '/' || issues.magazine AND coversearch_issue.coverIssueNumber = issues.issueNumber" +
            " LEFT JOIN suggested_issues ON coversearch_issue.coverPublicationCode = suggested_issues.publicationCode AND coversearch_issue.coverIssueNumber = suggested_issues.issueNumber")
    fun findAll(): LiveData<List<CoverSearchIssueWithUserIssueAndScore>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertList(coverSearchIssues: List<CoverSearchIssue>)

    @Query("DELETE FROM coversearch_issue")
    fun deleteAll()
}