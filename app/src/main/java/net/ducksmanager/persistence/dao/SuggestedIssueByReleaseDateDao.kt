package net.ducksmanager.persistence.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import net.ducksmanager.persistence.models.composite.SuggestedIssueByReleaseDateSimple

@Dao
interface SuggestedIssueByReleaseDateDao  {
    @Query("SELECT suggested_issues_by_release_date.* FROM suggested_issues_by_release_date ORDER BY oldestdate DESC")
    fun findAll() : List<SuggestedIssueByReleaseDateSimple>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertList(issueList: List<SuggestedIssueByReleaseDateSimple>)

    @Query("DELETE FROM suggested_issues_by_release_date")
    fun deleteAll()

}