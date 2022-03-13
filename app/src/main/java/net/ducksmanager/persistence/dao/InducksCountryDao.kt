package net.ducksmanager.persistence.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import net.ducksmanager.persistence.models.coa.InducksCountryName
import net.ducksmanager.persistence.models.composite.InducksCountryNameWithNotification
import net.ducksmanager.persistence.models.composite.InducksCountryNameWithPossession

@Dao
interface InducksCountryDao {
    @Query("SELECT DISTINCT inducks_countryname.*, COUNT(DISTINCT user_issues.country || user_issues.magazine || user_issues.issueNumber) AS possessedIssues, issue_count.count AS referencedIssues" +
                " FROM inducks_countryname" +
                " LEFT JOIN issues AS user_issues ON inducks_countryname.countryCode = user_issues.country" +
                " LEFT JOIN inducks_issue_count issue_count ON inducks_countryname.countryCode = issue_count.code" +
                " WHERE issue_count.count > 0" +
                " GROUP BY inducks_countryname.countryCode" +
                " ORDER BY inducks_countryname.countryName COLLATE LOCALIZED")
    fun findAllWithPossession(): LiveData<List<InducksCountryNameWithPossession>>

    @Query(" SELECT DISTINCT inducks_countryname.*, CASE WHEN notificationCountries.country IS NULL THEN 0 ELSE 1 END AS isNotified" +
                " FROM inducks_countryname" +
                " LEFT JOIN notificationCountries ON inducks_countryname.countryCode = notificationCountries.country" +
                " ORDER BY inducks_countryname.countryName COLLATE LOCALIZED")
    fun findAllWithNotification(): LiveData<List<InducksCountryNameWithNotification>>

    @Query("SELECT * FROM inducks_countryname WHERE countryCode = :countryCode")
    fun findByCountryCode(countryCode: String): LiveData<InducksCountryName>

    @Query("SELECT * FROM inducks_countryname WHERE countryCode IN (:countryCodes)")
    fun findByCountryCodes(countryCodes: Set<String>): List<InducksCountryName>

    @Insert
    fun insertList(issueList: List<InducksCountryName>)

    @Query("DELETE FROM inducks_countryname")
    fun deleteAll()
}