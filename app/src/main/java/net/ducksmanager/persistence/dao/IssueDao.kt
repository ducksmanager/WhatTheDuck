package net.ducksmanager.persistence.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import net.ducksmanager.persistence.models.dm.Issue

@Dao
interface IssueDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertList(issueList: List<Issue>)

    @Query("DELETE FROM issues")
    fun deleteAll()
}