package net.ducksmanager.persistence.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import net.ducksmanager.persistence.models.dm.ContributionTotalPoints

@Dao
interface ContributionTotalPointsDao {
    @get:Query("SELECT * FROM contribution_total_points")
    val contributions: LiveData<List<ContributionTotalPoints>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertList(contributionList: List<ContributionTotalPoints>)
}