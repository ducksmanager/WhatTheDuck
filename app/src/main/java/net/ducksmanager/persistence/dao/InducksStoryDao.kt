package net.ducksmanager.persistence.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import net.ducksmanager.persistence.models.coa.InducksStory

@Dao
interface InducksStoryDao {
    @Query("SELECT * FROM inducks_story")
    fun findAll(): List<InducksStory>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSet(issueList: Set<InducksStory>)
}