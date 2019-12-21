package net.ducksmanager.persistence.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import net.ducksmanager.persistence.models.composite.SuggestedIssueSimple

@Dao
interface SuggestedIssueDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertList(issueList: List<SuggestedIssueSimple>)

    @Query("DELETE FROM suggested_issues")
    fun deleteAll()
}