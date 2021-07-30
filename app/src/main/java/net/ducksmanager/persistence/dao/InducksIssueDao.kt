package net.ducksmanager.persistence.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import net.ducksmanager.persistence.models.coa.InducksIssueWithCoverUrl
import net.ducksmanager.persistence.models.composite.InducksIssueWithUserData

@Dao
interface InducksIssueDao {
    @Query(
        " SELECT inducks_issue.*, issues.*, purchases.*, suggested_issues.suggestionScore" +
        " FROM inducks_issue" +
        " LEFT JOIN issues ON inducks_issue.inducksPublicationCode = issues.country || '/' || issues.magazine AND inducks_issue.inducksIssueNumber = issues.issueNumber" +
        " LEFT JOIN suggested_issues ON inducks_issue.inducksPublicationCode = suggested_issues.publicationCode AND inducks_issue.inducksIssueNumber = suggested_issues.issueNumber" +
        " LEFT JOIN purchases ON issues.issuePurchaseId = purchases.purchaseId" +
        " WHERE inducksPublicationCode = :publicationCode")
    fun findByPublicationCode(publicationCode: String): LiveData<List<InducksIssueWithUserData>>

    @Query(
        " SELECT inducks_issue.*, issues.*, purchases.*, suggested_issues.suggestionScore" +
        " FROM inducks_issue" +
        " INNER JOIN issues ON inducks_issue.inducksPublicationCode = issues.country || '/' || issues.magazine AND inducks_issue.inducksIssueNumber = issues.issueNumber" +
        " LEFT JOIN suggested_issues ON inducks_issue.inducksPublicationCode = suggested_issues.publicationCode AND inducks_issue.inducksIssueNumber = suggested_issues.issueNumber" +
        " LEFT JOIN purchases ON issues.issuePurchaseId = purchases.purchaseId" +
        " WHERE inducksPublicationCode = :publicationCode AND inducksIssueNumber = :issueNumber")
    fun findUserOwnedByPublicationCodeAndIssueNumber(publicationCode: String, issueNumber: String): LiveData<List<InducksIssueWithUserData>>

    @Query("DELETE FROM inducks_issue WHERE inducksPublicationCode = :publicationCode")
    fun deleteByPublicationCode(publicationCode: String)

    @Insert
    fun insertList(issueList: List<InducksIssueWithCoverUrl>)
}